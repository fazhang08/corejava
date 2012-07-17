/**
 * 
 */
package com.lexst.db.account;

import java.io.*;
import java.security.*;


public class SHA1User implements Serializable {

	private static final long serialVersionUID = -4642694079532595946L;

	/* sha1 username */
	protected byte[] username;
	
	/* sha1 password */
	protected byte[] password;

	/* hash value */
	private int hash;
	
	/**
	 * 
	 */
	public SHA1User() {
		super();
		hash = 0;
	}

	/**
	 * SHA1 username
	 * @return
	 */
	public byte[] getUsername() {
		return this.username;
	}
	
	/**
	 * SHA1 password
	 * @return
	 */
	public byte[] getPassword() {
		return password;
	}

	/**
	 * generate sha1 code
	 * @param username
	 * @param data
	 * @return
	 */
	protected byte[] generate(String data) {
		byte[] value = data.getBytes();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(value);
			return md.digest();
		} catch (NoSuchAlgorithmException exp) {

		}
		return null;
	}

	/**
	 * check username
	 * @param b
	 * @return
	 */
	public boolean isMatchUsername(byte[] b) {
		if (b != null && username != null && b.length == username.length) {
			for (int i = 0; i < b.length; i++) {
				if (b[i] != username[i]) return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @param b
	 * @return
	 */
	public boolean isMatchPassword(byte[] b) {
		if (b != null && password != null && b.length == password.length) {
			for (int i = 0; i < b.length; i++) {
				if (b[i] != password[i]) return false;
			}
			return true;
		}
		return false;
	}
	
	private String getHexString(byte[] bytes) {
		StringBuilder buff = new StringBuilder();
		for (int i = 0; bytes != null && i < bytes.length; i++) {
			String s = String.format("%X", bytes[i] & 0xff);
			if (s.length() == 1) s = "0" + s;
			buff.append(s);
		}
		return buff.toString();
	}

	/**
	 * get username (hex style)
	 * @return
	 */
	public String getHexUsername() {
		return getHexString(username);
	}

	/**
	 * set username (palin text)
	 * @param text
	 */
	public void setTextUsername(String text) {
		byte[] b = generate(text.toLowerCase());
		setSHA1Username(b);
	}

	/**
	 * set username (hex style)
	 * @param hex
	 */
	public void setHexUsername(String hex) {
		if (hex == null || hex.length() != 40) {
			throw new IllegalArgumentException("invalid sha1 username");
		}

		username = new byte[20];
		for (int i = 0, n = 0; i < hex.length(); i += 2) {
			String s = hex.substring(i, i + 2);
			username[n++] = (byte) Integer.parseInt(s, 16);
		}
	}
	
	/**
	 * set username (20 bytes)
	 * @param b
	 */
	public void setSHA1Username(byte[] b) {
		if (b == null || b.length == 0) {
			username = null;
			return;
		}
		if (b.length != 20) {
			throw new IllegalArgumentException("invalid sha1 username");
		}
		this.username = new byte[b.length];
		System.arraycopy(b, 0, username, 0, b.length);
	}

	/**
	 * set password (plain text)
	 * @param text
	 */
	public void setTextPassword(String text) {
		byte[] b = generate(text);
		setSHA1Password(b);
	}

	public String getHexPassword() {
		return getHexString(password);
	}

	/**
	 * set password (hex string)
	 * @param hex
	 */
	public void setHexPassword(String hex) {
		if (hex == null || hex.length() != 40) {
			throw new IllegalArgumentException("invalid sha1 password");
		}

		password = new byte[20];
		for (int i = 0, n = 0; i < hex.length(); i += 2) {
			String s = hex.substring(i, i + 2);
			password[n++] = (byte) Integer.parseInt(s, 16);
		}
	}
	
	/**
	 * set password (20 bytes)
	 * @param b
	 */
	public void setSHA1Password(byte[] b) {
		if (b == null || b.length == 0) {
			password = null;
			return;
		}
		if (b.length != 20) {
			throw new IllegalArgumentException("invalid sha1 password");
		}
		this.password = new byte[b.length];
		System.arraycopy(b, 0, password, 0, b.length);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if (arg == null || !(arg instanceof SHA1User)) {
			return false;
		} else if (arg == this) {
			return true;
		}
		SHA1User s = (SHA1User) arg;
		return isMatchUsername(s.username) && isMatchPassword(s.password);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (hash == 0) {
			for (int i = 0; username != null && i < username.length; i++) {
				if (i == 0) hash = username[i] & 0xff;
				else hash ^= username[i] & 0xff;
			}
			for (int i = 0; password != null && i < password.length; i++) {
				hash ^= password[i] & 0xff;
			}
		}
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return String.format("%s:%s", getHexUsername(), getHexPassword());
	}

}