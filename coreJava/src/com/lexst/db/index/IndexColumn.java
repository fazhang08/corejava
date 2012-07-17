/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * index column root class
 * 
 * @author scott.jian lexst@126.com
 * 
 * @version 1.0 6/12/2009
 * 
 * @see com.lexst.db.index
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.index;

import java.io.Serializable;

import com.lexst.db.Type;
import com.lexst.db.column.Column;

public class IndexColumn implements Serializable {
	private static final long serialVersionUID = 1L;

	// index type
	protected byte type;
	// column
	protected Column column;

	/**
	 * @param type
	 */
	public IndexColumn(byte type) {
		super();
		this.setType(type);
	}

	/**
	 * @param object
	 */
	public IndexColumn(IndexColumn object) {
		super();
		this.type = object.type;
		this.column = object.column;
	}

	public boolean isType(byte type) {
		return Type.SHORT_INDEX <= type && type <= Type.DOUBLE_INDEX;
	}

	public void setType(byte b) {
		if (!isType(b)) {
			throw new IllegalArgumentException("invalid index type!");
		}
		this.type = b;
	}

	public byte getType() {
		return type;
	}

	public boolean isShort() {
		return type == Type.SHORT_INDEX;
	}

	public boolean isInteger() {
		return type == Type.INTEGER_INDEX;
	}

	public boolean isLong() {
		return type == Type.LONG_INDEX;
	}

	public boolean isReal() {
		return type == Type.REAL_INDEX;
	}

	public boolean isDouble() {
		return type == Type.DOUBLE_INDEX;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	public Column getColumn() {
		return this.column;
	}

	public short getColumnId() {
		return column.getId();
	}

	public IndexColumn clone() {
		return new IndexColumn(this);
	}
}