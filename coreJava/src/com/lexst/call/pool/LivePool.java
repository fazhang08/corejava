/**
 * 
 */
package com.lexst.call.pool;

import java.util.*;

import com.lexst.log.client.*;
import com.lexst.site.*;
import com.lexst.site.live.*;
import com.lexst.util.host.*;

public class LivePool extends LocalPool {
	
	private static LivePool selfHandle = new LivePool();

	// host address -> live site instance
	private Map<SiteHost, LiveSite> mapSite = new HashMap<SiteHost, LiveSite>();

	// host address -> last time
	private Map<SiteHost, Long> mapTime = new HashMap<SiteHost, Long>();

	/**
	 * 
	 */
	private LivePool() {
		super();
	}
	
	/**
	 * @return
	 */
	public static LivePool getInstance() {
		return LivePool.selfHandle;
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
		Logger.info("LivePool.process, into...");
		while(!isInterrupted()) {
			this.check();
			this.delay(1000);
		}
		Logger.info("LivePool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * add live site
	 * @param object
	 * @return
	 */
	public boolean add(Site object) {
		if(object == null || !object.isLive()) {
			return false;
		}
		LiveSite site = (LiveSite)object;
		SiteHost host =	site.getHost();
		
		boolean success = false;
		super.lockSingle();
		try {
			if (!mapSite.containsKey(host)) {
				mapSite.put(host, site);
				mapTime.put(host, System.currentTimeMillis());
				success = true;
			} else {
				Logger.warning("LivePool.add, %s existed", host);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		Logger.note(success, "LivePool.add, login live site %s", host);
		return success;
	}
	
	/**
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
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		Logger.note(success, "LivePool.remove, logout live site %s", host);
		return success;
	}

	/**
	 * @param object
	 * @return
	 */
	public boolean update(Site object) {
		if (object == null || !object.isLive()) {
			return false;
		}
		SiteHost host = object.getHost();
		Logger.info("LivePool.update, relogin live site %s", host);
		this.remove(host);
		return add(object);
	}
	
	/**
	 * refresh host
	 * @param host
	 * @return
	 */
	public boolean refresh(SiteHost host) {
		boolean success = false;
		super.lockSingle();
		try {
			if (mapSite.containsKey(host)) {
				mapTime.put(host, System.currentTimeMillis());
				success = true;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return success;
	}
	
	private void check() {
		
	}

}