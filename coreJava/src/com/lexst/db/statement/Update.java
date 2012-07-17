/**
 *
 */
package com.lexst.db.statement;

import java.util.*;

import com.lexst.db.column.Column;
import com.lexst.db.schema.*;

public class Update extends Query {

	private static final long serialVersionUID = 1L;

	// new column
	private ArrayList<Column> values = new ArrayList<Column>();

	/**
	 *
	 */
	public Update() {
		super(BasicObject.UPDATE_METHOD);
	}
	
	/**
	 * 
	 * @param space
	 */
	public Update(Space space) {
		this();
		this.setSpace(space);
	}

	public void add(Column column) {
		values.add(column);
	}

	public List<Column> values() {
		return values;
	}

}