/**
 *
 */
package com.lexst.call;

public class WebLauncher extends CallLauncher {

	// static handle
	private static WebLauncher selfHandle = new WebLauncher();

	/**
	 *
	 */
	private WebLauncher() {
		super();
		super.setExitVM(false);
		super.setLogging(true);
	}

	/**
	 * return the WebLauncher of handle
	 */
	public static WebLauncher getInstance() {
		return WebLauncher.selfHandle;
	}

}