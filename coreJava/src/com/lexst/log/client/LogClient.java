/**
 *
 */
package com.lexst.log.client;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import com.lexst.fixp.*;
import com.lexst.fixp.client.FixpPacketClient;
import com.lexst.thread.*;
import com.lexst.util.host.*;


public final class LogClient extends VirtualThread {
	// log suffix
	private final static String suffix = ".log";
	
	// log server address
	private SocketHost remote = new SocketHost(SocketHost.UDP);
	// fixp client
	private FixpPacketClient client = new FixpPacketClient();
	
	// log file
	private String filename;
	private int today = -1;
	// log buffer
	private LogBuffer buff = new LogBuffer();

	// log time format
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

	private LogConfigure config;
	
	private LogPrinter printer;

	/**
	 * default constructor
	 */
	public LogClient() {
		super();
		config = new LogConfigure();
		client.setSubPacketTimeout(1000);	//keep timeout, 1000 millisecond
		client.setReceiveTimeout(120);	//120 second
	}

	public void setLogConfigure(LogConfigure cfg) {
		this.config = cfg;
		buff.ensureCapacity(config.getBufferSize());
	}

	public LogConfigure getLogConfigure() {
		return this.config;
	}
	
	public void setLogPrinter(LogPrinter s) {
		this.printer = s;
	}
	public LogPrinter getLogPrinter() {
		return this.printer;
	}

	/**
	 * close tcp socket and udp socket
	 */
	private void closeSocket() {
		client.close();
	}

	/**
	 * create a directory
	 * @param path
	 */
	private boolean createDirectory(String path) {
		Logger.info("LogClient.createDirectory, log directory '%s'", path);
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return file.mkdirs();
		}
		return true;
	}

	/**
	 * check file, choose a file name
	 * @return boolean
	 */
	private boolean choose() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String today = df.format(new java.util.Date());
		for (int index = 1; index < Integer.MAX_VALUE; index++) {
			String s = String.format("%s%s%s(%d)%s", config.getDirectory(), File.separator, today, index, LogClient.suffix);
			File file = new File(s);
			if(!file.exists()) {
				this.filename = s;
				return true;
			}
		}
		return false;
	}

	/**
	 * load udp connect
	 * @param ip
	 * @param port
	 * @throws SocketException
	 */
	private boolean bind() {
		try {
			return client.bind();
		} catch (IOException exp) {
			
		}
		return false;
	}

	/**
	 * @param config
	 * @param host (log server address)
	 * @return
	 * @throws IOException
	 */
	public boolean load(LogConfigure config, SiteHost host) {		
		boolean success = false;
		if (config.isNoneMode() || config.isBufferMode()) {
			setLogConfigure(config);
			success = true;
		} else if (config.isFileMode()) {
			setLogConfigure(config);
			createDirectory(config.getDirectory());
			success = this.choose();
		} else if(config.isServerMode()) {
			if (host == null) return false;
			remote.set(host.getUDPHost());
			success = bind();
			setLogConfigure(config);
		} else {
			throw new IllegalArgumentException("invalid log mode id!");
		}
		// load thread
		if (success) {
			// pre-print
			String s = buff.flush();
			if (config.isPrint() && !s.isEmpty()) {
				if (printer != null) {
					printer.print(s);
				} else {
					System.out.print(s);
				}
			}
			// start thread
			return start();
		}
		return success;
	}

	/**
	 * stop service
	 */
	public void stopService() {
		this.stop();
	}

	/**
	 * datagram send
	 * @param log
	 */
	private void send(String log) {
		byte[] b = toUTF8(log);
		if (b == null) return;

		Command cmd = new Command(Request.APP, Request.ADD_LOG);
		Packet packet = new Packet(remote, cmd);
		packet.setData(b);
		try {
			Packet reply = client.batch(packet);
			cmd = reply.getCommand();
			if (cmd.getResponse() == Response.OKAY) {
				// success
			} else {
				// failed
			}
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}
	
	private byte[] toUTF8(String log) {
		try {
			return log.getBytes("UTF-8");
		} catch (UnsupportedEncodingException exp) {

		}
		return null;
	}

	/**
	 * write text to local disk
	 * @param log
	 */
	private void writeLog(String log) {
		if (this.filename == null) {
			boolean success = this.choose();
			if (!success) return;
		}
		byte[] b = toUTF8(log);
		if (b == null || b.length == 0) return;
		
		try {
			FileOutputStream out = new FileOutputStream(filename, true);
			out.write(b, 0, b.length);
			out.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		} catch(Throwable exp) {
			exp.printStackTrace();
		}
		// check file 
		File file = new File(filename);
		// when sizeout, choose a new file
		if (file.length() >= config.getFileSize()) {
			boolean success = this.choose();
			if (!success) {
				this.filename = null;
				return;
			}
		} else {
			Calendar dar = Calendar.getInstance();
			dar.setTime(new java.util.Date(System.currentTimeMillis()));
			int day = dar.get(Calendar.DAY_OF_MONTH);
			if (this.today == -1) {
				this.today = day;
			} else if (this.today != day) {
				this.today = day;
				this.choose();
			}
		}
	}

	/**
	 * send data to disk or log server
	 */
	private void flush() {
		if(buff.isEmpty()) return;
		String s = buff.remove();
		if (config.isFileMode()) {
			this.writeLog(s); // write to disk
		} else if(config.isServerMode()) {
			this.send(s);	// send to log server
		}
	}

	/**
	 * send log message
	 * @param level
	 * @param log
	 */
	public void sendLog(int level, String log) {
		String s = String.format("%s: %s %s\r\n", LogLevel.getText(level), sdf.format(new Date()), log);
		// console print
		if (config.isPrint()) {
			if (printer != null) {
				printer.print(s);
			} else {
				System.out.print(s);
			}
		}
		// not send
		if (config.isNoneMode()) return;

		// save data
		buff.append(s);
		// when temporary mode, save it
		if (config.isBufferMode()) {
			if (buff.length() >= 524288) buff.clear();
		} else if (buff.isFull()) {
			// flush data to disk or log server
			this.wakeup();
		}
	}

	/**
	 * flush to console
	 */
	public void gushing() {
		String s = buff.remove();
		if (config.isBufferMode()) {
			if (s != null && s.length() > 0) {
				System.out.println(s);
			}
		}
	}

	/**
	 *
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("LogClient.process, timeout:%d, into...", config.getTimeout());
		this.setSleep(config.getTimeout());
		while (!isInterrupted()) {
			this.flush();
			this.sleep();
		}
		Logger.info("LogClient.process, exit");
		this.flush();
	}
	
	/**
	 *
	 */
	@Override
	public void finish() {
		// close socket
		this.closeSocket();
	}
}