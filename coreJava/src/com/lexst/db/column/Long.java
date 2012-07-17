/**
 * 
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class Long extends Column {

	private static final long serialVersionUID = 1L;

	private long value;

	/**
	 *
	 */
	public Long() {
		super(Type.LONG);
	}

	/**
	 * @param id
	 */
	public Long(short id) {
		super(Type.LONG, id);
	}

	/**
	 * @param id
	 * @param num
	 */
	public Long(short id, long num) {
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

	/**
	 * build
	 * @param head
	 * @param body
	 * @return
	 */
	public int build(ByteArrayOutputStream head, ByteArrayOutputStream body) {
		byte tag = (byte) (isNull() ? 1 : 0);
		tag <<= 6;
		tag |= (getType() & 0x3f);
		head.write(tag);

		int size = 1;
		if (!isNull()) {
			byte[] b = bytes();
			body.write(b, 0, b.length);
			size += b.length;
		}
		return size;
	}

	/**
	 * @param b (big-endian order)
	 * @param headoff
	 * @param bodyoff
	 * @return
	 */
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
		if (arg == null || arg.getClass() != Long.class) {
			return false;
		}
		Long col = (Long) arg;
		return value == col.value;
	}

	public int hashCode() {
		return (int) (value >>> 32 ^ value);
	}

}