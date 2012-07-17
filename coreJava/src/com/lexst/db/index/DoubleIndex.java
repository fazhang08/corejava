/**
 *
 */
package com.lexst.db.index;

import com.lexst.db.Type;
import com.lexst.db.column.Column;

public class DoubleIndex extends IndexColumn {

	private static final long serialVersionUID = 1L;

	private double value;

	/**
	 *
	 */
	public DoubleIndex() {
		super(Type.DOUBLE_INDEX);
	}

	/**
	 * @param object
	 */
	public DoubleIndex(DoubleIndex object) {
		super(object);
		this.value = object.value;
	}

	/**
	 * @param value
	 */
	public DoubleIndex(double value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param value
	 * @param column
	 */
	public DoubleIndex(double value, Column column) {
		this();
		this.setValue(value);
		this.setColumn(column);
	}

	public void setValue(double num) {
		this.value = num;
	}

	public double getValue() {
		return this.value;
	}

	public DoubleIndex clone() {
		return new DoubleIndex(this);
	}
}