/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp udp server
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

import java.io.*;
import java.net.*;
import java.util.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.util.lock.*;

public class FixpPacketMonitor extends FixpMonitor implements IPacketListener {

	/* default fixp listen port */
	public final static int FIXP_PACKET_PORT = 5000;

	// print event log
	private boolean print;
	// local socket address
	private SocketHost local;
	private SingleLock lock = new SingleLock();
	private DatagramSocket server;
	// helper
	private FixpPacketHelper helper;

	/* socket address -> cipher text */
	private Map<SocketHost, Cipher> mapCipher = new TreeMap<SocketHost, Cipher>();

	/**
	 * default constrator
	 */
	public FixpPacketMonitor(int tasksize) {
		super();
		this.print = true;	// default print log
		local = new SocketHost(SocketHost.UDP);
		local.setPort(FixpPacketMonitor.FIXP_PACKET_PORT);
		helper = new FixpPacketHelper(tasksize); //5 subtask thread
		helper.setPacketListener(this);
	}

	/**
	 * 
	 */
	public FixpPacketMonitor() {
		this(1);
	}

	/**
	 * @param ip
	 * @param port
	 */
	public FixpPacketMonitor(String ip, int port) {
		this();
		this.setLocal(ip, port);
	}

	/**
	 * @param host
	 */
	public FixpPacketMonitor(SocketHost host) {
		this();
		this.setLocal(host);
	}

	/**
	 * set local address
	 * @param host
	 */
	public void setLocal(SocketHost host) {
		local.setIP(host.getIP());
		local.setPort(host.getPort());
	}

	public void setLocal(String ip, int port) {
		local.setIP(ip);
		local.setPort(port);
	}
	
	/**
	 * 
	 */
	public void setSecurity(Security security) {
		super.setSecurity(security);
		helper.setSecurity(security);
	}

	/**
	 * @return
	 */
	public SocketHost getLocal() {
		return this.local;
	}

	/**
	 * when debug status, print log
	 * @param b
	 */
	public void setPrint(boolean b) {
		this.print = b;
	}
	public boolean isPrint() {
		return this.print;
	}

	/**
	 * set RPC handle
	 * @param instance
	 */
	public void setRPCall(RPCInvoker instance) {
		helper.setRPCallImpl(instance);
	}

	public RPCInvoker getRPCall() {
		return helper.getRPCallImpl();
	}

	/**
	 * set packet call handle
	 * @param instance
	 */
	public void setPacketCall(PacketInvoker instance) {
		helper.setPacketCallImpl(instance);
	}

	public PacketInvoker getPacketCall() {
		return helper.getPacketCallImpl();
	}

	/*
	 * stop service
	 * @see com.lexst.thread.VirtualThread#stop()
	 */
	public void stop() {
		super.stop();
		this.close();
	}

	/*
	 * stop service
	 * @see com.lexst.thread.VirtualThread#stop(com.lexst.thread.Notifier)
	 */
	public void stop(Notifier notify) {
		super.stop(notify);
		this.close();
	}

	/**
	 * close socket
	 */
	private void close() {
		if (server == null) return;
		try {
			server.close();
		} catch (Throwable exp) {

		} finally {
			server = null;
		}
	}

	/**
	 * bind socket
	 * @return
	 */
	private boolean bind() {
		String localIP = local.getIP();
		if (!local.isValid() || localIP == null || IP4Style.isLoopbackIP(localIP)) {
			localIP = IP4Style.getFirstPrivateAddress();
			local.setIP(localIP);
		}
		int port = local.getPort();
		
		if(print) {
			com.lexst.log.client.Logger.info("FixpPacketMonitor.bind, bind to %s:%d", localIP, port);
		}

		InetSocketAddress address = new InetSocketAddress(localIP, port);
		try {
			server = new DatagramSocket(address);
			server.setSoTimeout(0); // no limit time
			// when port not match
			if (port != server.getLocalPort()) {
				local.setPort(server.getLocalPort());
			}
			return true;
		} catch (SocketException exp) {
			if(print) {
				com.lexst.log.client.Logger.error(exp);
			}
		} catch (Throwable exp) {
			if (print) {
				com.lexst.log.client.Logger.error(exp);
			}
		}
		return false;
	}

	/**
	 * re-bind to local address
	 * @return boolean
	 */
	private boolean rebind() {
		boolean success = false;
		while (!this.isInterrupted()) {
			this.close();
			this.delay(1000); // sleep 1 second, free native socket
			success = this.bind();
			if (success) break;
		}
		return success;
	}

	/**
	 * send datagram packet
	 * @param address
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	private boolean send(SocketHost remote, byte[] b, int off, int len) {
		SocketAddress address = new InetSocketAddress(remote.getIP(), remote.getPort());
		boolean success = false;
		lock.lock();
		try {
			DatagramPacket packet = new DatagramPacket(b, off, len, address);
			server.send(packet);
			success = true;
		} catch (IOException exp) {
			exp.printStackTrace();
			if(print) {
				com.lexst.log.client.Logger.error(exp);
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
			if(print) {
				com.lexst.log.client.Logger.fatal(exp);
			}
		} finally {
			lock.unlock();
		}
		return success;
	}

	/**
	 * create a error packet
	 * @param code
	 * @return
	 */
	protected Packet invalid(short code) {
		Command cmd = new Command(code);
		Packet packet = new Packet(cmd);
		packet.addMessage(Key.SPEAK, "sorry!");
		return packet;
	}
	
