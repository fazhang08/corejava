/**
 * 
 */
package com.lexst.algorithm.diffuse;

import com.lexst.util.naming.Project;
import com.lexst.algorithm.*;

/**
 * diffuse interface
 */
public abstract class DiffuseTask extends BasicTask {
	
	/**
	 * default
	 */
	public DiffuseTask() {
		super();
	}
	
	public DiffuseTask(Project project) {
		super(project);
	}

//	/**
//	 * set diffuse project
//	 * @param s
//	 */
//	public void setProject(Project s) {
//		this.project = s;
//	}
//
//	/**
//	 * get diffuse project
//	 * @return
//	 */
//	public Project getProject() {
//		return this.project;
//	}

}