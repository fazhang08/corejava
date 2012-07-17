/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * sql standard object 
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 5/5/2009
 * 
 * @see com.lexst.db.statement
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.statement;

import java.io.*;

import com.lexst.db.schema.*;
import com.lexst.util.*;

public class SQLObject extends BasicObject {

	private static final long serialVersionUID = 1L;

	// table space
	protected Space space;

	/**
	 *
	 */
	protected SQLObject() {
		super();
	}

	/**
	 *
	 */
	public SQLObject(byte method) {
		super(method);
	}

	public void setSpace(Space s) {
		this.space = new Space(s);
	}
	public Space getSpace() {
		return this.space;
	}

	protected byte[] buildMethod(byte method, byte[] data) {
		int size = 5 + data.length;
		byte[] b = Numeric.toBytes(size);

		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		out.write(b, 0, b.length);
		out.write(method);
		out.write(data, 0, data.length);
		return out.toByteArray();
	}

	protected byte[] buildField(byte id, byte[] data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		int size = (data == null ? 0 : data.length);
		// write identity
		out.write(id);
		// write data size
		byte[] b = Numeric.toBytes(size);
		out.write(b, 0, b.length);
		// write data
		if (size > 0) {
			out.write(data, 0, size);
		}
		return out.toByteArray();
	}

	protected Body splitField(byte[] data, int offset, int len) {
		int off = offset, end = offset + len;

		if (off + 5 > end) {
			throw new IllegalArgumentException("error field head");
		}
		// read identity
		byte id = data[off++];
		// read data size
		int size = Numeric.toInteger(data, off, 4);
		off += 4;

		if (off + size > end) {
			throw new IllegalArgumentException("error field");
		}
		// read data
		byte[] bytes = new byte[size];
		System.arraycopy(data, off, bytes, 0, size);
		off += size;

		// get body
		Body body = new Body(id, size, bytes);
		body.setLength(off - offset);
		return body;
	}

	protected byte[] buildSpace() {
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
		byte[] data = buff.toByteArray();
		return buildField(BasicObject.SPACE, data);
	}

	protected int splitSpace(byte[] data, int offset, int len) {
		int off = offset;
		// db size and table size
		int db_sz = data[off++] & 0xff;
		int table_sz = data[off++] & 0xff;
		if (!Space.inSchemaSize(db_sz)) {
			throw new IllegalArgumentException("invalid space db size");
		}
		if (!Space.inTableSize(table_sz)) {
			throw new IllegalArgumentException("invalid space table size");
		}
		if (off + db_sz + table_sz > data.length) {
			throw new IllegalArgumentException("invalid space table size");
		}

		// db name
		String db  = new String(data, off, db_sz);
		off += db_sz;
		// table name
		String table = new String(data, off, table_sz);
		off += table_sz;
		// set space
		this.setSpace(new Space(db, table));
		return off - offset;
	}

}