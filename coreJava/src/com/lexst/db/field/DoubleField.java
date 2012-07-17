/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class DoubleField extends Field {
	// default serial number
	private static final long serialVersionUID = 1L;
	// default value
	private double value;

	/**
	 *
	 */
	public DoubleField() {
		super(Type.DOUBLE);
	}
	
	/**
	 *
	 * @param num
	 */
	public DoubleField(double num) {
		this();
		this.setValue(num);
	}
	
	/**
	 * @param field
	 */
	public DoubleField(DoubleField field) {
		super(field);
		this.value = field.value;
	}

	/**
	 * @param columnId
	 * @param name
	 * @param num
	 */
	public DoubleField(short columnId, String name, double num) {
		super(Type.DOUBLE, columnId, name);
		this.setValue(num);
	}

	public void setValue(double num) {
		this.value = num;
		this.setNullable(false);
	}

	public double getValue() {
		return this.value;
	}

	@Override
	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.Double(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.Double(columnId, value);
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

		long num = Double.doubleToLongBits(value);
		b = Numeric.toBytes(num);
		buf.write(b, 0, b.length); // default value

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

		long num = Numeric.toLong(b, off, 8);
		off += 8;
		value = Double.longBitsToDouble(num);

		return off - offset;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.field.Field#fresh()
	 */
	public Field fresh() {
		return new DoubleField(this);
	}

}