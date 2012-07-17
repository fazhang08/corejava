/**
 *
 */
package com.lexst.algorithm.aggregate;

import com.lexst.algorithm.BasicTask;
import com.lexst.util.naming.Project;

/**
 * aggregate interface
 */
public abstract class AggregateTask extends BasicTask {

	/* request identity (dc or adc identity) */
	private long identity;

	/**
	 * default structor
	 */
	public AggregateTask() {
		super();
	}
	
	public AggregateTask(Project project) {
		this();
		super.setProject(project);
	}

	public void setIdentity(long id) {
		this.identity = id;
	}

	public long getIdentity() {
		return this.identity;
	}

//	public void setProject(Project s) {
//		this.project = s;
//	}
//
//	public Project getProject() {
//		return this.project;
//	}

	/**
	 * thread wait
	 * 
	 * @param timeout
	 */
	protected synchronized void delay(long timeout) {
		try {
			super.wait(timeout);
		} catch (InterruptedException exp) {

		}
	}

	/**
	 * notify thread
	 */
	protected synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException exp) {

		}
	}

}