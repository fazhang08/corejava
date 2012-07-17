/**
 *
 */
package com.lexst.work;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.algorithm.*;
import com.lexst.algorithm.aggregate.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.*;
import com.lexst.fixp.*;
import com.lexst.fixp.Entity;
import com.lexst.log.client.*;
import com.lexst.remote.*;
import com.lexst.remote.client.data.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.work.*;
import com.lexst.thread.*;
import com.lexst.util.datetime.*;
import com.lexst.util.host.*;
import com.lexst.util.lock.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;
import com.lexst.visit.impl.work.*;
import com.lexst.work.pool.*;
import com.lexst.xml.*;

public class Launcher extends JobLauncher implements TaskTrigger, TaskEventListener {

	private static Launcher selfHandle = new Launcher();
	
	// local type
	private WorkSite local = new WorkSite();

	private SingleLock lock = new SingleLock();
	
	/* task identity -> task instance (DCTask instance) */
	private Map<Long, AggregateTask> mapTask = new HashMap<Long, AggregateTask>();

	/**
	 * default constructor
	 */
	private Launcher() {
		super();
		super.setExitVM(true);
		streamImpl = new WorkStreamInvoker(this);
		packetImpl = new WorkPacketInvoker(fixpPacket, this);
	}

	/**
	 * @return
	 */
	public static Launcher getInstance() {
		return Launcher.selfHandle;
	}
	
	/**
	 * get local
	 * @return
	 */
	public WorkSite getLocal() {
		return this.local;
	}
	
