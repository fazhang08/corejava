/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author yj.liang lexst@126.com
 * @version 1.0 1/3/2009
 * 
 * @see com.lexst.util.lock
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.lock;

/**
 * 处理加锁,解锁操作. 锁定后,必须有解锁
 * 
 * 此类虽然简单,但是是一个非常重要的同步处理类
 *
 */
public final class SingleLock  {

	/* lock tag */
	private boolean locked;

	/* lock timeout (milli-second) */
	private long timeout;

	/**
	 * construct method
	 * @param timeout
	 */
	public SingleLock(long timeout) {
		super();
		this.locked = false;
		this.setTimeout(timeout);
	}

	/**
	 * construct method
	 *
	 */
	public SingleLock() {
		this(20L);
	}
	
	public void setTimeout(long millisecond) {
		if (millisecond > 0L) this.timeout = millisecond;
	}
	public long getTimeout() {
		return this.timeout;
	}
	
	/**
	 * check locked
	 * @return
	 */
	public synchronized boolean isLocked() {
		return this.locked;
	}

	/**
	 * enter locked status
	 */
	public synchronized void lock() {
		while(locked) {
			try {
				this.wait(timeout);
			}catch(InterruptedException exp) {
				exp.printStackTrace();
			}
		}
		this.locked = true;
	}
	
	/**
	 * cancel lock
	 * @return
	 */
	public synchronized boolean unlock() {
		if(locked) {
			this.locked = false;
			try {
				this.notify();
			}catch(IllegalMonitorStateException exp) { }
			return true;
		}
		return false;
	}
}