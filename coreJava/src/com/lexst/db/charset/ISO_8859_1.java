/**
 *
 */
package com.lexst.db.charset;

import java.io.UnsupportedEncodingException;

public class ISO_8859_1 implements SQLChar {
	private static final long serialVersionUID = 1L;

	private final static String name = "ISO-8859-1";

	/**
	 *
	 */
	public ISO_8859_1() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.charset.SQLChar#getType()
	 */
	public int getType() {
		return SQLCharType.CHAR;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Char#decode(byte[])
	 */
	@Override
	public String decode(byte[] raws) {
		try {
			return new String(raws, ISO_8859_1.name);
		} catch (UnsupportedEncodingException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Char#encode(java.lang.String)
	 */
	@Override
	public byte[] encode(String text) {
		try {
			return text.getBytes(ISO_8859_1.name);
		} catch (UnsupportedEncodingException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.charset.Charset#encode(byte[])
	 */
	@Override
	public byte[] encode(byte[] text) {
		try {
			return new String(text).getBytes(ISO_8859_1.name);
		} catch (UnsupportedEncodingException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Char#getName()
	 */
	@Override
	public String getName() {
		return ISO_8859_1.name;
	}

}