/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp udp client
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/16/2009
 * 
 * @see com.lexst.fixp.client
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp.client;

import java.io.*;
import java.net.*;

import com.lexst.fixp.*;
import com.lexst.util.*;
import com.lexst.util.host.*;

public class FixpPacketClient extends FixpClient {
	
	private final static int UDP_MAXSIZE = 65507;

	/* udp socket */
	private DatagramSocket socket;
		
	private int packet_identity;
	
	private int subpacket_timeout;

	/**
	 *
	 */
	public FixpPacketClient() {
		super();
		this.packet_identity = 1;
		this.setSubPacketTimeout(3000); // 3 second
	}

	/**
	 * @return
	 */
	private int nextPacketIdentify() {
		return this.packet_identity++;
	}
	
	/**
	 * subpacket timeout
	 * @param millisecond
	 */
	public void setSubPacketTimeout(int millisecond) {
		if (millisecond >= 10) {
			this.subpacket_timeout = millisecond;
		}
	}

	public int getSubPacketTimeout() {
		return this.subpacket_timeout;
	}
	
	/**
	 * check bound
	 * @return
	 */
	public boolean isBound() {
		return socket != null && socket.isBound();
	}

	/**
	 * check connect
	 * @return boolean
	 */
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	/**
	 * check closed
	 * @return
	 */
	public boolean isClosed() {
		return socket == null || socket.isClosed();
	}
	
