/**
 *
 */
package com.lexst.db.charset;

import java.io.*;

/**
 *
 * UTF-16 big-endian code
 */
public final class UTF16 implements SQLChar {
	private static final long serialVersionUID = 1L;

	private final static String name = "UTF-16BE";

	/**
	 *
	 */
	public UTF16() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.charset.SQLChar#getType()
	 */
	public int getType() {
		return SQLCharType.NCHAR;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Charset#decode(byte[])
	 */
	@Override
	public String decode(byte[] b) {
		try {
			if (b != null && b.length > 0) {
				return new String(b, UTF16.name);
			}
		} catch (UnsupportedEncodingException exp) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Charset#encode(java.lang.String)
	 */
	@Override
	public byte[] encode(String s) {
		try {
			if (s != null) {
				return s.getBytes(UTF16.name);
			}
		} catch (UnsupportedEncodingException exp) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Charset#encode(byte[])
	 */
	@Override
	public byte[] encode(byte[] b) {
		try {
			if (b != null && b.length > 0) {
				return new String(b).getBytes(UTF16.name);
			}
		} catch (UnsupportedEncodingException exp) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Charset#getName()
	 */
	@Override
	public String getName() {
		return UTF16.name;
	}

}