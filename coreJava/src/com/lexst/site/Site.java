/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com  All rights reserved
 * 
 * lexst basic class
 * 
 * @author lei.zhang lexst@126.com
 * 
 * @version 1.0 3/2/2009
 * 
 * @see com.lexst.site
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.site;

import java.io.*;

import com.lexst.util.host.*;

public class Site implements Serializable {
	private static final long serialVersionUID = 2620726187813830228L;

	/* lexst node set */
	public final static int NONE = 0;
	public final static int TOP_SITE = 1;
	public final static int HOME_SITE = 2;
	public final static int LIVE_SITE = 3;
	public final static int LOG_SITE = 4;
	public final static int DATA_SITE = 5;
	public final static int CALL_SITE = 6;
	public final static int WORK_SITE = 7;
	public final static int BUILD_SITE = 8;

	/* site type */
	private int type;
	/* site local address */
	private SiteHost host;

	/**
	 * default construct
	 */
	public Site() {
		super();
		this.type = Site.NONE;
	}

	/**
	 * @param type
	 */
	public Site(int type) {
		this();
		this.setType(type);
	}

	/**
	 * @param ip
	 * @param tcport
	 * @param udport
	 */
	public Site(int type, int ip, int tcport, int udport) {
		this(type);
		host = new SiteHost(ip, tcport, udport);
	}

	/**
	 * @param strIP
	 * @param tcport
	 * @param udport
	 */
	public Site(int type, String strIP, int tcport, int udport) {
		this(type);
		host = new SiteHost(strIP, tcport, udport);
	}
	
	/**
	 * @param site
	 */
	public Site(Site site) {
		this.type = site.type;
		this.host = new SiteHost(site.host);
	}

	public void setType(int id) {
		if (Site.TOP_SITE <= id && id <= Site.BUILD_SITE) {
			this.type = id;
			return;
		}
		throw new IllegalArgumentException("invalid site type");
	}
	
	public static String translate(int type) {
		switch(type) {
		case Site.TOP_SITE:
			return "TOP";
		case Site.HOME_SITE:
			return "HOME";
		case Site.LIVE_SITE:
			return "LIVE";
		case Site.LOG_SITE:
			return "LOG";
		case Site.WORK_SITE:
			return "WORK";
		case Site.DATA_SITE:
			return "DATA";
		case Site.CALL_SITE:
			return "CALL";
		case Site.BUILD_SITE:
			return "BUILD";
		}
		return "NONE";
	}

	public int getType() {
		return this.type;
	}

	public boolean isTop() {
		return type == Site.TOP_SITE;
	}

	public boolean isHome() {
		return type == Site.HOME_SITE;
	}
	
	public boolean isLive() {
		return type == Site.LIVE_SITE;
	}

	public boolean isCall() {
		return this.type == Site.CALL_SITE;
	}

	public boolean isData() {
		return type == Site.DATA_SITE;
	}

	public boolean isLog() {
		return type == Site.LOG_SITE;
	}

	public boolean isWork() {
		return type == Site.WORK_SITE;
	}
	
	public boolean isBuild() {
		return type == Site.BUILD_SITE;
	}

	public String getIP() {
		return host.getIP();
	}
	public void setIP(String ip) {
		host.setIP(ip);
	}

	public int getTCPort() {
		return host.getTCPort();
	}

	public int getUDPort() {
		return host.getUDPort();
	}
	
	public void setHost(String ip, int tcport, int udport) {
		host = new SiteHost(ip, tcport, udport);
	}

	public void setHost(int ip, int tcport, int udport) {
		host = new SiteHost(ip, tcport, udport);
	}

	public void setHost(SiteHost host) {
		this.host = host;
	}

	public SiteHost getHost() {
		return this.host;
	}

	public void set(Site site) {
		this.type = site.type;
		this.host.set(site.host);
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || !(obj instanceof Site)) {
			return false;
		}
		Site site = (Site) obj;
		return type == site.type && host.equals(site.host);
	}

	public int hashCode() {
		return host.hashCode();
	}
}