/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author yj.liang lexst@126.com
 * @version 1.0 1/10/2010
 * @see com.lexst.util.datetime
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.datetime;

import java.util.*;

/**
 * 这是一个工具类,处理时间和LONG类型的转换,时间是以完整的日期格式,包括年,月,日,时,分,秒,毫秒
 *
 * 	millisecond: 10
 * 	second:6		10
 * 	minute:6		16
 * 	hour: 5			22
 * 	day: 5			27
 * 	month: 4		32
 * 	year: 			36
 *
 * @author steven
 */
public final class SimpleTimeStamp {

	/**
	 * 格式化日期时间,将参数合并为一个长整数
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 * @param millisecond
	 * @return
	 */
	public static long format(int year, int month, int day, int hour, int minute, int second, int millisecond) {
		long value = millisecond & 0x3FF;
		long v = (second & 0x3F); value |= (v << 10);
		v = minute & 0x3F;	value |= (v << 16);
		v = hour & 0x1F;	value |= (v << 22);
		v = day & 0x1F;		value |= (v << 27);
		v = month & 0xF;	value |= (v << 32);
		v = year & 0xFFFF;	value |= (v << 36);
		return value;
	}

	public static long format(Date date) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(date);
		int year = dar.get(Calendar.YEAR);
		int month = dar.get(Calendar.MONTH) + 1;
		int day = dar.get(Calendar.DAY_OF_MONTH);
		int hour = dar.get(Calendar.HOUR_OF_DAY);
		int minute = dar.get(Calendar.MINUTE);
		int second = dar.get(Calendar.SECOND);
		int millisecond = dar.get(Calendar.MILLISECOND);
		return SimpleTimeStamp.format(year, month, day, hour, minute, second, millisecond);
	}
	
	public static long format() {
		Date date = new Date(System.currentTimeMillis());
		return SimpleTimeStamp.format(date);
	}

	public static Date format(long time) {
		int year = (int)((time >>> 36) & 0xFFFF);
		int month = (int)((time >>> 32) & 0xF);
		int day = (int)((time >>> 27) & 0x1F);
		int hour = (int)((time >>> 22) & 0x1F);
		int minute = (int) ((time >>> 16) & 0x3F);
		int second = (int) ((time >>> 10) & 0x3F);
		int millisecond = (int) (time & 0x3FF);

		Calendar dar = Calendar.getInstance();
		dar.set(year, month - 1, day, hour, minute, second);
		dar.set(Calendar.MILLISECOND, millisecond);
		return dar.getTime();
	}

}