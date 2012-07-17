/**
 * 客户端日志服务接口
 *
 * 客户端日志服务配置分为两部分:
 * 1. 依从于本地的配置 (日志设置级别,本地IP地址)
 * 2. 依从于服务中心的配置(日志服务器的IP地址,TCP UDP端口分配)
 */
package com.lexst.log.client;

import java.io.*;

import com.lexst.util.host.*;

/**
 * @author steven
 *
 * 与Logger的区别是: 这里面的函数都是非静态调用,必须在构造一个类实例
 *
 * @version 1.0
 */
public final class Logger2 {
	// 定义当前日志的级别(DEBUG级别最低)
	private int logLevel = LogLevel.debug;
	// 发送日志的客户端对象
	private LogClient client = new LogClient();

	/**
	 *
	 */
	public Logger2() {
		super();
	}

	/**
	 * 定义日志级别
	 * @param level
	 */
	public void setLogLevel(final int level) {
		if(LogLevel.isLevel(level)) {
			logLevel = level;
		}
	}

	/**
	 * start log service
	 * @param logUtil
	 * @param remote
	 * @return
	 */
	public boolean loadService(LogConfigure logUtil, SiteHost remote) {
		if(client.isRunning()) {
			System.out.println("Logger2, log client running!");
			return false;
		}

		switch( logUtil.getLevel()) {
		case LogLevel.debug:
			Logger.setLevel(LogLevel.debug);
			break;
		case LogLevel.info:
			Logger.setLevel(LogLevel.info);
			break;
		case LogLevel.warning:
			Logger.setLevel(LogLevel.warning);
			break;
		case LogLevel.error:
			Logger.setLevel(LogLevel.error);
			break;
		case LogLevel.fatal:
			Logger.setLevel(LogLevel.fatal);
			break;
		}

		boolean success = client.load(logUtil, remote);
		return success;
	}

	/**
	 * stop log service
	 */
	public synchronized void stopService() {
		if(!client.isRunning()) return;

		info("shutdown log service");
		client.stopService();
	}

	/**
	 * debug 数据
	 * @param log
	 */
	public void debug(String log) {
		if (client != null && logLevel <= LogLevel.debug) {
			client.sendLog(LogLevel.debug, log);
		}
	}

	/**
	 * debug 数据
	 * @param format
	 * @param args
	 */
	public void debug(String format, Object ... args) {
		String s = String.format(format, args);
		this.debug(s);
	}

	/**
	 * info日志
	 * @param log
	 */
	public void info(String log) {
		if (client != null && logLevel <= LogLevel.info) {
			client.sendLog(LogLevel.info, log);
		}
	}

	/**
	 * info 日志
	 * @param format
	 * @param args
	 */
	public void info(String format, Object ... args) {
		String s = String.format(format, args);
		this.info(s);
	}

	/**
	 * warning日志
	 * @param log
	 */
	public void warning(String log) {
		if (client != null && logLevel <= LogLevel.warning) {
			client.sendLog(LogLevel.warning, log);
		}
	}

	/**
	 * warning 日志
	 * @param format
	 * @param args
	 */
	public void warning(String format, Object ... args) {
		String s = String.format(format, args);
		this.warning(s);
	}

	/**
	 * error日志
	 * @param log
	 */
	public void error(String log) {
		if (client != null && logLevel <= LogLevel.error) {
			client.sendLog(LogLevel.error, log);
		}
	}

	private static String throwText(Throwable e) {
		if (e == null) return "";
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		PrintStream s = new PrintStream(out, true);
		e.printStackTrace(s);
		byte[] data = out.toByteArray();
		return new String(data, 0, data.length);
	}
	
	/**
	 * error日志
	 * @param log
	 * @param exp
	 */
	public void error(String log, Throwable handle) {
		if (client != null && logLevel <= LogLevel.error) {
			StringBuilder buff = new StringBuilder();
			buff.append(String.format("%s - ", log));
			buff.append(Logger2.throwText(handle));
			client.sendLog(LogLevel.error, buff.toString());
		}
	}

	/**
	 * error 日志
	 * @param format
	 * @param args
	 */
	public void error(String format, Object ... args) {
		String s = String.format(format, args);
		this.error(s);
	}

	/**
	 * error 日志
	 * @param handle
	 * @param format
	 * @param args
	 */
	public void error(Throwable handle, String format, Object ... args) {
		String s = String.format(format, args);
		this.error(s, handle);
	}

}