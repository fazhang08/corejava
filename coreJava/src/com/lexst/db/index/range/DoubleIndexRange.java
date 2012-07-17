/**
 *
 */
package com.lexst.db.index.range;

import com.lexst.db.Type;

public final class DoubleIndexRange extends IndexRange {
	private static final long serialVersionUID = 1L;

	private double begin, end;

	/**
	 *
	 */
	public DoubleIndexRange() {
		super(Type.DOUBLE_INDEX);
		begin = end = 0;
	}

	/**
	 * @param index
	 */
	public DoubleIndexRange(DoubleIndexRange index) {
		super(index);
		this.begin = index.begin;
		this.end = index.end;
	}

	/**
	 *
	 * @param chunkId
	 * @param columnId
	 */
	public DoubleIndexRange(long chunkId, short columnId) {
		super(Type.DOUBLE_INDEX, chunkId, columnId);
		begin = end = 0;
	}

	/**
	 * @param chunkId
	 * @param columnId
	 * @param begin
	 * @param end
	 */
	public DoubleIndexRange(long chunkId, short columnId, double begin, double end) {
		this(chunkId, columnId);
		this.setRange(begin, end);
	}

	public void setRange(double begin, double end) {
		if (begin > end) {
			throw new IllegalArgumentException("invalid double range!");
		}
		this.begin = begin;
		this.end = end;
	}
	public double getBegin() {
		return this.begin;
	}
	public double getEnd() {
		return this.end;
	}

	public boolean inside(double value) {
		return begin <= value && value <= end;
	}

	/* (non-Javadoc)
	 * @see com.lexst.call.index.Index#ectype()
	 */
	@Override
	public IndexRange ectype() {
		return new DoubleIndexRange(this);
	}
}
