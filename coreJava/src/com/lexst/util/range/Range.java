/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * basic range
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

import java.io.*;
import java.math.*;
import java.util.*;

public class Range implements Serializable, Comparable<Range> {
	// serial version identity
	private static final long serialVersionUID = 1L;

	//code range
	private BigInteger begin;
	private BigInteger end;

	/**
	 * construct method
	 */
	public Range() {
		super();
	}
	/**
	 * construct method
	 * @param begin
	 * @param end
	 */
	public Range(BigInteger begin, BigInteger end) {
		this();
		this.setRange(begin, end);
	}
	/**
	 * construct method
	 * @param begin
	 * @param end
	 * @param radix
	 */
	public Range(String begin, String end, int radix) {
		this();
		this.setRange(begin, end, radix);
	}

	public Range(Range range) {
		this();
		this.setRange(range);
	}

	/**
	 * 将一个Range按指定的块数分割
	 * @param blocks
	 * @return Range[]
	 */
	public Range[] split(final int blocks) {
		if (this.begin == null || this.end == null) {
			throw new NullPointerException("undefine range!");
		}
		// 最小分块是1,<1即出错
		if (blocks < 1) {
			throw new IllegalArgumentException("illegal blocks define:" + blocks);
		}

		// 存储集
		ArrayList<Range> array = new ArrayList<Range>();
		// 确定一个BLOCK的范围
		BigInteger biBlocks = BigInteger.valueOf(blocks);
		BigInteger sect = end.subtract(begin).add(BigInteger.ONE);
		BigInteger field = sect.divide(biBlocks);
		if(sect.remainder(biBlocks).compareTo(BigInteger.ZERO)!=0) {
			field = field.add(BigInteger.ONE);
		}
		// 分块开始
		BigInteger previous = this.begin;
		while(true) {
			BigInteger next = previous.add(field);
			if (next.compareTo(this.end) >= 0) {
				next = this.end;
				array.add(new Range(previous, next));
				break;
			} else {
				if(next.compareTo(previous)>0) {
					next = next.subtract(BigInteger.ONE);
				}
				array.add(new Range(previous, next));
				previous = next.add(BigInteger.ONE);
			}
		}

		// 保存数组
		Range[] ranges = new Range[array.size()];
		return array.toArray(ranges);
	}

	/**
	 * 设置范围
	 * @param begin
	 * @param end
	 */
	public void setRange(BigInteger begin, BigInteger end) {
		if (begin == null || end == null) {
			throw new NullPointerException("null range wrong!");
		}
		// begin<=end, 否则认为出错
		if (begin.compareTo(end)>0) {
			throw new IllegalArgumentException("Invalid Range!");
		}
		this.begin = begin;
		this.end = end;
	}
	/**
	 * 设置码范围
	 * @param range
	 */
	public void setRange(Range range) {
		this.setRange(range.getBegin(), range.getEnd());
	}

	/**
	 * 设置码范围
	 * @param begin
	 * @param end
	 * @param radix
	 */
	public void setRange(String begin, String end, int radix) {
		if(begin==null || end==null) return;
		begin = begin.trim();
		end = end.trim();
		if(begin.length()<1 || end.length()<1) return;
		// 定义范围
		BigInteger bi = new BigInteger(begin, radix);
		BigInteger ei = new BigInteger(end, radix);
		setRange(bi, ei);
	}

	public BigInteger size() {
		return end.subtract(begin).add(BigInteger.ONE);
	}

	/**
	 * 是否在范围内
	 * @return boolean
	 */
	public boolean isInside(BigInteger value) {
		if (value == null) return false;
		return (begin.compareTo(value) <= 0 && value.compareTo(end) <= 0);
	}
	public boolean isInside(Range range) {
		if(range==null) return false;
		return begin.compareTo(range.begin) <= 0
				&& range.end.compareTo(end) <= 0;
	}

