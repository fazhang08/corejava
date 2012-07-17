/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp packet analyser
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/16/2009
 * 
 * @see com.lexst.fixp.monitor
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp.monitor;

import java.util.*;

import com.lexst.thread.*;
import com.lexst.util.host.*;
import com.lexst.fixp.*;

final class PacketTask extends VirtualThread {

	private FixpPacketHelper helper;
	
	/* keep packet set */
	private Vector<Bucket> buckets = new Vector<Bucket>(50);
	/* normal packet set */
	private Vector<Packet> packets = new Vector<Packet>(50);	
	
	/**
	 * 
	 */
	public PacketTask(FixpPacketHelper object) {
		super();
		this.helper = object;
	}
	
	/**
	 * save a packet
	 * @param packet
	 */
	public void add(Packet packet) {
		boolean empty = packets.isEmpty();
		packets.add(packet);
		if(empty) this.wakeup();
	}
	
	/**
	 * save keep packet
	 * @param bucket
	 */
	public void add(Bucket bucket) {
		boolean empty = buckets.isEmpty();
		buckets.add(bucket);
		if(empty) this.wakeup();
	}
	
	/**
	 * 
	 */
	private void invokePacket() {
		int size = packets.size();
		for (int i = 0; i < size; i++) {
			if(packets.isEmpty()) break;
			Packet request = packets.remove(0);
			if (request == null) continue;

			Command cmd = request.getCommand();
			Packet resp = null;
			if (cmd.isRPCall()) { // RPC command
				resp = helper.callRPC(request);
			} else { // not RPC, call other interface
				resp = helper.callMethod(request);
			}
			if(resp == null) continue;

			// send raw data to client
			SocketHost remote = request.getRemote();
			helper.send(remote, resp);
		}
	}
	
	/**
	 * 
	 */
	private void invokeBucket() {
		int size = buckets.size();
		for(int i = 0; i < size; i++) {
			if(buckets.isEmpty()) break;
			Bucket bucket = buckets.remove(0);
			if(bucket == null) continue;
			
			int packetId = bucket.getPacketId();
			
			Packet request = bucket.compose();
			Command cmd = request.getCommand();
			Packet resp = null;
			if(cmd.isRPCall()) {
				resp = helper.callRPC(request); //call RPC instance
			} else {
				resp = helper.callMethod(request); // call method instance
			}
			if(resp == null) continue;

			// save to helper and send packet
			SocketHost remote = bucket.getRemote();
			helper.reply(remote, resp, packetId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!super.isInterrupted()) {
			if (buckets.size() > 0) {
				this.invokeBucket();
			} else if (packets.size() > 0) {
				this.invokePacket();
			} else {
				this.delay(1000);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		helper.release(this);
	}

}