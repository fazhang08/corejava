/**
 * 
 */
package com.lexst.security;

import java.math.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;

import javax.crypto.*;


public class SecureGenerator {
	
	static final String DES_ALGO = "DES";
	static final String DES3_ALGO = "DESede";
	static final String AES_ALGO = "AES";

	/**
	 * 
	 */
	public SecureGenerator() {
		super();
	}
	
//	public static byte[] encode(byte[] b) {
//		StringBuilder buff = new StringBuilder(b.length * 2);
//		for (int i = 0; i < b.length; i++) {
//			String s = String.format("%X", b[i] & 0xff);
//			if (s.length() == 1) buff.append('0');
//			buff.append(s);
//		}
//		return buff.toString().getBytes();
//	}
//
//	public static byte[] decode(byte[] b) {
//		byte[] res = new byte[b.length / 2];
//		for (int i = 0, n = 0; i < b.length; i += 2) {
//			int value = Integer.parseInt(new String(b, i, 2), 16) & 0xff;
//			res[n++] = (byte) value;
//		}
//		return res;
//	}

	public static RSAPublicKey buildRSAPublicKey(String modulus, String exponent) {
		try {
			KeyFactory keyFac = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(
					modulus, 16), new BigInteger(exponent, 16));
			return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
		} catch (NoSuchAlgorithmException exp) {

		} catch (InvalidKeySpecException ex) {

		}
		return null;
	}

	public static RSAPrivateKey buildRSAPrivateKey(String modulus, String exponent) {
		try {
			KeyFactory keyFac = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(
					new BigInteger(modulus, 16), new BigInteger(exponent, 16));
			return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
		} catch (NoSuchAlgorithmException exp) {

		} catch (InvalidKeySpecException ex) {

		}
		return null;
	}
	
	public static SecretKey buildAESKey(byte[] pwd) {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance(SecureGenerator.AES_ALGO);
			generator.init(128, random);
			return generator.generateKey();
		} catch (NoSuchAlgorithmException exp) {

		}
		return null;
	}
	
	public static SecretKey buildDESKey(byte[] pwd) {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance(SecureGenerator.DES_ALGO); //"DES");
			generator.init(56, random); //must be 56
			SecretKey key = generator.generateKey();
			return key;
		} catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
		}
		return null;
	}
	
	public static SecretKey buildDES3Key(byte[] pwd) {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance(SecureGenerator.DES3_ALGO); //"DESede");
			generator.init(168, random); // must be 56*3
			SecretKey key = generator.generateKey();
			return key;
		} catch (NoSuchAlgorithmException exp) {
			exp.printStackTrace();
		}
		return null;
	} 
	
	public static SecretKey buildBlowfishKey(byte[] pwd) {
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(pwd);
			KeyGenerator generator = KeyGenerator.getInstance("Blowfish");
			generator.init(64, random); //size must be multiple of 8
			return generator.generateKey();
		} catch (NoSuchAlgorithmException exp) {

		}
		return null;
	}

}