/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * this file is part of lexst, lexst basic pool
 * 
 * @author zheng.liu lexst@126.com
 * @version 1.0 5/2/2009
 * 
 * @see com.lexst.pool
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.pool;

import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;
import com.lexst.util.lock.*;

public abstract class Pool extends VirtualThread {

	// lock object
	private MutexLock lock = new MutexLock();

	// pool check time
	protected long checkTime;
	// site refresh timeout
	protected long refreshTimeout;
	// delete timeout
	protected long deleteTime;
	// instance
	protected IPacketListener listener;

	/**
	 *
	 */
	protected Pool() {
		super();
		this.setSleep(5);
		this.setSiteTimeout(20);
		this.setDeleteTimeout(60);
	}

	public void setListener(IPacketListener object) {
		this.listener = object;
	}

	public IPacketListener getListener() {
		return this.listener;
	}

	public void setDeleteTimeout(int second) {
		if (second >= 5) {
			deleteTime = second * 1000;
		}
	}

	public long getDeleteTimeout() {
		return this.deleteTime;
	}

	public void setSiteTimeout(int second) {
		if (second >= 5) {
			this.refreshTimeout = second * 1000;
		}
	}

	public int getSiteTimeout() {
		return (int) (this.refreshTimeout / 1000);
	}

	protected boolean lockSingle() {
		return lock.lockSingle();
	}

	protected boolean unlockSingle() {
		return lock.unlockSingle();
	}

	protected boolean lockMulti() {
		return lock.lockMulti();
	}

	protected boolean unlockMulti() {
		return lock.unlockMulti();
	}

	/**
	 * send timeout packet to remote
	 * @param remote
	 * @param local
	 * @return
	 */
	protected boolean sendTimeout(SiteHost remote, SiteHost local, int num) {
		for (int i = 0; i < num; i++) {
			Command cmd = new Command(Request.NOTIFY, Request.COMEBACK);
			Packet packet = new Packet(cmd);
			// local listen server address
			packet.addMessage(new Message(Key.SERVER_IP, local.getIP()));
			packet.addMessage(new Message(Key.SERVER_TCPORT, local.getTCPort()));
			packet.addMessage(new Message(Key.SERVER_UDPORT, local.getUDPort()));
			packet.addMessage(new Message(Key.TIMEOUT, refreshTimeout)); // second
			SocketHost address = remote.getUDPHost();
			// send to client
			listener.send(address, packet);
		}
		return true;
	}
}