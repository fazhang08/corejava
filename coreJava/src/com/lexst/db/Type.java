/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * data type define
 * 
 * @author lei.zhang lexst@126.com
 * 
 * @version 1.0 5/7/2009
 * @see com.lexst.db
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db;

public class Type {
		
	/* data type (c, java) */
	public final static byte RAW = 1;
	public final static byte CHAR = 2;
	public final static byte RCHAR = 3;
	public final static byte NCHAR = 4;
	public final static byte RNCHAR = 5;
	public final static byte WCHAR = 6;
	public final static byte RWCHAR = 7;
	public final static byte SHORT = 8;
	public final static byte INTEGER = 9;
	public final static byte LONG = 10;
	public final static byte REAL = 11;
	public final static byte DOUBLE = 12;
	public final static byte DATE = 13;
	public final static byte TIME = 14;
	public final static byte TIMESTAMP = 15;

	/* field identity */
	public final static byte NONE_INDEX = 0;
	public final static byte PRIME_INDEX = 1;
	public final static byte SLAVE_INDEX = 2;

	/* index column identity */
	public final static byte SHORT_INDEX = 1;
	public final static byte INTEGER_INDEX = 2;
	public final static byte LONG_INDEX = 3;
	public final static byte REAL_INDEX = 4;
	public final static byte DOUBLE_INDEX = 5;

	/* chunk status */
	public final static byte INCOMPLETE_CHUNK = 1;
	public final static byte COMPLETE_CHUNK = 2;

	/* chunk rank */
	public final static byte PRIME_CHUNK = 1;
	public final static byte SLAVE_CHUNK = 2;

	/**
	 * variable value
	 * @return
	 */
	public static boolean isVariable(byte type) {
		return type == Type.RAW || type == Type.CHAR || type == Type.RCHAR
				|| type == Type.NCHAR || type == Type.RNCHAR
				|| type == Type.WCHAR || type == Type.RWCHAR;
	}

	public static boolean isWord(byte type) {
		return type == Type.CHAR || type == Type.RCHAR || type == Type.NCHAR
				|| type == Type.RNCHAR || type == Type.WCHAR
				|| type == Type.RWCHAR;
	}

	public static String translate(byte i) {
		String s = "Undefine";
		switch(i) {
		case Type.RAW:
			s = "Raw"; break;
		case Type.CHAR:
			s = "Char"; break;
		case Type.RCHAR:
			s = "RChar"; break;
		case Type.NCHAR:
			s = "NChar"; break;
		case Type.RNCHAR:
			s = "RNChar"; break;
		case Type.WCHAR:
			s = "WChar"; break;
		case Type.RWCHAR:
			s = "RWChar"; break;
		case Type.SHORT:
			s = "Short"; break;
		case Type.INTEGER:
			s = "Integer"; break;
		case Type.LONG:
			s = "Long"; break;
		case Type.REAL:
			s = "Real"; break;
		case Type.DOUBLE:
			s = "Double"; break;
		case Type.DATE:
			s = "Date"; break;
		case Type.TIME:
			s = "Time"; break;
		case Type.TIMESTAMP:
			s = "TimeStamp"; break;
		}
		return s;
	}

}