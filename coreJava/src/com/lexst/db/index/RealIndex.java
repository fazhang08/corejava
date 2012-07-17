/**
 *
 */
package com.lexst.db.index;

import com.lexst.db.Type;
import com.lexst.db.column.Real;

public class RealIndex extends IndexColumn {

	private static final long serialVersionUID = 1L;

	private float value;

	/**
	 *
	 */
	public RealIndex() {
		super(Type.REAL_INDEX);
	}

	/**
	 * @param object
	 */
	public RealIndex(RealIndex object) {
		super(object);
		this.value = object.value;
	}

	/**
	 * @param num
	 */
	public RealIndex(float num) {
		this();
		this.setValue(num);
	}

	/**
	 * @param num
	 * @param column
	 */
	public RealIndex(float num, Real column) {
		this();
		this.setValue(num);
		this.setColumn(column);
	}

	public void setValue(float num) {
		this.value = num;
	}

	public float getValue() {
		return this.value;
	}
}
