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
import com.lexst.site.log.*;
import com.lexst.util.host.*;


public class LogPool extends Pool {

	private static LogPool selfHandle = new LogPool();

	private Map<SiteHost, LogSite> mapSite = new TreeMap<SiteHost, LogSite>();

	private Map<SiteHost, Long> mapTime = new TreeMap<SiteHost, Long>();

	/**
	 *
	 */
	private LogPool() {
		super();
	}

	/**
	 * @return
	 */
	public static LogPool getInstance() {
		return LogPool.selfHandle;
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
	 * login site
	 * @param obj
	 * @return
	 */
	public boolean add(Site obj) {
		if (obj == null || obj.getType() != Site.LOG_SITE) {
			Logger.error("invalid log site!");
			return false;
		}

		LogSite site = (LogSite) obj;
		SiteHost host = site.getHost();
		boolean success = false;
		super.lockSingle();
		try {
			if (!mapSite.containsKey(host)) {
				mapTime.put(host, System.currentTimeMillis());
				mapSite.put(host, site);
				success = true;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		
		Logger.note(success, "LogPool.add, log site %s", host);
		return success;
	}

	/**
	 * remove host
	 * @param host
	 * @return
	 */
	public boolean remove(SiteHost host) {
		boolean success = false;
		super.lockSingle();
		try {
			mapTime.remove(host);
			success = mapSite.remove(host) != null;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		Logger.note(success, "LogPool.remove, log site %s", host);
		return success;
	}

	/**
	 * update log address
	 * @param site
	 * @return
	 */
	public boolean update(Site site) {
		Logger.info("LogPool.update, log site %s", site.getHost());
		this.remove(site.getHost());
		return this.add(site);
	}

	/**
	 * refresh log site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			LogSite site = mapSite.get(host);
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
		Logger.debug("LogPool.refresh, log site %s refresh status %d", host, code);
		return code;
	}
	
	/**
	 * find a log site
	 * @param type
	 * @return
	 */
	public SiteHost find(int type) {
		Logger.info("LogPool.find, find log site '%s'", Site.translate(type));

		SiteHost logHost = null;
		int min = Integer.MAX_VALUE;
		super.lockSingle();
		try {
			for (SiteHost host : mapSite.keySet()) {
				LogSite site = mapSite.get(host);
				if (site.contains(type)) {
					if (site.getCount() < min) {
						min = site.getCount();
						logHost = host;
					}
				}
			}
			if(logHost == null) {
				Logger.error("LogPool.find, cannot find log site '%s'", Site.translate(type));
				return null;
			}
			LogSite logSite = mapSite.get(logHost);
			logSite.addCount(1);

			LogNode node = logSite.find(type);
			SiteHost host = new SiteHost(logSite.getHost().getIP(), node.getPort(), node.getPort());
			return host;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return null;
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
		// delete timeout site
		for (SiteHost host : dels) {
			Logger.error("LogPool.check, delete timeout site:%s", host);
			this.remove(host);
		}
		// notify timeout site
		SiteHost listen = Launcher.getInstance().getLocalHost();
		for (SiteHost host : notifys) {
			Logger.warning("LogPool.check, notify timeout site:%s", host);
			this.sendTimeout(host, listen, 2);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
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
		Logger.info("LogPool.process, into...");
		while(!isInterrupted()) {
			this.sleep();
			if(isInterrupted()) break;
			this.check();
		}
		Logger.info("LogPool.process, exit");
	}

}