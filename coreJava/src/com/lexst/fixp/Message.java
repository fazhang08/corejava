/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp message (head information)
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/13/2009
 * 
 * @see com.lexst.fixp
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp;

import java.io.*;

import com.lexst.util.*;


public final class Message {
	/* message key */
	private short key;
	/* value type */
	private byte type;
	/* message value */
	private byte[] value;

	/**
	 * @param msg
	 */
	protected Message(Message msg) {
		super();
		this.key = msg.key;
		this.type = msg.type;
		if (msg.value != null && msg.value.length > 0) {
			value = new byte[msg.value.length];
			System.arraycopy(msg.value, 0, value, 0, value.length);
		}
	}

	/**
	 *
	 */
	public Message() {
		super();
	}
	
	public Message(short key) {
		this();
		this.setKey(key);
	}

	public Message(short key, byte[] b) {
		this(key);
		this.setValue(b);
	}

	public Message(short key, boolean value) {
		this(key);
		this.setValue(value);
	}

	public Message(short key, short value) {
		this(key);
		this.setValue(value);
	}

	public Message(short key, int value) {
		this(key);
		this.setValue(value);
	}

	public Message(short key, long value) {
		this(key);
		this.setValue(value);
	}

	public Message(short key, String value) {
		this(key);
		this.setValue(value);
	}
	
	public Message(short key, float value) {
		this(key);
		this.setValue(value);
	}
	
	public Message(short key, double value) {
		this(key);
		this.setValue(value);
	}

	public Message(short key, byte type, byte[] value, int valueSize) {
		this(key);
		this.setValue(type, value, valueSize);
	}

	public Message(short key, byte type, byte[] value) {
		this(key, type, value, value.length);
	}

	/**
	 * 将消息参数转换成字节格式
	 * 注意:
	 * 1. key, valuSize只做little-endian到big-endian的转换,不做压缩
	 * 2. value如果是short, int, long类型,在设置时,已经做压缩处理
	 */
	public byte[] build() {
		if (value == null || value.length == 0) {
			return null;
		}

		int size = 4 + value.length;
		byte[] bs = new byte[size];
		byte[] b = Numeric.toBytes(key); // key, not compress
		System.arraycopy(b, 0, bs, 0, b.length);

		// 合并消息值参数类型和长度定义
		short tysz = this.type;
		tysz = (short) (((tysz & 0xF) << 12) | (value.length & 0xFFF));
		b = Numeric.toBytes(tysz);	// type and value size, not compress
		System.arraycopy(b, 0, bs, 2, b.length);

		System.arraycopy(value, 0, bs, 4, value.length);
		return bs;
	}

	/**
	 * parse a message, return message byte size
	 * @param b
	 * @param off
	 * @return
	 */
	public int resolve(byte[] b, int off) {
		if (off + 4 >= b.length) {
			throw new ArrayIndexOutOfBoundsException("packet index out!");
		}
		// message key
		this.key = Numeric.toShort(b, off, 2);
		// type and value size
		short tpsz = Numeric.toShort(b, off + 2, 2);
		// value type
		this.type = (byte) ((tpsz >>> 12) & 0xF);
		// value size
		int len = tpsz & 0xfff;
		if(off + 4 + len > b.length) {
			throw new ArrayIndexOutOfBoundsException("message value missing!");
		}
		value = new byte[len];
		System.arraycopy(b, off + 4, value, 0, len);
		// message byte size
		return 4 + len;
	}

