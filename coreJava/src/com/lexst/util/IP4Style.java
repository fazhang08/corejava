package com.lexst.util;

import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
 * ip adress, version 4
 *
 * @author scott
 */
public final class IP4Style {

	/**
	 * inner address for the ip v4
	 */
	private final static String IPAS = "10.0.0.0";
	private final static String IPAE = "10.255.255.255";
	private final static String IPBS = "172.16.0.0";
	private final static String IPBE = "172.31.255.255";
	private final static String IPCS = "192.168.0.0";
	private final static String IPCE = "192.168.255.255";

	private static long IPABegin;
	private static long IPAEnd;
	private static long IPBBegin;
	private static long IPBEnd;
	private static long IPCBegin;
	private static long IPCEnd;

	private static long IPLOOPBACK;

	private static final String regex = "^\\s*(\\d{1,3}).(\\d{1,3}).(\\d{1,3}).(\\d{1,3})\\s*$";
	private static Pattern pattern;

	static {
		IP4Style.pattern = Pattern.compile(IP4Style.regex);

		IP4Style.IPABegin = IP4Style.transfer(IP4Style.IPAS);
		IP4Style.IPAEnd = IP4Style.transfer(IP4Style.IPAE);

		IP4Style.IPBBegin = IP4Style.transfer(IP4Style.IPBS);
		IP4Style.IPBEnd = IP4Style.transfer(IP4Style.IPBE);

		IP4Style.IPCBegin = IP4Style.transfer(IP4Style.IPCS);
		IP4Style.IPCEnd = IP4Style.transfer(IP4Style.IPCE);

		IP4Style.IPLOOPBACK = IP4Style.transfer("127.0.0.1");
	}

	/**
	 * @param ip4addr
	 * @return
	 */
	public static long transfer(String ip4addr) {
		Matcher matcher = IP4Style.pattern.matcher(ip4addr);
		if (!matcher.matches()) {
			return -1L;
		}
		if (matcher.groupCount() != 4) {
			return -1L;
		}

		long value = 0L;
		for (int shift = 24, i = 1; 0 <= shift; shift -= 8, i++) {
			long param = Long.parseLong(matcher.group(i));
			if (!(0 <= param && param <= 255)) {
				return -1L;
			}
			value = value | ((param & 0xFF) << shift);
		}
		return value;
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isPublicIP(String ip) {
		if(IP4Style.isLoopbackIP(ip)) return false;
		return !IP4Style.isPrivateIP(ip);
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isPrivateIP(String ip) {
		if(IP4Style.isLoopbackIP(ip)) return false;
		long value = IP4Style.transfer(ip);
		return (IP4Style.IPABegin <= value && value <= IP4Style.IPAEnd)
				|| (IP4Style.IPBBegin <= value && value <= IP4Style.IPBEnd)
				|| (IP4Style.IPCBegin <= value && value <= IP4Style.IPCEnd);
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isPrivateIP(int ip) {
		return (IP4Style.IPABegin <= ip && ip <= IP4Style.IPAEnd)
				|| (IP4Style.IPBBegin <= ip && ip <= IP4Style.IPBEnd)
				|| (IP4Style.IPCBegin <= ip && ip <= IP4Style.IPCEnd);
	}

	/**
	 * @param ip
	 * @return boolean
	 */
	public static boolean isLoopbackIP(String ip) {
		if ("localhost".equalsIgnoreCase(ip) || "127.0.0.1".equals(ip)) {
			return true;
		}
		long value = IP4Style.transfer(ip);
		return (value == IP4Style.IPLOOPBACK);
	}

	/**
	 * @param value
	 * @return
	 */
	public static boolean isLoopbackIP(int value){
		return (value == IP4Style.IPLOOPBACK);
	}

	/**
	 * @return
	 */
	public static String[] getAllPublicAddress() {
		ArrayList<String> array = new ArrayList<String>();
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			InetAddress[] hosts = InetAddress.getAllByName(localHost.getHostName());
			for (int i = 0; hosts != null && i < hosts.length; i++) {
				String localIP = hosts[i].getHostAddress();
				if(IP4Style.isPublicIP(localIP)) {
					array.add(localIP);
				}
			}
		} catch (UnknownHostException exp) {
			exp.printStackTrace();
		}
		if (array.isEmpty()) return null;
		String[] all = new String[array.size()];
		return array.toArray(all);
	}

	/**
	 * @return
	 */
	public static String getFirstPublicAddress() {
		String[] all = IP4Style.getAllPublicAddress();
		return (all == null ? null : all[0]);
	}

	/**
	 * get all local ip
	 * @return
	 */
	public static String[] getAllPrivateAddress() {
		ArrayList<String> a = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
			while (enums.hasMoreElements()) {
				NetworkInterface ni = enums.nextElement();
				Enumeration<java.net.InetAddress> hosts = ni.getInetAddresses();
				while (hosts.hasMoreElements()) {
					InetAddress local = hosts.nextElement();
					String localIP = local.getHostAddress();
					if (IP4Style.isPrivateIP(localIP)) {
						a.add(localIP);
					}
				}
			}
		} catch (SocketException exp) {

		}
		if(a.isEmpty()) return null;
		String[] all = new String[a.size()];
		return a.toArray(all);
	}

	/**
	 * @return
	 */
	public static String getFirstPrivateAddress() {
		String[] all = IP4Style.getAllPrivateAddress();
		if (all != null && all.length > 0) {
			return all[0];
		}
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException exp) {

		}
		return null;
	}

