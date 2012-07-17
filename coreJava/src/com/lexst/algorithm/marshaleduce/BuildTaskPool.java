/**
 * 
 */
package com.lexst.algorithm.marshaleduce;

import com.lexst.algorithm.*;
import com.lexst.util.naming.Naming;

public class BuildTaskPool extends TaskPool {

	private static BuildTaskPool selfHandle = new BuildTaskPool();
	
	/**
	 * 
	 */
	public BuildTaskPool() {
		super();
	}

	public static BuildTaskPool getInstance() {
		return BuildTaskPool.selfHandle;
	}
	
	/**
	 * @param naming
	 * @return
	 */
	public BuildTask find(Naming naming) {
		return (BuildTask)super.findTask(naming);
	}
	
	/**
	 * find naming object
	 * @param naming
	 * @return
	 */
	public BuildTask find(String naming) {
		return (BuildTask)super.findTask(naming);
	}
}