/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author yj.liang lexst@126.com
 * @version 1.0 10/3/2009
 * 
 * @see com.lexst.util.lock
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.lock;

/**
 * 这是一个互斥的操作,
 * 做multi操作时,不允许single,做single操作时,不允许multi
 * multi锁比single锁的优先级高
 *
 * 这是一个非常重要的类!!!
 *
 */
public final class MutexLock  {

	/* 多向锁,允许同时有多个并发存在. 同时与单向锁互斥 */
	private int multi;

	/* 单向锁,一个时间内只允许有一个单向锁存在. 同时与多向锁互斥 */
	private boolean single;

	/* 锁定超时,单位:毫秒 */
	private long timeout;

	private boolean multiInto;
	private boolean singleInto;

	/**
	 * construct method
	 */
	public MutexLock() {
		super();
		this.multi = 0;
		this.single = false;
		this.multiInto = false;
		this.singleInto = false;
		this.setTimeout(20L);
	}

	/**
	 * construct method
	 * @param timeout (milli-second)
	 */
	public MutexLock(long timeout) {
		this();
		this.setTimeout(timeout);
	}

	/**
	 * 锁定等待延时(最好50毫秒以内)
	 * @param millisecond
	 */
	public void setTimeout(long millisecond) {
		if (millisecond >= 10L) {
			this.timeout = millisecond;
		}
	}
	public long getTimeout() {
		return this.timeout;
	}

	/**
	 * 唤醒
	 */
	private synchronized void wakeup() {
		try {
			super.notify();
		}catch(IllegalMonitorStateException exp) { }
	}

	/**
	 * 多向锁锁定
	 * @return
	 */
	public boolean isLockMulti() {
		return multi > 0;
	}
	/**
	 * 单向锁锁定
	 * @return
	 */
	public boolean isLockSingle() {
		return single;
	}

	/**
	 * 允许多向锁同时多个锁定,只允许单向锁同时一个锁定
	 * m (多向锁)
	 *
	 * @param m
	 */
	private synchronized boolean lock(boolean m) {
		if (m) {
			this.multiInto = true;	//多向锁进入状态
			while (this.single) { // 必须等待单向锁解除
				try {
					this.wait(timeout);
				} catch (InterruptedException exp) {
				}
			}
			this.multi++;
			this.multiInto = false;	//多向锁退出状态
		} else {
			if(this.single) return false;	//如果单向锁已经锁定,不接受处理
			this.singleInto = true;		//单向锁进入状态
			while (this.multi > 0) { // 必须等待多向锁全部解除
				try {
					this.wait(timeout);
				} catch (InterruptedException exp) {
				}
			}
			this.single = true;
			this.singleInto = false;//单向锁退出状态
		}
		return true;
	}

	/**
	 * 解锁
	 * @param m
	 * @return
	 */
	private synchronized boolean unlock(boolean m) {
		if(m) {
			if (this.multi < 1) return false;
			multi--;
			if (this.singleInto && multi == 0) this.wakeup(); //如果单向锁定,多向锁已经结束时,通知!
		} else {
			if (!this.single) return false;	//如果单向锁未锁定,不接受解锁
			this.single = false;
			if (this.multiInto) this.wakeup(); //如果多向锁进入状态,通知它退出!
		}
		return true;
	}

	/**
	 * 多向并发锁锁定. 如果单向锁已经锁定,必须等待单向锁解锁,才允许继续
	 * @return boolean
	 */
	public boolean lockMulti() {
		return this.lock(true);
	}

	/**
	 * 多向锁解锁. 因为多向锁优先级高,所以不考虑单向锁的状态
	 * (多向锁解锁前,可能单向锁已经锁定. 因为优先级高,不考虑单向锁的解锁)
	 * @return
	 */
	public boolean unlockMulti() {
		return this.unlock(true);
	}

	/**
	 * 多向锁的优先级比单向锁高.
	 * 单身锁锁定,如果有多向锁,必须等待全部结束,才允许锁定.
	 * 锁定后,如果多向锁仍存在,继续等待
	 * @return
	 */
	public boolean lockSingle() {
		return this.lock(false);
	}

	/**
	 * 对单向锁定解锁. 如果有多向锁,通知开锁!
	 * (单向锁解锁前,多向锁肯定是0, 通知解锁只是一个尝试,并不能确定多向锁在锁定)
	 * @return
	 */
	public boolean unlockSingle() {
		return this.unlock(false);
	}
}