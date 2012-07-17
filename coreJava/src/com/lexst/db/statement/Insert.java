/**
 *
 */
package com.lexst.db.statement;

import java.io.*;
import java.util.*;

import com.lexst.db.column.*;
import com.lexst.db.row.*;
import com.lexst.db.schema.*;
import com.lexst.util.*;

/**
 * single "insert"
 */
public class Insert extends DefaultInsert  {

	private static final long serialVersionUID = 1L;

	// a item
	protected Row row = new Row();

	/**
	 * insert method
	 */
	public Insert() {
		super();
	}
	
	/**
	 * @param table
	 */
	public Insert(Table table) {
		super(table);
	}

	public Row getRow() {
		return row;
	}

	public boolean add(Column value) {
		return row.add(value);
	}

	public Collection<Column> list() {
		return row.list();
	}

	public void clear() {
		row.clear();
	}

	public boolean isEmpty() {
		return row.isEmpty();
	}

	public int size() {
		return row.size();
	}

	private byte[] buildRow(int filend) {
		// byte size
		byte[] data = row.build(filend + 8);
		ByteArrayOutputStream buff = new ByteArrayOutputStream(8 + data.length);
		byte[] b = Numeric.toBytes(data.length);
		buff.write(b, 0, b.length);
		// row count
		int count = 1;
		b = Numeric.toBytes(count);
		buff.write(b, 0, b.length);
		// write data
		buff.write(data, 0, data.length);

		return buff.toByteArray();
	}

	public byte[] build() {
		byte[] b_version = Numeric.toBytes(DefaultInsert.VERSION);
		byte[] b_space = build_space();
		int tagSize = 4 + b_version.length + b_space.length;
		// create table
		byte[] b_table = buildSheet();
		// create row set
		int headSize = tagSize + b_table.length;

		byte[] data = buildRow(headSize);
		int allsize = headSize + data.length;
		byte[] b_allsize = Numeric.toBytes(allsize);
		// build all
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		buff.write(b_allsize, 0, b_allsize.length);
		buff.write(b_version, 0, b_version.length);
		buff.write(b_space, 0, b_space.length);
		buff.write(b_table, 0, b_table.length);
		buff.write(data, 0, data.length);
		return buff.toByteArray();
	}

}