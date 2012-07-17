/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * float range
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

public class RealRange implements Serializable, Comparable<RealRange> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private float begin, end;

	/**
	 *
	 */
	public RealRange() {
		super();
		begin = end = 0;
	}
	/**
	 * @param begin
	 * @param end
	 */
	public RealRange(float begin, float end) {
		this();
		this.setRange(begin, end);
	}

	/**
	 * @param begin
	 * @param end
	 */
	public void setRange(float begin, float end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid float range!");
		}
		this.begin = begin;
		this.end = end;
	}

	public float getBegin() {
		return this.begin;
	}

	public float getEnd() {
		return this.end;
	}

	public boolean inside(float value) {
		return begin <= value && value <= end;
	}

	/**
	 * 比较前后两个结点阵是否一致
	 * @param object
	 * @return boolean
	 */
	public boolean equals(Object arg) {
		if (arg == null) return false;
		if (arg == this) return true;
		if (!(arg instanceof RealRange)) return false;
		RealRange range = (RealRange) arg;
		return (begin == range.begin && end == range.end);
	}

	/**
	 * return hash code
	 * @return int
	 */
	public int hashCode() {
		return (int) (Float.floatToIntBits(begin) ^ Float.floatToIntBits(end));
	}

	public String toString() {
		return String.format("%f - %f", this.begin, this.end);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RealRange range) {
		if (begin == range.begin) {
			return end < range.end ? -1 : (end == range.end ? 0 : 1);
		}
		return begin < range.begin ? -1 : 1;
	}

}
