/**
 * 
 */
package com.lexst.algorithm.diffuse;

import com.lexst.algorithm.*;
import com.lexst.util.naming.Naming;

public class DiffuseTaskPool extends TaskPool {

	private static DiffuseTaskPool selfHandle = new DiffuseTaskPool();

	/**
	 * 
	 */
	private DiffuseTaskPool() {
		super();
	}

	public static DiffuseTaskPool getInstance() {
		return DiffuseTaskPool.selfHandle;
	}

	/**
	 * @param naming
	 * @return
	 */
	public DiffuseTask find(Naming naming) {
		return (DiffuseTask) super.findTask(naming);
	}

	/**
	 * @param naming
	 * @return
	 */
	public DiffuseTask find(String naming) {
		return (DiffuseTask) super.findTask(naming);
	}

}
