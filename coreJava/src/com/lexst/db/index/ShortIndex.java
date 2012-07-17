/**
 *
 */
package com.lexst.db.index;

import com.lexst.db.Type;
import com.lexst.db.column.Column;

public class ShortIndex extends IndexColumn {

	private static final long serialVersionUID = 1L;

	private short value;

	/**
	 *
	 */
	public ShortIndex() {
		super(Type.SHORT_INDEX);
	}

	/**
	 * @param object
	 */
	public ShortIndex(ShortIndex object) {
		super(object);
		this.value = object.value;
	}

	public ShortIndex(short value) {
		this();
		this.setValue(value);
	}

	/**
	 * @param value
	 * @param column
	 */
	public ShortIndex(short value, Column column) {
		this();
		this.setValue(value);
		this.setColumn(column);
	}

	public void setValue(short num) {
		this.value = num;
	}

	public short getValue() {
		return this.value;
	}

	public ShortIndex clone() {
		return new ShortIndex(this);
	}
}