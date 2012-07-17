/**
 *
 */
package com.lexst.db.schema;

import java.io.*;

public class Layout implements Serializable {
	private static final long serialVersionUID = -5380910180647710514L;

	public final static byte ASC = 1;
	public final static byte DESC = 2;

	private short columnId;
	private byte type;

	private Layout next;

	/**
	 *
	 */
	public Layout() {
		this.columnId = 0;
		this.type = 0;
	}

	/**
	 *
	 * @param columnId
	 * @param type
	 */
	public Layout(short columnId, byte type) {
		this();
		this.set(columnId, type);
	}

	/**
	 * @param s
	 */
	public Layout(Layout s) {
		this();
		this.columnId = s.columnId;
		this.type = s.type;
		if (s.next != null) {
			this.next = new Layout(s.next);
		}
	}
	
	public void set(short columnId, byte type) {
		if (type != Layout.ASC && type != Layout.DESC) {
			throw new IllegalArgumentException("layout not match!");
		}
		this.columnId = columnId;
		this.type = type;
	}

	public short getColumnId() {
		return this.columnId;
	}

	public byte getType() {
		return type;
	}
	
	public boolean isASC() {
		return type == Layout.ASC;
	}

	public boolean isDESC() {
		return type == Layout.DESC;
	}
	
	public static boolean isASC(byte type) {
		return type == Layout.ASC;
	}
	
	public static boolean isDESC(byte type) {
		return type == Layout.DESC;
	}

	public Layout clone() {
		return new Layout(this);
	}

	public void setLast(Layout object) {
		if (this.next == null) {
			this.next = object;
		} else {
			this.next.setLast(object);
		}
	}

	public Layout getLast() {
		if(next != null) {
			return next.getLast();
		}
		return this;
	}

	public Layout getNext() {
		return this.next;
	}

	public static byte parse(String s) {
		if("ASC".equalsIgnoreCase(s)) {
			return Layout.ASC;
		} else if("DESC".equals(s)) {
			return Layout.DESC;
		}
		return 0;
	}

}