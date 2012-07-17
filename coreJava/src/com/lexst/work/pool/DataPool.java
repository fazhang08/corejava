/**
 * 
 */
package com.lexst.work.pool;

import java.util.*;

import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.util.host.*;

public class DataPool extends Pool {

	private static DataPool selfHandle = new DataPool();
	
	// client address -> site host
	private Map<SocketHost, SiteHost> mapHost = new TreeMap<SocketHost, SiteHost>();
	// socket address -> last time
	private Map<SocketHost, Long> mapTime = new TreeMap<SocketHost, Long>();

	/**
	 * 
	 */
	private DataPool() {
		super();
	}

	/**
	 * get static handle
	 * @return
	 */
	public static DataPool getInstance() {
		return DataPool.selfHandle;
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
		Logger.info("DataPool.process, into...");
		while(!isInterrupted()) {
			this.delay(3000);
			if(isInterrupted()) break;
			// check timeout site
			this.check();
		}
		Logger.info("DataPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.mapHost.clear();
		this.mapTime.clear();
	}
	
	/**
	 * refresh data site time
	 * @param host
	 * @return
	 */
	public short refresh(SocketHost socket) {
		Logger.debug("DataPool.refresh, data site %s active", socket);
		short code = Response.SERVER_ERROR;
		super.lockSingle();
		try {
			if(mapHost.containsKey(socket)) {
				mapTime.put(socket, System.currentTimeMillis());
				code = Response.ISEE;
			} else {
				code = Response.NOTLOGIN;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return code;
	}
	
	/**
	 * add a data site
	 * @param socket : client socket address
	 * @param host : data lisetener address
	 * @return
	 */
	public short add(SocketHost socket, SiteHost host) {
		if (host == null) {
			return Response.CLIENT_ERROR;
		}
		
		Logger.info("DataPool.add, from %s", host);
		super.lockSingle();
		try {
			if (mapHost.containsKey(socket)) {
				Logger.error("DataPool.add, duplicate socket host %s", host);
				return Response.IP_EXISTED;
			}
			mapHost.put(socket, host);
			mapTime.put(socket, System.currentTimeMillis());
			return Response.ACCEPTED;
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}

	/**
	 * remove a data site
	 * @param host
	 * @return
	 */
	public short remove(SocketHost socket) {
		Logger.info("DataPool.remove, data site %s", socket);
		
		super.lockSingle();
		try {
			if(!mapHost.containsKey(socket)) {
				return Response.NOTACCEPTED;
			}
			mapHost.remove(socket);
			mapTime.remove(socket);
			return Response.ACCEPTED;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}

	/**
	 * update data site
	 * @param host
	 * @return
	 */
	public short update(SocketHost socket, SiteHost host) {
		this.remove(socket);
		return this.add(socket, host);
	}
	
	/**
	 * check timeout site
	 */
	private void check() {
		int size = mapTime.size();
		if(size == 0) return;

		ArrayList<SocketHost> dels = new ArrayList<SocketHost>(size);
		super.lockSingle();
		try {
			long now = System.currentTimeMillis();
			for (SocketHost host : mapTime.keySet()) {
				Long value = mapTime.get(host);
				if (value == null) {
					dels.add(host);
					continue;
				}
				long time = value.longValue();
				if (now - time >= deleteTime) {
					dels.add(host);
				}
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			super.unlockSingle();
		}
		// delete timeout site
		for(SocketHost socket : dels) {
			this.remove(socket);
		}
	}

}