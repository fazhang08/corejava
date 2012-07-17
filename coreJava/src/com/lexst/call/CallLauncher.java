/**
 *
 */
package com.lexst.call;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.call.effect.*;
import com.lexst.call.pool.*;
import com.lexst.db.charset.*;
import com.lexst.db.schema.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.call.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.impl.call.*;
import com.lexst.xml.*;


public class CallLauncher extends JobLauncher {
	
	// local site
	private CallSite local = new CallSite();
	// table space set
	private SpaceLoader loader = new SpaceLoader();
	// schema charset
	private SchemaCharset charset = new SchemaCharset();
	
	/**
	 * default constructor
	 */
	protected CallLauncher() {
		super();
		super.setLogging(true);
		packetImpl = new CallPacketInvoker(this, fixpPacket);
		streamImpl = new CallStreamInvoker();
		CallVisitImpl.setInstance(this);
	}

	/**
	 * call site configure
	 * @return
	 */
	public CallSite getLocal() {
		return local;
	}

	/**
	 * ping service
	 */
	public void nothing() {
		// Logger.info("this is nothing method");
	}

//	/**
//	 * register work space
//	 * @param space
//	 * @return
//	 */
//	public boolean registerWorkSpace(Space space) {
//		return WorkPool.getInstance().register(space);
//	}

	/**
	 * 
	 * @param space
	 * @return
	 */
	public boolean addSpace(Space space) {
		boolean success = loader.add(space);
		Logger.note(success, "CallLauncher.addSpace, space is '%s'", space);
		return success;
	}
	
	/**
	 * check space table exists
	 * @param space
	 * @return
	 */
	public boolean containsSpace(Space space) {
		return loader.exists(space);
	}
	
	public List<Space> listSpace() {
		return loader.keySet();
	}
	
