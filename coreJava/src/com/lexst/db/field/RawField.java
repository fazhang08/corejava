/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class RawField extends VariableField {
	// default serial number
	private static final long serialVersionUID = 1L;

	// default value
	private byte[] value;

	/**
	 *
	 */
	public RawField() {
		super(Type.RAW);
		setPacking(Field.NOT_PACKING);
	}
	
	/**
	 * @param field
	 */
	public RawField(RawField field) {
		super(field);
		if (field.value != null && field.value.length > 0) {
			setValue(field.value);
		}
	}

	/**
	 * @param b
	 */
	public RawField(byte[] b) {
		this();
		this.setValue(b);
	}
	/**
	 * @param columnId
	 * @param name
	 * @param b
	 */
	public RawField(short columnId, String name, byte[] b) {
		super(Type.RAW, columnId, name);
		setPacking(Field.NOT_PACKING);
		setValue(b);
	}

	/**
	 * @param b
	 */
	public void setValue(byte[] b) {
		if (b == null || b.length == 0) {
			throw new java.lang.NullPointerException("invalid raw data!");
		}
		value = new byte[b.length];
		System.arraycopy(b, 0, value, 0, b.length);
		this.setNullable(false);
	}

	/**
	 * @return
	 */
	public byte[] getValue() {
		return this.value;
	}

	@Override
	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.Raw(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.Raw(columnId, value);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.head.Field#build()
	 */
	@Override
	public byte[] build() {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(128);
		// data type
		buf.write(dataType);
		// column id
		byte[] b = Numeric.toBytes(columnId);
		buf.write(b, 0, b.length);
		// name size (max size:64) and name
		byte[] bs = name.getBytes();
		byte sz = (byte) (bs.length & 0xFF);
		buf.write(sz);
		buf.write(bs, 0, bs.length);
		// index type
		buf.write(indexType);
		// index limit size
		b = Numeric.toBytes(indexSize);
		buf.write(b, 0, b.length);
		// allow null
		buf.write((byte) (allowNull ? 1 : 0));
		// compress type
		buf.write(packing);
		// default value size and defalut value
		int size = (value == null ? 0 : value.length);
		b = Numeric.toBytes(size);
		buf.write(b, 0, b.length);
		if (size > 0) {
			buf.write(value, 0, value.length);
		}
		return buf.toByteArray();
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
		// column id
		columnId = Numeric.toShort(b, off, 2);
		off += 2;
		// name size and name
		byte sz = b[off];
		off += 1;
		byte[] nm = new byte[sz];
		System.arraycopy(b, off, nm, 0, sz);
		name = new String(nm, 0, nm.length);
		off += sz;
		// index type (primary, slave, none)
		indexType = b[off];
		off += 1;
		indexSize = Numeric.toInteger(b, off, 4);
		off += 4;
		// allow null
		allowNull = (b[off] == 1 ? true : false);
		off += 1;
		// compress type
		packing = b[off];
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
		return new RawField(this);
	}
}