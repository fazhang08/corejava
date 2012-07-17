package util;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ArrayLinkTest {

	
	private static Logger logger = Logger.getLogger(ArrayLinkTest.class);
	static
	{	
		PropertyConfigurator.configure ("./config/log4j.properties");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
		test();
		Vector v = new Vector();

	}
	
	public static void test()
	{
		ArrayList list = new ArrayList();
		
		LinkedList link = new LinkedList();
		
		logger.debug("********* Start Insert Test **********");
		
		Date listStart = new Date();
		//logger.debug("ListStartDate: " + listStart.getTime());
		

		for(int i = 0 ; i < 1000000 ; i++)
		{
			list.add("" + i);
		}
		
		Date listEnd = new Date();
		//logger.debug("ListEndDate: " + listEnd.getTime());
		
		logger.debug("ListStartEndOffset: " + (listEnd.getTime()-listStart.getTime()));
		
		
		
		Date linkStart = new Date();
		//logger.debug("LinkStartDate: " + linkStart.getTime());
		
		
		for(int i = 0 ; i < 1000000 ; i++)
		{
			link.add("" + i);
		}
		
		Date linkEnd = new Date();
		//logger.debug("LinkEndDate: " + linkEnd.getTime());
		
		logger.debug("LinkStartEndOffset: " + (linkEnd.getTime()-linkStart.getTime()));
		
		logger.debug("********** End Insert Test **********" + "\n");
		
		
		
		
		logger.debug("********** Start Get Test **********");
		
		Date listGetStart = new Date();
		//logger.debug("listGetStart: " + listGetStart.getTime());
		
		logger.debug("ListGet: " + list.get(100000));
		
		
		Date listGetEnd = new Date();
		//logger.debug("listGetEnd: " + listGetEnd.getTime());
		
		logger.debug("ListGetStartEndOffset: " + (listGetEnd.getTime()-listGetStart.getTime()));
		
		
		
		Date linkGetStart = new Date();
		//logger.debug("LinkGetStartDate: " + linkGetStart.getTime());
		
		
		logger.debug("LinkGet: " + link.get(100000));
		
		Date linkGetEnd = new Date();
		//logger.debug("linkGetEnd: " + linkGetEnd.getTime());
		
		logger.debug("LinkStartEndOffset: " + (linkGetEnd.getTime()-linkGetStart.getTime()));
		
		logger.debug("********** End Get Test **********");
		
		
	}
	

}
