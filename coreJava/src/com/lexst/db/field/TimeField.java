/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class TimeField extends Field {
	// default serial number
	private static final long serialVersionUID = 1L;

	// time function type (eg. now function)
	private byte function;
	// default value
	private int value;

	/**
	 *
	 */
	public TimeField() {
		super(Type.TIME);
		function = Field.NOT_FUNCTION;
		value = 0;
	}
	
	/**
	 * @param field
	 */
	public TimeField(TimeField field) {
		super(field);
		this.function = field.function;
		this.value = field.value;
	}

	/**
	 * @param columnId
	 * @param name
	 * @param function
	 * @param value
	 */
	public TimeField(short columnId, String name, byte function, int value) {
		super(Type.TIME, columnId, name);
		this.setFunction(function);
		this.setValue(value);
	}

	/**
	 * set time function
	 * @param b
	 */
	public void setFunction(byte b) {
		this.function = b;
	}
	public byte getFunction() {
		return this.function;
	}

	/**
	 * set default value
	 * @param num
	 */
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
			return new com.lexst.db.column.Time(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.Time(columnId, value);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.head.Field#build()
	 */
	@Override
	public byte[] build() {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(128);
		buf.write(dataType);		// data type
		byte[] b = Numeric.toBytes(columnId);
		buf.write(b, 0, b.length);	// column id

		byte[] bs = name.getBytes();
		byte sz = (byte) (bs.length & 0xFF);
		buf.write(sz);					// name size

		buf.write(bs, 0, bs.length);	// name bytes

		buf.write(indexType);			// index type
		byte tag = (byte) (allowNull ? 1 : 0);
		buf.write(tag);					// allow null

		buf.write( function );			// default type
		b = Numeric.toBytes(value);
		buf.write(b, 0, b.length);		//default value

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

		function = b[off];
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
		return new TimeField(this);
	}
}