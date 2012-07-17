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
 * time format, include: hour, minute, second, milli-second
 *
 *	millisecond: 10		0
 * 	second:6			10
 * 	minute:6			16
 * 	hour: 5				22
 */
public final class SimpleTime  {

	public static int format(int hour, int minute, int second, int millisecond) {
		int value = ((hour & 0x1F) << 22);
		value |= ((minute & 0x3F) << 16);
		value |= ((second & 0x3F) << 10);
		value |= (millisecond & 0x3FF);
		return value;
	}

	public static int format(Date time) {
		Calendar dar = Calendar.getInstance();
		dar.setTime(time);
		int hour = dar.get(Calendar.HOUR_OF_DAY);
		int minute = dar.get(Calendar.MINUTE);
		int second = dar.get(Calendar.SECOND);
		int millisecond = dar.get(Calendar.MILLISECOND);
		return SimpleTime.format(hour, minute, second, millisecond);
	}

	public static int format() {
		Date date = new Date(System.currentTimeMillis());
		return SimpleTime.format(date);
	}

	public static Date format(int time) {
		int hour = ((time >>> 22) & 0x1F);
		int minute = ((time >>> 16) & 0x3F);
		int second = ((time >>> 10) & 0X3F);
		int millisecond = time & 0x3FF;
		Calendar dar = Calendar.getInstance();

		dar.set(0, 0, 0);
		dar.set(Calendar.HOUR_OF_DAY, hour);
		dar.set(Calendar.MINUTE, minute);
		dar.set(Calendar.SECOND, second);
		dar.set(Calendar.MILLISECOND, millisecond);
		return dar.getTime();
	}

}