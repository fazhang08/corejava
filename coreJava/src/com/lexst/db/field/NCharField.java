/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class NCharField extends WordField {
	// serial number
	private static final long serialVersionUID = 1L;

	// default narrow char (byte style)
	private byte[] value;

	/**
	 *
	 */
	public NCharField() {
		super(Type.NCHAR);
		setPacking(Field.NOT_PACKING);
	}
	
	/**
	 * @param field
	 */
	public NCharField(NCharField field) {
		super(field);
		if (field.value != null && field.value.length > 0) {
			this.setValue(field.value);
		}
	}

	/**
	 * @param value
	 */
	public NCharField(byte[] value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param columnId
	 * @param name
	 * @param value
	 */
	public NCharField(short columnId, String name, byte[] value) {
		super(Type.NCHAR, columnId, name);
		this.setValue(value);
	}


	public void setValue(byte[] b) {
		if (b == null || b.length < 2 || b.length % 2 != 0) {
			throw new IllegalArgumentException("invalid nchar style!");
		}
		value = new byte[b.length];
		System.arraycopy(b, 0, value, 0, b.length);
		this.setNullable(false);
	}

	public byte[] getValue() {
		return value;
	}

	@Override
	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.NChar(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.NChar(columnId, value);
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
		byte bit = (byte) (allowNull ? 1 : 0);
		buff.write(bit);
		// case sensitive
		bit = (byte) (super.sentient ? 1 : 0);
		buff.write(bit);
		// support like
		bit = (byte) (super.like ? 1 : 0);
		buff.write(bit);
		// encode type
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
		// index type : primary, slave, none
		indexType = b[off];
		off += 1;
		// index limit size
		indexSize = Numeric.toInteger(b, off, 4);
		off += 4;

		allowNull = (b[off] == 1 ? true : false);
		off += 1;

		this.sentient = (b[off] == 1);
		off += 1;
		this.like = (b[off] == 1);
		off += 1;
		this.packing = b[off];
		off += 1;

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
		return new NCharField(this);
	}
}