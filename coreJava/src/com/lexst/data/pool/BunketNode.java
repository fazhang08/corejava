/**
 * 
 */
package com.lexst.data.pool;


final class BunketNode {
	
	private long offset;
	private int length;

	/**
	 * default trip
	 */
	public BunketNode(long off, int len) {
		super();
		this.setOffset(off);
		this.setLength(len);
	}
	
	public void setOffset(long  value) {
		this.offset = value;
	}
	public long getOffset() {
		return this.offset;
	}
	
	public void setLength(int value) {
		this.length = value;
	}
	
	public int getLength() {
		return this.length;
	}
	
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object == null || !(object instanceof BunketNode)) {
			return false;
		}
		BunketNode trip = (BunketNode) object;
		return offset == trip.offset && length == trip.length;
	}

	public int hashCode() {
		return (int) (offset ^ length);
	}

}
