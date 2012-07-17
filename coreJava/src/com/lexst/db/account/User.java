/**
 *
 */
package com.lexst.db.account;

import java.io.*;

public class User extends SHA1User implements Serializable {

	private static final long serialVersionUID = -4671056115993869527L;

	private long maxsize;
	
	/**
	 *
	 */
	public User() {
		super();
		maxsize = 0;
	}

	/**
	 * @param username
	 */
	public User(String username) {
		this();
		setTextUsername(username);
	}

	/**
	 * @param username
	 * @param password
	 */
	public User(String username, String password) {
		this();
		this.setTextUsername(username);
		this.setTextPassword(password);
	}

	/**
	 * user account
	 * 
	 * @param user
	 */
	public User(User user) {
		this();
		this.set(user);
	}

	/**
	 * clone user account
	 * 
	 * @param user
	 */
	public void set(User user) {
		setSHA1Username(user.username);
		setSHA1Password(user.password);
		setMaxSize(user.maxsize);
	}

	public void setMaxSize(long value) {
		this.maxsize = value;
	}

	public long getMaxSize() {
		return this.maxsize;
	}
	
	/**
	 * @param user
	 * @return
	 */
	public boolean isMatch(User user) {
		return isMatchUsername(user.username) && isMatchPassword(user.password);
	}

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean isMatch(String username, String password) {
		byte[] b1 = this.generate(username.toLowerCase());
		byte[] b2 = this.generate(password);
		return isMatchUsername(b1) && isMatchPassword(b2);
	}
}