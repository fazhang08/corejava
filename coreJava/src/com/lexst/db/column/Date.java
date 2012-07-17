/**
 *
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class Date extends Column {
	private static final long serialVersionUID = 1L;

	private int value;

	/**
	 *
	 */
	public Date() {
		super(Type.DATE);
	}

	/**
	 * @param type
	 * @param id
	 */
	public Date(short id) {
		super(Type.DATE, id);
	}

	/**
	 * @param id
	 * @param num
	 */
	public Date(short id, int num) {
		this(id);
		this.setValue(num);
	}

	public void setValue(int num) {
		this.value = num;
		this.setNull(false);
	}

	public int getValue() {
		return this.value;
	}

	@Override
	public byte[] bytes() {
		return Numeric.toBytes(value);
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
		if (bodyoff + 4 > b.length) {
			return null;
		}
		value = Numeric.toInteger(b, bodyoff, 4);
		bodysize += 4;

		return new int[] { headsize, bodysize };
	}
	
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != Date.class) {
			return false;
		}
		Date col = (Date) arg;
		return value == col.value;
	}

	public int hashCode() {
		return value;
	}
}