	/**
	 * remove a task (dc task)
	 */
	@Override
	public boolean removeTask(long identity) {
		lock.lock();
		try {
			return mapTask.remove(identity) != null;
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	private DC resolve(com.lexst.fixp.Entity entity) {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (int index = 0; true; index++) {
			Message msg = entity.findMessage(Key.DC_OBJECT, index);
			if (msg == null) break;
			byte[] b = msg.getValue();
			buff.write(b, 0, b.length);
		}
		byte[] b = buff.toByteArray();
		if (b == null || b.length == 0) return null;
		try {
			Apply apply = Apply.resolve(b);
			return (DC)apply.getParameters()[0];
		} catch(IOException exp) {
			Logger.error(exp);
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		}
		return null;
	}

	/**
	 * @param object
	 */
	@Override
	public void dc(DCPair object) {
		DC dc = resolve(object.getRequest());
		if (dc == null) {
			object.finish();
			return;
		}

		long identity = dc.getIdentity();
		
		// get set
		boolean first = false;
		DCTask task = null;
		lock.lock();
		try {
			task = (DCTask) mapTask.get(identity);
			first = (task == null);
			// when first, create a new task object
			if (first) {
				task = (DCTask) AggregateTaskPool.getInstance().find(dc.getToNaming());
				if (task == null) {
					object.finish();
					return;
				}
				task.setDC(dc);
				task.setIdentity(identity);
				task.setLocal(local.getHost().getIPValue());
				task.setTrigger(this);
				// save instance
				mapTask.put(identity, task);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
		Logger.debug("Launcher.dc, first is %b", first);
		if (task == null) {
			object.finish();
			return;
		}
		// doing
		task.inject(object);
		// when first, start thread
		if(first) task.start();
	}
	
	/**
	 * execute SQL "adc" command
	 * @param adc
	 * @param table
	 * @param stream
	 * @return
	 */
	@Override
	public Entity adc(ADC adc, DCTable table, boolean stream) {
//		//1. 找到对应的类定义
//		Naming naming = new Naming(adc.getToNaming());
//		Project project = mapProject.get(naming);
//		if (project == null) { // error
//			Command cmd = new Command(Response.DC_SERVERERR);
//			return stream ? new Stream(cmd) : new Packet(cmd);
//		}
//		
//		String task_class = project.getTaskClass();
//		ADCTask task = null;
//		try {
//			task = (ADCTask) Class.forName(task_class).newInstance();
//		} catch (InstantiationException exp) {
//			Logger.error(exp);
//		} catch (IllegalAccessException exp) {
//			Logger.error(exp);
//		} catch (ClassNotFoundException exp) {
//			Logger.error(exp);
//		} catch (Throwable exp) {
//			Logger.fatal(exp);
//		}
		
		//1. 找到对应的类
		ADCTask task = (ADCTask)AggregateTaskPool.getInstance().find(adc.getToNaming());
		if(task == null) {
			Command cmd = new Command(Response.DC_SERVERERR);
			return stream ? new Stream(cmd) : new Packet(cmd);
		}
		task.setADC(adc);

		//2. 启动data client, 抓取数据
		for(SiteHost host : table.keySet()) {
			DCArea area = table.get(host);
			long jobid = area.getIdentity();
			
			try {
				DataClient client = new DataClient(true, host.getTCPHost());
				client.reconnect();
				for (DCField field : area.list()) {
					byte[] data = client.downloadDCField(jobid, field.getMod(), field.getBegin(), field.getEnd());
					if (data != null && data.length > 0) {
						task.add(field, data);
					}
				}
				client.exit();
				client.close();
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (IOException exp) {
				Logger.error(exp);
			}
		}
		
		//3. 完成,执行处理
		byte[] result = task.execute();
		
		// 发送给调用节点
		Command cmd = new Command(Response.DC_FOUND);
		Entity reply = null;
		if (stream) {
			reply = new Stream(cmd);
		} else {
			reply = new Packet(cmd);
		}
		reply.setData(result);
		return reply;
	}

	/**
	 * rpc call
	 */
	public void nothing() {
		
	}
	
	/**
	 * @param client
	 */
	private boolean loadTime(HomeClient client) {
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;

		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if(client.isClosed()) client.reconnect();
				long time = client.currentTime();
				Logger.info("Launcher.loadTime, set time %d", time);
				if (time != 0L) {
					int ret = SystemTime.set(time);
					success = (ret == 0);
					break;
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(500);
		}
		if(nullable) complete(client);
		Logger.note("Launcher.loadTime", success);
		return success;
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// site logout
		logout(null);
		// stop pool
		stopPool();
		// stop listen
		stopListen();
		// stop log
		stopLog();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// connect to home site
		HomeClient client = bring(home);
		if (client == null) return false;

		//1. load log
		boolean	success = loadLog(local.getType(), client);
		Logger.note("Launcher.init, load log", success);
		if(!success) return false;
		//2. get refresh time
		if(success) {
			success = loadTimeout(local.getType(), client);
			Logger.note(success, "Launcher.init, set site timeout %d", getSiteTimeout());
			if(!success) stopLog();
		}
		//3. set system time
		if (success) {
			loadTime(client);
		}
		//4. load table
		if(success) {
			success = loadTable(client);
			Logger.note("Launcher.init, load table", success);
			if (!success) stopLog();
		}
		//5. load listen
		if (success) {
			Class<?>[] cls = { WorkVisitImpl.class };
			success = loadListen(cls, local.getHost());
			Logger.note("Launcher.init, load listen", success);
			if (!success) stopLog();
		}
		//6. load work pool
		if(success) {
			success = loadPool();
			Logger.note("Launcher.init, load pool", success);
			if (!success) {
				stopListen();
				stopLog();
			}
		}
		//7. login to home
		if (success) {
			success = login(client);
			Logger.note("Launcher.init, login", success);
			if (!success) {
				stopPool();
				stopListen();
				stopLog();
			}
		}
	
		// close home-client
		complete(client);
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("Launcher.process, into ...");
		while (!isInterrupted()) {
			long end = System.currentTimeMillis() + 1000;

			if (super.isLoginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.login(null);
			} else if(super.isReloginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.relogin(null);
			} else if (isMaxSiteTimeout()) {
				this.refreshEndTime();
				this.relogin(null);
			} else if (isSiteTimeout()) {
				hello(local.getType(), home); // active to home
			}

			long timeout = end - System.currentTimeMillis();
			if (timeout > 0) this.delay(timeout);
		}
		Logger.info("Launcher.process, exit");
	}

	/**
	 * load table configure
	 * @param client
	 * @return
	 */
	private boolean loadTable(HomeClient client) {
		boolean success = false;
		boolean nullable = (client == null);
		try {
			if(nullable) client = super.bring(home);
			if(client == null) return false;
			for(Naming naming : AggregateTaskPool.getInstance().listNaming()) {
				Project project = AggregateTaskPool.getInstance().findProject(naming);
				for(Space space : project.getSpaces()) {
					Table table = client.findTable(space);
					if (table == null) {
						Logger.error("Launcher.loadTable, cannot find %s", naming);
						return false;
					}
					project.setTable(space, table);
					Logger.info("Launcher.loadTable, load table '%s'", space);
				}
			}
			success = true;
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			if(nullable) complete(client);
		}
		return success;
	}

	/**
	 * load work pool
	 * @return
	 */
	private boolean loadPool() {
		Logger.info("Launcher.loadPool, load data pool");
		// set packet listener
		DataPool.getInstance().setListener(fixpPacket);

		AggregateTaskPool.getInstance().setTaskEventListener(this);
		boolean success = AggregateTaskPool.getInstance().start();
		if (success) {
			success = DataPool.getInstance().start();
		}
		return success;
	}

	/**
	 * stop work pool
	 */
	private void stopPool() {
		Logger.info("Launcher.stopPool, stop all pool...");

		AggregateTaskPool.getInstance().stop();
		while (AggregateTaskPool.getInstance().isRunning()) {
			this.delay(200);
		}
		
		DataPool.getInstance().stop();
		while(DataPool.getInstance().isRunning()) {
			this.delay(200);
		}
	}

	/**
	 * login to home site
	 * @param client
	 * @return
	 */
	private boolean login(HomeClient client) {
		Logger.info("Launcher.login, %s login to %s", local.getHost(), home);
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;
		
		// save all naming
		local.clear();
		local.addAll(AggregateTaskPool.getInstance().listNaming());

		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.login(local);
				break;
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(1000);
		}
		if (nullable) complete(client);
		return success;
	}
	
	/**
	 * logout from home site
	 * @param client
	 */
	private boolean logout(HomeClient client) {
		Logger.info("Launcher.logout, %s logout from %s", local.getHost(), home);
		
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;
		
		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.logout(local.getType(), local.getHost());
				break;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(1000);
		}
		if (nullable) complete(client);
		return success;
	}
	
	/**
	 * relogin site
	 * @param client
	 * @return
	 */
	private boolean relogin(HomeClient client) {
		Logger.info("Launcher.relogin, %s from %s", local.getHost(), home);
			
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;
		
		// save all naming
		local.clear();
		local.addAll(AggregateTaskPool.getInstance().listNaming());

		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.relogin(local);
				break;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(1000);
		}
		if (nullable) complete(client);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.algorithm.TaskEventListener#updateTask()
	 */
	@Override
	public void updateTask() {
		setOperate(BasicLauncher.RELOGIN);
	}
	
	/**
	 * load configure
	 * @param filename
	 * @return
	 */
	private boolean loadLocal(String filename) {
		XMLocal xml = new XMLocal();
		Document document = xml.loadXMLSource(filename);
		if (document == null) {
			return false;
		}
		// home host
		SiteHost host = super.splitHome(document);
		home.set(host);
		// local host
		host = super.splitLocal(document);
		local.setHost(host);
		// shutdown
		if(!loadShutdown(document)) {
			Logger.error("Launcher.loadLocal, cannot resolve shutdown address");
			return false;
		}
		
		// resovle security configure file
		if(!super.loadSecurity(document)) {
			Logger.error("Launcher.loadLocal, cannot parse security file");
			return false;
		}

		// load class task
		String path = xml.getXMLValue(document.getElementsByTagName("task-root"));
		if (path != null && path.length() > 0) {
			AggregateTaskPool.getInstance().setRoot(path);
		}
		
		// load log configure
		return Logger.loadXML(filename);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}
		// load configure
		String filename = args[0];
		boolean success = Launcher.getInstance().loadLocal(filename);
		Logger.note("Launcher.main, load local", success);
		// start thread
		if (success) {
			success = Launcher.getInstance().start();
			Logger.note("Launcher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
		}
	}

}