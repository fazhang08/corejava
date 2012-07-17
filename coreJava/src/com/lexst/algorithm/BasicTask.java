/**
 * 
 */
package com.lexst.algorithm;

import com.lexst.util.naming.*;

public class BasicTask {

	/* user resource configure */
	protected Project project;

	/**
	 * 
	 */
	public BasicTask() {
		super();
	}

	/**
	 * @param project
	 */
	public BasicTask(Project project) {
		this();
		this.setProject(project);
	}

	/**
	 * set task project
	 * 
	 * @param s
	 */
	public void setProject(Project s) {
		this.project = s;
	}

	/**
	 * get task project
	 * 
	 * @return
	 */
	public Project getProject() {
		return this.project;
	}

//	/**
//	 * @param s
//	 */
//	public abstract void setProject(String s);

}