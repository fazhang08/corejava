/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * socket ip address (int value)
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

public class IP implements Serializable, Comparable<IP> {
	private static final long serialVersionUID = 1L;

	private final static String regex = "^(\\d{1,3}).(\\d{1,3}).(\\d{1,3}).(\\d{1,3})$";
	private final static Pattern pattern = Pattern.compile(regex);

	private int value;

	/**
	 *
	 */
	public IP() {
		super();
		value = 0;
	}

	/**
	 * @param ip
	 */
	public IP(int ip) {
		this();
		this.setValue(ip);
	}
	
	/**
	 * address
	 * @param address
	 */
	public IP(String address) throws UnknownHostException {
		this();
		if (!IP.isIPv4(address)) {
			InetAddress host = InetAddress.getByName(address);
			address = host.getHostAddress();
			if (!IP.isIPv4(address)) {
				throw new UnknownHostException("invalid host " + address);
			}
		}
		value = IP.translate(address);
	}

	public void setValue(int ip) {
		this.value = ip;
	}

	public int getValue() {
		return this.value;
	}

	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != IP.class) {
			return false;
		}
		IP ip = (IP) arg;
		return value == ip.value;
	}

	public int hashCode() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IP ip) {
		if (value == ip.value) {
			return 0;
		}
		return value < ip.value ? -1 : 1;
	}

	public String toString() {
		int[] s = new int[4];
		for (int off = 24, i = 0; off >= 0; off -= 8) {
			s[i++] = (value >>> off) & 0xff;
		}
		return String.format("%d.%d.%d.%d", s[0], s[1], s[2], s[3]);
	}
	
	
	/**
	 * @param ip
	 * @return
	 */
	public static boolean isIPv4(String ip) {
		if (ip == null) return false;
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	/**
	 * ipv4 translate to int
	 * @param value
	 * @return
	 */
	public static int translate(String value) {
		Matcher matcher = pattern.matcher(value);
		if(!matcher.matches()) {
			throw new IllegalArgumentException("invalid ip:" + value);
		}
		int size = matcher.groupCount();
		int ip = 0;
		for (int i = 1, off = 24; i <= size; i++, off -= 8) {
			String s = matcher.group(i);
			int num = Integer.parseInt(s);
			ip |= (num << off);
		}
		return ip;
	}

	/**
	 * int to ipv4
	 * @param ip
	 * @return
	 */
	public static String translate(int ip) {
		StringBuilder buf = new StringBuilder();
		for (int off = 24; off >= 0; off -= 8) {
			if (buf.length() > 0) buf.append('.');
			int value = (ip >>> off) & 0xff;
			buf.append(String.valueOf(value));
		}
		return buf.toString();
	}

}