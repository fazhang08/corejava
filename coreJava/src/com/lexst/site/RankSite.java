/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com All rights reserved
 * 
 * rank site, lexst basic class
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

public class RankSite extends Site {
	
	private static final long serialVersionUID = -8947419540620904476L;

	public final static byte PRIME_SITE = 1;
	public final static byte SLAVE_SITE = 2;

	private byte rank = 0;

	/**
	 * 
	 */
	public RankSite() {
		super();
		rank = 0;
	}

	/**
	 * @param type
	 */
	public RankSite(int type) {
		super(type);
		rank = 0;
	}

	/**
	 * @param type
	 * @param ip
	 * @param tcport
	 * @param udport
	 */
	public RankSite(int type, int ip, int tcport, int udport) {
		super(type, ip, tcport, udport);
		rank = 0;
	}

	/**
	 * @param type
	 * @param strIP
	 * @param tcport
	 * @param udport
	 */
	public RankSite(int type, String strIP, int tcport, int udport) {
		super(type, strIP, tcport, udport);
		rank = 0;
	}

	/**
	 * @param site
	 */
	public RankSite(Site site) {
		super(site);
		rank = 0;
	}

	public void setRank(byte value) {
		if (value != RankSite.PRIME_SITE && value != RankSite.SLAVE_SITE) {
			throw new IllegalArgumentException("invalid site rank");
		}
		this.rank = value;
	}

	public byte getRank() {
		return this.rank;
	}

	public boolean isPrime() {
		return rank == RankSite.PRIME_SITE;
	}

	public boolean isSlave() {
		return rank == RankSite.SLAVE_SITE;
	}

}