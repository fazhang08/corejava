/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;


public class TimeStampField extends Field {
	// default serial number
	private static final long serialVersionUID = 1L;

	// function type, default is 0, none-function
	private byte function;
	// default value
	private long value;

	/**
	 *
	 */
	public TimeStampField() {
		super(Type.TIMESTAMP);
		function = 0;
		value = 0;
	}

	/**
	 * @param field
	 */
	public TimeStampField(TimeStampField field) {
		super(field);
		this.function = field.function;
		this.value = field.value;
	}

	/**
	 * @param type
	 */
	public TimeStampField(long value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param columnId
	 * @param name
	 * @param function
	 * @param value
	 */
	public TimeStampField(short columnId, String name, byte function, long value) {
		super(Type.TIMESTAMP, columnId, name);
		this.setFunction(function);
		this.setValue(value);
	}

	/**
	 * set function id
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
	public void setValue(long num) {
		this.value = num;
		this.setNullable(false);
	}

	public long getValue() {
		return this.value;
	}

	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.TimeStamp(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.TimeStamp(columnId, value);
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

		buf.write(function); // function type
		b = Numeric.toBytes(value);
		buf.write(b, 0, b.length);		// default value

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
		value = Numeric.toLong(b, off, 8);
		off += 8;

		return off - offset;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.field.Field#fresh()
	 */
	public Field fresh() {
		return new TimeStampField(this);
	}
}