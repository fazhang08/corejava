/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * double range
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

public class DoubleRange implements Serializable, Comparable<DoubleRange> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private double begin, end;

	/**
	 *
	 */
	public DoubleRange() {
		super();
		begin = end = 0;
	}

	/**
	 * @param begin
	 * @param end
	 */
	public DoubleRange(double begin, double end) {
		this();
		this.set(begin, end);
	}

	public void set(double begin, double end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid double range!");
		}
		this.begin = begin;
		this.end = end;
	}

	public double getBegin() {
		return begin;
	}

	public double getEnd() {
		return end;
	}

	public boolean inside(double value) {
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
		if (!(arg instanceof DoubleRange)) return false;
		DoubleRange range = (DoubleRange) arg;
		return (begin == range.begin && end == range.end);
	}

	public String toString() {
		return String.format("%f - %f", this.begin, this.end);
	}

	/**
	 * return hash code
	 * @return int
	 */
	public int hashCode() {
		return (int) (Double.doubleToLongBits(begin) ^ Double.doubleToLongBits(end));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DoubleRange range) {
		if (begin == range.begin) {
			return end < range.end ? -1 : (end == range.end ? 0 : 1);
		}
		return begin < range.begin ? -1 : 1;
	}

}
