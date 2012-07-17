/**
 *
 */
package com.lexst.db.index;

import com.lexst.db.Type;
import com.lexst.db.column.Column;

public class IntegerIndex extends IndexColumn {

	private static final long serialVersionUID = 1L;

	private int value;

	/**
	 *
	 */
	public IntegerIndex() {
		super(Type.INTEGER_INDEX);
	}

	/**
	 * @param object
	 */
	public IntegerIndex(IntegerIndex object) {
		super(object);
		this.value = object.value;
	}

	/**
	 * @param value
	 */
	public IntegerIndex(int value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param value
	 * @param column
	 */
	public IntegerIndex(int value, Column column) {
		this();
		this.setValue(value);
		this.setColumn(column);
	}

	public void setValue(int num) {
		this.value = num;
	}

	public int getValue() {
		return this.value;
	}

	public IntegerIndex clone() {
		return new IntegerIndex(this);
	}
}
