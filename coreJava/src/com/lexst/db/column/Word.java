/**
 * 
 */
package com.lexst.db.column;

abstract class Word extends Column {

	private static final long serialVersionUID = 1L;
	
	private int hash;

	/**
	 * @param type
	 */
	public Word(byte type) {
		super(type);
	}
	
	/**
	 * @param type
	 * @param id
	 */
	public Word(byte type, short id) {
		super(type, id);
	}
	
	void setHash(byte[] b) {
		this.hash = 0;
		for (int i = 0; b != null && i < b.length; i++) {
			hash += b[i];
		}
	}
	
	private final boolean isAlpha(byte b) {
		return ('a' <= b && b <= 'z') || ('A' <= b && b <= 'Z');
	}
	
	private final boolean match(byte b1, byte b2) {
		if (!isAlpha(b1) || !isAlpha(b2)) return false;
		return b1 + 32 == b2 || b1 - 32 == b2;
	}

	boolean match(boolean sentient, byte[] b1, byte[] b2) {
		if (b1 == null || b2 == null || b1.length != b2.length) {
			return false;
		}
		if (sentient) {
			for (int i = 0; i < b1.length; i++) {
				if (b1[i] != b2[i]) return false;
			}
		} else {
			for (int i = 0; i < b1.length; i++) {
				if (b1[i] != b2[i]) {
					if (!match(b1[i], b2[i])) return false;
				}
			}
		}
		return true;
	}
	
	public int hashCode() {
		return this.hash;
	}
}
