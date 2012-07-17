/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * short range
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 7/12/2009
 * 
 * @see com.lexst.util.range
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.range;

import java.io.Serializable;

public class ShortRange implements Serializable, Comparable<ShortRange> {
	private static final long serialVersionUID = 1L;

	private short begin, end;

	/**
	 *
	 */
	public ShortRange() {
		super();
		begin = end = 0;
	}

	/**
	 * @param begin
	 * @param end
	 */
	public ShortRange(short begin, short end) {
		this();
		this.set(begin, end);
	}

	public void set(short begin, short end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid short range!");
		}
		this.begin = begin;
		this.end = end;
	}

	public short getBegin() {
		return begin;
	}

	public short getEnd() {
		return end;
	}

	public boolean inside(short value) {
		return begin <= value && value <= end;
	}

	/**
	 * 比较前后两个结点阵是否一致
	 * 
	 * @param object
	 * @return boolean
	 */
	public boolean equals(Object arg) {
		if (arg == this)
			return true;
		if (arg == null)
			return false;
		if (!(arg instanceof ShortRange))
			return false;

		ShortRange range = (ShortRange) arg;
		return (begin == range.begin && end == range.end);
	}

	/**
	 * return hash code
	 * 
	 * @return int
	 */
	public int hashCode() {
		return begin ^ end;
	}

	public String toString() {
		return String.format("%d - %d", this.begin, this.end);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ShortRange range) {
		if (begin == range.begin) {
			return end < range.end ? -1 : (end == range.end ? 0 : 1);
		}
		return begin < range.begin ? -1 : 1;
	}

}