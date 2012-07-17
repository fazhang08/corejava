/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 lexst.com  All rights reserved
 * 
 * basic launcher of manager site (top site and home site)
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 10/23/2011
 * 
 * @see com.lexst.thread
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.thread;

import java.util.*;

import org.w3c.dom.*;

import com.lexst.log.client.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.xml.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * <friend-sites>
 * 	<copy-time> 30 </copy-time> <!-- second -->
 *  <active-time> 5 </active-time>  
 *  <detect-time> 180 </detect-time>
 *  
 * 	<friend-site>
 *		<ip> 192.168.0.122 </ip>
 *		<tcp-port> 8866 </tcp-port>
 *		<udp-port> 8866 </udp-port>
 * 	</friend-site>
 * 
 * 	<friend-site>
 *		<ip> 192.168.0.123 </ip>
 *		<tcp-port> 6688 </tcp-port>
 *		<udp-port> 6688 </udp-port>
 * 	</friend-site>
 *  
 * </friend-sites>
 */

public abstract class HubLauncher extends BasicLauncher {

	/* run site, only home site and top site*/
	protected boolean runflag;
	
	/* time parameters */
	protected int copyInterval;
	protected int activeInterval;
	protected int detectInterval;

	/* relation site address */
	protected List<SiteHost> friends = new ArrayList<SiteHost>();
	
	/**
	 * default constructor
	 */
	protected HubLauncher() {
		super();
		this.runflag = false;
		this.setCopyInterval(30);
		this.setActiveInterval(5);
		this.setDetectInterval(5 * 60);
	}
	
	protected void setCopyInterval(int second) {
		if (second < 1) {
			throw new IllegalArgumentException("invalid copy time:" + second);
		}
		this.copyInterval = second;
	}
	
	public int getCopyInterval() {
		return this.copyInterval;
	}
	
	protected void setActiveInterval(int second) {
		if (second < 1) {
			throw new IllegalArgumentException("invalid active time:" + second);
		}
		this.activeInterval = second;
	}
	
	public int getActiveInterval() {
		return this.activeInterval;
	}
	
	protected void setDetectInterval(int second) {
		if (second < 1) {
			throw new IllegalArgumentException("invalid detect time:" + second);
		}
		this.detectInterval = second;
	}
	public int getDetectInterval() {
		return this.detectInterval;
	}

	public boolean isRunsite() {
		return this.runflag;
	}

	/**
	 * 选择一台主机,必须是哈希码最大的
	 * @param host
	 * @return
	 */
	public SiteHost voting(SiteHost[] hosts) {
		if (this.runflag) {
			return null;
		}

		Map<Integer, SiteHost> map = new HashMap<Integer, SiteHost>(16);
		for (SiteHost host : hosts) {
			int hash = host.hashCode();
			map.put(hash, host);
		}

		int value = 0;
		for (int hash : map.keySet()) {
			if (value == 0 || value < hash) value = hash;
		}

		SiteHost host = map.get(value);
		return host;
	}

	/**
	 * resolve configure
	 * @param doc
	 * @return
	 */
	protected boolean loadFriends(Document doc) {
		NodeList list = doc.getElementsByTagName("friend-sites");
		if (list == null || list.getLength() == 0) return true;
		if (list.getLength() != 1) return false;

		Element elem = (Element) list.item(0);

		XMLocal xml = new XMLocal();
		String value = xml.getValue(elem, "copy-time");
		try {
			this.setCopyInterval(Integer.parseInt(value));
		} catch (NumberFormatException exp) {
			Logger.error(exp);
			return false;
		}
		
		value = xml.getValue(elem, "active-time");
		try {
			this.setActiveInterval(Integer.parseInt(value));
		} catch (NumberFormatException exp) {
			Logger.error(exp);
			return false;
		}
		
		value = xml.getValue(elem, "detect-time");
		try {
			this.setDetectInterval(Integer.parseInt(value));
		} catch (NumberFormatException exp) {
			Logger.error(exp);
			return false;
		}

		// all address
		list = elem.getElementsByTagName("friend-site");
		if (list == null || list.getLength() == 0) return false;

		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			elem = (Element) list.item(i);
			String ip = xml.getValue(elem, "ip");

			if (IP4Style.isLoopbackIP(ip)) {
				ip = IP4Style.getFirstPrivateAddress();
			} else {
				try {
					ip = InetAddress.getByName(ip).getHostAddress();
				} catch (UnknownHostException exp) {
					Logger.error(exp);
					return false;
				}
			}

			if (!IP4Style.isIPv4(ip)) {
				Logger.error("invalid ip address %s", ip);
				return false;
			}

			String tcport = xml.getValue(elem, "tcp-port");
			String udport = xml.getValue(elem, "udp-port");
			try {
				SiteHost host = new SiteHost(ip, Integer.parseInt(tcport), Integer.parseInt(udport));
				// save backup site
				friends.add(host);
			} catch (NumberFormatException exp) {
				Logger.error(exp);
				return false;
			}
		}
		return true;
	}

}