/**
 *
 */
package com.lexst.db.charset;

import java.io.*;

public final class UTF32 implements SQLChar {
	private static final long serialVersionUID = 1L;

	private final static String name = "UTF-32";

	/**
	 * default constricator
	 */
	public UTF32() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.charset.SQLChar#getType()
	 */
	public int getType() {
		return SQLCharType.WCHAR;
	}

	/* (non-Javadoc)
	 * @see com.lexst.charset.Charset#decode(byte[])
	 */
	@Override
	public String decode(byte[] b) {
		try {
			if (b != null && b.length > 0) {
				return new String(b, UTF32.name);
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
			if (s != null && s.length() > 0) {
				return s.getBytes(UTF32.name);
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
				return new String(b).getBytes(UTF32.name);
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
		return UTF32.name;
	}
}