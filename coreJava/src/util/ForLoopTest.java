package util;

import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ForLoopTest {

	private static Logger logger = Logger.getLogger(ForLoopTest.class);
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		PropertyConfigurator.configure ("./config/log4j.properties");

		ArrayList list = initList();
		
		print1(list);
		print2(list);

	}

	public static ArrayList initList()
	{
		ArrayList rList = null;
		
		rList = new ArrayList();
		
		for(int i = 0 ; i < 5000000 ; i++)
		{
			String sValue = "Number: " + i;
			
			rList.add(sValue);
		}
		
		return rList;
	}
	
	public static void print1(ArrayList list)
	{
		int size = list.size();
		long startTime = System.currentTimeMillis();

		for(int i = 0 ; i < size ; i ++)
		{
			String sValue = "" + i;
		}
		
		long costTime = System.currentTimeMillis() - startTime;
		
		logger.debug("costTime:" + costTime);
		
	}
	

	public static void print2(ArrayList list)
	{
		long startTime = System.currentTimeMillis();
		
		for(int i = 0 ; i < list.size() ; i ++)
		{
			String sValue = "" + i;
		}
		
		long costTime = System.currentTimeMillis() - startTime;
		
		logger.debug("costTime:" + costTime);
		
	}
}
