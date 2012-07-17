/**
 *
 */
package com.lexst.db.charset;

import java.io.Serializable;

public interface SQLChar extends Serializable {

	/**
	 * get sql character type
	 * @return
	 */
	int getType();

	/**
	 * get language name
	 * @return
	 */
	String getName();

	/**
	 * java bytes to bytes
	 * @param b
	 * @return
	 */
	byte[] encode(byte[] b);

	/**
	 * java string to bytes
	 * @param text
	 * @return
	 */
	byte[] encode(String text);

	/**
	 * bytes to java string
	 * @param raws
	 * @return
	 */
	String decode(byte[] raws);
}