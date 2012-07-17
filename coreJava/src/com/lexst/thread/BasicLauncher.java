/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com  All rights reserved
 * 
 * lexst launcher basic class
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/5/2009
 * 
 * @see com.lexst.thread
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.thread;

import java.io.*;
import java.net.*;

import org.w3c.dom.*;

import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.invoke.*;
import com.lexst.invoke.impl.*;
import com.lexst.log.client.*;
import com.lexst.shutdown.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.xml.*;

public abstract class BasicLauncher extends VirtualThread {
	
	/* user operate */
	public final static int NONE = 0;
	public final static int LOGIN = 1;
	public final static int RELOGIN = 2;
	public final static int LOGOUT = 3;

	private RPCInvokerImpl rpcImpl;
	protected PacketInvoker packetImpl;
	protected StreamInvoker streamImpl;

	// tcp listener
	protected FixpStreamMonitor fixpStream;
	// udp listener
	protected FixpPacketMonitor fixpPacket;
	// send refresh site
	protected boolean arouse;
	// max site sleep time
	protected long siteTimeout;
	// last respond time
	private long replyTime;
	
	// user operate type
	private int operate;
	
	/**
	 *
	 */
	protected BasicLauncher() {
		super();
		rpcImpl = new RPCInvokerImpl();
		fixpStream = new FixpStreamMonitor();
		fixpPacket = new FixpPacketMonitor(2);
		arouse = false;
		setSiteTimeout(20);
		replyTime = System.currentTimeMillis();
		this.setOperate(BasicLauncher.NONE);
	}
	
	public void setOperate(int value) {
		if (BasicLauncher.NONE <= value && value <= BasicLauncher.LOGOUT) {
			this.operate = value;
			if (super.isRunning()) super.wakeup();
		}
	}

	public int getOperate() {
		return this.operate;
	}
	
	public boolean isNoneOperate() {
		return this.operate == BasicLauncher.NONE;
	}

	public boolean isLoginOperate() {
		return this.operate == BasicLauncher.LOGIN;
	}

	public boolean isReloginOperate() {
		return this.operate == BasicLauncher.RELOGIN;
	}

	public boolean isLogoutOperate() {
		return this.operate == BasicLauncher.LOGOUT;
	}

	public SocketHost getStreamHost() {
		return fixpStream.getLocal();
	}

	public SocketHost getPacketHost() {
		return fixpPacket.getLocal();
	}

	public void setSiteTimeout(int second) {
		if (second >= 1) {
			siteTimeout = second * 1000;
		}
	}

	public int getSiteTimeout() {
		return (int) (siteTimeout / 1000);
	}

	/**
	 * fixp packet monitor send to here
	 */
	public void comeback() {
		this.arouse = true;
		this.wakeup();
	}

	/**
	 * @param cls
	 * @return
	 */
	protected boolean addInstance(Class<?> cls) {
		return rpcImpl.addInstance(cls);
	}

	/**
	 * start RPC listen service
	 * @param cls
	 * @param local
	 * @return
	 */
	protected boolean loadListen(Class<?>[] cls, SiteHost local) {
		String localIP = local.getIP();
		if (localIP == null || IP4Style.isLoopbackIP(localIP)) {
			localIP = IP4Style.getFirstPrivateAddress();
			local.setIP(localIP);
		}
		fixpStream.setLocal(localIP, local.getTCPort());
		fixpPacket.setLocal(localIP, local.getUDPort());

		boolean success = true;
		for (int i = 0; cls != null && i < cls.length; i++) {
			success = rpcImpl.addInstance(cls[i]);
			if (!success) return false;
		}

		if (success) {
			fixpStream.setRPCall(rpcImpl);
			fixpPacket.setRPCall(rpcImpl);
			fixpStream.setStreamCall(streamImpl);
			fixpPacket.setPacketCall(packetImpl);
		}
		// start stream listener
		if (success) {
			success = fixpStream.start();
		}
		// start packet listener
		if (success) {
			success = fixpPacket.start();
			if (!success) {
				fixpStream.stop();
			}
		}
		
		if (success) {
			while (!fixpStream.isRunning()) {
				this.delay(200);
			}
			while (!fixpPacket.isRunning()) {
				this.delay(200);
			}
		}
		
		return success;
	}

	/**
	 * stop fixp service
	 */
	protected void stopListen() {
		Notifier not1 = new Notifier();
		Notifier not2 = new Notifier();
		fixpStream.stop(not1);
		fixpPacket.stop(not2);

		while(!not1.isKnown()) {
			this.delay(200);
		}
		while(!not2.isKnown()) {
			this.delay(200);
		}
	}