	/**
	 * wait xx milli-second
	 * @param timeout
	 */
	protected synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {

		}
	}

	/**
	 * @param input
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	private int full(InputStream input, byte[] b, int off, int len) throws IOException {
		int count = 0, sz = 0;
		do {
			int size = input.read(b, off, len - count);
			if (size < 0) {
				sz++;
				if (sz > 10) {
					throw new IOException("read message error!");
				}
				this.delay(500);
				continue;
			}
			sz = 0;
			count += size;
			if(count >= len) break;
			off += size;
		} while(true);
		return count;
	}

	/**
	 * resolve data, return message size
	 *
	 * @param input
	 * @return
	 * @throws java.io.IOException
	 */
	public int resolve(InputStream input) throws IOException {
		byte[] b = new byte[4];
		// read message prefix
		this.full(input, b, 0, b.length);
		this.key = Numeric.toShort(b, 0, 2);
		// read type and value size, big-endian
		short tysz = Numeric.toShort(b, 2, 2);
		// message type
		this.type = (byte)((tysz >>> 12) & 0xF);
		// message value
		int len = tysz & 0xFFF;
		value = new byte[len];
		// read message value
		this.full(input, value, 0, value.length);

		return 4 + len;
	}

	/**
	 * @param key
	 */
	public void setKey(short key) {
		this.key = key;
	}
	public short getKey() {
		return this.key;
	}

	/**
	 * @return byte
	 */
	public byte getType() {
		return this.type;
	}
	
	public boolean isBinary() {
		return type == Value.BINARY;
	}

	public boolean isChar() {
		return type == Value.CHAR;
	}

	public boolean isBoolean() {
		return type == Value.BOOLEAN;
	}

	public boolean isShort() {
		return type == Value.INT16;
	}

	public boolean isInt() {
		return type == Value.INT32;
	}

	public boolean isLong() {
		return type == Value.INT64;
	}

	public boolean isFloat() {
		return type == Value.FLOAT32;
	}

	public boolean isDouble() {
		return type == Value.FLOAT64;
	}

	/**
	 * set value
	 */
	public void setValue(byte valueType, byte[] value, int valueSize) {
		if (value != null && value.length >= valueSize) {
			this.value = new byte[valueSize];
			System.arraycopy(value, 0, this.value, 0, valueSize);
			this.type = valueType;
		}
	}

	public void setValue(byte type, byte[] b) {
		if (b != null && b.length > 0) {
			this.setValue(type, b, b.length);
		}
	}

	public byte[] getValue() {
		return this.value;
	}

	public void setValue(byte[] value) {
		this.setValue(Value.BINARY, value, value.length);
	}

	public void setValue(boolean f) {
		byte[] b = new byte[1];
		b[0] = (byte)(f ? 1 : 0);
		this.setValue(Value.BOOLEAN, b);
	}
	public boolean booleanValue() {
		return (value != null && value[0] == 1);
	}

	public void setValue(short value) {
		byte[] b = Numeric.toBytes(value, true);
		this.setValue(Value.INT16, b, b.length);
	}

	public short shortValue() {
		return Numeric.toShort(value, 0, value.length);
	}

	public void setValue(int value) {
		byte[] b = Numeric.toBytes(value, true);
		this.setValue(Value.INT32, b, b.length);
	}

	public int intValue() {
		return Numeric.toInteger(value, 0, value.length);
	}

	public void setValue(long value) {
		byte[] b = Numeric.toBytes(value, true);
		this.setValue(Value.INT64, b, b.length);
	}

	public long longValue() {
		return Numeric.toLong(value, 0, value.length);
	}

	public void setValue(String value) {
		if (value == null || value.length() == 0) {
			return;
		}
		byte[] b = value.getBytes();
		this.setValue(Value.CHAR, b, b.length);
	}
	public String stringValue() {
		return new String(value, 0, value.length);
	}

	public void setValue(float value) {
		int num = Float.floatToIntBits(value);
		byte[] b = Numeric.toBytes(num, true);
		this.setValue(Value.FLOAT32, b, b.length);
	}
	
	public float floatValue() {
		int num = Numeric.toInteger(value, 0, value.length);
		return Float.intBitsToFloat(num);
	}
	
	public void setValue(double value) {
		long num = Double.doubleToLongBits(value);
		byte[] b = Numeric.toBytes(num, true);
		this.setValue(Value.FLOAT64, b, b.length);
	}

	public double doubleValue() {
		long num = Numeric.toLong(value, 0, value.length);
		return Double.longBitsToDouble(num);
	}

}