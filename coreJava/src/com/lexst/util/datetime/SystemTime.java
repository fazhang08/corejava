/**
 * 
 */
package com.lexst.util.datetime;

public class SystemTime {

	public static boolean loaded = false;

	static {
		try {
			System.loadLibrary("lexstnow");
			SystemTime.loaded = true;
		} catch (UnsatisfiedLinkError exp) {
			exp.printStackTrace();
		} catch (SecurityException exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * get system time
	 * @return
	 */
	public native static long get();
	
	/**
	 * set system time
	 * @param time
	 * @return
	 */
	public native static int set(long time);

}