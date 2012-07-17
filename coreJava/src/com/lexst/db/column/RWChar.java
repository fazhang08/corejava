/**
 *
 */
package com.lexst.db.column;

import java.io.*;

import com.lexst.db.*;
import com.lexst.db.field.*;
import com.lexst.util.*;

public class RWChar extends Word {

	private static final long serialVersionUID = 1L;

	private byte[] value;
	private short left, right;

	/**
	 *
	 */
	public RWChar() {
		super(Type.RWCHAR);
	}

	/**
	 * @param type
	 * @param id
	 */
	public RWChar(short id) {
		super(Type.RWCHAR, id);
	}

	/**
	 * @param id
	 * @param b
	 */
	public RWChar(short id, byte[] b) {
		this(id);
		this.setValue(b);
	}

	/**
	 * @param id
	 * @param left
	 * @param right
	 * @param b
	 */
	public RWChar(short id, short left, short right, byte[] b) {
		this(id);
		this.setLeft(left);
		this.setRight(right);
		this.setValue(b);
	}

	/**
	 * set value
	 * @param b
	 */
	public void setValue(byte[] b) {
		value = new byte[b.length];
		System.arraycopy(b, 0, value, 0, b.length);
		this.setNull(false);
		this.setHash(value);
	}

	public byte[] getValue() {
		return this.value;
	}

	public void setLeft(short i) {
		this.left = i;
	}
	public int getLeft() {
		return this.left;
	}

	public void setRight(short i) {
		this.right = i;
	}
	public short getRight() {
		return this.right;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.column.Column#bytes()
	 */
	@Override
	public byte[] bytes() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.column.Column#build(java.io.ByteArrayOutputStream, java.io.ByteArrayOutputStream)
	 */
	@Override
	public int build(ByteArrayOutputStream head, ByteArrayOutputStream body) {
		byte tag = (byte) (isNull() ? 1 : 0);
		tag <<= 6;
		tag |= (getType() & 0x3F);
		head.write(tag);

		int count = 1;
		if (!isNull()) {
			int len = (value == null ? 0 : value.length);
			byte[] b = com.lexst.util.Numeric.toBytes(len);
			head.write(b, 0, b.length);
			count += b.length;
			b = com.lexst.util.Numeric.toBytes(left);
			head.write(b, 0, b.length);
			count += b.length;
			b = com.lexst.util.Numeric.toBytes(right);
			head.write(b, 0, b.length);
			count += b.length;
			if (len > 0) {
				body.write(value, 0, value.length);
				count += len;
			}
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.column.Column#resolve(byte[], int, int)
	 */
	@Override
	public int[] resolve(byte[] b, int headoff, int bodyoff) {
		int headCount = 0, bodyCount = 0;
		// parse head
		byte tag = b[headoff++];
		headCount += 1;

		byte less = (byte) ((tag >>> 6) & 0x3);
		setNull(less == 1);
		byte type = (byte) (tag & 0x3f);
		if (!match_type(type)) {
			return null;
		}
		// if not null
		if (!isNull()) {
			int len = Numeric.toInteger(b, headoff, 4);
			headCount += 4; headoff += 4;
			this.left = Numeric.toShort(b, headoff, 2);
			headCount += 2; headoff += 2;
			this.right = Numeric.toShort(b, headoff, 2);
			headCount += 2; headoff += 2;
			if (len > 0) {
				if (bodyoff + len > b.length) {
					return null;
				}
				value = new byte[len];
				System.arraycopy(b, bodyoff, value, 0, len);
				bodyCount = len;
			}
		}
		return new int[] { headCount, bodyCount };
	}
	
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != RWChar.class) {
			return false;
		}
		if (field == null || field.getClass() != WCharField.class) {
			return false;
		}

		WCharField fid = (WCharField) field;
		RWChar col = (RWChar) arg;
		if (left == col.left && right == col.right) {
			return match(fid.isSentient(), value, col.value);
		}
		return false;
	}

}