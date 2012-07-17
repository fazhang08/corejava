/**
 * 
 */
package com.lexst.algorithm;

import java.net.*;
import java.io.*;

import com.lexst.log.client.*;

public class TaskClassLoader extends URLClassLoader {
	
	/**
	 * 
	 */
	public TaskClassLoader() {
		super(new URL[0], ClassLoader.getSystemClassLoader());
	}

	/**
	 * @param filename
	 */
	public boolean addJar(String filename) {
		File file = new File(filename);
		if(!file.exists()) return false;
		
		Logger.info("TaskClassLoader.add, load '%s'", filename);
		
		try {
			super.addURL(file.toURI().toURL());
			return true;
		} catch (MalformedURLException exp) {
			Logger.error(exp);
		}
		return false;
	}


}