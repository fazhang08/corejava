/**
 *
 */
package com.lexst.call;

import com.lexst.log.client.*;

public class Launcher extends CallLauncher {

	private static Launcher selfHandle = new Launcher();

	/**
	 *
	 */
	private Launcher() {
		super();
		super.setExitVM(true);
	}

	/*
	 * return a static handle
	 */
	public static Launcher getInstance() {
		return Launcher.selfHandle;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}
		String filename = args[0];
		boolean success = Launcher.getInstance().loadLocal(filename);
		Logger.note("Launcher.main, load local", success);
		if (success) {
			success = Launcher.getInstance().start();
			Logger.note("Launcher.main, start service", success);
		}
		if (!success) {
			Logger.gushing();
		}
	}

}