	/**
	 * refresh reply time (active reply)
	 */
	public void refreshEndTime() {
		replyTime = System.currentTimeMillis();
	}
	
	protected boolean isMaxSiteTimeout() {
		return System.currentTimeMillis() - replyTime >= siteTimeout * 3;
	}

	protected boolean isSiteTimeout() {
		return System.currentTimeMillis() - replyTime >= siteTimeout;
	}

	/**
	 * send active packet to target host(top or home)
	 * @param num
	 * @param sitetype
	 * @param remote
	 */
	protected void hello(int num, int sitetype, SocketHost remote) {
		Command cmd = new Command(Request.NOTIFY, Request.HELO);
		Packet packet = new Packet(cmd);
		packet.addMessage(Key.BIND_IP, fixpPacket.getLocal().getIP());
		packet.addMessage(Key.SITE_TYPE, sitetype);
		packet.addMessage(Key.IP, fixpStream.getLocal().getIP());
		packet.addMessage(Key.TCPORT, fixpStream.getLocal().getPort());
		packet.addMessage(Key.UDPORT, fixpPacket.getLocal().getPort());
		// send a request packet to home site
		for (int i = 0; i < num; i++) {
			if (i > 0) this.delay(10);
			fixpPacket.send(remote, packet);
		}
	}
	
	/**
	 * ping to home site or top site
	 * @param sitetype
	 * @param remote
	 */
	protected void hello(int sitetype, SiteHost remote) {
		int number = 1;
		if (arouse) {
			number = 3;
			arouse = false;
		}
		for (int i = 0; i < number; i++) {
			hello(number, sitetype, remote.getUDPHost());
		}
	}

	/**
	 * parse home site
	 * @param doc
	 * @return
	 */
	protected SiteHost splitHome(Document doc) {
		XMLocal xml = new XMLocal();
		Element elem = (Element) doc.getElementsByTagName("home-site").item(0);
		String ip = xml.getValue(elem, "ip");
		if (IP4Style.isLoopbackIP(ip)) {
			ip = IP4Style.getFirstPrivateAddress();
		} else {
			try {
				ip = InetAddress.getByName(ip).getHostAddress();
			} catch (UnknownHostException exp) {
				Logger.error(exp);
				return null;
			}
		}
		if (!IP4Style.isIPv4(ip)) {
			return null;
		}
		String tcport = xml.getValue(elem, "tcp-port");
		String udport = xml.getValue(elem, "udp-port");
		return new SiteHost(ip, Integer.parseInt(tcport), Integer.parseInt(udport));
	}

	/**
	 * parse local site
	 * @param doc
	 * @return
	 */
	protected SiteHost splitLocal(Document doc) {
		XMLocal xml = new XMLocal();
		Element elem = (Element) doc.getElementsByTagName("local-site").item(0);
		String ip = xml.getValue(elem, "ip");
		if (IP4Style.isLoopbackIP(ip)) {
			ip = IP4Style.getFirstPrivateAddress();
		} else {
			try {
				ip = InetAddress.getByName(ip).getHostAddress();
			} catch (UnknownHostException exp) {
				Logger.error(exp);
				return null;
			}
		}
		if (!IP4Style.isIPv4(ip)) {
			return null;
		}
		String tcport = xml.getValue(elem, "tcp-port");
		String udport = xml.getValue(elem, "udp-port");
		return new SiteHost(ip, Integer.parseInt(tcport), Integer.parseInt(udport));
	}

	/**
	 * @param document
	 * @return
	 */
	protected boolean loadShutdown(Document document) {
		XMLocal xml = new XMLocal();
		Element elem = (Element) document.getElementsByTagName("accept-shutdown-address").item(0);
		String[] all = xml.getXMLValues(elem.getElementsByTagName("ip"));
		if (all == null) return false;
		for (String ip : all) {
			if (!IP4Style.isIPv4(ip)) return false;
			ShutdownTable.getInstance().add(ip);
		}
		return true;
	}
	
	protected boolean loadSecurity(Document document) {
		XMLocal xml = new XMLocal();
		// resovle security configure file
		String path = xml.getXMLValue(document.getElementsByTagName("security-file"));
				
		if (path != null && path.length() > 0) {
			Security safe = new Security();
			if (!safe.parse(path)) {
				return false;
			}
			fixpPacket.setSecurity(safe);
			fixpStream.setSecurity(safe);
		}
		
		return true;
	}
	
	protected byte[] readFile(File file) {
		if (!file.exists()) return null;
		byte[] data = new byte[(int) file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(data);
			in.close();
			return data;
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	protected boolean flushFile(File file, byte[] b) {
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(b);
			out.close();
			return true;
		} catch (IOException exp) {

		}
		return false;
	}

}