	/**
	 * get local address
	 * @return
	 */
	public SocketHost getLocal() {
		if (socket == null) {
			return null;
		}
		return new SocketHost(SocketHost.UDP, socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
	}
	
	/**
	 * @return
	 */
	public InetAddress getLocalAddress() {
		if (socket == null) {
			return null;
		}
		return socket.getLocalAddress();
	}

	/**
	 * @return
	 */
	public InetAddress getRemoteAddress() {
		if (socket == null) {
			return null;
		}
		return socket.getInetAddress();
	}

	/**
	 * close socket
	 */
	public void close() {
		if (socket == null) return;
		try {
			socket.close();
		} catch (Throwable exp) {

		} finally {
			socket = null;
		}
	}

	/**
	 * create a datagram socket and bind to local address
	 * @param local
	 * @return
	 * @throws IOException
	 */
	public boolean bind(SocketAddress local) throws IOException {
		// if null, bind a local ip and 0 port
		if (local == null) {
			String ip = (bindIP == null ? IP4Style.getFirstPrivateAddress() : bindIP);
			local = new InetSocketAddress(ip, 0);
		}
		// bind local address
		socket = new DatagramSocket(local);
		socket.setSoTimeout(receive_timeout > 0 ? receive_timeout * 1000 : 0);
		return true;
	}

	/**
	 * @throws IOException
	 */
	public boolean bind() throws IOException {
		return bind(null);
	}

	/**
	 * connect to remote server
	 * @param address
	 * @throws IOException
	 */
	private void connect(SocketAddress address) throws IOException {
		// when connected, close it
		if (isConnected()) {
			this.close();
		}

		if (isClosed() || !isBound()) {
			if (!this.bind()) {
				throw new BindException("bind failed!");
			}
		}
		if (receive_buffsize > 0) {
			socket.setReceiveBufferSize(receive_buffsize);
		}
		if (send_buffsize > 0) {
			socket.setSendBufferSize(send_buffsize);
		}
		// receive timeout
		socket.setSoTimeout(receive_timeout > 0 ? receive_timeout * 1000 : 0);
		// connect to server
		socket.connect(address);
	}

	/**
	 * connect server
	 *
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public void connect(String ip, int port) throws IOException {
		if (ip == null || port < 1) {
			throw new IllegalArgumentException("host is null, or port<1");
		}
		InetSocketAddress address = new InetSocketAddress(ip, port);
		this.connect(address);
		this.setRemote(new SocketHost(SocketHost.UDP, ip, port));
	}

	/**
	 * @param host
	 * @throws IOException
	 */
	public void connect(SocketHost host) throws IOException {
		this.connect(host.getIP(), host.getPort());
	}

	/**
	 * receive packet
	 * @return
	 * @throws IOException
	 */
	public Packet receive() throws IOException {
		// receive data from server
		byte[] b = new byte[FixpPacketClient.UDP_MAXSIZE];
		DatagramPacket dp = new DatagramPacket(b, 0, b.length);
		socket.receive(dp);
		
		SocketHost remote = new SocketHost(SocketHost.UDP, dp.getAddress().getHostAddress(), dp.getPort());
		// resolve data and return
		byte[] data = dp.getData();
		Packet packet = new Packet(remote, data, dp.getOffset(), dp.getLength());
		
		data = packet.getData();
		if (cipher != null && data != null && data.length > 0) {
			data = cipher.decrypt(data);
			if(data == null) {
				throw new IOException("decrypt failed!");
			}
			packet.setData(data);
		}
		
		return packet;
	}

	/**
	 * @param packet
	 * @throws IOException
	 */
	public void send(Packet packet) throws IOException {
		if (isClosed() || !isBound()) {
			if(!this.bind()) {
				throw new IOException("bind failed");
			}
		}
		
		byte[] data = packet.getData();
		if (cipher != null && data != null && data.length > 0) {
			data = cipher.encrypt(data);
			if(data == null) {
				throw new IOException("encrypt failed!");
			}
			packet.setData(data);
		}
		
		packet.replaceMessage(new Message(Key.BIND_IP, socket.getLocalAddress().getHostAddress()));
		SocketAddress remote = packet.getRemote().getAddress();
		// build bytes and send data
		byte[] b = packet.build();
		DatagramPacket dp = new DatagramPacket(b, 0, b.length, remote);
		socket.send(dp);
	}

	/**
	 * set timeout
	 * @param millisecond
	 * @throws SocketException
	 */
	private void setSoTimeout(int millisecond) throws SocketException {
		socket.setSoTimeout(millisecond);
	}

	/**
	 * send a packet, return datagram and resolve
	 * @param packet
	 * @return
	 */
	public Packet execute(Packet packet) throws IOException {
		// check socket status
		this.check(packet.getRemote());
		// socket receive timeout
		this.setSoTimeout(receive_timeout > 0 ? receive_timeout * 1000 : 0);
		// send packet
		this.send(packet);
		// receive packet
		return this.receive();
	}

	/* below is keep packet method */

	/**
	 * split a big packet 
	 * @param request
	 * @param packetId
	 * @return
	 */
	private Packet[] split(Packet request, int packetId) {
		SocketHost remote = request.getRemote();
		Command cmd = request.getCommand();
		byte[] data = request.getData();
		
		if (data == null || data.length == 0) {
			// this is empty data packet
			Packet packet = new Packet(remote, cmd);
			packet.addMessage(Key.SUBPACKET_COUNT, 1);
			packet.addMessage(Key.SUBPACKET_SERIAL, 1);
			packet.addMessage(Key.PACKET_IDENTIFY, packetId);
			packet.addMessage(Key.SUBPACKET_TIMEOUT, subpacket_timeout);
			for(Message msg : request.messages()) {
				packet.addMessage(msg);
			}
			return new Packet[] { packet };
		}

		int blocks = data.length / Bucket.MAX_DATASIZE;
		if (data.length % Bucket.MAX_DATASIZE != 0) blocks++;

		Packet[] packets = new Packet[blocks];
		for(int index = 0, off = 0; index < blocks; index++) {
			int size = (off + Bucket.MAX_DATASIZE < data.length ? Bucket.MAX_DATASIZE : data.length - off);
			byte[] b = new byte[size];
			System.arraycopy(data, off, b, 0, b.length);
			off += size;
			
			packets[index] = new Packet(remote, cmd);
			packets[index].addMessage(Key.SUBPACKET_COUNT, blocks);
			packets[index].addMessage(Key.SUBPACKET_SERIAL, index + 1);
			packets[index].addMessage(Key.PACKET_IDENTIFY, packetId);
			packets[index].addMessage(Key.SUBPACKET_TIMEOUT, subpacket_timeout);
			for(Message msg : request.messages()) {
				packets[index].addMessage(msg);
			}
			packets[index].setData(b);
		}
		return packets;
	}
	
	/**
	 * @param packets
	 * @throws IOException
	 */
	private void sendPackets(Packet[] packets) throws IOException {
		for(int i = 0; i < packets.length; i++) {
			this.send(packets[i]);
		}
	}
	
	/**
	 * @param remote
	 * @param packetId
	 * @param serials
	 * @throws IOException
	 */
	private void sendRetryPacket(SocketHost remote, int packetId, int[] serials) throws IOException {
		for (int i = 0; serials != null && i < serials.length; i++) {
			Command cmd = new Command(Request.NOTIFY, Request.RETRY_SUBPACKET);
			Packet packet = new Packet(remote, cmd);
			packet.addMessage(Key.PACKET_IDENTIFY, packetId);
			packet.addMessage(Key.SUBPACKET_SERIAL, serials[i]);
			this.send(packet);
		}
	}
	
	/**
	 * release keep packet
	 * @param remote
	 * @param packetId
	 * @throws IOException
	 */
	private void cancel(SocketHost remote, int packetId) throws IOException {
		Command cmd = new Command(Request.NOTIFY, Request.CANCEL_PACKET);
		Packet request = new Packet(remote, cmd);
		request.addMessage(Key.PACKET_IDENTIFY, packetId);
		
		setSoTimeout(5000);
		
		// cancel status
		while (true) {
			this.send(request);
			try {
				Packet resp = this.receive();
				// if "okay" packet, next; if "not-found" exit!
				cmd = resp.getCommand();
				if (cmd.getResponse() == Response.OKAY) break;
				if (cmd.getResponse() == Response.NOTFOUND) return;
			} catch (SocketTimeoutException exp) {

			}
		}
		
		// close status
		while(true) {
			this.send(request);
			try {
				Packet resp = this.receive();
				//if "not-found" packet, exit
				cmd = resp.getCommand();
				if (cmd.getResponse() == Response.NOTFOUND) break;
			} catch(SocketTimeoutException exp) {
				
			}
		}
		
		setSoTimeout(receive_timeout > 0 ? receive_timeout * 1000 : 0);
	}
	
	/**
	 * @param remote
	 * @throws IOException
	 */
	private void check(SocketHost remote) throws IOException {
		if (isConnected()) {
			if (remote.isValid() && !remote.equals(super.getRemote())) {
				this.close();
				if (!this.bind()) {
					throw new BindException("bind failed");
				}
				this.connect(remote);
			}
		} else if (isClosed() || !isBound()) {
			this.close();
			if (!this.bind()) {
				throw new BindException("bind failed");
			}
		}
	}

	/**
	 * send keep packet and receive new packet
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public Packet batch(Packet request) throws IOException {
		int packetId = this.nextPacketIdentify();
		Packet[] packets = this.split(request, packetId);
		SocketHost remote = request.getRemote();
		// check bind and connect
		this.check(remote);
		// set receive timeout
		setSoTimeout(subpacket_timeout);
		// send all packet to server
		this.sendPackets(packets);

		int timeout = 0;
		boolean not_reply = true;
		Bucket bucket = new Bucket();
		while (true) {
			// next send all packet
			if (not_reply && timeout > 0 && timeout % 5000 == 0) {
				this.sendPackets(packets);
			}
			// receive reply packet
			Packet resp = null;
			try {
				resp = this.receive();
				not_reply = false;
				timeout = 0;
			} catch (SocketTimeoutException exp) {
				if (receive_timeout > 0) {
					timeout += subpacket_timeout;
					if (timeout >= receive_timeout * 1000) {
						throw new SocketTimeoutException("receive packet timeout!");
					}
				}
			}

			if (resp == null) {
				if (!bucket.isEmpty()) {
					// check missing packet, and post to server
					int[] serials = bucket.missings();
					this.sendRetryPacket(remote, packetId, serials);
				}
				continue;
			}
			
			// check receive packet
			Command cmd = resp.getCommand();
			if (cmd.isRetrySubPacket()) {
				int serial = resp.findInt(Key.SUBPACKET_SERIAL);
				if (serial > 0 && serial <= packets.length) {
					this.send(packets[serial - 1]);
				}
			} else {
				// save a sub packet
				bucket.add(resp);
				if (bucket.isFull()) break;
			}
		}
		// release keep packet
		this.cancel(remote, packetId);
		// reset timeout
		setSoTimeout(receive_timeout > 0 ? receive_timeout * 1000 : 0);
		
		return bucket.compose();
	}
	
	/**
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public Packet single(Packet request) throws IOException {
		int packetId = this.nextPacketIdentify();
		request.addMessage(Key.PACKET_IDENTIFY, packetId);
		request.addMessage(Key.SUBPACKET_COUNT, 1);
		request.addMessage(Key.SUBPACKET_SERIAL, 1);
		request.addMessage(Key.SUBPACKET_TIMEOUT, subpacket_timeout);
		
		SocketHost remote = request.getRemote();
		// check bind and connect
		this.check(remote);
		// set receive timeout
		setSoTimeout(subpacket_timeout);
		// send packet to server
		this.send(request);
		
		int timeout = 0;
		Packet resp = null;
		while (true) {
			// next send
			if (timeout > 0 && timeout % 5000 == 0) {
				this.send(request);
			}
			// receive reply packet
			try {
				resp = this.receive();
				break;
			} catch (SocketTimeoutException exp) {
				if (receive_timeout > 0) {
					timeout += subpacket_timeout;
					if (timeout >= receive_timeout * 1000) {
						throw new SocketTimeoutException("receive packet timeout!");
					}
				}
			}
		}

		// reset timeout
		setSoTimeout(receive_timeout > 0 ? receive_timeout * 1000 : 0);

		return resp;
	}
}