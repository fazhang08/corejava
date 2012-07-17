/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp keep packet
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

import java.util.*;
import java.io.ByteArrayOutputStream;

import com.lexst.util.host.SocketHost;

public final class Bucket {
	
	public final static int MAX_DATASIZE = 480;
	
	private SocketHost remote;
	private Command command;
	
	private int packetId;
	
	/* integer base from 1 */
	private Map<Integer, Packet> mapPacket = new TreeMap<Integer, Packet>();

	private int count;
	
	private long timeout, startime, endtime;
	
	/**
	 *  default constrator
	 */
	public Bucket() {
		super();
		timeout = 0;
		count = 0;
		startime = endtime = System.currentTimeMillis();
	}
	
	/**
	 * @param packet
	 */
	public Bucket(Packet packet) {
		this();
		this.add(packet);
	}
	
	public long startTime() {
		return this.startime;
	}
	
	public void refreshTime() {
		this.endtime = System.currentTimeMillis();
	}
	
	public void setPacketId(int i) {
		this.packetId = i;
	}
	public int getPacketId() {
		return this.packetId;
	}
	
	public boolean isEmpty() {
		return mapPacket.isEmpty();
	}
	
	public int size() {
		return mapPacket.size();
	}
	
	public Packet get(int serial) {
		return mapPacket.get(serial);
	}

	public void add(Packet packet) {
		int serial = packet.findInt(Key.SUBPACKET_SERIAL);
		mapPacket.put(serial, packet);

		if (count == 0) {
			count = packet.findInt(Key.SUBPACKET_COUNT);
		} else if (count != packet.findInt(Key.SUBPACKET_COUNT)) {
			throw new IllegalArgumentException("invalid sub packet");
		}
		if (timeout == 0) {
			timeout = packet.findInt(Key.SUBPACKET_TIMEOUT);
		} else if (timeout != packet.findInt(Key.SUBPACKET_TIMEOUT)) {
			throw new IllegalArgumentException("invalid subpacket timeout");
		}

		if (remote == null) {
			remote = packet.getRemote();
		} else if (!remote.equals(packet.getRemote())) {
			throw new IllegalArgumentException("invalid socket address");
		}
		
		if(command == null) {
			command = packet.getCommand();
		} else if(!command.equals(packet.getCommand())) {
			throw new IllegalArgumentException("invalid fixp command");
		}

		if (packetId == 0) {
			packetId = packet.findInt(Key.PACKET_IDENTIFY);
		} else if (packetId != packet.findInt(Key.PACKET_IDENTIFY)) {
			throw new IllegalArgumentException("invalid packet identity");
		}

		this.refreshTime();
	}
	
	public SocketHost getRemote() {
		return this.remote;
	}
	
	public boolean isTimeout() {
		return timeout > 0 && System.currentTimeMillis() - endtime >= timeout;
	}
	
	public boolean isFull() {
		return count > 0 && mapPacket.size() == count;
	}
	
	public int[] missings() {
		ArrayList<Integer> a = new ArrayList<Integer>();
		for(int serial = 1; serial <= count; serial++) {
			if(!mapPacket.containsKey(serial)) {
				a.add(serial);
			}
		}
		int size = a.size();
		int[] b = null;
		if (size > 0) {
			b = new int[size];
			for (int i = 0; i < size; i++) {
				b[i] = a.get(i);
			}
		}
		return b;
	}
	
	public Packet compose() {		
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		
		for (int serial : mapPacket.keySet()) {
			Packet sub = mapPacket.get(serial);
			byte[] b = sub.getData();
			if (b != null && b.length > 0) {
				buff.write(b, 0, b.length);
			}
		}
		
		List<Message> list = mapPacket.get(1).messages();
		short[] refuses = { Key.PACKET_IDENTIFY, Key.SUBPACKET_COUNT, Key.SUBPACKET_SERIAL, Key.SUBPACKET_TIMEOUT };
		
		Packet packet = new Packet(remote, command);
		for(Message msg : list) {
			short key = msg.getKey();
			boolean found = false;
			for(int i = 0; i < refuses.length; i++) {
				found = (refuses[i]== key);
				if(found) break;
			}
			if(!found) packet.addMessage(msg);
		}
		
		byte[] b = buff.toByteArray();
		if (b != null && b.length > 0) {
			packet.setData(b);
		}
		return packet;
	}
}