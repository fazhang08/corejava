/**
 *
 */
package com.lexst.db.index.range;

import com.lexst.db.Type;

public final class RealIndexRange extends IndexRange {
	private static final long serialVersionUID = 1L;

	private float begin, end;

	/**
	 *
	 */
	public RealIndexRange() {
		super(Type.REAL_INDEX);
		begin = end = 0f;
	}

	/**
	 * @param range
	 */
	public RealIndexRange(RealIndexRange range) {
		super(range);
		this.begin = range.begin;
		this.end = range.end;
	}

	/**
	 *
	 * @param chunkId
	 * @param columnId
	 */
	public RealIndexRange(long chunkId, short columnId) {
		super(Type.REAL_INDEX, chunkId, columnId);
		begin = end = 0f;
	}

	/**
	 * @param chunkId
	 * @param columnId
	 * @param begin
	 * @param end
	 */
	public RealIndexRange(long chunkId, short columnId, float begin , float end) {
		this(chunkId, columnId);
		this.setRange(begin, end);
	}

	public void setRange(float begin, float end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid real range!");
		}
		this.begin = begin;
		this.end = end;
	}
	public float getBegin() {
		return this.begin;
	}
	public float getEnd() {
		return this.end;
	}

	public boolean inside(float value) {
		return begin <= value && value <= end;
	}

	/* (non-Javadoc)
	 * @see com.lexst.call.index.Index#ectype()
	 */
	@Override
	public IndexRange ectype() {
		return new RealIndexRange(this);
	}
}