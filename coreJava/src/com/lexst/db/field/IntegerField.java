/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class IntegerField extends Field {
	// serial number
	private static final long serialVersionUID = 1L;

	// default int value
	private int value;

	/**
	 *
	 */
	public IntegerField() {
		super(Type.INTEGER);
		this.value = 0; //default
	}
	
	/**
	 * @param field
	 */
	public IntegerField(IntegerField field) {
		super(field);
		this.value = field.value;
	}
	
	/**
	 *
	 * @param defValue
	 */
	public IntegerField(int defValue) {
		this();
		this.setValue(defValue);
	}

	/**
	 * @param columnId
	 * @param name
	 * @param defValue
	 */
	public IntegerField(short columnId, String name, int defValue) {
		super(Type.INTEGER, columnId, name);
		this.setValue(defValue);
	}

	public void setValue(int num) {
		this.value = num;
		this.setNullable(false);
	}

	public int getValue() {
		return this.value;
	}

	@Override
	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.Integer(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.Integer(columnId, value);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.head.Field#build()
	 */
	@Override
	public byte[] build() {
		if(isNullable()) {
			value = 0;
		}

		ByteArrayOutputStream buf = new ByteArrayOutputStream(128);
		// data type
		buf.write(dataType);
		// column number
		byte[] b = Numeric.toBytes(columnId);
		buf.write(b, 0, b.length);
		// name size and name
		byte[] nm = name.getBytes();
		byte sz = (byte) (nm.length & 0xFF);
		buf.write(sz);
		buf.write(nm, 0, nm.length);
		// index type
		buf.write(indexType);
		// allow null or not
		byte bit = (byte) (allowNull ? 1 : 0);
		buf.write(bit);
		// default value
		b = Numeric.toBytes(value);
		buf.write(b, 0, b.length);

		return buf.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.head.Field#resolve(byte[], int)
	 */
	@Override
	public int resolve(byte[] b, int offset) {
		int off = offset;

		dataType = b[off];
		off += 1;
		columnId = Numeric.toShort(b, off, 2);
		off += 2;

		byte sz = b[off];
		off += 1;
		byte[] nm = new byte[sz];
		System.arraycopy(b, off, nm, 0, sz);
		name = new String(nm, 0, nm.length);
		off += sz;

		indexType = b[off];
		off += 1;

		allowNull = (b[off] == 1 ? true : false);
		off += 1;

		value = Numeric.toInteger(b, off, 4);
		off += 4;

		return off - offset;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.field.Field#fresh()
	 */
	public Field fresh() {
		return new IntegerField(this);
	}
}