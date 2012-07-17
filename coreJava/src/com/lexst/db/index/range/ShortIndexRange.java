/**
 *
 */
package com.lexst.db.index.range;

import com.lexst.db.Type;

public final class ShortIndexRange extends IndexRange {
	private static final long serialVersionUID = 1L;

	private short begin, end;

	/**
	 *
	 */
	public ShortIndexRange() {
		super(Type.SHORT_INDEX);
		this.begin = end = 0;
	}

	/**
	 * @param index
	 */
	public ShortIndexRange(ShortIndexRange index) {
		super(index);
		this.begin = index.begin;
		this.end = index.end;
	}

	/**
	 * @param chunkId
	 * @param columnId
	 */
	public ShortIndexRange(long chunkId, short columnId) {
		super(Type.SHORT_INDEX, chunkId, columnId);
		begin = end = 0;
	}

	/**
	 * @param chunkId
	 * @param columnId
	 * @param begin
	 * @param end
	 */
	public ShortIndexRange(long chunkId, short columnId, short begin, short end) {
		this(chunkId, columnId);
		this.setRange(begin, end);
	}

	public void setRange(short begin, short end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid small range!");
		}
		this.begin = begin;
		this.end = end;
	}
	public short getBegin() {
		return this.begin;
	}
	public short getEnd() {
		return this.end;
	}

	public boolean inside(short value) {
		return begin <= value && value <= end;
	}

	/* (non-Javadoc)
	 * @see com.lexst.call.index.Index#ectype()
	 */
	@Override
	public IndexRange ectype() {
		return new ShortIndexRange(this);
	}
}