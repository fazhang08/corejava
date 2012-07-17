/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author zhicheng.liang lexst@126.com
 * @version 1.0 11/29/2010
 * @see com.lexst.util
 * @license GNU Lesser General Public License (LGPL)
 */

package com.lexst.util;

/**
 * short, int, long to byte, and byte to short,int,long
 *
 */
public final class Numeric {

	/**
	 * filte 0 byte
	 * @param value
	 * @return
	 */
	private static byte[] compress(byte[] value) {
		int end = value.length - 1;
		for (; end >= 0; end--) {
			if (value[end] != 0) break;
		}
		if (end < 0) end = 0;
		byte[] b = new byte[end + 1];
		System.arraycopy(value, 0, b, 0, b.length);
		return b;
	}

	/* short value */
	
	public static byte[] toBytes(short value) {
		byte[] b = new byte[2];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) ((value >>> 8) & 0xff);
		return b;
	}
	
	/**
	 * @param value
	 * @param compress
	 * @return
	 */
	public static byte[] toBytes(short value, boolean compress) {
		byte[] b = toBytes(value);
		if (compress) {
			return Numeric.compress(b);
		}
		return b;
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public static short toShort(byte[] b, int off, int len) {
		int end = off + len;
		short value = (short) (b[off++] & 0xff);
		if (off < end) value |= ((b[off++] & 0xff) << 8);
		return value;
	}

	/**
	 * byte array to short
	 * @param b
	 * @return
	 */
	public static short toShort(byte[] b) {
		int len = (b.length < 2 ? b.length : 2);
		return Numeric.toShort(b, 0, len);
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static short toShort(byte[] b, int off) {
		int len = (b.length - off < 2 ? b.length - off : 2);
		return Numeric.toShort(b, off, len);
	}

	/* int value */
	
	/**
	 * change to bytes (little-endian)
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(int value) {
		byte[] b = new byte[4];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) ((value >>> 8) & 0xff);
		b[2] = (byte) ((value >>> 16) & 0xff);
		b[3] = (byte) ((value >>> 24) & 0xff);
		return b;
	}

	/**
	 * int to byte array
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(int value, boolean compress) {
		byte[] b = toBytes(value);
		if (compress) {
			return Numeric.compress(b);
		}
		return b;
	}

	/**
	 * change to int (little-endian)
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public static int toInteger(byte[] b, int off, int len) {
		int end = off + len;
		int value = b[off++] & 0xff;
		if (off < end) value |= ((b[off++] & 0xff) << 8);
		if (off < end) value |= ((b[off++] & 0xff) << 16);
		if (off < end) value |= ((b[off++] & 0xff) << 24);
		return value;
	}

	/**
	 * byte array to int
	 * @param b
	 * @return
	 */
	public static int toInteger(byte[] b) {
		int len = (b.length < 4 ? b.length : 4);
		return Numeric.toInteger(b, 0, len);
	}

	/**
	 * @param b
	 * @param off
	 * @return
	 */
	public static int toInteger(byte[] b, int off) {
		int len = (b.length - off < 4 ? b.length - off : 4);
		return Numeric.toInteger(b, off, len);
	}

	/** long value **/

	/**
	 * change to bytes (little-endian)
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(long value) {
		byte[] b = new byte[8];
		b[0] = (byte) (value & 0xff);
		b[1] = (byte) ((value >>> 8) & 0xff);
		b[2] = (byte) ((value >>> 16) & 0xff);
		b[3] = (byte) ((value >>> 24) & 0xff);
		b[4] = (byte) ((value >>> 32) & 0xff);
		b[5] = (byte) ((value >>> 40) & 0xff);
		b[6] = (byte) ((value >>> 48) & 0xff);
		b[7] = (byte) ((value >>> 56) & 0xff);
		return b;
	}

	/**
	 * long to byte array
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(long value, boolean compress) {
		byte[] b = toBytes(value);
		if (compress) {
			return Numeric.compress(b);
		}
		return b;
	}

	/**
	 *
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 */
	public static long toLong(byte[] b, int off, int len) {
		int end = off + len;
		long value = b[off++] & 0xff;
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 8);
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 16);
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 24);
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 32);
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 40);
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 48);
		if (off < end) value |= ((long) ((b[off++] & 0xff)) << 56);
		return value;
	}

	/**
	 * byte to long
	 * @param b
	 * @return
	 */
	public static long toLong(byte[] b) {
		int len = (b.length < 8 ? b.length : 8);
		return Numeric.toLong(b, 0, len);
	}

	/**
	 * byte to long
	 * @param b
	 * @param off
	 * @return
	 */
	public static long toLong(byte[] b, int off) {
		int len = (b.length - off < 8 ? b.length - off : 8);
		return Numeric.toLong(b, off, len);
	}

}