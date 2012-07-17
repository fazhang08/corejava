package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.Ostermiller.util.CSVParser;

public class FileHandle {
	
	public static String[]  readSourseFile(String sPath)
	{
		
		File file = null;
		
		String resoursePath = "";
		
		BufferedReader br = null;
		
		String[]path = null;
		
		CSVParser cp = null;
		
		
		try
		{
		
			resoursePath = sPath;
			
			if(resoursePath==null||resoursePath.length()<=0)
				return null;
			
			file = new   File(resoursePath);
			
			br = new BufferedReader(new FileReader(file));
			 
			path = new String[5];
			 
			int i =0;
			 
			while(br.read()!=-1)
			 { 
				 path[i] = br.readLine();
				 
				 i++;
			 }
			
			br.close();
			
			if(path.length<5)
				 return null;
			
		}catch(Exception e)
		{
			System.out.println("readSourseFile method is exist errror");
			e.printStackTrace();
		}
		
		return path;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String[] fileName = null;
		
		String source_path = "C:\\Finley\\doc\\maximo\\webTest\\Wangzij\\ASSETSOURCEPATH.txt";
		
		fileName = readSourseFile(source_path);
		
		for(int i = 0 ; i<fileName.length ; i++)
		{
			System.out.println(fileName[i]);
		}
	}

}
