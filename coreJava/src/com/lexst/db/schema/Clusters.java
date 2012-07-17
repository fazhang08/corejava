/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 lexst.com. All rights reserved
 * 
 * lexst cluster address
 * 
 * @author lei.zhang lexst@126.com
 * 
 * @version 1.0 12/9/2011
 * @see com.lexst.db.schema
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.schema;

import java.io.*;
import java.util.*;

public class Clusters implements Serializable {

	private static final long serialVersionUID = -4265709137404017307L;

	/* home site number */
	private int number;

	/* home ip address */
	private List<String> array = new ArrayList<String>();

	/**
	 * default
	 */
	public Clusters() {
		super();
		number = 0;
	}
	
	/**
	 * @param number
	 */
	public Clusters(int number) {
		this();
		this.setNumber(number);
	}

	/**
	 * set host number
	 * @param value
	 */
	public void setNumber(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("invalid host number: " + value);
		}
		this.number = value;
	}

	/**
	 * get host number
	 * @return
	 */
	public int getNumber() {
		return this.number;
	}

	/**
	 * save a host address
	 * @param ip
	 * @return
	 */
	public boolean add(String ip) {
		if (array.contains(ip)) return false;
		return array.add(ip);
	}

	/**
	 * remove a host address
	 * @param ip
	 * @return
	 */
	public boolean remove(String ip) {
		return array.remove(ip);
	}

	public List<String> list() {
		return array;
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public int size() {
		return array.size();
	}

	public void clear() {
		array.clear();
	}
}