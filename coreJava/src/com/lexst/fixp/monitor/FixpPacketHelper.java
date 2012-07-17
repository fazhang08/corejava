/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp udp processer
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/20/2009
 * 
 * @see com.lexst.fixp.monitor
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp.monitor;

import java.io.*;
import java.util.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.remote.*;
import com.lexst.security.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.host.*;

final class FixpPacketHelper extends VirtualThread {
	// packet array
	private Vector<Packet> array = new Vector<Packet>();
	// rpc instance
	private RPCInvoker rpcInstance;
	// fixp packet instance
	private PacketInvoker packetInstance;
	// fixp packet listener
	private IPacketListener listener;

	// request keep packet set
	private Map<SocketHost, Bucket> requests = new HashMap<SocketHost, Bucket>();
	// response keep packet set
	private Hashtable<SocketHost, Bucket> resps = new Hashtable<SocketHost, Bucket>();

	// subthread task
	private int taskIndex;
	private PacketTask[] tasks;

	// security manager	
	private Security security;

	/**
	 * default constrator
	 */
	public FixpPacketHelper(int tasksize) {
		super();
		taskIndex = 0;
		tasks = new PacketTask[tasksize];
	}

	public void setPacketListener(IPacketListener instance) {
		this.listener = instance;
	}
	public IPacketListener getPacketListener() {
		return this.listener;
	}

	/**
	 * RPC instance handle
	 * @param instance
	 */
	public void setRPCallImpl(RPCInvoker instance) {
		this.rpcInstance = instance;
	}
	public RPCInvoker getRPCallImpl() {
		return this.rpcInstance;
	}

