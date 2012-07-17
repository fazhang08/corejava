/**
 * 
 */
package com.lexst.algorithm.collect;

import com.lexst.util.naming.*;
import com.lexst.db.schema.Space;
import com.lexst.algorithm.BasicTask;

public abstract class CollectTask extends BasicTask {

	protected Project project;
	
	protected Space space;
//	protected Table table;
	
	protected String writeto;

	/**
	 * 
	 */
	public CollectTask() {
		super();
	}

	/**
	 * @param project
	 */
	public CollectTask(Project project) {
		this();
		this.setProject(project);
	}
	
//	public void setTable(Table table) {
//		if(table == null) {
//			this.table = null;
//		} else {
//		this.table = table;
//		}
//	}
//	
//	public Table getTable() {
//		return this.table;
//	}
	
	/**
	 * show:[schema.table]
	 * @param s
	 */
	public void setSpace(Space s) {
		if (s == null) {
			space = null;
		} else {
			space = new Space(s);
		}
	}

	public Space getSpace() {
		return space;
	}

	/**
	 * writeto:[local filename]
	 * @param s
	 */
	public void setWriteto(String s) {
		this.writeto = s;
	}

	public String getWriteto() {
		return this.writeto;
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
//
//	/**
//	 * @param s
//	 */
//	public abstract void setProject(String s);
//	
	/**
	 * task operate, return result status
	 * @param params
	 * @param data
	 * @return
	 */
	public abstract int execute(Object[] params, byte[] data);

}