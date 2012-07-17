/**
 * 
 */
package com.lexst.home;

import com.lexst.thread.*;

final class Helper extends VirtualThread {

	private Launcher launcher;

	/**
	 * 
	 */
	public Helper(Launcher i) {
		super();
		this.launcher = i;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!super.isInterrupted()) {
			launcher.swing();
			this.delay(1000);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
