/**
 * 
 */
package com.lexst.algorithm.aggregate;

import com.lexst.db.statement.*;

public abstract class DCTask extends AggregateTask implements Runnable {

	// task trigger
	private TaskTrigger trigger;

	// thread handle
	private Thread thread;

	// into thread
	private boolean into;

	protected DC dc;

	private int localIP;

	/**
	 * 
	 */
	public DCTask() {
		super();
		this.into = false;
	}

	public void setDC(DC arg) {
		this.dc = arg;
	}

	public DC getDC() {
		return this.dc;
	}

	public void setLocal(int ip) {
		this.localIP = ip;
	}

	public int getLocal() {
		return this.localIP;
	}

	public void setTrigger(TaskTrigger obj) {
		this.trigger = obj;
	}

	public TaskTrigger getTrigger() {
		return this.trigger;
	}

	/**
	 * into dc mode
	 */
	protected synchronized void into() {
		this.into = true;
		if (thread != null) {
			try {
				notify();
			} catch (IllegalMonitorStateException exp) {

			}
		}
	}

	/**
	 * start thread and init data
	 * 
	 * @return boolean
	 */
	public boolean start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!this.into) {
			this.delay(10L);
		}
		execute();
		trigger.removeTask(getIdentity());
		thread = null;
	}

	/**
	 * launch object
	 * 
	 * @param object
	 */
	public abstract void inject(DCPair object);

	/**
	 * execute "dc" command
	 */
	public abstract void execute();
}