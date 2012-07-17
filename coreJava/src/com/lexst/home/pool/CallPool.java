/**
 *
 */
package com.lexst.home.pool;

import java.io.*;
import java.util.*;

import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.home.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.call.*;
import com.lexst.site.*;
import com.lexst.site.call.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;

public class CallPool extends Pool {

	private static CallPool selfHandle = new CallPool();

	// site host -> call site
	private Map<SiteHost, CallSite> mapSite = new TreeMap<SiteHost, CallSite>();

	/* naming -> call site host */
	private Map<Naming, SiteSet> mapNaming = new TreeMap<Naming, SiteSet>();
	
	/* space -> site set */
	private Map<Space, SiteSet> mapSpace = new TreeMap<Space, SiteSet>();

	// site host, timeout
	private Map<SiteHost, Long> mapTime = new TreeMap<SiteHost, Long>();
	
	private boolean refresh_datasite;
	private boolean refresh_worksite;

	/**
	 *
	 */
	private CallPool() {
		super();
	}

	/**
	 * @return
	 */
	public static CallPool getInstance() {
		return CallPool.selfHandle;
	}
	
	public Map<Space, Integer> measure() {
		Map<Space, Integer> map = new HashMap<Space, Integer>();
		super.lockMulti();
		try {
			for (Space space : mapSpace.keySet()) {
				SiteSet set = mapSpace.get(space);
				int num = set.size();
				map.put(space, num);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return map; 
	}

	/**
	 * @return
	 */
	public List<SiteHost> gather() {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		array.addAll(mapSite.keySet());
		return array;
	}

	/**
	 * @return
	 */
	public int countSite() {
		return mapSite.size();
	}
	
	private CallClient apply(SocketHost host) {
		CallClient client = new CallClient(true, host);
		try {
			client.reconnect();
			return client;
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return null;
	}
	
	private void complete(CallClient client) {
		if (client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {

		}
	}

	/**
	 * create table space to call site
	 * @param space
	 * @return
	 */
	public boolean createSpace(Table table) {
		Space space = table.getSpace();
		// 找到一台空闲的节点,建立映射空间
		SiteHost host = null;
		int count = Integer.MAX_VALUE;
		// 找到连接空间最少的主机
		super.lockMulti();
		try {
			for (SiteHost socket : mapSite.keySet()) {
				CallSite site = mapSite.get(socket);
				if (site.size() < count) {
					count = site.size();
					host = socket;
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if(host == null) {
			Logger.error("cannot found call site by call pool!");
			return false;
		}

		CallSite site = mapSite.get(host);
		SocketHost socket = site.getHost().getTCPHost();
		CallClient client = apply(socket);
		boolean success = false;
		try {
			if (client != null) {
				success = client.createSpace(table);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} 
		this.complete(client);
		Logger.note(success, "CallPool.createSpace, create table space '%s' to '%s'(call site)", space, socket);
		return success;
	}

	/**
	 * delete table space from call site
	 * @param space
	 * @return
	 */
	public boolean deleteSpace(Space space) {
		SiteHost[] sites = this.findSite(space);
		if (sites == null || sites.length == 0) {
			return true;
		}

		int count = 0;
		for (SiteHost site : sites) {
			CallClient client = apply(site.getTCPHost());
			try {
				if (client != null) {
					boolean success = client.deleteSpace(space);
					if (success) count++;
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.error(exp);
			}
			this.complete(client);
		}
		Logger.info("CallPool.deleteSpace, delete table space '%s', count:%d", space, count);
		return count == sites.length;
	}

	/**
	 * @param db
	 * @param table
	 * @return
	 */
	public SiteHost[] findSite(String db, String table) {
		return findSite(new Space(db, table));
	}
	
	/**
	 * find site host by space
	 * @param space
	 * @return
	 */
	public SiteHost[] findSite(Space space) {
		Logger.debug("CallPool.findSite, space is '%s' size %d", space, mapSpace.size());
		
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		this.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if(set != null) {
				array.addAll(set.list());
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockMulti();
		}
		
		int size = array.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	/**
	 * @param naming
	 * @return
	 */
	public SiteHost[] findSite(String naming) {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		this.lockMulti();
		try {
			SiteSet set = mapNaming.get(new Naming(naming));
			if (set != null) array.addAll(set.list());
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockMulti();
		}
		
		Logger.debug("CallPool.findSite, naming:%s, sites:%d", naming, array.size());
		
		int size = array.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	/**
	 * @param naming
	 * @param space
	 * @return
	 */
	public SiteHost[] findSite(String naming, Space space) {
		ArrayList<SiteHost> a1 = new ArrayList<SiteHost>();
		ArrayList<SiteHost> a2 = new ArrayList<SiteHost>();
		this.lockMulti();
		try {
			// check naming
			SiteSet set = mapNaming.get(new Naming(naming));
			if (set != null) a1.addAll(set.list());
			// check space
			set = mapSpace.get(space);
			if(set != null) a2.addAll(set.list());
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockMulti();
		}
		
		if(a1.isEmpty() || a2.isEmpty()) return null;
		
		// join host
		ArrayList<SiteHost> a = new ArrayList<SiteHost>();
		for(SiteHost host : a1) {
			if(a2.contains(host)) a.add(host);
		}
		
		int size = a.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return a.toArray(hosts);
	}

	/**
	 * @param host
	 * @return
	 */
	public boolean exists(SiteHost host) {
		boolean exist = false;
		super.lockMulti();
		try {
			exist = mapTime.containsKey(host);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return exist;
	}
	
	/**
	 * refresh call site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			CallSite site = mapSite.get(host);
			if (site != null) {
				mapTime.put(host, System.currentTimeMillis());
				code = Response.ISEE;
			} else {
				code = Response.NOTLOGIN;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		Logger.debug("CallPool.refresh, call site %s refresh status %d", host, code);
		return code;
	}

	

	/**
	 * add a call site
	 * @param object
	 * @return
	 */
	public boolean add(Site object) {
		if (object == null || !object.isCall()) {
			return false;
		}
		CallSite site = (CallSite) object;
		SiteHost host = (SiteHost) site.getHost().clone();

		Logger.info("CallPool.add, call site %s, naming size:%d, space size:%d",
				host, site.listAllNaming().size(), site.list().size());

		boolean success = false;
		this.lockSingle();
		try {
			if (!mapSite.containsKey(host)) {
				mapSite.put(host, site);
				mapTime.put(host, System.currentTimeMillis());
				// save all naming (diffuse naming and aggregate naming)
				for(Naming naming : site.listAllNaming()) {
					
					Logger.debug("CallPool.add, naming is:%s", naming);
					
					SiteSet set = mapNaming.get(naming);
					if(set == null) {
						set = new SiteSet();
						mapNaming.put(naming, set);
					}
					set.add(host);
				}
				// save space
				for (Space space : site.list()) {
					SiteSet set = mapSpace.get(space);
					if (set == null) {
						set = new SiteSet();
						mapSpace.put(space, set);
					}
					set.add(host);
				}
				success = true;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockSingle();
		}
		return success;
	}

	/**
	 * remove a site host
	 * @param host
	 * @return
	 */
	public boolean remove(SiteHost host) {
		Logger.info("CallPool.remove, call site %s", host);

		boolean success = false;
		this.lockSingle();
		try {
			mapTime.remove(host);
			CallSite site = mapSite.remove(host);
			if(site == null) return false;
			// remove naming
			for(Naming naming : site.listAllNaming()) {
				SiteSet set = mapNaming.get(naming);
				if(set != null) set.remove(host);
				if(set == null || set.isEmpty()) mapNaming.remove(naming);
			}
			// remove space
			for (Space space : site.list()) {
				SiteSet set = mapSpace.get(space);
				if (set != null) set.remove(host);
				if (set == null || set.isEmpty()) mapSpace.remove(space);
			}
			success = true;
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockSingle();
		}
		return success;
	}

	/**
	 * update call site
	 * @param site
	 * @return
	 */
	public boolean update(Site site) {
		if (site == null || !site.isCall()) {
			return false;
		}
		SiteHost host = site.getHost();
		Logger.info("CallPool.update, call site %s", host);
		// remove site host
		remove(host);
		// add site
		return add(site);
	}

	/**
	 * 检查节点超时
	 */
	private void check() {
		this.checkTimeout();
		// notify callsite, update all datasite
		if (refresh_datasite) {
			refresh_datasite = false;
			this.broadcastDataNaming();
		}
		// notify callsite, update all worksite
		if (refresh_worksite) {
			refresh_worksite = false;
			this.broadcastWorkNaming();
		}
	}

	/**
	 * check timeout
	 */
	private void checkTimeout() {
		int size = mapTime.size();
		if(size == 0) return;

		ArrayList<SiteHost> dels = new ArrayList<SiteHost>(size);
		ArrayList<SiteHost> notifys = new ArrayList<SiteHost>(size);
		super.lockSingle();
		try {
			long nowTime = System.currentTimeMillis();
			for (SiteHost host : mapTime.keySet()) {
				Long value = mapTime.get(host);
				if (value == null) {
					dels.add(host);
					continue;
				}
				long time = value.longValue();
				if (nowTime - time >= deleteTime) {
					dels.add(host);
				} else if (nowTime - time >= refreshTimeout) {
					notifys.add(host);
				}
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			super.unlockSingle();
		}
		// delete timeout site
		for(SiteHost host : dels) {
			Logger.error("CallPool.checkTimeout, delete timeout site:%s", host);
			this.remove(host);
		}
		
		SiteHost listen = Launcher.getInstance().getLocalHost();
		// send timeout message
		for (SiteHost host : notifys) {
			Logger.warning("CallPool.checkTimeout, notify timeout site:%s", host);
			this.sendTimeout(host, listen, 2);
		}
	}

	public void refreshWorkSite() {
		this.refresh_worksite = true;
		this.wakeup();
	}
	
	public void refreshDataSite() {
		this.refresh_datasite = true;
		this.wakeup();
	}

	private void broadcastWorkNaming() {
		ArrayList<SiteHost> a = new ArrayList<SiteHost>();
		super.lockMulti();
		try {
			a.addAll(mapSite.keySet());
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		for (SiteHost site : a) {
			Command cmd = new Command(Request.NOTIFY, Request.REFRESH_WORKSITE);
			Packet packet = new Packet(cmd);

			SocketHost remote = site.getUDPHost();
			listener.send(remote, packet);
		}
	}
	
	private void broadcastDataNaming() {
		ArrayList<SiteHost> a = new ArrayList<SiteHost>();
		super.lockMulti();
		try {
			a.addAll(mapSite.keySet());
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		for (SiteHost site : a) {
			Command cmd = new Command(Request.NOTIFY, Request.REFRESH_DATASITE);
			Packet packet = new Packet(cmd);

			SocketHost remote = site.getUDPHost();
			listener.send(remote, packet);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapTime.clear();
		mapSite.clear();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("CallPool.process, into...");
		while (!isInterrupted()) {
			this.check();
			this.delay(1000);
		}
		Logger.info("CallPool.process, exit");
	}

}