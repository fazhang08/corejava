/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 lexst.com. All rights reserved
 * 
 * dc and adc basic class
 * 
 * @author scott.liu lexst@126.com
 * 
 * @version 1.0 6/12/2011
 * 
 * @see com.lexst.db.statement
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.statement;

import java.util.*;

import com.lexst.util.host.SiteHost;

public class DC extends BasicComputing {

	private static final long serialVersionUID = 1L;

	private int from_allsite;

	private int from_index;
	
	private int to_allsite;

	/* work host set */
	private List<SiteHost> array = new ArrayList<SiteHost>();

	/**
	 * default constructor
	 */
	public DC() {
		super(BasicObject.DC_METHOD);
		from_allsite = from_index = 0;
	}
	
	public void defineToSites(int i) {
		this.to_allsite = i;
	}
	public int getDefineToSites() {
		return this.to_allsite;
	}

	public void defineFromSites(int i) {
		this.from_allsite = i;
	}

	public int getDefineFromSites() {
		return this.from_allsite;
	}

	public void defineFromIndex(int i) {
		this.from_index = i;
	}

	public int getDefineFromIndex() {
		return this.from_index;
	}

	/**
	 * add work site ip
	 * 
	 * @param host
	 */
	public boolean addToAddress(SiteHost host) {
		if (array.contains(host))
			return false;
		return array.add(host);
	}

	/**
	 * @param list
	 * @return
	 */
	public int addToAddress(Collection<SiteHost> list) {
		int count = 0;
		for (SiteHost host : list) {
			if (addToAddress(host))
				count++;
		}
		return count;
	}

	/**
	 * @return
	 */
	public List<SiteHost> listToAddress() {
		return array;
	}

	/**
	 * copy object
	 */
	public DC clone() {
		DC dc = new DC();
		super.set(dc);
		dc.from_allsite = this.from_allsite;
		dc.from_index = this.from_index;
		dc.to_allsite = this.to_allsite;
		dc.array.addAll(this.array);
		return dc;
	}

}