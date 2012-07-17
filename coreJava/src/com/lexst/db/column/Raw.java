/**
 *
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;

public class Raw extends Word {
	private static final long serialVersionUID = 1L;

	private byte[] value;

	/**
	 *
	 */
	public Raw() {
		super(Type.RAW);
	}

	/**
	 * @param id
	 */
	public Raw(short id) {
		super(Type.RAW, id);
	}

	/**
	 * @param id
	 * @param value
	 */
	public Raw(short id, byte[] value) {
		this(id);
		this.setValue(value);
	}

	public void setValue(byte[] b) {
		if (b != null && b.length > 0) {
			this.value = new byte[b.length];
			System.arraycopy(b, 0, value, 0, b.length);
			this.setNull(false);
		} else {
			value = null;
			this.setNull(true);
		}
		setHash(value);
	}

	public byte[] getValue() {
		return this.value;
	}

	@Override
	public byte[] bytes() {
		return value;
	}

	/**
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
			int count = (value == null ? 0 : value.length);
			byte[] b = com.lexst.util.Numeric.toBytes(count);
			head.write(b, 0, b.length);
			size += b.length;
			if (count > 0) {
				body.write(value, 0, value.length);
				size += count;
			}
		}
		return size;
	}

	/**
	 * @param b
	 * @param headoff
	 * @param bodyoff
	 * @return
	 */
	public int[] resolve(byte[] b, int headoff, int bodyoff) {
		int headsize = 0, bodysize = 0;
		// parse head
		byte tag = b[headoff++];
		headsize += 1;

		byte less = (byte) ((tag >>> 6) & 0x3);
		setNull(less == 1);
		byte type = (byte) (tag & 0x3f);
		if (!match_type(type)) {
			return null;
		}
		// if not null
		if (!isNull()) {
			int count = com.lexst.util.Numeric.toInteger(b, headoff);
			headsize += 4;
			if (count > 0) {
				if (bodyoff + count > b.length) {
					return null;
				}
				value = new byte[count];
				System.arraycopy(b, bodyoff, value, 0, count);
				bodysize = count;
			}
		}

		return new int[] { headsize, bodysize };
	}
	
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != Raw.class) {
			return false;
		}
		Raw object = (Raw) arg;
		return match(false, value, object.value);
	}

}
