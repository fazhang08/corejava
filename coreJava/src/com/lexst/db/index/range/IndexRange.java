/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * index range
 * 
 * @author scott.jian lexst@126.com
 * 
 * @version 1.0 6/23/2009
 * 
 * @see com.lexst.db.index.range
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.index.range;

import java.io.Serializable;

import com.lexst.db.Type;


public abstract class IndexRange implements Serializable, Comparable<IndexRange> {

	private static final long serialVersionUID = 1L;

	// index type
	protected byte type;
	// column identity
	protected short columnId;
	// chunk identity
	protected long chunkId;

	/**
	 *
	 */
	protected IndexRange() {
		super();
	}

	/**
	 * @param range
	 */
	public IndexRange(IndexRange range) {
		this();
		this.type = range.type;
		this.columnId = range.columnId;
		this.chunkId = range.chunkId;
	}

	/**
	 * @param type
	 */
	public IndexRange(byte type) {
		this();
		this.setType(type);
	}

	/**
	 *
	 * @param chunkId
	 * @param columnId
	 */
	public IndexRange(byte type, long chunkId, short columnId) {
		this();
		this.setType(type);
		this.setChunkId(chunkId);
		this.setColumnId(columnId);
	}

	public void setColumnId(short id) {
		this.columnId = id;
	}
	public short getColumnId() {
		return this.columnId;
	}

	public void setChunkId(long id) {
		this.chunkId = id;
	}
	public long getChunkId() {
		return this.chunkId;
	}

	public boolean isType(byte type) {
		return Type.SHORT_INDEX <= type && type <= Type.DOUBLE_INDEX;
	}

	public void setType(byte b) {
		if (!isType(b)) {
			throw new IllegalArgumentException("invalid index type!");
		}
		this.type = b;
	}

	public byte getType() {
		return type;
	}

	public boolean isShort() {
		return type == Type.SHORT_INDEX;
	}

	public boolean isInteger() {
		return type == Type.INTEGER_INDEX;
	}

	public boolean isLong() {
		return type == Type.LONG_INDEX;
	}

	public boolean isReal() {
		return type == Type.REAL_INDEX;
	}

	public boolean isDouble() {
		return type == Type.DOUBLE_INDEX;
	}

	/**
	 *
	 */
	public boolean equals(Object obj) {
		IndexRange in = (IndexRange) obj;
		return chunkId == in.chunkId && columnId == in.columnId;
	}

	public int hashCode() {
		return (int) (chunkId ^ columnId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IndexRange index) {
		if(chunkId == index.chunkId) {
			return 0;
		}
		return chunkId < index.chunkId ? -1 : 1;
	}

	public abstract IndexRange ectype();
}