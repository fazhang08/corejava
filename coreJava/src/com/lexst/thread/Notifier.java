/**
 *
 */
package com.lexst.thread;


public class Notifier {

	private boolean known;

	/**
	 *
	 */
	public Notifier() {
		super();
		this.known = false;
	}

	public boolean isKnown() {
		return this.known;
	}

	/**
	 * thread wait
	 * @param timeout
	 */
	public synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		}catch(InterruptedException exp) {

		}
	}

	/**
	 * notify thread
	 */
	public synchronized void wakeup() {
		this.known = true;
		try {
			this.notify();
		}catch(IllegalMonitorStateException exp) {

		}
	}

}