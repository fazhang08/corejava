/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2012 lexst.com, All rights reserved
 * 
 * task resource configure
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 2/1/2012
 * 
 * @see com.lexst.util.naming
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.naming;

import java.util.*;

import com.lexst.db.schema.Space;
import com.lexst.db.schema.Table;

/**
 * project style
 * 
 * <task>
 * 	<project-class> org.xx.xxx.EngineProject </project-class> <!-- from "Project" class -->
 * 
 * 	<naming> diffuse-object </naming>
 * 	<task-class> org.xxx.xxx.EngineTask </task-class>
 * 	<resource> filename or data </resource> <!-- user data -->
 * </task>
 * 
 */
public abstract class Project {

	/* naming object */
	private Naming naming;

	/* task class name */
	private String taskClass;

	/* user resource configure */
	protected String resource;

	/* user table set */
	protected Map<Space, Table> mapTable = new TreeMap<Space, Table>();

	/**
	 * 
	 */
	public Project() {
		super();
	}

	public void setNaming(Naming s) {
		naming = new Naming(s);
	}

	public Naming getNaming() {
		return naming;
	}

	public void setTaskClass(String name) {
		this.taskClass = name;
	}

	public String getTaskClass() {
		return this.taskClass;
	}

	public Set<Space> getSpaces() {
		return mapTable.keySet();
	}

	public boolean setTable(Space space, Table table) {
		return mapTable.put(space, table) == null;
	}
	
	public Table getTable(Space space) {
		return mapTable.get(space);
	}

	/**
	 * get resource data (string)
	 * 
	 * @return
	 */
	public String getResource() {
		return this.resource;
	}

	/**
	 * save resource and resolve it
	 * 
	 * @param s
	 */
	public abstract void setResource(String s);

}