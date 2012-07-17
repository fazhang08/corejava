/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com, All rights reserved
 * 
 * basic class
 * 
 * @author scott.liu lexst@126.com
 * 
 * @version 1.0 1/6/2010
 * 
 * @see com.lexst.call.pool
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.call.pool;

import java.io.*;

import com.lexst.call.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.home.*;
import com.lexst.util.host.*;

abstract class LocalPool extends Pool {

	protected SiteHost home = new SiteHost();
	
	protected CallLauncher callInstance;
	
	protected long refreshTime;
	
	/**
	 * 
	 */
	protected LocalPool() {
		super();
		refreshTime = 0;
	}

	public void setHome(SiteHost host) {
		home.set(host);
	}
	public SiteHost getHome() {
		return home;
	}
	
	public void setBoss(CallLauncher call) {
		this.callInstance = call;
	}

	/**
	 * connect home site
	 * @param stream
	 * @return
	 */
	protected HomeClient bring(boolean stream) {
		SocketHost host = (stream ? home.getTCPHost() : home.getUDPHost());
		HomeClient client = new HomeClient(stream, host);
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
	
	/**
	 * @return
	 */
	protected HomeClient bring() {
		return bring(true);
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

}