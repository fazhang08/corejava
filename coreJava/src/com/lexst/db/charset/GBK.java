/**
 *
 */
package com.lexst.db.charset;

import java.io.UnsupportedEncodingException;


public final class GBK implements SQLChar {
	private static final long serialVersionUID = 1L;

	private final static String name = "GBK";

	/**
	 *
	 */
	public GBK() {
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
			return new String(raws, GBK.name);
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
			return text.getBytes(GBK.name);
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
			return new String(text).getBytes(GBK.name);
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
		return GBK.name;
	}

}