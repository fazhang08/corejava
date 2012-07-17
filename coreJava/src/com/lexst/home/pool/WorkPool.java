/**
 *
 */
package com.lexst.home.pool;

import java.util.*;

import com.lexst.fixp.*;
import com.lexst.home.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.site.*;
import com.lexst.site.work.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;

public class WorkPool extends Pool {

	private static WorkPool selfHandle = new WorkPool();

	private Map<SiteHost, WorkSite> mapSite = new TreeMap<SiteHost, WorkSite>();
	
	private Map<SiteHost, Long> mapTime = new TreeMap<SiteHost, Long>();
	
	private Map<Naming, SiteSet> mapNaming = new TreeMap<Naming, SiteSet>();

	/**
	 *
	 */
	private WorkPool() {
		super();
	}

	/**
	 * return a static handle
	 * @return
	 */
	public static WorkPool getInstance() {
		return WorkPool.selfHandle;
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
	 * get all worksite
	 * @return
	 */
	public WorkSite[] batch() {
		WorkSite[] sites = null;
		super.lockMulti();
		try {
			int size = mapSite.size();
			if (size > 0) {
				sites = new WorkSite[size];
				mapSite.values().toArray(sites);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return sites;
	}
	
	/**
	 * refresh work site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			WorkSite site = mapSite.get(host);
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
		Logger.debug("WorkPool.refresh, site %s refresh status %d", host, code);
		return code;
	}
	
	/**
	 * find work site by naming
	 * @param naming
	 * @return
	 */
	public SiteHost[] find(String naming) {
		Naming s = new Naming(naming);
		
		Logger.info("WorkPool.find, find work site by '%s'", s);
		
		ArrayList<SiteHost> a = new ArrayList<SiteHost>();
		this.lockSingle();
		try {
			SiteSet set = mapNaming.get(s);
			if (set != null) {
				a.addAll(set.list());
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		
		if(a.isEmpty()) return null;
		SiteHost[] hosts = new SiteHost[ a.size() ];
		return a.toArray(hosts);
	}

	/**
	 * add a work site, include task
	 * @param object
	 * @return
	 */
	public boolean add(Site object) {
		return add(object, true);
	} 
	
	private boolean add(Site object, boolean notify) {
		if (object == null || !object.isWork()) {
			return false;
		}
		WorkSite site = (WorkSite)object;
		SiteHost host = site.getHost();
		
		Logger.info("WorkPool.add, work site %s", host);

		boolean success = false;
		this.lockSingle();
		try {
			if (mapSite.containsKey(host)) {
				return false;
			}
			// save site
			mapSite.put(host, site);
			mapTime.put(host, System.currentTimeMillis());
			for (Naming naming : site.list()) {
				SiteSet set = mapNaming.get(naming);
				if (set == null) {
					set = new SiteSet();
					mapNaming.put(naming, set);
				}
				set.add(host);
			}
			success = true;
			
			if (notify) {
				CallPool.getInstance().refreshWorkSite();
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return success;
	}

	/**
	 * delete a work site, include task
	 * @param host
	 * @return
	 */
	public boolean remove(SiteHost host) {
		return remove(host, true);
	}
	
	/**
	 * @param host
	 * @param notify
	 * @return
	 */
	private boolean remove(SiteHost host, boolean notify) {
		Logger.info("WorkPool.remove, work site %s", host);
		
		boolean success = false;
		this.lockSingle();
		try {
			mapTime.remove(host);
			WorkSite site = mapSite.remove(host);
			if (site != null) {
				for (Naming naming : site.list()) {
					SiteSet set = mapNaming.get(naming);
					if (set != null) {
						set.remove(host);
					}
					if (set == null || set.isEmpty()) {
						mapNaming.remove(naming);
					}
				}
				success = true;
				
				if(notify) {
					CallPool.getInstance().refreshWorkSite();
				}
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return success;
	}

	/**
	 * update work site
	 * @param site
	 * @return
	 */
	public boolean update(Site site) {
		if (site == null || !site.isWork()) {
			return false;
		}
		SiteHost host = site.getHost();
		Logger.info("WorkPool.update, work site %s", host);
		remove(host, false);
		return add(site, true);
	}

	/**
	 * check timeout site
	 */
	private void check() {
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
		// remove timeout site
		for (SiteHost host : dels) {
			Logger.error("WorkPool.check, delete timeout site:%s", host);
			remove(host);
		}
		// notify site
		SiteHost listen = Launcher.getInstance().getLocalHost();
		for (SiteHost host : notifys) {
			Logger.warning("WorkPool.check, notify timeout site:%s", host);
			this.sendTimeout(host, listen, 2);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.mapNaming.clear();
		this.mapSite.clear();
		this.mapTime.clear();
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
		Logger.info("WorkPool.process, into...");
		while (!isInterrupted()) {
			this.check();
			this.delay(1000);
		}
		Logger.info("WorkPool.process, exit");
	}

}