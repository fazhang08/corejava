/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;

import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.data.*;
import com.lexst.remote.client.home.*;
import com.lexst.util.host.*;


abstract class LocalPool extends Pool {

	/**
	 * 
	 */
	protected LocalPool() {
		super();
	}

	/**
	 * return a home client handle
	 * 
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
		if (client == null) return;
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
	 * return a data client handle
	 * @return
	 */
	protected DataClient apply(SiteHost data) {
		SocketHost address = data.getTCPHost();
		DataClient client = new DataClient(true, address);
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
	protected void complete(DataClient client) {
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
	
	protected String buildFilename(String root, long chunkid) {
		while (root.charAt(root.length() - 1) == File.separatorChar) {
			root = root.substring(0, root.length() - 1);
		}
		// check local file
		String cid = String.format("%x", chunkid);
		while (cid.length() < 16) {
			cid = "0" + cid;
		}
		return String.format("%s%c%s.lxdb", root, File.separatorChar, cid);
	}
}
