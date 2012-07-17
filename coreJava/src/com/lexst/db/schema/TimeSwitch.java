/**
 * space optimize trigger
 */
package com.lexst.db.schema;

import java.io.*;
import java.util.*;

import com.lexst.util.datetime.*;

public class TimeSwitch implements Serializable {
	private static final long serialVersionUID = 1L;

	public final static int HOURLY = 1;
	public final static int DAILY = 2;
	public final static int WEEKLY = 3;
	public final static int	MONTHLY = 4;

	// table space
	private Space space;
	
	private int type;
	// target time
	private long time;
	// expire time
	private long expire;

	/**
	 * 
	 */
	public TimeSwitch() {
		super();
		time = 0;
		expire = 0;
	}

	/**
	 * @param space
	 * @param type
	 * @param time
	 */
	public TimeSwitch(Space space, int type, long time) {
		this();
		this.setSpace(space);
		this.setType(type);
		this.setTime(time);
		expire += time;
	}

	public void setSpace(Space s) {
		this.space = new Space(s);
	}

	public Space getSpace() {
		return this.space;
	}

	public void setType(int i) {
		if (!(TimeSwitch.HOURLY <= i && i <= TimeSwitch.MONTHLY)) {
			throw new IllegalArgumentException("invalid type");
		}
		this.type = i;
	}

	public int getType() {
		return this.type;
	}

	public void setTime(long value) {
		this.time = value;
		this.bring();
	}

	public long getTime() {
		return this.time;
	}
	
	/**
	 * set optimize time video.item hourly "12:12"
	 * set optimize time video.item daily "0:23:12"		
	 * set optimize time video.item weekly "1 0:12:12"  	(1-7)
	 * set optimize time video.item monthly "31 0:12:23"	(1-31)
	 */
	public void bring() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		Date date = SimpleTimeStamp.format(time);
		Calendar check = Calendar.getInstance();
		check.setTime(date);
		
		switch (type) {
		case TimeSwitch.HOURLY:
//			System.out.printf("now %d - expried %d\n", now.get(Calendar.MINUTE), check.get(Calendar.MINUTE));
			
			int minute = check.get(Calendar.MINUTE); // trigger minute
			int check_minute = now.get(Calendar.MINUTE);
			if (minute < check_minute || (minute == check_minute && check.get(Calendar.SECOND) < now.get(Calendar.SECOND))) {
				now.add(Calendar.HOUR, 1); // when timeout, next hour
			}
			now.set(Calendar.MINUTE, minute);
			now.set(Calendar.SECOND, check.get(Calendar.SECOND));
			break;
		case TimeSwitch.DAILY:
			int hour = check.get(Calendar.HOUR_OF_DAY);
			int check_hour = now.get(Calendar.HOUR_OF_DAY);
			if (hour < check_hour || (hour == check_hour && check.get(Calendar.MINUTE) < now.get(Calendar.MINUTE))) {
				now.add(Calendar.DAY_OF_YEAR, 1);
			}
			now.set(Calendar.HOUR_OF_DAY, hour);
			now.set(Calendar.MINUTE, check.get(Calendar.MINUTE));
			now.set(Calendar.SECOND, check.get(Calendar.SECOND));
			break;
		case TimeSwitch.WEEKLY:
			int day1 = check.get(Calendar.DAY_OF_MONTH);
			check.set(Calendar.DAY_OF_WEEK, day1);
			int check_day1 = now.get(Calendar.DAY_OF_WEEK);
			if (day1 < check_day1 || (day1 == check_day1 && check.get(Calendar.HOUR_OF_DAY) < now.get(Calendar.HOUR_OF_DAY))) {
				now.add(Calendar.WEEK_OF_YEAR, 1);
			}
			now.set(Calendar.DAY_OF_WEEK, day1);
			now.set(Calendar.HOUR_OF_DAY, check.get(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, check.get(Calendar.MINUTE));
			now.set(Calendar.SECOND, check.get(Calendar.SECOND));
			break;
		case TimeSwitch.MONTHLY:
			int day2 = check.get(Calendar.DAY_OF_MONTH);
			int check_day2 = now.get(Calendar.DAY_OF_MONTH);
			if (day2 < check_day2 || (day2 == check_day2 && check.get(Calendar.HOUR_OF_DAY) < now.get(Calendar.HOUR_OF_DAY))) {
				now.add(Calendar.MONTH, 1);
			}
			now.set(Calendar.DAY_OF_MONTH, day2);
			now.set(Calendar.HOUR_OF_DAY, check.get(Calendar.HOUR_OF_DAY));
			now.set(Calendar.MINUTE, check.get(Calendar.MINUTE));
			now.set(Calendar.SECOND, check.get(Calendar.SECOND));
			break;
		}
		
		this.expire = now.getTimeInMillis();
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
//		System.out.printf("trigger time %s\n", sdf.format(now.getTime()));
	}
	
	public boolean isExpired() {
		return System.currentTimeMillis() >= expire;
	}


//	public static void main(String[] args) {
//		int week = 11;
//		int hour = 12;
//		int minute = 1;
//		int second = 2;
//		long time = com.lexst.util.datetime.SimpleTimeStamp.format(0, 0, week, hour, minute, second, 0);
//		Space space = new Space("Video", "Item");
//		int type = TimeTrigger.HOURLY;
//		type = TimeTrigger.DAILY;
//		type = TimeTrigger.WEEKLY;
//		type = TimeTrigger.MONTHLY;
//		TimeTrigger tt = new TimeTrigger(space, type, time);
//
//		boolean su = tt.isExpired();
//		System.out.printf("%s expried %b\n", tt.getSpace(), su);
//	}

}