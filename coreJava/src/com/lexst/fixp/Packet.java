/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp data set (udp mode)
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

import com.lexst.util.host.*;


public class Packet extends Entity {

	/**
	 * default constractor
	 */
	public Packet() {
		super();
		remote.setType(SocketHost.UDP);
	}

	/**
	 * @param host
	 */
	public Packet(SocketHost host) {
		super(host);
	}

	/**
	 *
	 * @param cmd
	 */
	public Packet(Command cmd) {
		super(cmd);
	}

	/**
	 *
	 * @param cmd
	 */
	public Packet(SocketHost host, Command cmd) {
		super(host, cmd);
	}

	/**
	 *
	 * @param packet
	 * @throws FixpProtocolException
	 */
	public Packet(byte[] packet) {
		this();
		// resolve reply packet
		this.resolve(packet);
	}

	/**
	 * @param host
	 * @param data
	 * @param off
	 * @param len
	 * @throws FixpProtocolException
	 */
	public Packet(SocketHost host, byte[] data, int off, int len) throws FixpProtocolException {
		this(host);
		// resolve reply packet
		byte[] b = new byte[len];
		System.arraycopy(data, off, b, 0, b.length);
		this.resolve(b);
	}
	
	/**
	 * @param packet
	 */
	protected Packet(Packet packet) {
		super(packet);
	}

	/**
	 * check sub-packet identity
	 * @return
	 */
	public boolean isSubPacket() {
		Message msg1 = findMessage(Key.SUBPACKET_COUNT);
		Message msg2 = findMessage(Key.SUBPACKET_SERIAL);
		return msg1 != null && msg2 != null;
	}

	/**
	 * resolve packet data
	 *
	 * @param b
	 * @return
	 */
	public boolean resolve(byte[] b) {
		// resolve command
		cmd = new Command();
		cmd.resolve(b);
		// resolve all message
		int off = Command.COMMAND_SIZE;
		int count = cmd.getMessageCount();
		for (int i = 0; i < count; i++) {
			Message msg = new Message();
			int size = msg.resolve(b, off);
			if (size < 1) break;
			off += size;
			this.addMessage(msg);
		}
		// resolve packet data
		int len = getContentLength();
		if (len > 0) {
			data = new byte[len];
			System.arraycopy(b, off, data, 0, len);
			off += len;
		}
		return off == b.length;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		Packet packet = new Packet(this);
		return packet;
	}
}