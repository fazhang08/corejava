/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * long range
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
import java.math.BigInteger;

public final class LongRange implements Serializable, Comparable<LongRange> {

	private static final long serialVersionUID = 1L;

	// from  0
	private long begin, end;

	/**
	 *
	 */
	public LongRange() {
		super();
		begin = end = 0L;
	}

	/**
	 * @param begin
	 * @param end
	 */
	public LongRange(long begin, long end) {
		this();
		this.set(begin, end);
	}

	public long size() {
		return end - begin + 1;
	}

	public void set(long begin, long end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid long range!");
		}
		this.begin = begin;
		this.end = end;
	}

	/**
	 * @param LongRange
	 */
	public void set(LongRange range) {
		if (range != null) {
			set(range.getBegin(), range.getEnd());
		}
	}

	/**
	 * @param blocks
	 * @return LongRange[]
	 */
	public LongRange[] split(int blocks) {
		if (this.begin == 0 && this.end == 0) {
			return null;
		}
		// 最小分块是1,<1即出错
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks define:" + blocks);
		}

		BigInteger min = BigInteger.valueOf(this.begin);
		BigInteger max = BigInteger.valueOf(this.end);

		Range range = new Range(min, max);
		Range[] result = range.split(blocks);
		LongRange[] ranges = new LongRange[result.length];
		for (int i = 0; i < result.length; i++) {
			ranges[i] = new LongRange(result[i].getBegin().longValue(), result[i].getEnd().longValue());
		}
		return ranges;
	}

	public boolean inside(long value) {
		return (begin <= value && value <= end);
	}

	public boolean inside(LongRange range) {
		return (begin <= range.begin && range.end <= end);
	}

	/**
	 * 开始点否小于指定的值
	 * @return boolean
	 */
	public boolean beginLessBy(long value) {
		return this.begin<value;
	}
	/**
	 * 开始点是否等于指定的值
	 * @return boolean
	 */
	public boolean beginEqualsBy(long value) {
		return this.begin==value;
	}
	/**
	 * 开始点是否大于指定的值
	 * @return boolean
	 */
	public boolean beginGreatBy(long value) {
		return this.begin>value;
	}

	/**
	 * 结束点是否小于被比较值
	 * @return boolean
	 */
	public boolean endLessBy(long value) {
		return this.end<value;
	}
	/**
	 * 是否等于结束点值
	 * @return boolean
	 */
	public boolean endEqualsBy(long value) {
		return this.end==value;
	}
	/**
	 * 是否大于结束点值
	 * @return boolean
	 */
	public boolean endGreatBy(long value) {
		return this.end>value;
	}

	/**
	 * 判断当前LongRange结尾与另一个LongRange开始是否衔接
	 * @param before
	 * @return boolean
	 */
	public boolean isLinkupByAfter(LongRange after) {
		return this.end+1==after.getBegin();
	}

	/**
	 * 判断另一个LongRange的结尾与当前LongRange开始是否衔接
	 * @param before
	 * @return boolean
	 */
	public boolean isLinkupByBefore(LongRange before) {
		return before.getEnd()+1==this.begin;
	}

	/**
	 * 合并两个LongRange对象. 成功返回一个合并后的新对象,不成功,返回NULL
	 * @param previous
	 * @param next
	 * @return LongRange
	 */
	public static LongRange incorporate(LongRange after, LongRange before) {
		// 比较两个对象是否衔
		if(!after.isLinkupByAfter(before)) return null;
		// 组成一个合并后的新对象
		return new LongRange(after.getBegin(), before.getEnd());
	}

	/**
	 * 返回开始点
	 * @return long
	 */
	public long getBegin() {
		return this.begin;
	}
	/**
	 * 返回结束点
	 * @return long
	 */
	public long getEnd() {
		return this.end;
	}

	public void copy(LongRange range) {
		this.set(range);
	}

	public boolean isValid() {
		return begin != -1 && end != -1 && begin <= end;
	}

	/**
	 * 比较前后两个结点阵是否一致
	 * @param object
	 * @return boolean
	 */
	public boolean equals(Object arg) {
		if (arg == null) return false;
		if (arg == this) return true;
		if (!(arg instanceof LongRange)) return false;

		LongRange range = (LongRange)arg;
		return (begin == range.getBegin() && end == range.getEnd());
	}

	/**
	 * return hash code
	 * @return long
	 */
	public int hashCode() {
		return Long.valueOf(begin).hashCode() ^ Long.valueOf(end).hashCode();
	}

	public String toString() {
		return String.format("%d - %d", this.begin, this.end);
	}

	@Override
	public int compareTo(LongRange range) {
		if (begin == range.begin) {
			return end < range.end ? -1 : (end == range.end ? 0 : 1);
		}
		return begin < range.begin ? -1 : 1;
	}

}