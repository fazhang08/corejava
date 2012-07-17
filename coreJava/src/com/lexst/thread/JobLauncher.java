/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com  All rights reserved
 * 
 * basic launcher of job site (log, data, work, build, call)
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/8/2009
 * 
 * @see com.lexst.thread
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.thread;

import java.io.*;

import com.lexst.log.client.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public abstract class JobLauncher extends BasicLauncher {

	protected SiteHost home = new SiteHost();
	
	/**
	 * 
	 */
	protected JobLauncher() {
		super();
		this.setLogging(true);
	}
	
	/**
	 * set home site
	 * @param host
	 */
	public void setHub(SiteHost host) {
		this.home.set(host);
	}
	
	public SiteHost getHub() {
		return this.home;
	}

	/**
	 * load log service
	 * @param siteType
	 * @param client
	 * @return
	 */
	protected boolean loadLog(int siteType, HomeClient client) {
		SiteHost host = null;
		// find log site address from home site
		if (siteType != Site.NONE && client != null) {
			try {
				host = client.findLogSite(siteType);
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			if (host == null) {
				Logger.error("ExtendLauncher.loadLog, cannot find log site!");
				return false;
			}
		}
		// start log service
		return Logger.loadService(host);
	}

	/**
	 * stop log serivce
	 */
	protected void stopLog() {
		Logger.stopService();
	}

	/**
	 * return a home client handle
	 * @return
	 */
	protected HomeClient bring(SiteHost home) {
		SocketHost address = home.getTCPHost();
		HomeClient client = new HomeClient(true, address);
		for (int i = 0; i < 3; i++) {
			try {
				client.reconnect();
				return client;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(1000);
		}
		return null;
	}

	/**
	 * @param client
	 */
	protected void complete(HomeClient client) {
		if(client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
	}

	/**
	 * get site timeout (second)
	 * @param siteType
	 * @param client
	 * @return
	 */
	protected boolean loadTimeout(int siteType, HomeClient client) {
		boolean success = false;
		try {
			int second = client.getSiteTimeout(siteType);
			if (second >= 5) {
				setSiteTimeout(second);
				success = true;
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return success;
	}

}