/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author yj.liang lexst@126.com
 * @version 1.0 3/7/2009
 * 
 * @see com.lexst.util
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util;

public final class ASCII {

	public static boolean isASCII(char word) {
		return 0 <= word && word <= 255;
	}

	public static boolean isASCII_7(char word) {
		return 0 <= word && word <= 126;
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean isASCII_7(byte[] s) {
		if (s == null || s.length == 0) return false;
		for (int i = 0; i < s.length; i++) {
			if (!(0 <= s[i] && s[i] <= 126)) return false;
		}
		return true;
	}

	/**
	 * 全英文字母
	 * @param s
	 * @return
	 */
	public static boolean isASCII(String s) {
		if (s == null || s.length() == 0) return false;
		for (int i = 0; i < s.length(); i++) {
			char word = s.charAt(i);
			if (!ASCII.isASCII(word)) return false;
		}
		return true;
	}

	/**
	 * 可打印字符(包括空格)
	 *
	 * @param word
	 * @return
	 */
	public static boolean isPrint(char word) {
		return 0x20 <= word && word <= 126;
	}

	/**
	 * 可视字符
	 *
	 * @param word
	 * @return
	 */
	public static boolean isGraph(char word) {
		return 0x21 <= word && word <= 126;
	}

	/**
	 * 控制字符
	 *
	 * @param word
	 * @return
	 */
	public static boolean isControl(char word) {
		return 0 <= word && word <= 31;
	}

	/**
	 * 字母
	 *
	 * @param word
	 * @return
	 */
	public static boolean isAlpha(char word) {
		return ('A' <= word && word <= 'Z') || ('a' <= word && word <= 'z');
	}

	/**
	 * 全部是英文字母
	 * @param s
	 * @return
	 */
	public static boolean isAlpha(String s) {
		if (s == null || s.length() == 0) return false;
		for (int i = 0; i < s.length(); i++) {
			char word = s.charAt(i);
			if (!ASCII.isAlpha(word)) return false;
		}
		return true;
	}

	/**
	 * 大写字母
	 * @param word
	 * @return
	 */
	public static boolean isUpper(char word) {
		return 'A' <= word && word <= 'Z';
	}

	/**
	 * 全部是大写字母
	 * @param s
	 * @return
	 */
	public static boolean isUpper(String s) {
		if (s == null || s.length() == 0) return false;
		for (int i = 0; i < s.length(); i++) {
			char word = s.charAt(i);
			if (!ASCII.isUpper(word)) return false;
		}
		return true;
	}

	/**
	 * check low letter
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isLower(char word) {
		return 'a' <= word && word <= 'z';
	}

	/**
	 * check low letter
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isLower(String s) {
		if (s == null || s.length() == 0) return false;
		for (int i = 0; i < s.length(); i++) {
			char word = s.charAt(i);
			if (!ASCII.isLower(word)) return false;
		}
		return true;
	}

	/**
	 * check digit
	 *
	 * @param word
	 * @return
	 */
	public static boolean isDigit(char word) {
		return '0' <= word && word <= '9';
	}

	/**
	 * check digit
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isDigit(String s) {
		if (s == null || s.length() == 0) return false;
		for (int i = 0; i < s.length(); i++) {
			char word = s.charAt(i);
			if (!ASCII.isDigit(word)) return false;
		}
		return true;
	}

	/**
	 * check hex digit
	 *
	 * @param word
	 * @return
	 */
	public static boolean isXDigit(char word) {
		if(ASCII.isDigit(word)) return true;
		else if ('a' <= word && word <= 'f') return true;
		else if ('A' <= word && word <= 'F') return true;
		return false;
	}

	/**
	 * check hex digit
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isXDigit(String s) {
		if (s == null || s.length() == 0) return false;
		for (int i = 0; i < s.length(); i++) {
			char word = s.charAt(i);
			if(!ASCII.isXDigit(word)) return false;
		}
		return true;
	}

	/**
	 * check letter or digit
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isAlphaDigit(char word) {
		return ASCII.isAlpha(word) || ASCII.isDigit(word);
	}

	/**
	 * 符号
	 *
	 * @param word
	 * @return
	 */
	public static boolean isPunct(char word) {
		if (33 <= word && word <= 47) return true;
		if (58 <= word && word <= 64) return true;
		if (91 <= word && word <= 96) return true;
		if (123 <= word && word <= 126) return true;
		return false;
	}

	/**
	 * check space
	 *
	 * @param word
	 * @return
	 */
	public static boolean isSpace(char word) {
		return word == 0x20;
	}

	/**
	 * 字符
	 * @param word
	 * @return
	 */
	public static boolean isSign(char word) {
		return ASCII.isControl(word) || ASCII.isSpace(word)
				|| ASCII.isPunct(word);
	}

	/**
	 * 回车
	 *
	 * @param word
	 * @return
	 */
	public static boolean isCR(char word) {
		return word == 13;
	}

	/**
	 * 换行
	 *
	 * @param word
	 * @return
	 */
	public static boolean isLF(char word) {
		return word == 10;
	}

	/**
	 * 制表键
	 *
	 * @param word
	 * @return
	 */
	public static boolean isTab(char word) {
		return word == 9;
	}

	/**
	 * 全部是英文字母和英文数字(也可能是其中任意一种情况)
	 * @param text
	 * @return
	 */
	public static boolean isAlphaDigit(String text) {
		if (text == null || text.length() == 0) return false;
		for (int i = 0; i < text.length(); i++) {
			char word = text.charAt(i);
			if (!ASCII.isAlphaDigit(word)) return false;
		}
		return true;
	}

	/**
	 * 是小数
	 * @param s
	 * @return
	 */
	public static boolean isFloatDigit(String s) {
		if (s == null || s.length() == 0) return false;
		int fcount = 0;
		for(int i=0; i<s.length(); i++) {
			char word = s.charAt(i);
			if (word == '.') fcount++;	//是小数,加1
			else if(ASCII.isDigit(word)) continue;
			else return false;
		}
		return fcount==1;
	}

}