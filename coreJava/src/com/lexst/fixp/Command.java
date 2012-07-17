/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp command (request and response, head information)
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/12/2009
 * 
 * @see com.lexst.fixp
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp;

import com.lexst.util.*;

public final class Command {
	public final static short FIXP_VERSION = 0x100;

	public static final byte request = 2;
	public static final byte response = 3;

	// fixp command size, 7 byte!
	public static final byte COMMAND_SIZE = 7;

	// common head elements

	/* fixp tag, first word */
	private byte tag;
	/* fixp version, 2 byte */
	private short version;
	/* message count, 2 byte */
	private short message_items;

	// request command
	private byte major;
	private byte minor;
	// response code
	private short respcode;

	/**
	 *  default constructor
	 */
	public Command() {
		super();
		this.version = Command.FIXP_VERSION;
	}

	/**
	 * set request command
	 * @param major
	 * @param minor
	 */
	public Command(byte major, byte minor) {
		this();
		this.setRequest(major, minor);
	}

	/**
	 * set response command
	 * @param respcode
	 */
	public Command(short respcode)  {
		this();
		this.setResponse(respcode);
	}

	/**
	 * resolve command byte
	 * @param b
	 */
	public Command(byte[] b) {
		this();
		this.resolve(b);
	}
	
	/**
	 * @param cmd
	 */
	protected Command(Command cmd) {
		super();
		this.tag = cmd.tag;
		this.version = cmd.version;
		this.message_items = cmd.message_items;
		this.major = cmd.major;
		this.minor = cmd.minor;
		this.respcode = cmd.respcode;
	}

	/**
	 * clear all parameter
	 */
	public void clear() {
		this.tag = 0;
		this.version = 0;
		this.message_items = 0;

		this.major = this.minor = 0;
		this.respcode = 0;
	}

	/**
	 * check
	 * @return
	 */
	public boolean isRequest() {
		return this.major != 0 & this.minor != 0;
	}

	/**
	 * check
	 * @return
	 */
	public boolean isResponse() {
		return this.respcode != 0;
	}

	/**
	 * set request
	 * @param majorcmd
	 * @param minorcmd
	 */
	public void setRequest(byte majorcmd, byte minorcmd) {
		if (!Request.isCommand(majorcmd, minorcmd)) {
			throw new FixpProtocolException("invalid main command or sub command!");
		}
		this.major = majorcmd;
		this.minor = minorcmd;
	}

	public byte getMajor() {
		return this.major;
	}

	public byte getMinor() {
		return this.minor;
	}

	/**
	 * check rpc call
	 * @return
	 */
	public boolean isRPCall() {
		return this.major == Request.RPC;
	}

	public boolean isExit() {
		return major == Request.NOTIFY && minor == Request.EXIT;
	}
	
	public boolean isInitSecure() {
		return major == Request.NOTIFY && minor == Request.INIT_SECURE;
	}

	public boolean isLogout() {
		return major == Request.NOTIFY && minor == Request.LOGOUT;
	}

	public boolean isShutdown() {
		return major == Request.NOTIFY && minor == Request.SHUTDOWN;
	}

	public boolean isActive() {
		return major == Request.NOTIFY && minor == Request.HELO;
	}

	public boolean isComeback() {
		return major == Request.NOTIFY && minor == Request.COMEBACK;
	}
	
	public boolean isRetrySubPacket() {
		return major == Request.NOTIFY && minor == Request.RETRY_SUBPACKET;
	}
	
	public boolean isCancelPacket() {
		return major == Request.NOTIFY && minor == Request.CANCEL_PACKET;
	}

	/**
	 * set response code
	 * @param code
	 * @throws FixpProtocolException
	 */
	public void setResponse(short code) throws FixpProtocolException {
		if(!Response.isCode(code)) {
			throw new FixpProtocolException("invalid reply code!");
		}
		this.respcode = code;
	}
	public short getResponse() {
		return this.respcode;
	}

	public void setVersion(short ver) {
		this.version = ver;
	}
	public short getVersion() {
		return this.version;
	}

	public void setMessageCount(short count) {
		this.message_items = count;
	}
	public short getMessageCount() {
		return this.message_items;
	}
	
