/**
 * 
 */
package com.lexst.algorithm.marshaleduce;

import com.lexst.util.naming.*;
import com.lexst.algorithm.*;

public abstract class BuildTask extends BasicTask{

	protected BuildInbox inbox;

	/**
	 * 
	 */
	public BuildTask() {
		super();
	}
	
	/**
	 * @param project
	 */
	public BuildTask(Project project) {
		this();
		super.setProject(project);
	}

//	/**
//	 * set task project
//	 * 
//	 * @param s
//	 */
//	public void setProject(Project s) {
//		this.project = s;
//	}
//
//	/**
//	 * get task project
//	 * 
//	 * @return
//	 */
//	public Project getProject() {
//		return this.project;
//	}

	/**
	 * @param s
	 */
	public void setInbox(BuildInbox s) {
		this.inbox = s;
	}

	public BuildInbox getInbox() {
		return this.inbox;
	}

	/**
	 * release self
	 * 
	 * @return
	 */
	public boolean release() {
		if (inbox != null && project != null) {
			return inbox.removeTask(project.getNaming());
		}
		return false;
	}

	/**
	 * start job
	 * 
	 * @return
	 */
	public abstract boolean convert();

	/**
	 * stop job
	 */
	public abstract void halt();

}