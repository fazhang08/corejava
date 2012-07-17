/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com  All rights reserved. lexst@126.com
 * 
 * lexst thread basic class
 * 
 * @author yj.liang
 * 
 * @version 1.0 2/1/2009
 * 
 * @see com.lexst.thread
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.thread;

public abstract class VirtualThread implements Runnable {
	// thread handle
	private Thread thread;
	private boolean running;
	// stop tag
	private boolean interrupted;
	// sleep time
	private long sleep;
	// exit JVM, when close service, default is true
	private boolean exitVM;
	// set log print 
	private boolean logging;
	// notify handle
	private Notifier notifier;

	/**
	 * Constructs a basic thread
	 */
	public VirtualThread() {
		super();
		this.setSleep(5);
		this.interrupted = false;
		this.thread = null;
		this.running = false;
		this.exitVM = false;
		this.logging = false;
		this.notifier = null;
	}

	/**
	 * set vm mode
	 * @param b
	 */
	public void setExitVM(boolean b) {
		this.exitVM = b;
	}
	/**
	 * check
	 * @return
	 */
	public boolean isExitVM() {
		return this.exitVM;
	}
	
	/**
	 * @param b
	 */
	public void setLogging(boolean b) {
		this.logging = b;
	}
	public boolean isLogging() {
		return this.logging;
	}

	/**
	 * set sleep time, unit:second
	 * @param second
	 */
	public void setSleep(int second) {
		if (second >= 1) {
			this.sleep = second * 1000L;
		}
	}

	/**
	 * return sleep time, unit: milli-second
	 * @return
	 */
	public int getSleep() {
		return (int) (sleep / 1000L);
	}

	/**
	 * thread wait
	 * @param timeout
	 */
	public synchronized void delay(long timeout) {
		try {
			super.wait(timeout);
		}catch(InterruptedException exp) {

		}
	}

	/**
	 * sleep
	 */
	protected void sleep() {
		this.delay(this.sleep);
	}

	/**
	 * notify thread
	 */
	protected synchronized void wakeup() {
		try {
			this.notify();
		}catch(IllegalMonitorStateException exp) {

		}
	}

	/**
	 * start thread and init data
	 *
	 * @return boolean
	 */
	public boolean start() {
		synchronized (this) {
			if (thread != null) {
				return false;
			}
		}
		// init service
		boolean success = init();
		if (!success) {
			// print log
			if (logging) {
				com.lexst.log.client.Logger.gushing();
			}
			// exit java vm
			if (exitVM) {
				System.exit(0);
			}
			return false; // failed
		}
		// start thread
		thread = new Thread(this);
		thread.start();
		return true;
	}

	/**
	 * stop thread
	 */
	public void stop() {
		if (interrupted) return;
		interrupted = true;
		this.wakeup();
	}

	/**
	 * stop thread
	 * @param noti
	 */
	public void stop(Notifier noti) {
		this.notifier = noti;
		this.stop();
	}

	/**
	 * check interrupt
	 * @return
	 */
	public boolean isInterrupted() {
		return this.interrupted;
	}

	/**
	 * @param b
	 */
	protected void setInterrupted(boolean b) {
		this.interrupted = b;
	}

	/**
	 * check running status
	 */
	public boolean isRunning() {
		return running && thread != null;
	}

	/**
	 * run task
	 */
	public void run() {
		this.running = true;
		while(!isInterrupted()) {
			this.process();
		}
		this.finish();
		// notify handle
		if (notifier != null) {
			notifier.wakeup();
		}
		this.running = false;
		thread = null;
		// enforce JVM
		if(exitVM) {
			System.exit(0);
		}
	}

	/**
	 * init service
	 * @return
	 */
	public abstract boolean init();

	/**
	 * process task
	 */
	public abstract void process();

	/**
	 * stop service
	 */
	public abstract void finish();
}