/**
 *
 */
package com.lexst.remote.client;

import java.io.*;

/**
 * 线程客户端. 供子类使用. 子类必须实现"execute"方法
 *
 */
public abstract class ThreadClient extends RemoteClient implements Runnable {

	// thread handle
	private Thread thread;
	// stop identity
	private boolean interrupted;
	// thread sleep time
	private long sleep;

	/**
	 * construct method
	 */
	public ThreadClient(boolean stream) {
		super(stream);
		setSleep(10000L);
		thread = null;
		interrupted = false;
	}

	/**
	 * @param interfaceName
	 */
	public ThreadClient(boolean stream, String interfaceName) {
		this(stream);
		this.setInterfaceName(interfaceName);
	}

	/**
	 * set default delay time
	 * @param timeout (milli-second)
	 */
	public void setSleep(long timeout) {
		if (timeout >= 1000L) {
			this.sleep = timeout;
		}
	}

	public long getSleep() {
		return this.sleep;
	}

	/**
	 * thread wait
	 */
	protected void sleep() {
		this.delay(sleep);
	}

	/**
	 * stop thread and close socket
	 */
	public void stop() {
		interrupted = true;
		wakeup();
	}

	/**
	 * check thread status
	 * @return
	 */
	public boolean isInterrupted() {
		return this.interrupted;
	}

	/**
	 * @return
	 */
	public boolean isRunning() {
		return this.thread != null;
	}

	/**
	 * notify thread
	 */
	protected void wakeupThread() {
		if (thread != null) {
			this.wakeup();
		} else {
			thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * start thread
	 */
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * stop thread
	 */
	public void interrupt() {
		this.interrupted = true;
		this.wakeupThread();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (!isInterrupted()) {
			// execute job
			execute();
		}
		this.release();
		thread = null;
	}

	private void release() {
		try {
			if (isConnected()) {
				this.exit();
			}
		} catch (IOException exp) {
			exp.printStackTrace();
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			this.close();
		}
	}

	/**
	 * execute jobs
	 */
	protected abstract void execute();
}