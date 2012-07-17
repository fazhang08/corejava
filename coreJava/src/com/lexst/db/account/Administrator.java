/**
 *
 */
package com.lexst.db.account;

import java.io.Serializable;

public class Administrator extends SHA1User implements Serializable {

	private static final long serialVersionUID = -6886513042676909028L;

	/**
	 *
	 */
	public Administrator() {
		super();
	}

	/**
	 * @param dba
	 */
	public Administrator(Administrator dba) {
		this();
		this.set(dba);
	}

	/**
	 * @param dba
	 */
	public void set(Administrator dba) {
		super.setSHA1Username(dba.username);
		super.setSHA1Password(dba.password);
	}

	public boolean isMatch(String username, String password) {
		byte[] b1 = this.generate(username.toLowerCase());
		byte[] b2 = this.generate(password);
		return super.isMatchUsername(b1) && super.isMatchPassword(b2);
	}

}