	/**
	 * Packet Invoker instance handle
	 * @param instance
	 */
	public void setPacketCallImpl(PacketInvoker instance) {
		this.packetInstance = instance;
	}
	public PacketInvoker getPacketCallImpl() {
		return this.packetInstance;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public Security getSecurity() {
		return this.security;
	}
	
	private boolean matchAddress(Packet packet) {
		Message msg = packet.findMessage(Key.BIND_IP);
		if(msg == null) return false;
		String ip = msg.stringValue();
		SocketHost remote = packet.getRemote();
		return remote.getIP().equalsIgnoreCase(ip);
	}

	/**
	 * send data to client
	 * @param packet
	 */
	protected void send(Packet packet) {
		listener.send(packet.getRemote(), packet);
	}

	/**
	 * send data to client
	 * @param remote
	 * @param packet
	 */
	protected void send(SocketHost remote, Packet packet) {
		listener.send(remote, packet);
	}
	
	private boolean authenticate(Packet request) {
		if (security == null || security.isNone()) return true;

		SocketHost remote = request.getRemote();

		if (security.isAddressMatch()) {
			if (!matchAddress(request)) {
				return false;
			}
		} else if (security.isAddressCheck()) {
			// check address range
			long ip = remote.getIPv4() & Long.MAX_VALUE;
			if (!security.isLegalAddress(ip)) {
				return false;
			}
		} else if (security.isCipherCheck()) {

		} else if (security.isDoubleCheck()) {
			long ip = remote.getIPv4() & Long.MAX_VALUE;
			if (!security.isLegalAddress(ip)) {

			}
		}

		return true;
	}

	/**
	 * execute remote call
	 * @param request
	 * @return
	 */
	protected Packet callRPC(Packet request) {		
		if(!authenticate(request)) {
			Command cmd = new Command(Response.AUTHENTICATE_FAILED);
			Packet resp = new Packet(request.getRemote(), cmd);
			return resp;
		}

		byte[] data = request.getData();
		// resolve rpc data
		Apply apply = null;
		Reply reply = null;
		try {
			apply = Apply.resolve(data);
		} catch (IOException exp) {
			reply = new Reply(null, exp);
		} catch (ClassNotFoundException exp) {
			reply = new Reply(null, exp);
		} catch (Throwable exp) {
			reply = new Reply(null, exp);
		}
		if (reply == null) { // invoke RPC server
			reply = rpcInstance.invoke(apply);
		}
		data = reply.build();

		Command cmd = new Command(Response.OKAY);
		Packet resp = new Packet(request.getRemote(), cmd);
		resp.addMessage(Key.CONTENT_TYPE, Value.RAW_DATA);
		resp.setData(data);
		return resp;
	}

	/**
	 * @param request
	 * @return
	 */
	protected Packet callMethod(Packet request) {
		if(!authenticate(request)) {
			Command cmd = new Command(Response.AUTHENTICATE_FAILED);
			Packet resp = new Packet(request.getRemote(), cmd);
			return resp;
		}
		
		if (packetInstance != null) {
			Packet resp = packetInstance.invoke(request);
			return resp;
		}
		return null;
	}

	/**
	 * build a invalid packet
	 * @param host
	 */
	protected Packet invalid() {
		Command cmd = new Command(Response.SERVER_ERROR);
		Packet packet = new Packet(cmd);
		packet.addMessage(Key.SPEAK, "sorry, invalid!");
		return packet;
	}

	/* below is keep packet method */
	
	/**
	 * 
	 * @param request
	 */
	private void sendSubPacket(Packet request) {
		SocketHost remote = request.getRemote();
		int serial = request.findInt(Key.SUBPACKET_SERIAL);
		if(serial < 1) return; // this is invalid
		
		Bucket bucket = resps.get(remote);
		Packet sub = bucket.get(serial);
		if (sub == null) return; //not found

		this.listener.send(remote, sub);
	}

	/**
	 * @param request
	 */
	private void sendCancelPacket(Packet request) {
		SocketHost remote = request.getRemote();

		short code = (requests.containsKey(remote) ? Response.OKAY : Response.NOTFOUND);
		Command cmd = new Command(code);
		Packet resp = new Packet(remote, cmd);
		resp.addMessage(Key.SPEAK, "cancel keep");
		
		this.listener.send(remote, resp);
	}
	
	/**
	 * 
	 * @param packet
	 */
	private void addSubPacket(Packet packet) {
		SocketHost remote = packet.getRemote();
		Bucket bucket = requests.get(remote);
		if(bucket == null) {
			bucket = new Bucket();
			requests.put(remote, bucket);
		}
		bucket.add(packet);
		// when full, call
		if(bucket.isFull()) {
			requests.remove(remote);
			if(taskIndex >= tasks.length) taskIndex = 0;
			tasks[taskIndex++].add(bucket);
		}
	}

	/**
	 * @param remote
	 * @param resp
	 */
	protected void reply(SocketHost remote, Packet resp, int packetId) {
		Bucket bucket = this.split(remote, resp, packetId);
		this.resps.put(remote, bucket); // save packet set
		this.send(bucket);
	}

	/**
	 * @param bucket
	 */
	private void send(Bucket bucket) {
		for (int serial = 1; true; serial++) {
			Packet resp = bucket.get(serial);
			if(resp == null) break;
			this.send(resp);
		}
	}

	/**
	 * split a packet to sub packet
	 * @param remote
	 * @param resp
	 * @param packetId
	 * @return
	 */
	private Bucket split(SocketHost remote, Packet resp, int packetId) {
		Command cmd = resp.getCommand();
		byte[] data = resp.getData();
		
		if (data == null || data.length == 0) {
			Packet packet = new Packet(remote, cmd);
			for(Message msg : resp.messages()) packet.addMessage(msg);
			packet.addMessage(Key.SUBPACKET_COUNT, 1);
			packet.addMessage(Key.SUBPACKET_SERIAL, 1);
			packet.addMessage(Key.PACKET_IDENTIFY, packetId);
			packet.addMessage(Key.SUBPACKET_TIMEOUT, 3000);
			return new Bucket(packet);
		}

		int blocks = data.length / Bucket.MAX_DATASIZE;
		if (data.length % Bucket.MAX_DATASIZE != 0) blocks++;

		Bucket bucket = new Bucket();
		for (int serial = 1, off = 0; serial <= blocks; serial++) {
			int size = (off + Bucket.MAX_DATASIZE < data.length ? Bucket.MAX_DATASIZE : data.length - off);
			byte[] b = new byte[size];
			System.arraycopy(data, off, b, 0, b.length);
			off += size;
			
			Packet packet = new Packet(remote, cmd);
			packet.addMessage(Key.SUBPACKET_COUNT, blocks);
			packet.addMessage(Key.SUBPACKET_SERIAL, serial);
			packet.addMessage(Key.PACKET_IDENTIFY, packetId);
			packet.addMessage(Key.SUBPACKET_TIMEOUT, 3000); // 3 second
			for(Message msg : resp.messages()) packet.addMessage(msg);
			packet.setData(b);
			
			bucket.add(packet);
		}
		return bucket;
	}
	
	private void sendRetryPacket(SocketHost remote, int packetId, int[] serials) {
		for (int i = 0; serials != null && i < serials.length; i++) {
			Command cmd = new Command(Request.NOTIFY, Request.RETRY_SUBPACKET);
			Packet packet = new Packet(remote, cmd);
			packet.addMessage(Key.PACKET_IDENTIFY, packetId);
			packet.addMessage(Key.SUBPACKET_SERIAL, serials[i]);
			this.send(packet);
		}
	}

	private void checkMissingBucket()  {
		if(requests.isEmpty()) return;
		for (SocketHost remote : requests.keySet()) {
			Bucket bucket = requests.get(remote);
			// if timeout
			if(bucket.isTimeout()) {
				int[] serials = bucket.missings();
				this.sendRetryPacket(remote, bucket.getPacketId(), serials);
				bucket.refreshTime();
			}
		}
	}

	private void checkInvalidBucket() {
		if(requests.isEmpty() && resps.isEmpty()) return;
		
		long maxtime = 20 * 60 * 1000;
		long now = System.currentTimeMillis();
		ArrayList<SocketHost> a = new ArrayList<SocketHost>();
		for (SocketHost remote : requests.keySet()) {
			Bucket bucket = requests.get(remote);
			if (now - bucket.startTime() >= maxtime) {
				a.add(remote);
			}
		}
		for (SocketHost remote : a) {
			requests.remove(remote);
		}
		a.clear();

		ArrayList<SocketHost> b = new ArrayList<SocketHost>(resps.keySet());
		for (SocketHost remote : b) {
			Bucket bucket = resps.get(remote);
			if (now - bucket.startTime() >= maxtime) {
				a.add(remote);
			}
		}
		for (SocketHost remote : a) {
			resps.remove(remote);
		}
	}

	/**
	 * @param request
	 * @return
	 */
	private Cipher initSecure(Packet request) {
		Cipher cipher = null;
		byte[] data = request.getData();
		if (data != null) {
			cipher = init_secure(data);
		}

		short code = (cipher != null ? Response.SECURE_ACCEPTED : Response.NOTACCEPTED);
		Command cmd = new Command(code);
		Packet resp = new Packet(request.getRemote(), cmd);
		resp.addMessage(Key.SPEAK, "init secure");
		
		Integer packetId = request.findInt(Key.PACKET_IDENTIFY);
		if(packetId != null) {
			resp.addMessage(Key.PACKET_IDENTIFY, packetId);
			resp.addMessage(Key.SUBPACKET_COUNT, 1);
			resp.addMessage(Key.SUBPACKET_SERIAL, 1);
			resp.addMessage(Key.SUBPACKET_TIMEOUT, 3000);
		}
		// send to client
		this.send(resp);

		return cipher;
	}
	
	private Cipher init_secure(byte[] raws) {
		if (security == null) {
			return null;
		} else if (security.getType() != Security.CIPHERTEXT_CHECK
				&& security.getType() != Security.DOUBLE_CHECK) {
			return null;
		} else if (security.getPrivateKey() == null) {
			return null;
		}

		byte[] data = SecureDecryptor.rsaDecrypt(security.getPrivateKey(), raws);
		if (data == null) return null;

		int off = 0;
		if (off + 4 > data.length) return null; 
		int len = Numeric.toInteger(data, off, 4);
		off += 4;
		
		if(off + len > data.length) return null;
		String algo = new String(data, off, len);
		off += len;
		
		if(off + 4 > data.length) return null;
		len = Numeric.toInteger(data, off, 4);
		off += 4;
		
		if(off + len > data.length) return null;
		byte[] pwd = new byte[len];
		System.arraycopy(data, off, pwd, 0, pwd.length);
		off += pwd.length;
		
		try {
			return new Cipher(algo, pwd);
		} catch (IllegalArgumentException exp) {

		}
		
		return null;
	}
	
	/**
	 * close service
	 */
	private void goodbye(Packet request) {
		Integer packetId = request.findInt(Key.PACKET_IDENTIFY);

		SocketHost remote = request.getRemote();
		Command cmd = new Command(Response.OKAY);
		Packet resp = new Packet(remote, cmd);
		resp.addMessage(Key.SPEAK, "see you next time");
		
		if (packetId != null) {
			resp.addMessage(Key.SUBPACKET_COUNT, 1);
			resp.addMessage(Key.SUBPACKET_SERIAL, 1);
			resp.addMessage(Key.PACKET_IDENTIFY, packetId);
			resp.addMessage(Key.SUBPACKET_TIMEOUT, 3000);
		}

		this.send(resp);
	}
	
	/**
	 * check request packet
	 */
	private void subprocess() {
		int size = array.size();
		for (int i = 0; i < size; i++) {
			if(array.isEmpty()) break;
			Packet request = array.remove(0);
			if(request == null) continue;
			
			// check packet
			Command cmd = request.getCommand();
			
			if (cmd.isInitSecure()) {
				Cipher cipher = this.initSecure(request);
				// add ciphertext
				if (cipher != null) {
					listener.addCipher(request.getRemote(), cipher.getAlgorithmText(), cipher.getPassword());
				}
				continue;
			} else if (cmd.isExit()) {
				this.goodbye(request);
				// remove ciphertext
				listener.removeCipher(request.getRemote());
				continue;
			}
			
			if (cmd.isRetrySubPacket()) {
				this.sendSubPacket(request);
			} else if (cmd.isCancelPacket()) {
				this.sendCancelPacket(request);
			} else if (request.isSubPacket()) {
				this.addSubPacket(request);
			} else {
				// none keep-packet, call...
				if(taskIndex >= tasks.length) taskIndex = 0;
				tasks[taskIndex++].add(request);
			}
		}
	}

	/**
	 * release a sub-thread
	 * @param task
	 * @return
	 */
	protected boolean release(PacketTask task) {
		for(int i = 0; i < tasks.length; i++) {
			if(tasks[i] == task) {
				tasks[i] = null;
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		array.clear();
		
		// release all, exit
		while (true) {
			int count = 0;
			for (int i = 0; i < tasks.length; i++) {
				if (tasks[i] == null) count++;
			}
			if (count == tasks.length) break;
			this.delay(500);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// start sub thread
		for (int i = 0; i < tasks.length; i++) {
			tasks[i] = new PacketTask(this);
			tasks[i].start();
			this.delay(200);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!isInterrupted()) {
			if (array.size() > 0) {
				try {
					this.subprocess();
				} catch (java.lang.Error exp) {
					exp.printStackTrace();
				} catch (java.lang.Throwable exp) {
					exp.printStackTrace();
				}
			} else {
				delay(1000);
			}
			this.checkMissingBucket();
			this.checkInvalidBucket();
		}
		
		// stop all sub-thread
		for(int i = 0; i < tasks.length; i++) {
			tasks[i].stop();
		}
	}

	/**
	 * add a packet
	 * @param packet
	 * @return
	 */
	public boolean add(Packet packet) {
		if (packet == null) return false;
		boolean empty = array.isEmpty();
		boolean success = array.add(packet);
		if (empty) wakeup();
		return success;
	}
}