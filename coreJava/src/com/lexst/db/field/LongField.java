/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.db.column.*;
import com.lexst.util.*;

public class LongField extends Field {
	private static final long serialVersionUID = 1L;

	// default value
	private long value;

	/**
	 *
	 */
	public LongField() {
		super(Type.LONG);
		this.value = 0;
	}
	
	/**
	 * @param field
	 */
	public LongField(LongField field) {
		super(field);
		this.value = field.value;
	}

	/**
	 * @param name
	 */
	public LongField(String name) {
		this();
		this.setName(name);
	}

	/**
	 * @param value
	 */
	public LongField(long value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param columnId
	 * @param name
	 * @param value
	 */
	public LongField(short columnId, String name, long value) {
		super(Type.LONG, columnId, name);
		this.setValue(value);
	}

	/**
	 * @param name
	 * @param defValue
	 */
	public LongField(String name, long defValue) {
		this(name);
		this.setValue(defValue);
	}

	public void setValue(long num) {
		this.value = num;
		this.setNullable(false);
	}

	public long getValue() {
		return this.value;
	}

	public Column getDefault(short columnId) {
		// 如果允许空,返回一个空值
		if (allowNull) {
			return new com.lexst.db.column.Long(columnId);
		}
		// 不允许空,且不是空状态,提供一个默认值
		if (!nullable) {
			return new com.lexst.db.column.Long(columnId, value);
		}
		// otherwise, return null
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
		byte[] b = Numeric.toBytes(columnId);
		// column number
		buf.write(b, 0, b.length);
		byte[] nm = name.getBytes();
		byte nmSize = (byte) (nm.length & 0xFF);
		// name size and name
		buf.write(nmSize);
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

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.field.Field#resolve(byte[], int)
	 */
	public int resolve(byte[] b, int offset) {
		int off = offset;

		dataType = b[off];
		off += 1;
		columnId = Numeric.toShort(b, off, 2);
		off += 2;

		byte sz = b[off];
		off += 1;
		byte[] bs = new byte[sz];
		System.arraycopy(b, off, bs, 0, sz);
		name = new String(bs, 0, bs.length);
		off += sz;

		indexType = b[off];
		off += 1;

		allowNull = (b[off] == 1 ? true : false);
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
		return new LongField(this);
	}
}