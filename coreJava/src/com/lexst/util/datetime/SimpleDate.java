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

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * date format, include: year, month, day
 *
 */
public final class SimpleDate  {

	public static int format(int year, int month, int day) {
		int value = year & 0xFFFF;
		value <<= 9;
		value |= ((month & 0xF) << 5);
		value |= day & 0x1F;
		return value;
	}

	public static int format(Date date) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(date);
		int year = dar.get(Calendar.YEAR);
		int month = dar.get(Calendar.MONTH) + 1;
		int day = dar.get(Calendar.DAY_OF_MONTH);
		return SimpleDate.format(year, month, day);
	}

	public static int format() {
		Date date = new Date(System.currentTimeMillis());
		return SimpleDate.format(date);
	}

	public static Date format(int date) {
		int year = ((date >>> 9) & 0xFFFF);
		int month = ((date >>> 5) & 0xF);
		int day = date & 0x1F;
		Calendar dar = Calendar.getInstance();
		dar.set(year, month - 1, day, 0, 0, 0);
		dar.set(Calendar.MILLISECOND, 0);
		return dar.getTime();
	}

}