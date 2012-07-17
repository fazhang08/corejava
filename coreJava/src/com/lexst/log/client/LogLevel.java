/**
 * log rank 
 */
package com.lexst.log.client;


public final class LogLevel {

	// level statement
	public final static String Debug = "DEBUG";
	public final static String Info = "INFO";
	public final static String Warning = "WARN";
	public final static String Error = "ERROR";
	public final static String Fatal = "FATAL";

	// level identity
	public final static int none = 0;
	public final static int debug = 1;
	public final static int info = 2;
	public final static int warning = 3;
	public final static int error = 4;
	public final static int fatal = 5;

	public LogLevel() {
		super();
	}

	/**
	 * @param level
	 * @return boolean
	 */
	public static boolean isLevel(int level) {
		return (LogLevel.none <= level && level <= LogLevel.fatal);
	}

	/**
	 * @param level
	 * @return
	 */
	public static String getText(int level) {
		String s = "none";
		switch(level) {
		case LogLevel.debug:
			s = LogLevel.Debug;
			break;
		case LogLevel.info:
			s = LogLevel.Info;
			break;
		case LogLevel.warning:
			s = LogLevel.Warning;
			break;
		case LogLevel.error:
			s = LogLevel.Error;
			break;
		case LogLevel.fatal:
			s = LogLevel.Fatal;
			break;
		}
		return s;
	}

}