	private String show(byte[] b) {
		StringBuilder buff = new StringBuilder();
		for(int i = 0; i < b.length; i++) {
			String s = String.format("%X", b[i]);
			if(s.length() == 1) s = "0" +s;
			if(buff.length()>0) buff.append(" ");
			buff.append(s);
		}
		return buff.toString();
	}
	
	/**
	 * resolve command
	 * @param b
	 * @throws FixpProtocolException
	 */
	public void resolve(byte[] b)  {
		if (b.length < Command.COMMAND_SIZE) {
			throw new FixpProtocolException("packet size < " + Command.COMMAND_SIZE);
		}

		// clear all
		this.clear();

		int off = 0;
		this.tag = b[off++];
		byte status = (byte) ((this.tag >>> 6) & 0x3);

		if (status != Command.request && status != Command.response) {
			throw new FixpProtocolException("invalid command status! packet size:" + b.length + " | " + show(b));
		}

		byte cmdsize = (byte) (this.tag & 0x3F);
		if (cmdsize != Command.COMMAND_SIZE) {
			throw new FixpProtocolException("command size not match!, cmd size "+ cmdsize);
		}

		// fixp version
		version = Numeric.toShort(b, off, 2);
		off += 2;

		// check request or response
		if (status == Command.request) {
			major = b[off++];
			minor = b[off++];
			if (!Request.isCommand(major, minor)) {
				throw new FixpProtocolException(String.format("invalid request, %x - %x", major, minor));
			}
		} else if (status == Command.response) {
			this.respcode = Numeric.toShort(b, off, 2);
			off += 2;
			if (!Response.isCode(respcode)) {
				throw new FixpProtocolException("invalid reply, " + respcode);
			}
		}
		this.message_items = Numeric.toShort(b, off, 2);
	}

	/**
	 * 
	 * build to byte stream
	 * 1. fixp tag (tag, 1 byte)
	 * 2. fixp version (2 byte)
	 * 3. major command + minor command (2 byte) | response code (2 byte)
	 * 4. message item number (2 byte)
	 * 
	 * @return
	 * @throws FixpProtocolException
	 */
	public byte[] build()  {
		if(isRequest()) {
			tag = (byte) ((Command.request << 6) | (Command.COMMAND_SIZE & 0x3F));
		} else if(isResponse()) {
			tag = (byte) ((Command.response << 6) | (Command.COMMAND_SIZE & 0x3F));
		} else {
			throw new FixpProtocolException("invalid command!");
		}

		byte[] data = new byte[Command.COMMAND_SIZE];
		int off = 0;
		// set command tag
		data[off++] = this.tag;
		// set fixp version
		byte[] b = Numeric.toBytes(Command.FIXP_VERSION);
		System.arraycopy(b, 0, data, off, 2);
		off += 2;

		if(this.isRequest()) {
			data[off++] = this.major;
			data[off++] = this.minor;
		} else if(this.isResponse()) {
			b = Numeric.toBytes(respcode);
			System.arraycopy(b, 0, data, off, 2);
			off += 2;
		} else {
			return null;
		}

		// set fixp message count
		b = Numeric.toBytes(message_items);
		System.arraycopy(b, 0, data, off, 2);
		off += 2;
		return data;
	}

	public static boolean isFixpTag(byte tag) {
		byte status = (byte)((tag >>> 6) & 0x3);
		return (status == Command.request || status == Command.response);
	}

	public static byte getCommandSizeBy(byte tag) {
		byte cmdSize = (byte)(tag & 0x3F);
		return cmdSize;
	}

	/**
	 * version check
	 * @param version
	 * @return
	 */
	public static boolean matchVersion(short version) {
		return Command.FIXP_VERSION == version;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if(arg == null || arg.getClass() != Command.class) {
			return false;
		}
		if(arg == this) return true;
		
		Command cmd = (Command)arg;
		if (cmd.isRequest() && this.isRequest()) {
			if (major != cmd.major || minor != cmd.minor) return false;
		} else if(isResponse() && cmd.isResponse()) {
			if(this.respcode != cmd.respcode) return false;
		} else return false;
		
		if(this.tag != cmd.tag) return false;
		if(this.message_items != cmd.message_items) return false;
		if(this.version != cmd.version) return false;
		
		return true;
	}
}