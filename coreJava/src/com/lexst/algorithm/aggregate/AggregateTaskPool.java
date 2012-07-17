/**
 * 
 */
package com.lexst.algorithm.aggregate;

import com.lexst.algorithm.*;
import com.lexst.util.naming.Naming;

public class AggregateTaskPool extends TaskPool {

	private static AggregateTaskPool selfHandle = new AggregateTaskPool();

	/**
	 * 
	 */
	private AggregateTaskPool() {
		super();
	}

	/**
	 * @return
	 */
	public static AggregateTaskPool getInstance() {
		return AggregateTaskPool.selfHandle;
	}

	/**
	 * @param naming
	 * @return
	 */
	public AggregateTask find(Naming naming) {
		return (AggregateTask) super.findTask(naming);
	}

	/**
	 * @param naming
	 * @return
	 */
	public AggregateTask find(String naming) {
		return (AggregateTask) super.findTask(naming);
	}
}