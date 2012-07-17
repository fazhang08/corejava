/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * integer range
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

public final class IntegerRange implements Serializable, Comparable<IntegerRange> {

	private static final long serialVersionUID = 1L;

	private int begin, end;

	/**
	 * 
	 */
	public IntegerRange() {
		super();
		begin = end = 0;
	}

	/**
	 * @param begin
	 * @param end
	 */
	public IntegerRange(int begin, int end) {
		this();
		this.set(begin, end);
	}

	/**
	 * @param begin
	 * @param end
	 * @param radix
	 */
	public IntegerRange(String begin, String end, int radix) {
		int beginValue = Integer.parseInt(begin, radix);
		int endValue = Integer.parseInt(end, radix);
		this.set(beginValue, endValue);
	}

	/**
	 * @param range
	 */
	public IntegerRange(IntegerRange range) {
		this();
		this.setRange(range);
	}

	/**
	 * set integer range
	 * @param begin
	 * @param end
	 */
	public void set(int begin, int end) {
		// begin<=end, 否则认为出错
		if (begin > end) {
			throw new IllegalArgumentException("invalid int range!");
		}
		this.begin = begin;
		this.end = end;
	}

	/**
	 * set integer range
	 */
	public void setRange(IntegerRange range) {
		this.set(range.begin, range.end);
	}

	/**
	 * range length
	 * @return
	 */
	public int size() {
		return end - begin + 1;
	}

	/**
	 * 将一个IRange按指定的Blocks数目分块
	 * @param blocks
	 */
	public IntegerRange[] split(int blocks) {
		if (this.begin == 0 && this.end == 0) {
			return null;
		}
		// 最小分块是1,<1即出错
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks:" + blocks);
		}

		BigInteger min = BigInteger.valueOf(this.begin);
		BigInteger max = BigInteger.valueOf(this.end);

		Range range = new Range(min, max);
		Range[] result = range.split(blocks);
		IntegerRange[] ranges = new IntegerRange[result.length];
		for(int i=0; i<result.length; i++) {
			ranges[i] = new IntegerRange(result[i].getBegin().intValue(), result[i].getEnd().intValue());
		}
		return ranges;
	}

	public boolean inside(int value) {
		return (begin <= value && value <= end);
	}

	public boolean inside(IntegerRange range) {
		return (begin <= range.begin && range.end <= end);
	}

//	/**
//	 * 交叉
//	 * @param range
//	 * @return
//	 */
//	public boolean cross(IRange range) {
//		if (end > range.begin && end < range.end) {
//			return true;
//		} else if (range.end > begin && range.end < end) {
//			return true;
//		}
//		return false;
//	}

	/**
	 * 开始点否小于指定的值
	 * @return boolean
	 */
	public boolean beginLessBy(int value) {
		return this.begin<value;
	}
	
	/**
	 * 开始点是否等于指定的值
	 * @return boolean
	 */
	public boolean beginEqualsBy(int value) {
		return this.begin==value;
	}
	
	/**
	 * 开始点是否大于指定的值
	 * @return boolean
	 */
	public boolean beginGreatBy(int value) {
		return this.begin > value;
	}

	/**
	 * 结束点是否小于被比较值
	 * 
	 * @return boolean
	 */
	public boolean endLessBy(int value) {
		return this.end < value;
	}

	/**
	 * 是否等于结束点值
	 * 
	 * @return boolean
	 */
	public boolean endEqualsBy(int value) {
		return this.end == value;
	}

	/**
	 * 是否大于结束点值
	 * 
	 * @return boolean
	 */
	public boolean endGreatBy(int value) {
		return this.end > value;
	}

	/**
	 * 判断当前Field结尾与另一个Field开始是否衔接
	 * @param before
	 * @return boolean
	 */
	public boolean isLinkupByAfter(IntegerRange after) {
		return this.end + 1 == after.getBegin();
	}

	/**
	 * 判断另一个Field的结尾与当前Field开始是否衔接
	 * @param before
	 * @return boolean
	 */
	public boolean isLinkupByBefore(IntegerRange before) {
		return before.getEnd() + 1 == this.begin;
	}

	/**
	 * 合并两个Field对象. 成功返回一个合并后的新对象,不成功,返回NULL
	 * @param previous
	 * @param next
	 * @return Field
	 */
	public static IntegerRange incorporate(IntegerRange after, IntegerRange before) {
		// 比较两个对象是否衔
		if(!after.isLinkupByAfter(before)) return null;
		// 组成一个合并后的新对象
		return new IntegerRange(after.getBegin(), before.getEnd());
	}

	/**
	 * 返回开始点
	 * 
	 * @return int
	 */
	public int getBegin() {
		return this.begin;
	}

	/**
	 * 返回结束点
	 * 
	 * @return int
	 */
	public int getEnd() {
		return this.end;
	}

	public void copy(IntegerRange arg) {
		this.setRange(arg);
	}

	/**
	 * 比较前后两个结点阵是否一致
	 * @param object
	 * @return boolean
	 */
	public boolean equals(Object arg) {
		if (arg == null || !(arg instanceof IntegerRange)) return false;
		else if (arg == this) return true;

		IntegerRange range = (IntegerRange) arg;
		return (begin == range.begin && end == range.end);
	}

	/**
	 * return hash code
	 * @return int
	 */
	public int hashCode() {
		return begin ^ end;
	}

	public String toString() {
		return String.format("%d - %d", begin, end);
	}

	/**
	 *
	 */
	@Override
	public int compareTo(IntegerRange range) {
		if (begin == range.begin) {
			return end < range.end ? -1 : (end == range.end ? 0 : 1);
		}
		return begin < range.begin ? -1 : 1;
	}

}