/**
 *
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class Double extends Column {
	private static final long serialVersionUID = 1L;

	private double value;

	/**
	 *
	 */
	public Double() {
		super(Type.DOUBLE);
	}

	/**
	 * @param type
	 * @param id
	 */
	public Double(short id) {
		super(Type.DOUBLE, id);
	}

	public Double(short id, double num) {
		this(id);
		this.setValue(num);
	}

	public void setValue(double num) {
		this.value = num;
		this.setNull(false);
	}

	public double getValue() {
		return this.value;
	}


	@Override
	public byte[] bytes() {
		long num = java.lang.Double.doubleToLongBits(value);
		return Numeric.toBytes(num);
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.column.Column#build(java.io.ByteArrayOutputStream, java.io.ByteArrayOutputStream)
	 */
	@Override
	public int build(ByteArrayOutputStream head, ByteArrayOutputStream body) {
		byte tag = (byte) (isNull() ? 1 : 0);
		tag <<= 6;
		tag |= (getType() & 0x3f);
		head.write(tag);

		int size = 1;
		if (!isNull()) {
			byte[] b = this.bytes();
			body.write(b, 0, b.length);
			size += b.length;
		}
		return size;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.column.Column#parse(byte[], int, int)
	 */
	@Override
	public int[] resolve(byte[] b, int headoff, int bodyoff) {
		int headsize = 0, bodysize = 0;
		// parse head
		byte tag = b[headoff++];
		headsize += 1;
		// check null
		byte less = (byte) ((tag >>> 6) & 0x3);
		setNull(less == 1);
		byte type = (byte)(tag & 0x3f);
		if (!match_type(type)) {
			return null;
		}
		// parse body
		if (bodyoff + 8 > b.length) {
			return null;
		}
		long num = Numeric.toLong(b, bodyoff, 8);
		bodysize += 8;
		value = java.lang.Double.longBitsToDouble(num);

		return new int[] { headsize, bodysize };
	}

	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != Double.class) {
			return false;
		}
		Double col = (Double) arg;
		return value == col.value;
	}
	
	public int hashCode() {
		return (int) (value);
	}
}