/**
 * 
 */
package com.lexst.security;

import java.io.*;
import java.security.*;
import java.security.interfaces.*;
import javax.crypto.*;

public class SecureEncryptor extends SecureGenerator {

	/**
	 * default constructor
	 */
	public SecureEncryptor() {
		super();
	}

	public static byte[] rsaEncrypt(RSAPublicKey key, byte[] data) {
		byte[] raw = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			raw = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException exp) {

		} catch (NoSuchPaddingException exp) {

		} catch (InvalidKeyException exp) {

		} catch (IllegalBlockSizeException exp) {

		} catch (BadPaddingException exp) {

		}
		return raw;
	}

	public static byte[] aesEncrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildAESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.AES_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
		} catch (NoSuchPaddingException exp) {
			exp.printStackTrace();
		} catch (InvalidKeyException exp) {
			exp.printStackTrace();
		} catch (BadPaddingException exp) {
			exp.printStackTrace();
		} catch (IllegalBlockSizeException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public static byte[] desEncrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildDESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			ByteArrayOutputStream buff = new ByteArrayOutputStream(data.length - data.length % 8 + 8);
			CipherOutputStream cos = new CipherOutputStream(buff, cipher);
			cos.write(data, 0, data.length);
			cos.flush();
			cos.close();
			return buff.toByteArray();
		} catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
		} catch (NoSuchPaddingException exp) {
			exp.printStackTrace();
		} catch (InvalidKeyException exp) {
			exp.printStackTrace();
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public static byte[] des3Encrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildDES3Key(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES3_ALGO); // "DESede");
			cipher.init(Cipher.ENCRYPT_MODE, key);

			ByteArrayOutputStream buff = new ByteArrayOutputStream(data.length - data.length % 8 + 8);
			CipherOutputStream cos = new CipherOutputStream(buff, cipher);
			cos.write(data, 0, data.length);
			cos.flush();
			cos.close();
			return buff.toByteArray();
		} catch (NoSuchAlgorithmException exp) {
			
		} catch (NoSuchPaddingException exp) {
			
		} catch (InvalidKeyException exp) {
			
		} catch (IOException exp) {
			
		}
		return null;
	}
	
	public static byte[] blowfishEncrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildBlowfishKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException exp) {

		} catch (NoSuchPaddingException exp) {

		} catch (InvalidKeyException exp) {

		} catch (BadPaddingException exp) {

		} catch (IllegalBlockSizeException exp) {

		}
		return null;
	}
	                 

	public static byte[] md5Encrypt(byte[] data) {
		byte[] raw = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data);
			byte[] hash = md.digest();

			raw = new byte[hash.length + data.length];
			System.arraycopy(hash, 0, raw, 0, hash.length);
			System.arraycopy(data, 0, raw, hash.length, data.length);
		} catch (NoSuchAlgorithmException exp) {

		}
		return raw;
	}

	public static byte[] sha1Encrypt(byte[] data) {
		byte[] raw = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data);
			byte[] hash = md.digest();

			raw = new byte[hash.length + data.length];
			System.arraycopy(hash, 0, raw, 0, hash.length);
			System.arraycopy(data, 0, raw, hash.length, data.length);
		} catch (NoSuchAlgorithmException exp) {

		}
		return raw;
	}

//	public static void main(String[] args) {
//		Provider[] ps = Security.getProviders();
//		for(int i = 0; i < ps.length; i++) {
//			System.out.printf("provider name:%s\n", ps[i].getName());
//		}
//		
//		byte[] pwd = "www.lexst.com".getBytes();
//		
////		StringBuilder sb = new StringBuilder();
////		for(int i = 0; i < 4600; i++) {
////			sb.append('a');
////		}
////		byte[] data = sb.toString().getBytes();
////		for(int i = 0; i <data.length; i++) data[i] = (byte)65;
//		
//		byte[] data = "UnixSystem+Pentium@Lexst/SERVER".getBytes();
//		
//		byte[] raw = SecureEncryptor.desEncrypt(pwd, data);
//		System.out.printf("origin data size: %d\n", data.length);
//		System.out.printf("encrypt des3 raw size:%d, [%s]\n", raw.length, new String(raw));
//		
//		byte[] b = SecureDecryptor.desDecrypt(pwd, raw);
//		System.out.printf("decrypt string:%s\n", new String(b));
//		
////		raw = SecureEncryptor.md5Encrypt(data);
////		System.out.printf("data size:%d, md5 hash size:%d\n", data.length, raw.length);
////		
////		raw = SecureEncryptor.sha1Encrypt(data);
////		System.out.printf("data size:%d, sha1 hash size:%d", data.length, raw.length);
//	}


//	public static void main(String[] args) {
//		byte[] pwd = "www.lexst.com".getBytes();
//		byte[] data = "UNIX-SERVER".getBytes();
//
//		byte[] raw = SecureEncryptor.blowfishEncrypt(pwd, data);
//		System.out.printf("origin data size: %d\n", data.length);
//		System.out.printf("encrypt blowfish raw size:%d, String:%s\n", raw.length, new String(raw));
//		
//		byte[] b = SecureDecryptor.blowfishDecrypt(pwd, raw);
//		System.out.printf("decrypt string:%s\n", new String(b));
//	}

	public static void main(String[] args) {
		byte[] pwd = "www.lexst.com".getBytes();
		byte[] data = "LEXST".getBytes();

		byte[] raw = SecureEncryptor.aesEncrypt(pwd, data);
		System.out.printf("origin data size: %d\n", data.length);
		System.out.printf("encrypt aes raw size:%d, String:%s\n", raw.length, new String(raw));
		
		byte[] b = SecureDecryptor.aesDecrypt(pwd, raw);
		System.out.printf("decrypt string:%s\n", new String(b));
	}
}