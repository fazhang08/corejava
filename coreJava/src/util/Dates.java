package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Dates {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
	
		//getCalendarDate(Calendar.getInstance());
		
		//getGregorianCalendarDate();
		
		//getSimpleDateFormat();
		
		longConverCalendar();
	}

	public static void getCalendarDate(Calendar ca )
	{
		try
		{
			
			//Calendar ca = Calendar.getInstance();
			
			// 获得年份
			int year = ca.get(Calendar.YEAR);
			// 获得月份
			int month = ca.get(Calendar.MONTH) + 1;
			// 获得日期
			int date = ca.get(Calendar.DATE);
			// 获得小时
			int hour = ca.get(Calendar.HOUR_OF_DAY);
			// 获得分钟
			int minute = ca.get(Calendar.MINUTE);
			// 获得秒
			int second = ca.get(Calendar.SECOND);
			// 获得星期几（注意（这个与Date类是不同的）：1代表星期日、2代表星期1、3代表星期二，以此类推）
			int day_Week = ca.get(Calendar.DAY_OF_WEEK);
			
			System.out.println("CalendarDate: ");
			
			System.out.println("Year: " + year + "  Month: " + month + "  Date: " + date + 
					"  Hour: " + hour + "  Minute: " + minute + "  Second: " + second + "  DAY_OF_WEEK: " + day_Week);
			/**
			
			System.out.println("Day of Month: " + ca.get(Calendar.DAY_OF_MONTH));
			
			System.out.println("ca.getTime(): " + ca.getTime());
			
			System.out.println("ca: " + ca);
			*/
			
			System.out.println("***************************************");
			
		}catch(Exception e)
		{
			System.out.println("getCalendarDate method exist error !!!");
			
			e.printStackTrace();
		}
	}
	
	public static void getGregorianCalendarDate()
	{
		try
		{
			GregorianCalendar gc = new GregorianCalendar();
			// 获得年份
			int year = gc.get(GregorianCalendar.YEAR);
			// 获得月份
			int month = gc.get(Calendar.MONTH) + 1;
			// 获得日期
			int date = gc.get(Calendar.DATE);
			// 获得小时
			int hour = gc.get(Calendar.HOUR_OF_DAY);
			// 获得分钟
			int minute = gc.get(Calendar.MINUTE);
			// 获得秒
			int second = gc.get(Calendar.SECOND);
			// 获得星期几（注意（这个与Date类是不同的）：1代表星期日、2代表星期1、3代表星期二，以此类推）
			int day_Week = gc.get(GregorianCalendar.DAY_OF_WEEK);

			System.out.println("GregorianCalendar: ");
						
			System.out.println("Year: " + year + "  Month: " + month + "  Date: " + date + 
								"  Hour: " + hour + "  Minute: " + minute + "  Second: " + second + "  DAY_OF_WEEK: " + day_Week);

			System.out.println("Day of Month: " + gc.get(Calendar.DAY_OF_MONTH));

			System.out.println("gc.getTime(): " + gc.getTime());

			System.out.println("gc: " + gc);

			System.out.println("***************************************");
			
			
		}catch(Exception e )
		{
			System.out.println("getGregorianCalendarDate method exist error !!!");
			
			e.printStackTrace();
		}
		
	}
	
	public static void getSimpleDateFormat()
	{
		try
 {
			// SimpleDateFormat对象最常用的就是一下两招了：
			// 注意构造函数中是SimpleDateFormat类解析日期的模式，大小写代表的意义完全不一样哦
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			
			// 日期到字符串的转换
			String today = df.format(new Date());
			
			// 字符串到日期的转换
			Date date = df.parse("2009-06-12 02:06:37");
			
			System.out.println("SimpleDateFormat: ");
			
			System.out.println(".format(new Date()): " + df.format(new Date()));
			
			System.out.println("Today: " + today);
			
			System.out.println("Date: " + date);
			
			System.out.println("***************************************");
		}catch(Exception e)
		{
			System.out.println("getSimpleDateFormat() method exist error !!!");
			
			e.printStackTrace();
		}
	}

	public static void longConverCalendar()
	{
		try
		{
			System.out.println("longConverCalendar: ");

			// 将Calendar对象转换为相对时间
			Calendar c = Calendar.getInstance();
			
			long t = c.getTimeInMillis();
			
			getCalendarDate(c);
			
			System.out.println(t);
			
			
			// 将相对时间转换为Calendar对象
			Thread.sleep(10000);
			
			Calendar c1 = Calendar.getInstance();
			
			getCalendarDate(c1);
			
			c1.setTimeInMillis(t);
			
			getCalendarDate(c1);
			
			System.out.println("##########################################");
			
		}catch(Exception e)
		{
			System.out.println("longConverCalendar method exist error !!!");
			
			e.printStackTrace();
		}
	}
}
