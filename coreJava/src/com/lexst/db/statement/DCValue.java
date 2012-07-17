/**
 *
 */
package com.lexst.db.statement;

import java.io.*;

import com.lexst.util.*;

public final class DCValue implements Serializable {

	private static final long serialVersionUID = 8033820113323670539L;

	/* dc value type list */
	public static final byte RAW = 1;

	public static final byte CHAR = 2;

	public static final byte BOOLEAN = 3;

	public static final byte DOUBLE = 4;

	public static final byte INT64 = 5;

	public static final byte DATE = 6;

	public static final byte TIME = 7;

	public static final byte TIMESTAMP = 8;

	/* dc value type */
	private byte type;
	/* value naming */
	private String name;
	/* dc or adc data */
	private byte[] value;

	/**
	 *
	 */
	public DCValue() {
		super();
	}
	
	public DCValue(String name) {
		this();
		this.setName(name);
	}

	public DCValue(String name, byte[] b) {
		this(name);
		this.setValue(b);
	}

	public DCValue(String name, boolean value) {
		this(name);
		this.setValue(value);
	}

	public DCValue(String name, byte type, int value) {
		this(name);
		this.setValue(type, value);
	}

	public DCValue(String name, byte type, long value) {
		this(name);
		this.setValue(type, value);
	}

	public DCValue(String name, String value) {
		this(name);
		this.setValue(value);
	}
	
	public DCValue(String name, double value) {
		this(name);
		this.setValue(value);
	}

	public DCValue(String name, byte type, byte[] b, int off, int len) {
		this(name);
		this.setValue(type, b, off, len);
	}

	public DCValue(String name, byte type, byte[] b) {
		this(name, type, b, 0, b.length);
	}

	/**
	 * naming
	 * @param s
	 */
	public void setName(String s) {
		this.name = s;
	}
	
	public String getName() {
		return this.name;
	}

	/**
	 * @return byte
	 */
	public byte getType() {
		return this.type;
	}
	
	public boolean isRaw() {
		return type == DCValue.RAW;
	}

	public boolean isChar() {
		return type == DCValue.CHAR;
	}

	public boolean isBoolean() {
		return type == DCValue.BOOLEAN;
	}

	public boolean isDouble() {
		return type == DCValue.DOUBLE;
	}

	public boolean isLong() {
		return type == DCValue.INT64;
	}
	
	public boolean isDate() {
		return type == DCValue.DATE;
	}
	
	public boolean isTime() {
		return type == DCValue.TIME;
	}
	
	public boolean isTimeStamp() {
		return type == DCValue.TIMESTAMP;
	}
	
	/**
	 * save bytes
	 * @param t
	 * @param b
	 * @param off
	 * @param len
	 */
	public void setValue(byte t, byte[] b, int off, int len) {
		int size = len - off;
		value = new byte[size];
		System.arraycopy(b, off, value, 0, value.length);
		this.type = t;
	}

	public void setValue(byte type, byte[] b) {
		if (b != null && b.length > 0) {
			this.setValue(type, b, 0, b.length);
		}
	}

	public byte[] getValue() {
		return this.value;
	}

	/**
	 * binary value
	 * @param b
	 */
	public void setValue(byte[] b) {
		this.setValue(DCValue.RAW, b, 0, b.length);
	}

	public void setValue(boolean f) {
		byte[] b = new byte[1];
		b[0] = (byte) (f ? 1 : 0);
		this.setValue(DCValue.BOOLEAN, b);
	}

	public boolean booleanValue() {
		return (value != null && value[0] == 1);
	}

	public void setValue(byte type, long value) {
		byte[] b = Numeric.toBytes(value, true);
		this.setValue(type, b, 0, b.length);
	}

	public long longValue() {
		return Numeric.toLong(value, 0, value.length);
	}

	public long timestampValue() {
		return Numeric.toLong(value, 0, value.length);
	}
	
	public void setValue(String value) {
		if (value == null || value.length() == 0) {
			return;
		}
		byte[] b = value.getBytes();
		this.setValue(DCValue.CHAR, b, 0, b.length);
	}

	public String stringValue() {
		return new String(value, 0, value.length);
	}
	
	public void setValue(double value) {
		long num = Double.doubleToLongBits(value);
		byte[] b = Numeric.toBytes(num, true);
		this.setValue(DCValue.DOUBLE, b, 0, b.length);
	}

	public double doubleValue() {
		long num = Numeric.toLong(value, 0, value.length);
		return Double.longBitsToDouble(num);
	}

	public void setValue(byte type, int value) {
		byte[] b = Numeric.toBytes(value, true);
		this.setValue(type, b, 0, b.length);
	}

	public int dateValue() {
		return Numeric.toInteger(value, 0, value.length);
	}
	
	public int timeValue() {
		return Numeric.toInteger(value, 0, value.length);
	}
	
}