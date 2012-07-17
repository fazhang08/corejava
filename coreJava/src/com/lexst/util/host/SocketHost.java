/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * socket address (ip address, tcp port, udp port)
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

public final class SocketHost implements Serializable, Comparable<SocketHost> {
	private static final long serialVersionUID = 9114550696356884581L;

	private final static String SYNTAX = "^\\s*(?i)(TCP|UDP)://([0-9.]{1,}):([0-9]{1,})\\s*$";

	/* socket connect type */
	public final static byte NONE = 0;
	public final static byte UDP = 1;
	public final static byte TCP = 2;

	/* current socket type */
	private byte type;

	/* socket ip and port */
	private int ip, port;

	/**
	 * construct method
	 */
	protected SocketHost() {
		super();
		type = SocketHost.NONE;
		ip = -1;
		port = 0;
	}

	/**
	 * construct method
	 * @param type
	 */
	public SocketHost(byte type) {
		this();
		this.setType(type);
	}

	/**
	 * construct mehtod
	 * @param type
	 * @param addr
	 * @param port
	 */
	public SocketHost(byte type, String ip, int port) {
		this();
		this.set(type, ip, port);
	}

	/**
	 * @param type
	 * @param ip
	 * @param port
	 */
	public SocketHost(byte type, int ip, int port) {
		this();
		this.set(type, ip, port);
	}
	
	/**
	 * construct method
	 * @param ip
	 * @param port
	 */
	public SocketHost(String ip, int port) {
		this(SocketHost.NONE, ip, port);
	}

	/**
	 * construct method
	 * @param host
	 */
	public SocketHost(SocketHost host) {
		this();
		this.set(host);
	}

	/**
	 * @param param
	 */
	public SocketHost(String param) {
		this();
		this.resolve(param);
	}
	
	/**
	 * set socket host
	 * @param type
	 * @param ip
	 * @param port
	 */
	public void set(byte type, String ip, int port) {
		this.setType(type);
		this.setIP(ip);
		this.setPort(port);
	}
	
	/**
	 * set socket host
	 * @param type
	 * @param ip
	 * @param port
	 */
	public void set(byte type, int ip, int port) {
		this.setType(type);
		this.setIP(ip);
		this.setPort(port);
	}

	public boolean isValid() {
		return ip != -1 && port > 0;
	}
	
	public boolean isLegalType(int type) {
		return (SocketHost.NONE <= type && type <= SocketHost.TCP);
	}

	/**
	 * host type: udp or tcp
	 * @param type
	 */
	public void setType(byte type) {
		if(!isLegalType(type)) {
			throw new IllegalArgumentException("invalid host type!");
		}
		this.type = type;
	}

	public byte getType() {
		return this.type;
	}

	/**
	 * set socket host address(ip or dns name)
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
	
	public void setIP(int val) {
		this.ip = val;
	}

	/**
	 * @return String
	 */
	public String getIP() {
		return IP.translate(ip);
	}
	
	/**
	 * get ip object
	 * @return
	 */
	public IP getSocketIP() {
		return new IP(ip);
	}
	
	/**
	 * get int value
	 * @return
	 */
	public int getIPv4() {
		return ip;
	}

	/**
	 * set socket host port
	 * @param i
	 */
	public void setPort(int i) {
		if (i < 0 || i >= 0xFFFF) {
			throw new IllegalArgumentException("invalid host port, must >0 && <0xFFFF");
		}
		this.port = i;
	}

	/**
	 * return socket host port
	 * @return int
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * @param host
	 */
	public void set(SocketHost host) {
		this.set(host.type, host.ip, host.port);
	}
	
	/**
	 * @param param
	 */
	public void resolve(String param) {
		Pattern pattern = Pattern.compile(SocketHost.SYNTAX);
		Matcher matcher = pattern.matcher(param);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid value (" + param + ")");
		}

		String s1 = matcher.group(1);
		String s2 = matcher.group(2);
		String s3 = matcher.group(3);

		if ("TCP".equalsIgnoreCase(s1)) {
			type = SocketHost.TCP;
		} else if ("UDP".equalsIgnoreCase(s1)) {
			type = SocketHost.UDP;
		}

		ip = IP.translate(s2);
		port = Integer.parseInt(s3);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "none";
		switch (type) {
		case SocketHost.TCP:
			s = "TCP"; break;
		case SocketHost.UDP:
			s = "UDP"; break;
		}
		return String.format("%s://%s:%d", s, IP.translate(ip), port);
	}

	/**
	 *
	 * @return
	 */
	public InetSocketAddress getAddress() {
		return new InetSocketAddress(IP.translate(ip), this.port);
	}

	/**
	 * asc sort
	 * @param host
	 * @return
	 */
	public int compareTo(SocketHost host) {
		if (host == null) return -1;
		else if (host == this) return 0;
		if (type == host.type) {
			if(ip == host.ip) {
				if (port == host.port) {
					return 0;
				}
				return port < host.port ? -1 : 1;
			}
			return ip < host.ip ? -1 : 1;
		}
		return type < host.type ? -1 : 1;
	}
	
	/*
	 * compare match
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if (arg == this) {
			return true;
		} else if (arg == null || arg.getClass() != SocketHost.class) {
			return false;
		}

		SocketHost s = (SocketHost) arg;
		return type == s.type && ip == s.ip && port == s.port;
	}

	/*
	 * compute hash value
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return type ^ ip ^ port;
	}

	/*
	 * new object
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return new SocketHost(this);
	}


}