/**
 * 
 */
package com.lexst.db.statement;

import java.io.*;
import java.util.*;

import com.lexst.db.row.*;
import com.lexst.db.schema.*;
import com.lexst.util.*;

/**
 * 
 * multi "insert" 
 */
public class Inject extends DefaultInsert {
	private static final long serialVersionUID = 1L;

	private ArrayList<Row> array = new ArrayList<Row>(10);
	
	/**
	 * 
	 */
	public Inject() {
		super();
	}
	
	/**
	 * set capacity
	 * @param capacity
	 */
	public Inject(int capacity) {
		this();
		this.reserve(capacity);
	}
	
	/**
	 * @param table
	 */
	public Inject(Table table) {
		this();
		setTable(table);
	}
	
	/**
	 * @param table
	 * @param capacity
	 */
	public Inject(Table table, int capacity) {
		this();
		super.setTable(table);
		this.reserve(capacity);
	}

	/**
	 * set min capacity
	 * @param capacity
	 */
	public void reserve(int capacity) {
		if(capacity < 1) capacity = 10;
		if(capacity < array.size()) {
			capacity = array.size();
		}
		array.ensureCapacity(capacity);
	}

	public void add(Row row) {
		array.add(row);
	}
	
	public List<Row> list() {
		return array;
	}
	
	public void clear() {
		array.clear();
	}
	
	public boolean isEmpty() {
		return array.isEmpty();
	}
	
	public int size() {
		return array.size();
	}
	
	private byte[] buildRows(int filend) {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 512);
		int count = 0; filend += 8;
		for(Row row : array) {
			int len = row.build(filend, buff);
			filend += len;
			count++;
		}
		
		// byte size
		byte[] data = buff.toByteArray();
		// reset data
		buff.reset();
		// write data size and row count
		byte[] b = Numeric.toBytes(data.length);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(count);
		buff.write(b, 0, b.length);
		// write data
		buff.write(data, 0, data.length);
		return buff.toByteArray();
	}

	/**
	 * build inject object
	 * @return
	 */
	public byte[] build() {
		byte[] b_version = Numeric.toBytes(DefaultInsert.VERSION);
		byte[] b_space = build_space();
		int tagsize = 4 + b_version.length + b_space.length;
		// create table
		byte[] b_sheet = buildSheet();
		// create row set
		int headSize = tagsize + b_sheet.length;
		
		byte[] data = buildRows(headSize);
		int allsize = headSize + data.length;
		byte[] b_allsize = Numeric.toBytes(allsize);
		// build all
		ByteArrayOutputStream buff = new ByteArrayOutputStream(4 + allsize);
		buff.write(b_allsize, 0, b_allsize.length);
		buff.write(b_version, 0, b_version.length);
		buff.write(b_space, 0, b_space.length);
		buff.write(b_sheet, 0, b_sheet.length);
		buff.write(data, 0, data.length);
		return buff.toByteArray();
	}
	
//	public static void main(String[] args) {
//		Space space = new Space("PC", "ThinkPad");
//		byte[] bytes = new byte[10];
//		for (int i = 0; i < bytes.length; i++) {
//			bytes[i] = (byte) '!';
//		}
//
//		Table table = new Table(space);
//		short colid = 1;
//		SmallField sht = new SmallField(colid++, "SHORT", Short.MAX_VALUE);
//		sht.setIndexType(Type.PRIME_INDEX  );
//		RawField raw = new RawField(colid++, "RAW", bytes);
//		raw.setIndexType(Type.SLAVE_INDEX);
//		CharField chField = new CharField(colid++, "CHAR", bytes);
//
//		table.add(raw);
//		table.add(chField);
//		table.add(sht);
//
//		Inject push = new Inject(table);
//		push.setTable(table);
//
//		short pkey = -1000;
//		for(int i = 'a'; i <= 'z'-1; i++) {
//			for(int j = 0; j < 4; j++) {
//				colid = 1;
//				com.lexst.db.column.Small small_value = new com.lexst.db.column.Small(colid++, pkey);
//				for (int n = 0; n < bytes.length; n++) bytes[n] = (byte)i;
//				com.lexst.db.column.Raw raw_value = new com.lexst.db.column.Raw(colid++, bytes);
//				for (int n = 0; n < bytes.length; n++) bytes[n] = (byte)(i+1);
//				com.lexst.db.column.Char char_value = new com.lexst.db.column.Char(colid++, bytes);
//				Row row = new Row();
//				row.add(small_value); row.add(raw_value); row.add(char_value);
//				push.add(row);
//			}
//			System.out.printf("short value is %d\n", pkey);
//			pkey += 100;
//		}
//
//		byte[] b = push.build();
//
//		System.out.printf("byte size is %d\n", b.length);
//
////		// print
////		java.lang.StringBuilder buff = new java.lang.StringBuilder();
////		for(int i = 0; i<b.length; i++) {
////			String s = String.format("%X", b[i]);
////			if (s.length() == 1) s = "0" + s;
////			if(buff.length()>0) buff.append(" ");
////			buff.append(s);
////		}
////		System.out.printf("%s\n", buff.toString());
//
////		int gap = 'a' - 'A';
////		System.out.printf("gap is %d - %d - %d\n", gap, (int)'a', (int)'A');
//
//		try {
//			java.io.FileOutputStream out = new java.io.FileOutputStream("c:/push.bin");
//			out.write(b, 0, b.length);
//			out.close();
//		} catch (java.io.IOException exp) {
//			exp.printStackTrace();
//		}
//
//	}
	
	
//	public static void main(String[] args) {
//		Space space = new Space("PC", "ThinkPad");
//		Table table = new Table(space);
//		short cid = 1;
//		table.add(new CharField(cid++, "w1"));
//		table.add(new CharField(cid++, "w2"));
//		
//		cid = 1;
//		Insert insert = new Insert(table);
//		insert.add(new com.lexst.db.column.Char(cid++, "aix".getBytes()));
//		insert.add(new com.lexst.db.column.Char(cid++, "unix".getBytes()));
//
//		byte[] b1 = insert.build();
//		System.out.printf("insert len %d\n", b1.length);
//		
//		Inject inject = new Inject(table);
//		for (int i = 0; i < 1; i++) {
//			cid = 1;
//			Row row = new Row();
//			row.add(new com.lexst.db.column.Char(cid++, "aix".getBytes()));
//			row.add(new com.lexst.db.column.Char(cid++, "unix".getBytes()));
//			inject.add(row);
//		}
//
//		byte[] b2 = inject.build();
//		System.out.printf("inject len %d\n", b2.length);
//		for (int i = 0; i < b1.length; i++) {
//			if (b1[i] != b2[i]) {
//				System.out.printf("not match , at %d\n", i);
//			}
//		}
//		System.out.println("compare finished!");
//	}

}