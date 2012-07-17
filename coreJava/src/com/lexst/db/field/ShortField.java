/**
 *
 */
package com.lexst.db.field;

import java.io.*;

import com.lexst.db.*;
import com.lexst.util.*;

public class ShortField extends Field {
	// default serial number
	private static final long serialVersionUID = 1L;
	// default value
	private short value;

	/**
	 *
	 */
	public ShortField() {
		super(Type.SHORT);
	}
	
	/**
	 * @param field
	 */
	public ShortField(ShortField field) {
		super(field);
		this.value = field.value;
	}

	/**
	 *
	 * @param defValue
	 */
	public ShortField(short defValue) {
		this();
		this.setValue(defValue);
	}

	/**
	 *
	 * @param columnId
	 * @param name
	 * @param value
	 */
	public ShortField(short columnId, String name, short value) {
		super(Type.SHORT, columnId, name);
		this.setValue(value);
	}

	/**
	 * set default value
	 * @param num
	 */
	public void setValue(short num) {
		this.value = num;
		this.setNullable(false);
	}

	public short getValue() {
		return this.value;
	}

	@Override
	public com.lexst.db.column.Column getDefault(short columnId) {
		if (allowNull) {
			return new com.lexst.db.column.Short(columnId);
		}
		if (!nullable) {
			return new com.lexst.db.column.Short(columnId, value);
		}
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
		buf.write(dataType);		// data type
		byte[] b = Numeric.toBytes(columnId);
		buf.write(b, 0, b.length);	// column id

		byte[] bs = name.getBytes();
		byte sz = (byte) (bs.length & 0xFF);
		buf.write(sz);					// name size

		buf.write(bs, 0, bs.length);	// name byte

		buf.write(indexType);			// index type
		byte tag = (byte) (allowNull ? 1 : 0);
		buf.write(tag);				// allow null

		b = Numeric.toBytes(value);
		buf.write(b, 0, b.length);		//default value

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

		value = Numeric.toShort(b, off, 2);
		off += 2;

		return off - offset;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.field.Field#fresh()
	 */
	public Field fresh() {
		return new ShortField(this);
	}
}