/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class CharField extends WordField {
	// default serial number
	private static final long serialVersionUID = 1L;

	// default char value (byte style)
	private byte[] value;

	/**
	 *
	 */
	public CharField() {
		super(Type.CHAR);
	}
	
	/**
	 * @param field
	 */
	public CharField(CharField field) {
		super(field);
		if (field.value != null && field.value.length > 0) {
			this.setValue(field.value);
		}
	}

	/**
	 * @param columnId
	 * @param name
	 */
	public CharField(short columnId, String name) {
		super(Type.CHAR, columnId, name);
	}

	/**
	 *
	 * @param value
	 */
	public CharField(byte[] value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param columnId
	 * @param name
	 * @param value
	 */
	public CharField(short columnId, String name, byte[] value) {
		super(Type.CHAR, columnId, name);
		this.setValue(value);
	}

	public void setValue(byte[] b) {
		if (b == null || b.length == 0) {
			throw new NullPointerException("invalid default value!");
		}
		value = new byte[b.length];
		System.arraycopy(b, 0, value, 0, b.length);
		this.setNullable(false);
	}

	public byte[] getValue() {
		return this.value;
	}

	@Override
	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.Char(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.Char(columnId, value);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.head.Field#build()
	 */
	@Override
	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(128);
		// data type
		buff.write(dataType);
		// column number
		byte[] b = Numeric.toBytes(columnId);
		buff.write(b, 0, b.length);
		// name size(max size 64 byte) and name
		b = name.getBytes();
		byte sz = (byte) (b.length & 0xFF);
		buff.write(sz);
		buff.write(b, 0, b.length);
		// index type
		buff.write(indexType);
		// index limit size
		b = Numeric.toBytes(indexSize);
		buff.write(b, 0, b.length);
		// allow null or no
		buff.write((byte) (allowNull ? 1 : 0));
		// case sensitive
		buff.write((byte) (super.sentient ? 1 : 0));
		// support like
		buff.write((byte) (super.like ? 1 : 0));
		// compress type
		buff.write(super.packing);
		// default value size
		int size = (value == null ? 0 : value.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		// default value
		if (size > 0) {
			buff.write(value, 0, value.length);
		}
		return buff.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.head.Field#resolve(byte[], int)
	 */
	@Override
	public int resolve(byte[] b, int offset) {
		int off = offset;

		// data type
		dataType = b[off];
		off += 1;
		// field number
		columnId = Numeric.toShort(b, off, 2);
		off += 2;
		// name size and name
		byte sz = b[off];
		off += 1;
		byte[] nm = new byte[sz];
		System.arraycopy(b, off, nm, 0, sz);
		name = new String(nm, 0, nm.length);
		off += sz;
		// index type
		indexType = b[off];
		off += 1;
		// index limit size
		indexSize = Numeric.toInteger(b, off, 4);
		off += 4;
		// allow null
		allowNull = (b[off] == 1 ? true : false);
		off += 1;
		// case sensitive
		this.sentient = (b[off] == 1);
		off += 1;
		// support like
		this.like = (b[off] == 1);
		off += 1;
		// compress type
		this.packing = b[off];
		off += 1;
		// default value size and default value
		int size = Numeric.toInteger(b, off, 4);
		off += 4;
		if (size > 0) {
			value = new byte[size];
			System.arraycopy(b, off, value, 0, size);
			off += size;
		}
		return off - offset;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.field.Field#fresh()
	 */
	public Field fresh() {
		return new CharField(this);
	}
	
}