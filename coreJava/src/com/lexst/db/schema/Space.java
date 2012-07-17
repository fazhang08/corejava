/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * user table naming
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 2/2/2009
 * @see com.lexst.db.schema
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.schema;

import java.io.*;

public final class Space implements Serializable, Comparable<Space> {

	private static final long serialVersionUID = 4099615738507249866L;

	/* database name and table name byte size */
	public final static int MAX_SCHEMA_SIZE = 64;
	public final static int MAX_TABLE_SIZE = 64;

	/* database name */
	private String schema;

	/* table name */
	private String table;

	/* hash code */
	private int hash;

	public static boolean inSchemaSize(int size) {
		return 1 <= size && size <= Space.MAX_SCHEMA_SIZE;
	}

	public static boolean inTableSize(int size) {
		return 1 <= size && size <= Space.MAX_TABLE_SIZE;
	}

	/**
	 *
	 */
	public Space() {
		super();
		this.hash = 0;
	}

	/**
	 * @param schema
	 * @param table
	 */
	public Space(String schema, String table) {
		this();
		this.setSchema(schema);
		this.setTable(table);
	}

	/**
	 * @param space
	 */
	public Space(Space space) {
		this();
		this.set(space);
	}

	/**
	 * @param space
	 */
	public void set(Space space) {
		this.schema = space.schema;
		this.table = space.table;
		this.hash = space.hash;
	}
	
	/**
	 * @param schema
	 * @param table
	 */
	public void set(String schema, String table) {
		this.schema = schema;
		this.table = table;
		this.nextCode();
	}

	/**
	 * set database name
	 * 
	 * @param s
	 */
	public void setSchema(String s) {
		this.schema = s;
		this.nextCode();
	}

	/**
	 * get database name
	 * 
	 * @return
	 */
	public String getSchema() {
		return this.schema;
	}
	
	public boolean matchSchema(Space space) {
		return schema != null && schema.equalsIgnoreCase(space.schema);
	}

	/**
	 * set table name
	 * @param s
	 */
	public void setTable(String s) {
		this.table = s;
		this.nextCode();
	}

	/**
	 * get table name
	 * @return
	 */
	public String getTable() {
		return this.table;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != Space.class) {
			return false;
		} else if (arg == this) {
			return true;
		}

		Space s = (Space) arg;
		return schema != null && table != null && schema.equalsIgnoreCase(s.schema)
				&& table.equalsIgnoreCase(s.table);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hash == 0) {
			this.nextCode();
		}
		return hash;
	}
	
	private void nextCode() {
		if (schema != null && table != null) {
			hash = schema.toLowerCase().hashCode() ^ table.toLowerCase().hashCode();
		} else {
			hash = 0;
		}
	}

	public String toString() {
		return String.format("%s:%s", schema, table);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Space space) {
		if (schema.equalsIgnoreCase(space.schema)) {
			return table.toLowerCase().compareTo(space.table.toLowerCase());
		}
		return schema.toLowerCase().compareTo(space.schema.toLowerCase());
	}

}