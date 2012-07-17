/**
 * 
 */
package com.lexst.log.server;

import java.io.*;
import java.text.*;
import java.util.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.util.host.SocketHost;


final class LogPacketWriter implements PacketInvoker {

	// log max file length
	private final static long maxFilesize = 10 * 1024 * 1024;

	// log base directory
	private String path;
	
	private int today;
	
	/**
	 * 
	 */
	public LogPacketWriter() {
		super();
		this.today = -1;
	}
	
	public LogPacketWriter(String path) {
		this();
		this.setPath(path);
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.invoke.PacketInvoker#invoke(com.lexst.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		SocketHost remote = packet.getRemote();
		Command cmd = packet.getCommand();
		
		if (cmd.getMajor() == Request.APP && cmd.getMinor() == Request.ADD_LOG) {
			byte[] data = packet.getData();
			// write log to disk
			boolean success = writeLog(remote, data);
			// reply packet
			cmd = new Command(success ? Response.OKAY : Response.SERVER_ERROR);
		} else {
			cmd = new Command(Response.UNSUPPORT);
		}
		Packet resp = new Packet(remote, cmd);
		return resp;
	}

	public void setPath(String s) {
		this.path = s;
	}
	public String getPath() {
		return this.path;
	}
	
	/**
	 * create local directory by ip address
	 * @param logPath
	 */
	private String mkdirs(String ip) {
		char c = File.separatorChar;
		String logpath = this.path;
		logpath = logpath.replace('\\', c);
		logpath = logpath.replace('/', c);
		if (logpath.charAt(logpath.length() - 1) != c) logpath = logpath + c;
		logpath += ip;
		
		File file = new File(logpath);
		if (file.exists() && file.isDirectory()) return logpath;
		// not found, create it
		boolean success = file.mkdirs();
		return success ? logpath : null;
	}

	/**
	 * @param fromIP
	 * @return
	 */
	private String choose(String fromIP) {
		// create directory
		String logpath = mkdirs(fromIP);
		if (logpath == null) return null;
		
		// last filename		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String today = df.format(new Date(System.currentTimeMillis()));
		File last = null;
		int index = 1;
		for (; true; index++) {
			String s = String.format("%s%s%s(%d).log", logpath, File.separator, today, index);
			File file = new File(s);
			if (!file.exists()) {
				if(last == null) last = file;
				break;
			} else if (file.length() < LogPacketWriter.maxFilesize) {
				last = file; // next file
			}
		}
		return last.getAbsolutePath();
	}

	/**
	 * write log to disk
	 * 
	 * @param data
	 * @param len
	 */
	private boolean writeLog(SocketHost remote, byte[] data) {
		boolean success = false;
		String filename = this.choose(remote.getIP());
		if(filename == null) return false;
		try {
			FileOutputStream writer = new FileOutputStream(filename, true);
			writer.write(data, 0, data.length);
			writer.flush();
			writer.close();
			success = true;
		} catch (Throwable exp) {
			exp.printStackTrace();
		}

		// check date
		Calendar dar = Calendar.getInstance();
		dar.setTime(new Date(System.currentTimeMillis()));
		int day = dar.get(Calendar.DAY_OF_MONTH);
		if (today == -1 || today != day) {
			today = day;
		}
		return success;
	}

}