/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp data set (tcp mode)
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/13/2009
 * 
 * @see com.lexst.fixp
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp;

import java.io.*;
import java.text.*;
import java.util.*;

import com.lexst.util.host.*;


public class Stream extends Entity {

	private InputStream input;
	private OutputStream output;

	/**
	 *
	 */
	public Stream() {
		super();
		remote.setType(SocketHost.TCP);
	}

	/**
	 * @param cmd
	 */
	public Stream(Command cmd) {
		super(cmd);
		remote.setType(SocketHost.TCP);
	}

	/**
	 * @param host
	 */
	public Stream(SocketHost host) {
		super(host);
	}

	/**
	 * @param host
	 * @param cmd
	 */
	public Stream(SocketHost host, Command cmd) {
		super(host, cmd);
	}
	
	/**
	 * @param stream
	 */
	protected Stream(Stream stream) {
		super(stream);
		this.input = stream.input;
		this.output = stream.output;
	}

	/**
	 * get input handle
	 * @return
	 */
	public InputStream getInput() {
		return this.input;
	}

	/**
	 * get output handle
	 * @return
	 */
	public OutputStream getOutput() {
		return this.output;
	}

	/**
	 * delay
	 * @param timeout
	 */
	protected synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {

		}
	}

	/**
	 * read all
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	private int full(byte[] b, int off, int len) throws IOException {
		int count = 0, err = 0;
		while (true) {
			int size = input.read(b, off, len - count);
			if (size < 1) {
				err++;
				if (err > 3) {
					throw new IOException(String.format("read stream error! from %s, at %s", remote, now()));
				}
				delay(1000);
				continue;
			}
			err = 0;
			count += size;
			if (count >= len) break;
			off += size;
		}
		return count;
	}

	private String now() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
		return sdf.format(new Date(System.currentTimeMillis()));
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param readBody
	 * @throws IOException
	 */
	public void read(InputStream in, OutputStream out, boolean readBody) throws IOException {
		this.input = in;
		this.output = out;

		// read command
		byte[] b = new byte[Command.COMMAND_SIZE];
		this.full(b, 0, b.length);
		// resolve command and all message
		try {
			cmd = new Command(b);
			int count = cmd.getMessageCount();
			for (int i = 0; i < count; i++) {
				Message msg = new Message();
				msg.resolve(in);
				this.addMessage(msg);
			}
		} catch (FixpProtocolException exp) {
			throw new IOException(exp);
		}
		// read data
		if (readBody) {
			int len = getContentLength();
			// read data
			if (len > 0) {
				data = new byte[len];
				this.full(data, 0, data.length);
			}
		}
	}

	/**
	 * read data field
	 * @return
	 * @throws IOException
	 */
	public int readBody() throws IOException {
		int size = getContentLength();
		if (size < 1) return 0;
		data = new byte[size];
		int len = full(data, 0, data.length);
		if (len != data.length) {
			throw new IOException("not match stream!");
		}
		return len;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public byte[] readContent() throws IOException {
		int len = this.readBody();
		if(len < 1) return null;
		return data;
	}

	/**
	 * read all bytes
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int readFull(byte[] b, int off, int len) throws IOException {
		return full(b, off, len);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		Stream stream = new Stream(this);
		return stream;
	}
}