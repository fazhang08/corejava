/**
 *
 */
package com.lexst.db.charset;

import java.io.Serializable;


public class SQLCharType implements Serializable {
	private static final long serialVersionUID = 1L;

	/* character type list */
	public final static int CHAR = 1;

	public final static int NCHAR = 2;

	public final static int WCHAR = 3;

	/* user define name */
	private String name;

	/* user type */
	private int type;

	/* class name */
	private String clsname;

	/**
	 *
	 */
	public SQLCharType() {
		super();
	}

	/**
	 * @param name
	 */
	public SQLCharType(String name) {
		this();
		this.setName(name);
	}

	/**
	 * @param name
	 * @param type
	 * @param clsname
	 */
	public SQLCharType(String name, int type, String clsname) {
		this(name);
		this.setType(type);
		this.setClassName(clsname);
	}

	public void setType(int type) {
		if (SQLCharType.CHAR <= type && type <= SQLCharType.WCHAR) {
			this.type = type;
		}
	}

	public int getType() {
		return this.type;
	}

	public String toTypeString() {
		if(isChar()) return "Char";
		else if(isNChar()) return "NChar";
		else if(isWChar()) return "WChar";
		return "Undefine";
	}

	public boolean isChar() {
		return this.type == SQLCharType.CHAR;
	}

	public boolean isNChar() {
		return this.type == SQLCharType.NCHAR;
	}

	public boolean isWChar() {
		return this.type == SQLCharType.WCHAR;
	}

	public void setName(String s) {
		this.name = s;
	}

	public String getName() {
		return this.name;
	}

	public void setClassName(String s) {
		this.clsname = s;
	}

	public String getClassName() {
		return this.clsname;
	}

	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != SQLCharType.class) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		SQLCharType lm = (SQLCharType) obj;
		return name != null && name.equalsIgnoreCase(lm.name);
	}

	public int hashCode() {
		if (name == null) {
			return 0;
		}
		return name.toLowerCase().hashCode();
	}
}