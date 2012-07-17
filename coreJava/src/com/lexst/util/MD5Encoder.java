package com.lexst.util;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Encoder {
	private final static char[] HexDigit = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static final int md5str_size = 32;

	private boolean fStop;
	private MessageDigest mdHandle;

	/**
	 * 判断一个字符串是不是一个MD5的字符流
	 *
	 * @param value
	 * @return boolean
	 */
	public static boolean isMD5Tag(String value) {
		if (value == null) return false;
		value = value.trim();
		if (value.length() != MD5Encoder.md5str_size) {
			return false; // 如果不是32个字符,错误
		}

		int index = 0;
		for (; index < value.length(); index++) {
			char word = value.charAt(index);
			if ('0' <= word && word <= '9') continue;
			else if ('A' <= word && word <= 'F') continue;
			else if ('a' <= word && word <= 'f') continue;
			else break;
		}
		return index == value.length();
	}

	/**
	 * 将一个byte类型的数转换成十六进制的ASCII表示，
	 * 因为java中的byte的toString无法实现这一点，我们又没有C语言中的 sprintf(outbuf,"%02X",ib)
	 */
	public static String byteHex(byte ib) {
		char[] words = new char[2];
		words[0] = MD5Encoder.HexDigit[(ib >>> 4) & 0x0F];
		words[1] = MD5Encoder.HexDigit[ib & 0x0F];
		return String.valueOf(words);
	}
	/**
	 * 将一串字节流转换成十六进制的ASCII表示
	 * @param bits
	 * @return
	 */
	public static String toHexString(byte[] bits) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < bits.length; i++) {
			buff.append(MD5Encoder.byteHex(bits[i]));
		}
		return buff.toString();
	}

	/**
	 * MD5 Construct method
	 */
	public MD5Encoder() {
		super();
		this.fStop = false;
		this.mdHandle = getInstance();
	}

	/**
	 * 获处一个MD5生成器句柄
	 * @return
	 */
	private MessageDigest getInstance() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
		} catch (Throwable exp) {
			exp.printStackTrace();
		}
		return null;
	}

	// 停止进行MD5编码,这个调用发生在对文件进行编码的情况下
	public synchronized void stop(boolean f) {
		this.fStop = f;
	}

	public synchronized boolean isStop() {
		return this.fStop;
	}

	/**
	 * 从文件指定位置开始编码
	 * @param file
	 * @param offset
	 * @return
	 */
	public byte[] encode(File file, long offset) {
		if (file == null || file.isDirectory() || !file.exists())
			return null;
		if (offset < 0) {
			throw new IllegalArgumentException("invalid file offset!");
		}
		this.stop(false);

		long filesize = file.length();
		if (mdHandle == null) mdHandle = this.getInstance();
		if (mdHandle == null) return null;

		byte[] data = new byte[102400];
		try {
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			if (offset > 0) reader.seek(offset);
			for (long count = offset; count < filesize;) {
				if (isStop()) break; //如果中止就退出.适合于大容量的文件
				int size = reader.read(data, 0, data.length);
				if (size > 0) {
					mdHandle.update(data, 0, size);
					count += size;
				}
			}
			reader.close();
		} catch (IOException exp) {
			exp.printStackTrace();
			return null;
		} catch (Throwable exp) {
			exp.printStackTrace();
			return null;
		}

		//生成MD5编码
		return mdHandle.digest();
	}

	/**
	 * 对文件进行MD5编码处理
	 * @param file
	 * @return
	 */
	public String encode(File file) {
		byte[] bits = this.encode(file, 0L);
		if (bits == null) return null;
		return MD5Encoder.toHexString(bits);
	}

	/**
	 * 对多行字符串进行MD5编码
	 * @param data
	 * @return
	 */
	public String encode(String[] data) {
		if (mdHandle == null) mdHandle = getInstance();
		if (mdHandle == null) return null;
		if (data == null || data.length == 0) return null;

		int count = 0;
		for (String line : data) {
			if (line != null && line.length() > 0) {
				mdHandle.update(line.getBytes());
				count++;
			}
		}
		if (count == 0) return null;
		return MD5Encoder.toHexString(mdHandle.digest());
	}

	/**
	 * 对一行字符串进行MD5编码
	 * @param data
	 * @return
	 */
	public String encode(String data) {
		if (mdHandle == null) mdHandle = getInstance();
		if (mdHandle == null) return null;
		if (data == null || data.length() == 0) return null;
		mdHandle.update(data.getBytes());
		return MD5Encoder.toHexString(mdHandle.digest());
	}

	/**
	 * 对字节流进行MD5编码
	 * @param data
	 * @return
	 */
	public byte[] encode(byte[] data) {
		if (mdHandle == null) mdHandle = getInstance();
		if (mdHandle == null) return null;
		if (data == null || data.length == 0) return null;
		mdHandle.update(data);
		return mdHandle.digest();
	}

	/**
	 * @param data
	 * @param offset
	 * @param length (长度是从下标点开始计算,长度值必须<=data.length)
	 * @return
	 */
	public BigInteger encode(byte[] data, int offset, int length) {
		if (mdHandle == null) mdHandle = getInstance();
		if (mdHandle == null) return null;

		mdHandle.update(data, offset, length);
		byte[] bits = mdHandle.digest();
		String hex = MD5Encoder.toHexString(bits);
		return new BigInteger(hex, 16);
	}

	/**
	 * 生成一个MD5串号,这个串号必须是正整数
	 * @param data
	 * @return
	 */
	public BigInteger serial(byte[] data) {
		if (mdHandle == null) mdHandle = getInstance();
		if (mdHandle == null) return null;
		if (data == null || data.length == 0) return null;

		return this.encode(data, 0, data.length);
	}

}