/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp basic client
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/16/2009
 * 
 * @see com.lexst.fixp.client
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp.client;

import com.lexst.util.host.*;
import com.lexst.fixp.Cipher;

class FixpClient {
	// remote socket address
	protected SocketHost remote = new SocketHost(SocketHost.NONE);
	// local ip address
	protected String bindIP;

	// debug status
	protected boolean debug;
	// connect and receive timeout, unit:second
	protected int connect_timeout;
	protected int receive_timeout;
	// receive and send buffer size
	protected int receive_buffsize;
	protected int send_buffsize;

	/* security cipher */
	protected Cipher cipher;
	
	/**
	 *
	 */
	public FixpClient() {
		super();
		this.setConnectTimeout(60);
		this.setReceiveTimeout(0); //unlimit
		this.setReceiveBuffSize(512);
		this.setSendBuffSize(512);
		this.setDebug(false);
	}

	/**
	 * remote address
	 * @param host
	 */
	public void setRemote(SocketHost host) {
		this.remote.set(host);
	}

	public SocketHost getRemote() {
		return this.remote;
	}

	public void setBindIP(String s) {
		this.bindIP = s;
	}
	public String getBindIP() {
		return this.bindIP;
	}

	public void setDebug(boolean b) {
		debug = b;
	}

	public boolean isDebug() {
		return debug;
	}

	protected synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {

		}
	}
	
	protected synchronized void wakeup() {
		try {
			this.notify();
		} catch (IllegalMonitorStateException exp) {

		}
	}

	/**
	 * connect time
	 * @param second
	 */
	public void setConnectTimeout(int second) {
		if (second > 1) connect_timeout = second;
	}
	public int getConnectTimeout() {
		return connect_timeout;
	}

	/**
	 * receive time
	 * @param second
	 */
	public void setReceiveTimeout(int second) {
		if (second >= 0) {
			receive_timeout = second;
		}
	}
	public int getReceiveTimeout() {
		return receive_timeout;
	}

	/**
	 * socket receive buffer size
	 * @param size
	 */
	public void setReceiveBuffSize(int size) {
		if(size >= 128) {
			receive_buffsize = size;
		}
	}

	public int getReceiveBuffSize() {
		return receive_buffsize;
	}

	/**
	 * socket send buffer size
	 *
	 * @param size
	 */
	public void setSendBuffSize(int size) {
		if(size >= 128) {
			send_buffsize = size;
		}
	}

	public int getSendBuffSize() {
		return send_buffsize;
	}

	/**
	 * set cipher instance of null
	 * 
	 * @param cipher
	 */
	public void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}

	public Cipher getCipher() {
		return this.cipher;
	}
}