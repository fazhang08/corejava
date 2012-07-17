/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * column root class
 * 
 * @author scott.jian lexst@126.com
 * 
 * @version 1.0 5/6/2009
 * 
 * @see com.lexst.db.column
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;
import com.lexst.db.field.*;

public abstract class Column implements Serializable, Comparable<Column> {
	private static final long serialVersionUID = 1L;
	
	// column identity
	private short id;
	// null status
	private boolean nullable;
	// data type
	private byte type;
	
	// check field
	protected Field field;

	/**
	 *
	 */
	protected Column() {
		super();
		this.id = 0;
		this.type = 0;
		this.nullable = true;
	}

	/**
	 * @param type
	 */
	public Column(byte type) {
		this();
		this.setType(type);
	}

	/**
	 * @param type
	 * @param id
	 */
	public Column(byte type, short id) {
		this(type);
		this.setId(id);
	}
	
	/**
	 * set field object
	 * @param object
	 */
	public void setField(Field object) {
		this.field = object;
	}

	public Field getField() {
		return this.field;
	}

	public void setNull(boolean b) {
		this.nullable = b;
	}
	public boolean isNull() {
		return this.nullable;
	}

	public void setType(byte b) {
		if (Type.RAW <= b && b <= Type.TIMESTAMP) {
			this.type = b;
		} else {
			throw new IllegalArgumentException("invalid type!");
		}
	}

	public byte getType() {
		return this.type;
	}

	public boolean match_type(byte b) {
		return type == b;
	}

	public void setId(short i) {
		this.id = i;
	}

	public short getId() {
		return this.id;
	}

	/**
	 * variable value
	 * @return
	 */
	public boolean isVariable() {
		return type == Type.RAW || type == Type.CHAR ||
			type == Type.NCHAR || type == Type.WCHAR;
	}

	public final boolean isRaw() {
		return this.type == Type.RAW;
	}

	public final boolean isChar() {
		return type == Type.CHAR;
	}

	public final boolean isNChar() {
		return type == Type.NCHAR;
	}

	public final boolean isWChar() {
		return type == Type.WCHAR;
	}

	public final boolean isShort() {
		return type == Type.SHORT;
	}

	public final boolean isInteger() {
		return type == Type.INTEGER;
	}

	public final boolean isLong() {
		return type == Type.LONG;
	}

	public final boolean isReal() {
		return type == Type.REAL;
	}

	public final boolean isDouble() {
		return type == Type.DOUBLE;
	}

	public final boolean isDate() {
		return type == Type.DATE;
	}

	public final boolean isTime() {
		return type == Type.TIME;
	}

	public final boolean isTimestamp() {
		return type == Type.TIMESTAMP;
	}

	@Override
	public int compareTo(Column o) {
		return id < o.id ? -1 : (id == o.id ? 0 : 1);
	}

	public abstract byte[] bytes();

	public abstract int build(ByteArrayOutputStream head, ByteArrayOutputStream body);

	public abstract int[] resolve(byte[] b, int headoff, int bodyoff);
}