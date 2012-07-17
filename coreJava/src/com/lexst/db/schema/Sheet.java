/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * database field set 
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/8/2009
 * 
 * @see com.lexst.db.schema
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.schema;

import java.io.*;

import java.util.*;

import com.lexst.db.field.Field;
import com.lexst.util.Numeric;

public class Sheet implements Serializable {
	
	private static final long serialVersionUID = 1L;

	// item set offset - > field
	private Map<Integer, Field> mapField = new HashMap<Integer, Field>();

	/**
	 *
	 */
	public Sheet() {
		super();
	}

	public boolean add(int offset, Field field) {
		return mapField.put(offset, field) == null;
	}

	public Field find(int offset) {
		return mapField.get(offset);
	}

	public void clear() {
		mapField.clear();
	}

	public boolean isEmpty() {
		return mapField.isEmpty();
	}

	public int size() {
		return mapField.size();
	}

	/**
	 * build to byte
	 * @return
	 */
	public byte[] build() {
		ArrayList<Integer> a = new ArrayList<Integer>(mapField.keySet());
		Collections.sort(a);
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (int offset : a) {
			Field field = mapField.get(offset);
			short columnId = field.getColumnId();
			String name = field.getName();
			byte[] b = Numeric.toBytes(columnId);
			buff.write(b, 0, b.length);
			b = name.getBytes();
			byte sz = (byte) b.length;
			buff.write(sz);
			buff.write(b, 0, b.length);
		}

		byte[] data = buff.toByteArray();

		ByteArrayOutputStream all = new ByteArrayOutputStream();
		short count = (short) mapField.size();
		byte[] b = Numeric.toBytes(count);
		all.write(b, 0, b.length);
		b = Numeric.toBytes(data.length);
		all.write(b, 0, b.length);
		all.write(data, 0, data.length);

		return all.toByteArray();
	}

	/**
	 * resolve
	 * @param table
	 * @param b
	 * @param offset
	 * @return
	 */
	public int resolve(Table table, byte[] b, final int offset) {
		int off = offset;

		short count = Numeric.toShort(b, off, 2);
		off += 2;
		int size = Numeric.toInteger(b, off, 4);
		off += 4;

		int index = 0;
		for (short i = 0; i < count; i++) {
			short columnId = Numeric.toShort(b, off, 2);
			off += 2;
			byte len = b[off];
			off += 1;
			byte[] byte_name = new byte[len];
			System.arraycopy(b, off, byte_name, 0, byte_name.length);
			off += byte_name.length;
			String name = new String(byte_name);
			// check field
			Field field = table.find(columnId);
			if (field == null || !field.getName().equalsIgnoreCase(name)) {
				return -1;
			}
			// save data
			this.add(index, field);
			index++;
		}
		if(off - 6 != size) {
			return -1;
		}
		return off - offset;
	}
}