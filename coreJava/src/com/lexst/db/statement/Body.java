/**
 *
 */
package com.lexst.db.statement;

final class Body {
	int length;

	byte id;
	int datasize;
	byte[] data;

	/**
	 *
	 */
	public Body() {
		super();
	}

	public Body(byte id, int bodysize, byte[] b) {
		this.id = id;
		this.datasize = bodysize;
		this.data = b;
	}

	public void setLength(int i) {
		this.length = i;
	}

	public int length() {
		return this.length;
	}

}