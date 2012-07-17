/**
 * 
 */
package com.lexst.db.statement;

import java.io.*;

import com.lexst.db.field.*;
import com.lexst.db.schema.*;

public class DefaultInsert extends SQLObject {
	private static final long serialVersionUID = 1L;
	
	protected final static int VERSION = 1;

	protected Table table;

	/**
	 * 
	 */
	public DefaultInsert() {
		super(BasicObject.INSERT_METHOD);
	}
	
	/**
	 * @param table
	 */
	public DefaultInsert(Table table) {
		this();
		this.setTable(table);
	}

	public void setTable(Table t) {
		setSpace(t.getSpace());
		this.table = t;
	}

	public Table getTable() {
		return this.table;
	}
	
	protected byte[] build_space() {
		// space field
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		// db size and table size
		buff.write((byte) db.length);
		buff.write((byte) table.length);
		// database and table name
		buff.write(db, 0, db.length);
		buff.write(table, 0, table.length);
		return buff.toByteArray();
	}
	
	protected byte[] buildSheet() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);

		short cidCount = 0;
		for(short columnId : table.idSet()) {
			Field field = table.find(columnId);
			String name = field.getName();

			// column identity
			byte[] fid = com.lexst.util.Numeric.toBytes(columnId);
			buff.write(fid, 0, fid.length);
			// field name size and name
			byte[] b = name.getBytes();
			byte sz = (byte)(b.length & 0xff);
			buff.write(sz);
			buff.write(b, 0, b.length);
			cidCount++;
		}

		byte[] data = buff.toByteArray();
		byte[] count = com.lexst.util.Numeric.toBytes(cidCount);
		byte[] sz = com.lexst.util.Numeric.toBytes(data.length);

		buff.reset();
		buff.write(count ,0, count.length);
		buff.write(sz, 0, sz.length);
		buff.write(data, 0, data.length);

		return buff.toByteArray();
	}
	
}
