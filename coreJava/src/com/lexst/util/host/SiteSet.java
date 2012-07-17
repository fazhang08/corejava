/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * lexst container class
 * 
 * @author lei.zhang lexst@126.com
 * 
 * @version 1.0 3/2/2009
 * 
 * @see com.lexst.pool
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.host;

import java.util.*;

public final class SiteSet {

	private int index;
	private ArrayList<SiteHost> array;
	
	/**
	 * @param capacity
	 */
	public SiteSet(int capacity) {
		super();
		index = 0;
		if (capacity < 3) capacity = 3;
		array = new ArrayList<SiteHost>(capacity);
	}

	/**
	 * work site
	 */
	public SiteSet(){
		this(3);
	}
	
	/**
	 * @param hosts
	 */
	public SiteSet(SiteHost[] hosts) {
		this(hosts != null && hosts.length > 0 ? hosts.length : 3);
		add(hosts);
	}
	
	/**
	 * @param list
	 */
	public SiteSet(Collection<SiteHost> list) {
		this(list.size());
		add(list);
	}

	/**
	 * add a site
	 * @param host
	 * @return
	 */
	public boolean add(SiteHost host) {
		if(host == null || array.contains(host)) {
			return false;
		}
		return array.add(host);
	}

	/**
	 * @param hosts
	 * @return
	 */
	public int add(SiteHost[] hosts) {
		int count = 0;
		for (int i = 0; hosts != null && i < hosts.length; i++) {
			if (add(hosts[i])) count++;
		}
		return count;
	}
	
	/**
	 * @param list
	 * @return
	 */
	public int add(Collection<SiteHost> list) {
		int count = 0;
		for (SiteHost host : list) {
			if (add(host)) count++;
		}
		return count;
	}
	
	/**
	 * remove a site
	 * @param host
	 * @return
	 */
	public boolean remove(SiteHost host) {
		return array.remove(host);
	}
	
	/**
	 * remove site set
	 * @param list
	 * @return
	 */
	public int remove(Collection<SiteHost> list) {
		int count = 0;
		for (SiteHost host : list) {
			if (remove(host)) count++;
		}
		return count;
	}

	/**
	 * check a site
	 * @param host
	 * @return
	 */
	public boolean exists(SiteHost host) {
		return array.contains(host);
	}
	
	/**
	 * @return
	 */
	public List<SiteHost> list() {
		return array;
	}
	
	public boolean isEmpty() {
		return array.isEmpty();
	}
	
	public int size() {
		return array.size();
	}
	
	public void ensure() {
		int size = array.size();
		if (size == 0) size = 3;
		array.ensureCapacity(size);
	}
	
	public SiteHost[] toArray() {
		int size = array.size();
		if (size == 0) return null;
		SiteHost[] s = new SiteHost[size];
		return array.toArray(s);
	}
	
	public synchronized SiteHost next() {
		int size = array.size();
		if (size > 0) {
			if (index >= size) index = 0;
			return array.get(index++);
		}
		return null;
	}
	
	public synchronized SiteHost next(SiteHost previous) {
		int size = array.size();
		if (size == 0) return null;
		for (int i = 0; i < size; i++) {
			if (array.get(i) != previous) continue;
			if (i + 1 < size) {
				index = i + 1;
				return array.get(index);
			}
		}
		return array.get(index = 0);
	}
}