	/**
	 * 开始点否小于指定的值
	 * @return boolean
	 */
	public boolean beginLessBy(BigInteger value) {
		return begin!=null && begin.compareTo(value)<0;
	}
	/**
	 * 开始点是否等于指定的值
	 * @return boolean
	 */
	public boolean beginEqualsBy(BigInteger value) {
		return begin!=null && begin.compareTo(value)==0;
	}
	/**
	 * 开始点是否大于指定的值
	 * @return boolean
	 */
	public boolean beginGreatBy(BigInteger value) {
		return begin!=null && begin.compareTo(value)>0;
	}

	/**
	 * 结束点是否小于被比较值
	 * @return boolean
	 */
	public boolean endLessBy(BigInteger value) {
		return end!=null && end.compareTo(value)<0;
	}
	/**
	 * 是否等于结束点值
	 * @return boolean
	 */
	public boolean endEqualsBy(BigInteger value) {
		return end!=null && end.compareTo(value)==0;
	}
	/**
	 * 是否大于结束点值
	 * @return boolean
	 */
	public boolean endGreatBy(BigInteger value) {
		return end!=null && end.compareTo(value)>0;
	}

	/**
	 * 返回开始点
	 * @return BigInteger
	 */
	public BigInteger getBegin() {
		return this.begin;
	}
	/**
	 * 返回结束点
	 * @return BigInteger
	 */
	public BigInteger getEnd() {
		return this.end;
	}

	/**
	 * 判断当前Range结尾与另一个Range开始是否衔接
	 * @param before
	 * @return boolean
	 */
	public boolean isLinkupByAfter(Range after) {
		// if(this.end==null || after==null || after.getBegin()==null) return false;
		return end!=null && end.add(BigInteger.ONE).compareTo(after.getBegin())==0;
	}

	/**
	 * 判断另一个Range的结尾与当前Range开始是否衔接
	 * @param before
	 * @return boolean
	 */
	public boolean isLinkupByBefore(Range before) {
		// if(begin==null || before==null || before.getEnd()==null) return false;
		return begin!=null && before.getEnd().add(BigInteger.ONE).compareTo(this.begin)==0;
	}

	/**
	 * 合并两个Range对象. 成功返回一个合并后的新对象,不成功,返回NULL
	 * @param previous
	 * @param next
	 * @return Range
	 */
	public static Range incorporate(Range after, Range before) {
		// 比较两个对象是否衔
		if(!after.isLinkupByAfter(before)) return null;
		// 组成一个合并后的新对象
		return new Range(after.getBegin(), before.getEnd());
	}

	// 判断是否有效
	public boolean isValid() {
		return begin != null && end != null && begin.compareTo(end) <= 0;
	}

	/**
	 * 复制Range class
	 * @param range
	 */
	public void copy(Range range) {
		if(range!=null) {
			this.setRange(range.getBegin(), range.getEnd());
		}
	}

	/**
	 * @param arg
	 * @return
	 */
	@Override
	public int compareTo(Range range) {
//		if(arg==this) return 0;
//		if(!(arg instanceof Range)) return -1;
//		Range range = (Range)arg;

		if(range.getBegin().compareTo(this.getBegin())==0) {
			if(range.getEnd().compareTo(this.getEnd())==0) return 0;
			else if(range.getEnd().compareTo(this.getEnd())<1) return 1;
			else return -1;
		} else if(range.getBegin().compareTo(this.getBegin())<0) {
			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * 比较前后两个结点阵是否一致
	 * @param object
	 * @return boolean
	 */
	public boolean equals(Object arg) {
		if (arg == null) return false;
		if (arg == this) return true;
		if (!(arg instanceof Range)) return false;

		Range range = (Range) arg;
		return beginEqualsBy(range.getBegin()) && endEqualsBy(range.getEnd());
	}

	/**
	 * return hash code
	 * @return int
	 */
	public int hashCode() {
		if (begin != null && end != null) {
			return begin.hashCode() ^ end.hashCode();
		}
		return 1;
	}

	public String toString() {
		if(begin==null) return "begin not define!";
		if(end==null) return "end not define!";

		String b = begin.toString(16);
		String e = end.toString(16);
		if(!b.equals("0") && b.length()%2==1) b = "0" + b;
		if(!e.equals("0") && e.length()%2==1) e = "0" + e;

		String str = String.format("%s - %s", b, e);
		return str;
	}

}