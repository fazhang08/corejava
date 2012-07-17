/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.db.column.*;

public abstract class Field implements Serializable, Comparable<Field> {
	private static final long serialVersionUID = 1L;

	// char compress type
	public final static byte NOT_PACKING = 0;

	// date time function type
	public final static byte NOT_FUNCTION = 0;

	protected byte dataType;			// data type
	protected short columnId;			// system define
	protected String name;				// column name, user define
	protected byte indexType;			// index type: primary index, slave index, none index
	protected boolean allowNull;	 	// null value status, default is true
	protected boolean nullable;			// value status, default is true, not value

	/**
	 *
	 */
	public Field() {
		super();
		dataType = 0;
		columnId = 0;
		indexType = Type.NONE_INDEX;
		allowNull = true;
		nullable = true; //default is true, null value
	}
	
	/**
	 * @param type
	 */
	public Field(byte type) {
		this();
		this.setType(type);
	}

	/**
	 * @param type
	 * @param columnId
	 * @param name
	 */
	public Field(byte type, short columnId, String name) {
		this(type);
		this.setColumnId(columnId);
		this.setName(name);
	}

	/**
	 * @param field
	 */
	public Field(Field field) {
		super();
		this.dataType = field.dataType;
		this.columnId = field.columnId;
		this.name = field.name;
		this.indexType = field.indexType;
		this.allowNull = field.allowNull;
		this.nullable = field.nullable;
	}

	public void setColumnId(short id) {
		this.columnId = id;
	}
	public short getColumnId() {
		return this.columnId;
	}

	public void setName(String s) {
		this.name = s;
	}
	public String getName() {
		return this.name;
	}

	public void setType(byte b) {
		this.dataType = b;
	}
	public byte getType() {
		return this.dataType;
	}

	public boolean isRaw() {
		return dataType == Type.RAW;
	}

	public boolean isChar() {
		return dataType == Type.CHAR;
	}

	public boolean isNChar() {
		return dataType == Type.NCHAR;
	}

	public boolean isWChar() {
		return dataType == Type.WCHAR;
	}

	public boolean isShort() {
		return dataType == Type.SHORT;
	}

	public boolean isInteger() {
		return dataType == Type.INTEGER;
	}

	public boolean isLong() {
		return dataType == Type.LONG;
	}

	public boolean isReal() {
		return dataType == Type.REAL;
	}

	public boolean isDouble() {
		return dataType == Type.DOUBLE;
	}

	public boolean isDate() {
		return dataType == Type.DATE;
	}

	public boolean isTime() {
		return dataType == Type.TIME;
	}

	public boolean isTimeStamp() {
		return dataType == Type.TIMESTAMP;
	}

	public void setIndexType(byte b) {
		this.indexType = b;
	}
	public byte getIndexType() {
		return this.indexType;
	}

	/**
	 * check primary index
	 * 
	 * @return
	 */
	public boolean isPrimeIndex() {
		return indexType == Type.PRIME_INDEX;
	}

	/**
	 * is slave index
	 * 
	 * @return
	 */
	public boolean isIndex() {
		return indexType == Type.SLAVE_INDEX;
	}

	/**
	 * @return
	 */
	public boolean isNoneIndex() {
		return indexType == Type.NONE_INDEX;
	}

	public void setAllowNull(boolean b) {
		this.allowNull = b;
	}
	public boolean isAllowNull() {
		return this.allowNull;
	}

	protected void setNullable(boolean b) {
		this.nullable = b;
	}
	public boolean isNullable() {
		return this.nullable;
	}

	/**
	 *
	 */
	@Override
	public int compareTo(Field field) {
		int colId = field.getColumnId();
		return (columnId < colId ? -1 : (columnId == colId ? 0 : 1));
	}

	/**
	 * clone a new Field object
	 * @return
	 */
	public abstract Field fresh();
	
	/**
	 * get default column
	 * @return
	 */
	public abstract Column getDefault(short columnId);

	/**
	 * flush to byte array
	 * @return
	 */
	public abstract byte[] build();

	/**
	 * split byte array to self class
	 * @param b
	 * @param off
	 * @return
	 */
	public abstract int resolve(byte[] b, int off);

}