	/**
	 * send datagram packet 
	 * @param remote
	 * @param packet
	 * @return
	 */
	@Override
	public boolean send(SocketHost remote, Packet packet) {
		packet.replaceMessage(new Message(Key.BIND_IP, server.getLocalAddress().getHostAddress()));
		
		// encrypt data
		byte[] data = packet.getData();
		if (data != null && data.length > 0) {
			Cipher cipher = findCipher(remote);
			if (cipher != null) {
				data = cipher.encrypt(data);
				// when encrypt error
				if(data == null) {
					Packet resp = invalid(Response.ENCRYPT_FAILED);
					byte[] b = resp.build();
					this.send(remote, b, 0, b.length);
					return false;
				}
				packet.setData(data);
				
//				Packet sub = (Packet)packet.clone();
//				sub.setData(data);
//				packet = sub;
			}
		}
		
		// build data
		byte[] b = packet.build();
		return send(remote, b, 0, b.length);
	}

	@Override
	public boolean addCipher(SocketHost remote, String algo, byte[] pwd) {
		Cipher cipher = new Cipher(algo, pwd);
		boolean success = false;
		lock.lock();
		try {
			success = (mapCipher.put(remote, cipher) == null);
		} catch (Throwable exp) {

		} finally {
			lock.unlock();
		}
		return success;
	}

	@Override
	public boolean removeCipher(SocketHost remote) {
		boolean success = false;
		lock.lock();
		try {
			success = (mapCipher.remove(remote) != null);
		} catch (Throwable exp) {

		} finally {
			lock.unlock();
		}
		return success;
	}
	
	@Override
	public boolean existsCipher(SocketHost remote) {
		return findCipher(remote) != null;
	}
	
	private Cipher findCipher(SocketHost remote) {
		Cipher cipher = null;
		lock.lock();
		try {
			cipher = mapCipher.get(remote);
		} catch (Throwable exp) {

		} finally {
			lock.unlock();
		}
		return cipher;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// close socket
		this.close();
		// stop helper thread
		Notifier noti = new Notifier();
		helper.stop(noti);
		while (!noti.isKnown()) {
			this.delay(300);
		}
		if (print) {
			com.lexst.log.client.Logger.info("FixpPacketMonitor.finish, finished!");
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// bind local socket
		boolean success = bind();
		if (success) {
			// start helper thread
			success = helper.start();
			if (!success) {
				close();
			}
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		if (print) {
			com.lexst.log.client.Logger.info("FixpPacketMonitor.process, into...");
		}

		byte[] data = new byte[65507]; // max udp size
		DatagramPacket packet = new DatagramPacket(data, data.length);
		while (!isInterrupted()) {
			try {
				// receive packet
				server.receive(packet);
				// packet parameter
				String ip = packet.getAddress().getHostAddress();
				int port = packet.getPort();
				SocketHost remote = new SocketHost(SocketHost.UDP, ip, port);

				int len = packet.getLength();
				if(len == 0) {
					if(print) {
						com.lexst.log.client.Logger.info("FixpPacketMonitor.process, this is null packet");
					}
					continue;
				}
				byte[] b = packet.getData();
				int off = packet.getOffset();
				Packet request = new Packet(remote, b, off, len);
				
				// decrypt data
				byte[] raw = request.getData();
				if (raw != null && raw.length > 0) {
					Cipher cipher = findCipher(remote);
					if (cipher != null) {
						raw = cipher.decrypt(raw);
						if(raw == null) {
							throw new IOException("decrypt failed!");
						}
						request.setData(raw);
					}
				}
				
				// save packet
				helper.add(request);
				
			} catch (IOException exp) {
				if(isInterrupted()) break;
				// network error, rebind to local address
				if (print) {
					com.lexst.log.client.Logger.error(exp);
					com.lexst.log.client.Logger.error("FixpPacketMonitor.process, error packet from %s:%d",
						packet.getAddress().getHostAddress(), packet.getPort());
				} else {
					exp.printStackTrace();
				}
				this.rebind();
			} catch (Throwable exp) {
				if(isInterrupted()) break;
				// network error, rebind to local address
				if (print) {
					com.lexst.log.client.Logger.error(exp);
					com.lexst.log.client.Logger.error("FixpPacketMonitor.process, error packet from %s:%d ",
						packet.getAddress().getHostAddress(), packet.getPort());
				} else {
					exp.printStackTrace();
				}
			}
		}

		if(print) {
			com.lexst.log.client.Logger.info("FixpPacketMonitor.process, exit...");
		}
	}

}