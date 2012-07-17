/**
 *
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class TimeStamp extends Column {
	private static final long serialVersionUID = 1L;

	private long value;

	/**
	 *
	 */
	public TimeStamp() {
		super(Type.TIMESTAMP);
	}

	/**
	 * @param type
	 * @param id
	 */
	public TimeStamp(short id) {
		super(Type.TIMESTAMP, id);
	}

	/**
	 * @param id
	 * @param num
	 */
	public TimeStamp(short id, long num) {
		this(id);
		this.setValue(num);
	}

	public void setValue(long num) {
		this.value = num;
		this.setNull(false);
	}

	public long getValue() {
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
		if (bodyoff + 8 > b.length) {
			return null;
		}
		value = Numeric.toLong(b, bodyoff, 8);
		bodysize += 8;

		return new int[] { headsize, bodysize };
	}

	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != TimeStamp.class) {
			return false;
		}
		TimeStamp col = (TimeStamp) arg;
		return value == col.value;
	}
	
	public int hashCode() {
		return (int) (value >>> 32 ^ value);
	}
}