	/**
	 *
	 * @param ip
	 * @return
	 */
	public static boolean isIPv4(String ip) {
		if (ip == null) {
			return false;
		}
		Matcher matcher = IP4Style.pattern.matcher(ip);
		if (!matcher.matches()) {
			return false;
		}
		if (matcher.groupCount() != 4) {
			return false;
		}

		for (int i = 1; i < 5; i++) {
			long param = Long.parseLong(matcher.group(i));
			if (!(0 <= param && param <= 255)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * a class ip adress
	 * @param s
	 * @return
	 */
	public static boolean isA(String s) {
		long ip = IP4Style.transfer(s);
		if (ip == -1L) return false;
		long a = (ip >>> 24) & 0xFF;
		return (a >> 7) == 0L;
	}

	/**
	 * @param ip
	 * @return boolean
	 */
	public static boolean isA(int ip) {
		if (ip < 1) return false;
		int a = (ip >>> 24) & 0xFF;
		return (a >> 7) == 0L;
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isB(String s) {
		long ip = IP4Style.transfer(s);
		if (ip == -1L) return false;
		long a = (ip >>> 24) & 0xFF;
		return (a >>> 6) == 2L;
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isB(int ip) {
		int a = (ip >>> 24) & 0xFF;
		return (a >>> 6) == 2;
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isC(String s) {
		long ip = IP4Style.transfer(s);
		if (ip == -1L) return false;
		long a = (ip >>> 24) & 0xFF;
		return (a >>> 5) == 6L;
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isC(int ip) {
		int a = (ip >>> 24) & 0xFF;
		return (a >>> 5) == 6;
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isD(String s) {
		long ip = IP4Style.transfer(s);
		if (ip == -1L) return false;
		long a = (ip >>> 24) & 0xFF;
		return (a >>> 4) == 14L;
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isD(int ip) {
		int a = (ip >>> 24) & 0xFF;
		return (a >>> 4) == 14;
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isE(String s) {
		long ip = IP4Style.transfer(s);
		if (ip == -1L) return false;
		long a = (ip >>> 24) & 0xFF;
		return (a >>> 3) == 30L;
	}

	/**
	 * @param ip
	 * @return
	 */
	public static boolean isE(int ip) {
		int a = (ip >>> 24) & 0xFF;
		return (a >>> 3) == 30;
	}

	/**
	 * @param strIP
	 * @return
	 */
	public static boolean isGateway(String strIP) {
		long ip = IP4Style.transfer(strIP);
		if(IP4Style.isA(strIP)) {
			return (ip & 0xFFFFFFL) == 0xFFFFFFL;
		} else if(IP4Style.isB(strIP)) {
			return (ip & 0xFFFFL) == 0xFFFFL;
		} else if(IP4Style.isC(strIP)) {
			return (ip & 0xFFL) == 0xFFL;
		}

		return false;
	}

	/**
	 * @param strIP
	 * @return
	 */
	public static boolean isMulticast(String strIP) {
		long ip = IP4Style.transfer(strIP);
		if (IP4Style.isA(strIP)) {
			return (ip & 0xFFFFFFL) == 0L;
		} else if (IP4Style.isB(strIP)) {
			return (ip & 0xFFFFL) == 0L;
		} else if (IP4Style.isC(strIP)) {
			return (ip & 0xFFL) == 0L;
		}
		return false;
	}

	public static void main(String[] args) {
		IP4Style.getAllPrivateAddress();
	}

}