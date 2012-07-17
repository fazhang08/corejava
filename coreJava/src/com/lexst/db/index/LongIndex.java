/**
 *
 */
package com.lexst.db.index;

import com.lexst.db.Type;
import com.lexst.db.column.Column;

public class LongIndex extends IndexColumn {

	private static final long serialVersionUID = 1L;

	private long value;

	/**
	 *
	 */
	public LongIndex() {
		super(Type.LONG_INDEX);
	}

	/**
	 * @param object
	 */
	public LongIndex(LongIndex object) {
		super(object);
		this.value = object.value;
	}

	/**
	 * @param value
	 */
	public LongIndex(long value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param value
	 * @param column
	 */
	public LongIndex(long value, Column column) {
		this();
		this.setValue(value);
		this.setColumn(column);
	}

	public void setValue(long num) {
		this.value = num;
	}

	public long getValue() {
		return this.value;
	}

	public LongIndex clone() {
		return new LongIndex(this);
	}
}