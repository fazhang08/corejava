package thread;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Task {

	public void run()
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		// 日期到字符串的转换
		String today = df.format(new Date());
		
		System.out.println("Current Time : " + today);
	}
	
	

}
