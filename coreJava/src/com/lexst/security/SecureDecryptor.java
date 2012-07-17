/**
 * 
 */
package com.lexst.security;

import java.security.*;
import java.security.interfaces.*;
import javax.crypto.*;

public class SecureDecryptor extends SecureGenerator {

	/**
	 * default constructor
	 */
	public SecureDecryptor() {
		super();
	}

	public static byte[] rsaDecrypt(RSAPrivateKey key, byte[] data) {
		byte[] raw = null;
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			raw = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return raw;
	}

	public static byte[] aesDecrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildAESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.AES_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);
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
	
	public static byte[] desDecrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildDESKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);			
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

	public static byte[] des3Decrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildDES3Key(pwd);
		try {
			Cipher cipher = Cipher.getInstance(SecureGenerator.DES3_ALGO); // "DESede");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException exp) {
			
		} catch (NoSuchPaddingException exp) {
			
		} catch (InvalidKeyException exp) {
			
		} catch (BadPaddingException exp) {
			
		} catch (IllegalBlockSizeException exp) {
			
		}
		return null;
	}

	public static byte[] blowfishDecrypt(byte[] pwd, byte[] data) {
		SecretKey key = SecureGenerator.buildBlowfishKey(pwd);
		try {
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (NoSuchAlgorithmException exp) {

		} catch (NoSuchPaddingException exp) {

		} catch (InvalidKeyException exp) {

		} catch (BadPaddingException exp) {

		} catch (IllegalBlockSizeException exp) {

		}
		return null;
	}

	public static byte[] md5Decrypt(byte[] data) {
		if (data.length <= 16) {
			return null;
		}
		byte[] raw = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(data, 16, data.length - 16);
			byte[] hash = md.digest();

			for (int i = 0; i < hash.length; i++) {
				if (hash[i] != data[i]) return null;
			}

			raw = new byte[data.length - hash.length];
			System.arraycopy(data, hash.length, raw, 0, raw.length);
		} catch (NoSuchAlgorithmException exp) {

		}
		return raw;
	}

	public static byte[] sha1Decrypt(byte[] data) {
		if (data.length <= 20) {
			return null;
		}
		byte[] raw = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(data, 20, data.length - 20);
			byte[] hash = md.digest();

			for (int i = 0; i < hash.length; i++) {
				if (hash[i] != data[i]) return null;
			}

			raw = new byte[data.length - hash.length];
			System.arraycopy(data, hash.length, raw, 0, raw.length);
		} catch (NoSuchAlgorithmException exp) {

		}
		return raw;
	}

}