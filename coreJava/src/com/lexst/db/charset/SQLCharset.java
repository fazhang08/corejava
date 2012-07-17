/**
 *
 */
package com.lexst.db.charset;

import java.io.*;

public class SQLCharset implements Serializable {
	private static final long serialVersionUID = 1L;

	/* single char (1 word) */
	private SQLChar sql_char;
	/* narrow char (2 word) */
	private SQLChar sql_nchar;
	/* wide char (4 word) */
	private SQLChar sql_wchar;

	/**
	 *
	 */
	public SQLCharset() {
		super();
	}

	public void setChar(SQLChar s) {
		this.sql_char = s;
	}

	public SQLChar getChar() {
		return this.sql_char;
	}

	public void setNChar(SQLChar s) {
		this.sql_nchar = s;
	}

	public SQLChar getNChar() {
		return this.sql_nchar;
	}

	public void setWChar(SQLChar s) {
		this.sql_wchar = s;
	}

	public SQLChar getWChar() {
		return this.sql_wchar;
	}
}