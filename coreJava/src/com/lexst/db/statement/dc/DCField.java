/**
 * 
 */
package com.lexst.db.statement.dc;

import java.io.*;

import com.lexst.util.*;

public class DCField implements Serializable {

	private static final long serialVersionUID = -3795753280262580605L;

	/* split value */
	private int mod;

	/* file range, from 0 */
	private long begin, end;

	/**
	 * 
	 */
	public DCField() {
		super();
	}

	/**
	 * @param mod
	 * @param begin
	 * @param end
	 */
	public DCField(int mod, long begin, long end) {
		this();
		this.setMod(mod);
		this.setRange(begin, end);
	}

	public void setMod(int value) {
		this.mod = value;
	}

	public int getMod() {
		return this.mod;
	}

	public void setRange(long b, long e) {
		if (b > e) {
			throw new IllegalArgumentException("invalid range: " + b + "-" + e);
		}
		this.begin = b;
		this.end = e;
	}

	public long getBegin() {
		return this.begin;
	}

	public long getEnd() {
		return this.end;
	}

	public long length() {
		return this.end - this.begin + 1;
	}

	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		byte[] b = Numeric.toBytes(mod);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(begin);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(end);
		buff.write(b, 0, b.length);
		return buff.toByteArray();
	}

	public int resolve(byte[] data, int posi) {
		int off = posi;
		mod = Numeric.toInteger(data, off, 4);
		off += 4;
		begin = Numeric.toLong(data, off, 8);
		off += 8;
		end = Numeric.toLong(data, off, 8);
		off += 8;
		return off - posi;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg) {
		if (arg == this) {
			return true;
		} else if (arg == null || arg.getClass() != DCField.class) {
			return false;
		}

		DCField p = (DCField) arg;
		return mod == p.mod && begin == p.begin && end == p.end;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (begin ^ end ^ mod);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("mod:%d range:[%d - %d]", mod, begin, end);
	}

}