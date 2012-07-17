/**
 *
 */
package com.lexst.log.client;

import java.io.*;

import org.w3c.dom.*;

import com.lexst.xml.*;


public final class LogConfigure {
	// target mode
	private final static int NONE = 0;
	private final static int BUFFER = 1;
	private final static int FILE = 2;
	private final static int SERVER = 3;
	
	// conlose print
	private boolean print;
	// send mode;
	private int sendmode;
	// data buffer size
	private int buffsize;
	// data timeout
	private int timeout;
	// local directory
	private String logpath;
	// local file size
	private int filesize;
	// log level
	private int level;

	/**
	 *
	 */
	public LogConfigure() {
		this.print = false;
		this.sendmode = BUFFER;
		this.level = LogLevel.debug;
	}

	public boolean isPrint() {
		return print;
	}

	public int getMode() {
		return sendmode;
	}

	public boolean isNoneMode() {
		return sendmode == LogConfigure.NONE;
	}
	
	public boolean isBufferMode() {
		return sendmode == LogConfigure.BUFFER;
	}

	public boolean isFileMode() {
		return sendmode == LogConfigure.FILE;
	}
	
	public boolean isServerMode() {
		return sendmode == LogConfigure.SERVER;
	}

	public String getDirectory() {
		return this.logpath;
	}

	public int getFileSize() {
		return this.filesize;
	}

	public int getBufferSize() {
		return this.buffsize;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public int getLevel() {
		return this.level;
	}
	public void setLevel(int value) {
		if (!LogLevel.isLevel(level)) {
			throw new IllegalArgumentException("invalid log level:" + level);
		}
		this.level = value;
	}

	private void setPath(String s) {
		s = s.trim();
		if(s.isEmpty()) return;
		s = s.replace('\\', File.separatorChar);
		s = s.replace('/', File.separatorChar);
		while (true) {
			char c = s.charAt(s.length() - 1);
			if (c != File.separatorChar) break;
			s = s.substring(0, s.length() - 1);
		}
		logpath = s;
	}

	/**
	 * resolve xml log
	 * @return boolean
	 */
	public boolean loadXML(String filename)  {
		File file = new File(filename);
		if (!file.exists()) return false;
		byte[] b = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();
			return loadXML(b);
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		return false;
	}

	/**
	 * @param data
	 * @return
	 */
	public boolean loadXML(byte[] data)  {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(data);
		if (doc == null) return false;

		NodeList list = doc.getElementsByTagName("log");
		Element elem = (Element) list.item(0);

		String s = xml.getValue(elem, "level");
		if ("DEBUG".equalsIgnoreCase(s)) {
			level = LogLevel.debug;
		} else if ("INFO".equalsIgnoreCase(s)) {
			level = LogLevel.info;
		} else if ("WARNING".equalsIgnoreCase(s)) {
			level = LogLevel.warning;
		} else if ("ERROR".equalsIgnoreCase(s)) {
			level = LogLevel.error;
		} else if ("FATAL".equalsIgnoreCase(s)) {
			level = LogLevel.fatal;
		} else {
			throw new IllegalArgumentException("invalid log level!");
		}

		s = xml.getValue(elem, "console-print");
		print = "YES".equalsIgnoreCase(s);

		s = xml.getValue(elem, "directory");
		this.setPath(s);

		s = xml.getValue(elem, "filesize");
		filesize = Integer.parseInt(s) * 1024 * 1024;

		s = xml.getValue(elem, "send-mode");
		
		if ("SERVER".equalsIgnoreCase(s)) {
			sendmode = LogConfigure.SERVER;
		} else if ("FILE".equalsIgnoreCase(s)) {
			sendmode = LogConfigure.FILE;
		} else if ("NONE".equalsIgnoreCase(s)) {
			sendmode = LogConfigure.NONE;
		} else {
			throw new IllegalArgumentException("invalid send mode!");
		}
		s = xml.getValue(elem, "buffer-size");
		buffsize = Integer.parseInt(s) * 1024;
		s = xml.getValue(elem, "send-interval");
		timeout = Integer.parseInt(s);
		return true;
	}

//	public static void main(String[] args) {
//		String filename = "e:/lexst/top/local.xml";
//		LogConfigure lc = new LogConfigure();
//		lc.loadXML(filename);
//	}
}