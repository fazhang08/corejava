package com.lexst.util;

/**
 * short, int, long to byte, and byte to short,int,long
 * 
 * @author lexst.com
 *
 */
public final class Digit {

	public Digit() {
		super();
	}

	/**
	 * long to byte array, big engial
	 * @param value
	 * @param num
	 * @return
	 */
	public static byte[] toBytes(long value, int num) {
		byte[] b = new byte[num];
		for (int i = 0, offset = (num - 1) * 8; offset >= 0; offset -= 8) {
			b[i++] = (byte) ((value >>> offset) & 0xFFL);
		}
		return b;
	}

	/**
	 * short to byte array
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(short value) {
		return Digit.toBytes(value, 2);
	}
	/**
	 * int to byte array
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(int value) {
		return Digit.toBytes(value, 4);
	}
	/**
	 * long to byte array
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(long value) {
		return Digit.toBytes(value, 8);
	}
	
	/**
	 * @param b
	 * @param off	
	 * @param limitSize
	 * @return
	 */
	public static long toValue(byte[] b, int off, int limitSize) {
		if (b == null || b.length == 0) {
			throw new NullPointerException("byte is null!");
		}
		if (off + limitSize > b.length) {
			throw new IllegalArgumentException("byte space missing!");
		}
		long value = 0L, v = 0L;
		for (int i = off + limitSize - 1, shift = 0; i >= off; i--, shift += 8) {
			v = b[i] & 0xFFL; 
			v <<= shift;
			value |= v;
		}
		return value;
	}

	/**
	 * byte array to short
	 * @param b
	 * @return
	 */
	public static short toShort(byte[] b) {
		return (short)Digit.toValue(b, 0, 2);
	}
	public static short toShort(byte[] b, int off) {
		return (short)Digit.toValue(b, off, 2);
	}
	public static short toShort(byte[] b, int off, int len) {
		return (short) Digit.toValue(b, off, (len > 2 ? 2 : len));
	}

	/**
	 * byte array to int
	 * @param b
	 * @return
	 */
	public static int toInteger(byte[] b) {
		return (int)Digit.toValue(b, 0, 4);
	}
	public static int toInteger(byte[] b, int off) {
		return (int)Digit.toValue(b, off, 4);
	}
	public static int toInteger(byte[] b, int off, int len) {
		return (int) Digit.toValue(b, off, (len > 4 ? 4 : len));
	}
	
	/**
	 * byte array to long
	 * @param b
	 * @return long
	 */
	public static long toLong(byte[] b) {
		return Digit.toValue(b, 0, 8);
	}
	public static long toLong(byte[] b, int offset) {
		return Digit.toValue(b, offset, 8);
	}
	public static long toLong(byte[] b, int offset, int len) {
		return Digit.toValue(b, offset, (len > 8 ? 8 : len));
	}

}