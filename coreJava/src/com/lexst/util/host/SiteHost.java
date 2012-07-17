/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * site address (tcp and udp mode)
 * 
 * @author scott.jian lexst@126.com
 * 
 * @version 1.0 10/07/2009
 * 
 * @see com.lexst.util.host
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.host;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class SiteHost implements Serializable, Comparable<SiteHost> {
	
	private static final long serialVersionUID = -2938115244977013294L;
	
	private final static String SYNTAX = "^\\s*(?i)SITE://([0-9.]{1,}):([0-9]{1,})_([0-9]{1,})\\s*$";

	/* ip address(v4 style) */
	private int ip;

	/* tcp and udp port */
	private int udport, tcport;

	/**
	 *
	 */
	public SiteHost() {
		super();
		ip = -1;
		udport = tcport = 0;
	}

	/**
	 * @param host
	 */
	public SiteHost(SiteHost host) {
		super();
		this.ip = host.ip;
		this.tcport = host.tcport;
		this.udport = host.udport;
	}

	/**
	 * @param ip
	 * @param tcport
	 * @param udport
	 */
	public SiteHost(int ip, int tcport, int udport) {
		this();
		this.set(ip, tcport, udport);
	}

	/**
	 * @param ip
	 * @param tcport
	 * @param udport
	 */
	public SiteHost(String ip, int tcport, int udport) {
		this();
		this.set(ip, tcport, udport);
	}

	/**
	 * @param param
	 */
	public SiteHost(String param) {
		this();
		this.resolve(param);
	}
	
	/**
	 * set site host
	 * @param ip
	 * @param tcport
	 * @param udport
	 */
	public void set(String ip, int tcport, int udport) {
		this.setIP(ip);
		this.setTCPort(tcport);
		this.setUDPort(udport);
	}
	
	/**
	 * set site host
	 * @param ip
	 * @param tcport
	 * @param udport
	 */
	public void set(int ip, int tcport, int udport) {
		this.setIP(ip);
		this.setTCPort(tcport);
		this.setUDPort(udport);
	}

	/**
	 * copy SiteHost object
	 * @param host
	 */
	public void set(SiteHost host) {
		this.ip = host.ip;
		this.udport = host.udport;
		this.tcport = host.tcport;
	}

	/**
	 * set ip v4 address
	 * @param s
	 */
	public void setIP(String s) {
		if (!IP.isIPv4(s)) {
			try {
				InetAddress inet = java.net.InetAddress.getByName(s);
				s = inet.getHostAddress();
			} catch (java.net.UnknownHostException exp) {
				exp.printStackTrace();
			}
		}
		ip = IP.translate(s);
	}

	/**
	 * set site ipv4 address
	 * @param s
	 */
	public void setIP(int s) {
		this.ip = s;
	}
	public int getIPValue() {
		return this.ip;
	}
	public String getIP() {
		return IP.translate(ip);
	}

	/**
	 * set tcp port
	 * @param port
	 */
	public void setTCPort(int port) {
		if (port >= 0) this.tcport = port;
	}
	public int getTCPort() {
		return this.tcport;
	}

	/**
	 * set udp port
	 * @param port
	 */
	public void setUDPort(int port) {
		if (port >= 0) this.udport = port;
	}
	public int getUDPort() {
		return this.udport;
	}
	
	/**
	 * get socket host (tcp mode)
	 * @return
	 */
	public SocketHost getTCPHost() {
		return new SocketHost(SocketHost.TCP, ip, tcport);
	}
	
	/**
	 * get socket host (udp mode)
	 * @return
	 */
	public SocketHost getUDPHost() {
		return new SocketHost(SocketHost.UDP, ip, udport);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiteHost host) {
		if (host == this) return 0;
		if (host == null) return -1;

		if (ip == host.ip) {
			if (tcport == host.tcport) {
				if (udport == host.udport) {
					return 0;
				}
				return udport < host.udport ? -1 : 1;
			}
			return tcport < host.tcport ? -1 : 1;
		}
		return ip < host.ip ? -1 : 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if (arg == this) {
			return true;
		} else if (arg == null || arg.getClass() != SiteHost.class) {
			return false;
		}

		SiteHost s = (SiteHost) arg;
		return ip == s.ip && tcport == s.tcport && udport == s.udport;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ip ^ tcport ^ udport;
	}	

	/**
	 * @param param
	 */
	public void resolve(String param) {
		Pattern pattern = Pattern.compile(SiteHost.SYNTAX);
		Matcher matcher = pattern.matcher(param);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid value (" + param + ")");
		}

		String s1 = matcher.group(1);
		String s2 = matcher.group(2);
		String s3 = matcher.group(3);

		ip = IP.translate(s1);
		tcport = Integer.parseInt(s2);
		udport = Integer.parseInt(s3);
	}

	/*
	 * show site address
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("SITE://%s:%d_%d", IP.translate(ip), tcport, udport);
	}

	/*
	 * clone SiteHost object
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new SiteHost(this);
	}
}