	/**
	 * find table by space
	 * @param space
	 * @return
	 */
	public Table findTable(Space space) {
		Logger.debug("CallLauncher.findTable, find table '%s'", space);

		Table table = loader.find(space);
		// not found, to home site
		if (table == null) {
			HomeClient client = bring(home);
			if (client == null) {
				Logger.error("CallLauncher.findTable, cannot connect %s", home);
				return null; // error
			}
			try {
				table = client.findTable(space);
				if (table != null) {
					loader.update(space, table);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			complete(client);
		}
		return table;
	}

	/**
	 * find a space by schema
	 * @param db
	 * @return
	 */
	public SQLCharset findCharset(String db) {
		Logger.debug("CallLauncher.findCharset, find charset '%s'", db);

		SQLCharset set = charset.find(db);
		// not found, check home
		if (set == null) {
			HomeClient client = bring(home);
			if (client == null) {
				Logger.error("CallLauncher.findCharset, cannot connect %s",
						home);
				return null;
			}
			try {
				set = client.findCharset(db);
				if (set != null) charset.add(db, set);
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			complete(client);
		}
		return set;
	}

	/**
	 * create space service and save table configure
	 * @param table
	 * @return
	 */
	public boolean createSpace(Table table) {
		Space space = table.getSpace();
		Table object = loader.find(space);
		if (object != null) {
			Logger.error("CallLauncher.createSpace, existed space '%s'", space);
			return false;
		}
		Logger.info("CallLauncher.createSpace, create table space '%s'", space);
		// save space configure
		loader.update(space, table);
		local.add(space);
		this.setOperate(BasicLauncher.RELOGIN);
		return true;
	}

	/**
	 * close data connect
	 * @param space
	 * @return
	 */
	public boolean deleteSpace(Space space) {
		Table table = loader.find(space);
		if (table == null) {
			Logger.error("CallLauncher.deleteSpace, cannot found space '%s'", space);
			return false;
		}
		// remove space from configure
		loader.remove(space);
		local.remove(space);
		// close socket
		int count = DataPool.getInstance().stopSpace(space);
		Logger.info("CallLauncher.deleteSpace, stop space:%s, count:%d", space, count);
		this.setOperate(BasicLauncher.RELOGIN);
		return count >= 0;
	}
	
	/**
	 * request table space, when empty status
	 * @param client
	 * @param num
	 * @return
	 */
	private boolean balance(HomeClient client, int num) {
		// 如果有分配,就不请求新的空间
		if (loader.size() > 0) return true;
		
		// 如果没有,请求新的空间定义
		boolean nullable = (client == null);
		if(nullable) client = bring(home);
		if(client == null) return false;

		// apply space
		try {
			Space[] s = client.balance(num);
			for (int i = 0; s != null && i < s.length; i++) {
				Logger.info("CallLauncher.balance, space '%s'", s[i]);
				loader.add(s[i]);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if(nullable) complete(client);

		return true;
	}

	/**
	 * load database space
	 *
	 * @param client
	 * @return
	 */
	private boolean loadTable(HomeClient client) {
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;

		List<Space> list = loader.keySet();
		for (Space space : list) {
			for (int i = 0; i < 3; i++) {
				try {
					if(client.isClosed()) client.reconnect();
					// find atable
					Table table = client.findTable(space);
					if (table == null) {
						Logger.error("CallLauncher.loadTable, cannot find table '%s'", space);
					} else {
						Logger.info("CallLauncher.loadTable, load table '%s'", space);
						local.add(space);
						loader.update(space, table);
					}
					break;
				} catch (IOException exp) {
					Logger.error(exp);
				} catch (Throwable exp) {
					Logger.fatal(exp);
				}
				client.close();
				this.delay(1000);
			}
		}
		if(nullable) complete(client);
		return true;
	}

	/**
	 * load sql charset
	 * @param client
	 * @return
	 */
	private boolean loadCharset(HomeClient client) {
		if (loader.isEmpty()) return true;
		
		List<String> array = new ArrayList<String>();
		for(Space space : loader.keySet()) {
			String db = space.getSchema().toLowerCase();
			if (!array.contains(db)) {
				array.add(db);
			}
		}
		
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;

		for (String db : array) {
			Logger.debug("CallLauncher.loadCharset, find schema charset '%s'", db);
			for (int i = 0; i < 3; i++) {
				try {
					if (client.isClosed()) client.reconnect();
					SQLCharset sqlchar = client.findCharset(db);
					if (sqlchar != null) {
						charset.add(db, sqlchar);
						Logger.info("CallLauncher.loadCharset, save schema charset by '%s'", db);
					}
					break;
				} catch (IOException exp) {
					Logger.error(exp);
				} catch (Throwable exp) {
					Logger.fatal(exp);
				}
				client.close();
				delay(1000);
			}
		}
		// when init is null, close it
		if(nullable) complete(client);
		return true;
	}

	/**
	 * login to hoem site
	 * @param client
	 * @return
	 */
	private boolean login(HomeClient client) {
		Logger.info("CallLauncher.login, %s to %s", local.getHost(), home);
		
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) {
			Logger.error("CallLauncher.login, cannot connect %s", home);
			return false;
		}
				
		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.login(local);
				break;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			// when error, close
			client.close();
			delay(5000);
		}
		if(nullable) complete(client);
		return success;
	}
	
	/**
	 * relogin to home site
	 * @param client
	 * @return
	 */
	private boolean relogin(HomeClient client) {
		Logger.info("CallLauncher.relogin, %s to %s", local.getHost(), home);

		boolean nullable = (client == null);
		if(nullable) client = bring(home);
		if(client == null) {
			Logger.error("CallLauncher.relogin, cannot connect %s", home);
			return false;
		}

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
			this.delay(5000);
		}
		if(nullable) complete(client);
		return success;
	}

	/**
	 * logout from home site
	 */
	private boolean logout(HomeClient client) {
		Logger.info("CallLauncher.logout, %s from %s", local.getHost(), home);
		
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
			this.delay(5000);
		}
		if(nullable) complete(client);
		return success;
	}

	/**
	 * start call monitor
	 * @return
	 */
	private boolean loadPool() {
		DataPool.getInstance().setListener(fixpPacket);
		DataPool.getInstance().setHome(home);
		DataPool.getInstance().setBoss(this);
		
		WorkPool.getInstance().setListener(fixpPacket);
		WorkPool.getInstance().setHome(home);
		WorkPool.getInstance().setBoss(this);
		
		LivePool.getInstance().setListener(fixpPacket);
		LivePool.getInstance().setHome(home);
		LivePool.getInstance().setBoss(this);
		
		BuildPool.getInstance().setListener(fixpPacket);
		BuildPool.getInstance().setHome(home);
		BuildPool.getInstance().setBoss(this);
		
		boolean success = DataPool.getInstance().start();
		if (success) {
			success = WorkPool.getInstance().start();
		}
		if (success) {
			success = LivePool.getInstance().start();
		}
		if (success) {
			success = BuildPool.getInstance().start();
		}
		return success;
	}

	/**
	 * stop call monitor
	 */
	private void stopPool() {
		BuildPool.getInstance().stop();
		LivePool.getInstance().stop();
		DataPool.getInstance().stop();
		WorkPool.getInstance().stop();

		while(BuildPool.getInstance().isRunning()) {
			this.delay(200);
		}
		while(LivePool.getInstance().isRunning()) {
			this.delay(200);
		}
		while (DataPool.getInstance().isRunning()) {
			this.delay(200);
		}
		while (WorkPool.getInstance().isRunning()) {
			this.delay(200);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		HomeClient client = bring(home);
		if (client == null) return false;

		//1. load log service
		boolean success = loadLog(local.getType(), client);
		Logger.note("CallLauncher.init, load log", success);
		//2. check site time
		if (success) {
			success = loadTimeout(local.getType(), client);
			Logger.note(success, "CallLauncher.init, set site timeout %d", getSiteTimeout());
			if (!success) stopLog();
		}
		//3. start fixp listener
		if (success) {
			Class<?>[] cls = { CallVisitImpl.class };
			success = loadListen(cls, local.getHost());
			Logger.note("CallLauncher.init, load listen", success);
			if (!success) stopLog();
		}
		//4. apply space from home site
		if (success) {
			success = balance(client, 10);
			Logger.note(success, "CallLauncher.init, apply space, size:%d", loader.size());
			if (!success) {
				stopListen();
				stopLog();
			}
		}
		//5. get data charset and table configure
		if(success) {
			success = loadCharset(client);
			Logger.note("CallLauncher.init, load schema charset", success);
			if(!success) {
				stopListen();
				stopLog();
			}
		}
		//6. load space
		if (success) {
			success = loadTable(client);
			Logger.note("CallLauncher.init, load table space", success);
			if (!success) {
				stopListen();
				stopLog();
			}
		}
		//6. start monitor
		if (success) {
			success = loadPool();
			Logger.note("CallLauncher.init, load pool", success);
			if (!success) {
				stopPool();
				stopListen();
				stopLog();
			}
		}
		//7. login to home site
		if (success) {
			success = login(client);
			Logger.note("CallLauncher.init, login", success);
			if (!success) {
				stopPool();
				stopListen();
				stopLog();
			}
		}
		complete(client);
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		//1. logout local site
		logout(null);
		//2. stop monitor
		stopPool();
		//3. stop fixp service
		stopListen();
		//4.stop log service
		stopLog();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("CallLauncher.process, into...");
		long end, timeout;
		while (!isInterrupted()) {
			end = System.currentTimeMillis() + 1000;
			
			if (super.isLoginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.login(null);
			} else if (super.isReloginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.relogin(null);
			} else if (isMaxSiteTimeout()) {
				this.refreshEndTime();
				this.relogin(null);
			} else if (isSiteTimeout()) {
				this.hello(local.getType(), home);
			}
			
			timeout = end - System.currentTimeMillis();
			if (timeout > 0) delay(timeout);
		}
		Logger.info("CallLauncher.process, exit");
	}

	/**
	 * load local configure file
	 *
	 * @param filename
	 */
	public boolean loadLocal(String filename) {
		XMLocal xml = new XMLocal();
		Document document = xml.loadXMLSource(filename);
		if (document == null) return false;

		SiteHost host = splitHome(document);
		if(host == null) {
			return false;
		}
		home.set(host);
		host = splitLocal(document);
		if(host == null) {
			return false;
		}
		local.setHost(host);
		// resolve shutdown address
		if(!loadShutdown(document)) {
			Logger.error("Launcher.loadLocal, cannot parse shutdown address");
			return false;
		}
		
		// resovle security configure file
		if(!super.loadSecurity(document)) {
			Logger.error("Launcher.loadLocal, cannot parse security file");
			return false;
		}
		
		// load log configure
		return Logger.loadXML(filename);
	}

}