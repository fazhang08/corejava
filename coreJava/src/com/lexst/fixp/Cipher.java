/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 lexst.com. All rights reserved
 * 
 * fixp command (request and response, head information)
 * 
 * @author scott.jian
 * 
 * @version 1.0 10/7/2011
 * 
 * @see com.lexst.fixp
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp;

import java.io.Serializable;

import com.lexst.security.*;

public class Cipher implements Serializable {

	private static final long serialVersionUID = 5837917332244488191L;

	/* algorithm name list */
	public static final int DES = 1;

	public static final int DES3 = 2;
	
	public static final int AES = 3;
	
	public static final int BLOWFISH = 4;

	public static final int MD5 = 5;

	public static final int SHA1 = 6;

	/* algorithm name */
	private int algo;

	/* password */
	private byte[] pwd;

	/* cipher hashcode */
	private int hash;
	
	/**
	 * 
	 */
	public Cipher() {
		super();
		algo = 0;
		hash = 0;
	}
	
	/**
	 * @param algo
	 * @param pwd
	 */
	public Cipher(int algo, byte[] pwd) {
		this();
		this.set(algo, pwd);
	}
	
	/**
	 * @param algo
	 * @param pwd
	 */
	public Cipher(String algo, byte[] pwd) {
		this();
		int num = Cipher.translate(algo);
		if (num == -1) {
			throw new IllegalArgumentException("invalid algorithm " + algo);
		}
		this.set(num, pwd);
	}

	/**
	 * @param cipher
	 */
	public Cipher(Cipher cipher) {
		this();
		this.set(cipher.algo, cipher.pwd);
		this.hash = cipher.hash;
	}

	/**
	 * @param algo
	 * @param pwd
	 */
	public void set(int algo, byte[] pwd) {
		this.setAlgorithm(algo);
		this.setPassword(pwd);
	}

	public void setAlgorithm(int value) {
		if (!(Cipher.DES <= value && value <= Cipher.SHA1)) {
			throw new IllegalArgumentException("invalid algorithm value: " + value);
		}
		this.algo = value;
	}

	public int getAlgorithm() {
		return this.algo;
	}
	
	public String getAlgorithmText() {
		return Cipher.translate(algo);
	}
	
	/**
	 * @param name
	 * @return
	 */
	public static String translate(int name) {
		switch (name) {
		case Cipher.DES:
			return "DES";
		case Cipher.DES3:
			return "DES3";
		case Cipher.AES:
			return "AES";
		case Cipher.BLOWFISH:
			return "Blowfish";
		case Cipher.MD5:
			return "MD5";
		case Cipher.SHA1:
			return "SHA1";
		}
		return null;
	}

	/**
	 * translate to number 
	 * @param name
	 * @return
	 */
	public static int translate(String name) {
		if ("DES".equalsIgnoreCase(name)) {
			return Cipher.DES;
		} else if ("DES3".equalsIgnoreCase(name) || "3DES".equalsIgnoreCase(name)) {
			return Cipher.DES3;
		} else if("AES".equalsIgnoreCase(name)) {
			return Cipher.AES;
		} else if("Blowfish".equalsIgnoreCase(name)) {
			return Cipher.BLOWFISH;
		} else if ("MD5".equalsIgnoreCase(name)) {
			return Cipher.MD5;
		} else if ("SHA1".equalsIgnoreCase(name)) {
			return Cipher.SHA1;
		}
		return -1;
	}

	public void setPassword(byte[] b) {
		pwd = new byte[b.length];
		System.arraycopy(b, 0, pwd, 0, b.length);
	}

	public byte[] getPassword() {
		return pwd;
	}
	
	/**
	 * encrypt data
	 * @param data
	 * @return
	 */
	public byte[] encrypt(byte[] data) {
		byte[] raws = null;
		switch (this.algo) {
		case Cipher.DES:
			raws = SecureEncryptor.desEncrypt(pwd, data);
			break;
		case Cipher.DES3:
			raws = SecureEncryptor.des3Encrypt(pwd, data);
			break;
		case Cipher.AES:
			raws = SecureEncryptor.aesEncrypt(pwd, data);
			break;
		case Cipher.BLOWFISH:
			raws = SecureEncryptor.blowfishEncrypt(pwd, data);
			break;
		case Cipher.MD5:
			raws = SecureEncryptor.md5Encrypt(data);
			break;
		case Cipher.SHA1:
			raws = SecureEncryptor.sha1Encrypt(data);
			break;
		}
		return raws;
	}

	/**
	 * decrypt data
	 * @param data
	 * @return
	 */
	public byte[] decrypt(byte[] data) {
		byte[] raws = null;
		switch (this.algo) {
		case Cipher.DES:
			raws = SecureDecryptor.desDecrypt(pwd, data);
			break;
		case Cipher.DES3:
			raws = SecureDecryptor.des3Decrypt(pwd, data);
			break;
		case Cipher.AES:
			raws = SecureDecryptor.aesDecrypt(pwd, data);
			break;
		case Cipher.BLOWFISH:
			raws = SecureDecryptor.blowfishDecrypt(pwd, data);
			break;
		case Cipher.MD5:
			raws = SecureDecryptor.md5Decrypt(data);
			break;
		case Cipher.SHA1:
			raws = SecureDecryptor.sha1Decrypt(data);
			break;
		}
		return raws;
	}

	@Override
	public boolean equals(Object arg) {
		if (arg == null || !(arg instanceof Cipher)) {
			return false;
		} else if (arg == this) {
			return true;
		}

		Cipher c = (Cipher) arg;

		if (algo != c.algo) return false;
		
		if (pwd != null && c.pwd != null && pwd.length == c.pwd.length) {
			for (int i = 0; i < pwd.length; i++) {
				if (pwd[i] != c.pwd[i]) return false;
			}
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		if (hash == 0) {
			if(algo == 0) return 0;
			if (pwd == null || pwd.length == 0) return 0;
			int count = 0;
			for (int i = 0; i < pwd.length; i++) {
				count += (pwd[i] & 0xff);
			}
			hash = algo ^ count;
		}
		return hash;
	}

}