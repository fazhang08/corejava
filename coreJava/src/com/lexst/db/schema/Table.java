/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * this file is part of lexst, user table
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 6/3/2009
 * @see com.lexst.db.schema
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.schema;

import java.io.*;
import java.util.*;

import com.lexst.db.*;
import com.lexst.db.field.*;
import com.lexst.util.*;

public class Table implements Serializable, Comparable<Table> {

	private static final long serialVersionUID = 1749657662106888273L;

	// table version
	private static final int VERSION = 1;
	
	// host mode
	public static final int SHARE = 1;
	public static final int EXCLUSIVE = 2;
	
	// table space
	private Space space;
	// field array
	private ArrayList<Field> array = new ArrayList<Field>();
	// sort rule
	private Layout layout;
	
	// home site 
	private Clusters clusters = new Clusters();
	// master host num (default is 1)
	private int primes;
	// chunk backup number (default is 1)
	private int copy;
	// table store mode (share or exclusive, default is share)
	private int mode;
	// table chunk size (default is 64M)
	private int chunksize;
	// set cache mode (prime site check, default is true)
	private boolean caching;

	/**
	 *
	 */
	public Table() {
		super();
		space = new Space();
		this.setPrimes(1);
		this.setCopy(1);
		this.setMode(Table.SHARE);
		this.setChunkSize(64 * 1024 * 1024);
		this.setCaching(true);
	}

	/**
	 * @param space
	 */
	public Table(Space space) {
		this();
		this.setSpace(space);
	}

	/**
	 * @param capacity
	 */
	public Table(int capacity) {
		this();
		if (capacity > 0) {
			array.ensureCapacity(capacity);
		}
	}

	/**
	 * @param space
	 * @param capacity
	 */
	public Table(Space space, int capacity) {
		this(capacity);
		this.setSpace(space);
	}

	/**
	 * set space
	 * @param s
	 */
	public void setSpace(Space s) {
		space.set(s);
	}

	public Space getSpace() {
		return space;
	}
	
	/**
	 * get home site set
	 * @return
	 */
	public Clusters getClusters() {
		return this.clusters;
	}
	
	/**
	 * prime host number
	 * @param num
	 */
	public void setPrimes(int num) {
		if (num < 1) {
			throw new IllegalArgumentException("invalid prime host num:" + num);
		}
		this.primes = num;
	}

	public int getPrimes() {
		return this.primes;
	}
	
	/**
	 * chunk copy number
	 * @param num
	 */
	public void setCopy(int num) {
		if (num < 1) {
			throw new IllegalArgumentException("invalid copy num:" + num);
		}
		this.copy = num;
	}

	public int getCopy() {
		return this.copy;
	}
	
	/**
	 * space mode
	 * @param id
	 */
	public void setMode(int id) {
		if (id != Table.SHARE && id != Table.EXCLUSIVE) {
			throw new IllegalArgumentException("invalid store mode:" + id);
		}
		this.mode = id;
	}

	public int getMode() {
		return this.mode;
	}
	
	/**
	 * share mode
	 * 
	 * @return
	 */
	public boolean isShare() {
		return this.mode == Table.SHARE;
	}

	/**
	 * exclusive mode
	 * 
	 * @return
	 */
	public boolean isExclusive() {
		return this.mode == Table.EXCLUSIVE;
	}
	
	/**
	 * chunk size
	 * @param size
	 */
	public void setChunkSize(int size) {
		if (size < 1024 * 1024) {
			throw new IllegalArgumentException("invalid chunk size:" + size);
		}
		this.chunksize = size;
	}

	public int getChunkSize() {
		return this.chunksize;
	}
	
	public void setCaching(boolean b) {
		this.caching = b;
	}
	public boolean isCaching() {
		return this.caching;
	}

	/**
	 * set sort layout
	 * @param s
	 */
	public void setLayout(Layout s) {
		this.layout = s;
	}

	public Layout getLayout() {
		return this.layout;
	}
	
	/**
	 * add a field
	 * @param field
	 * @return
	 */
	public boolean add(Field field) {
		short colId = field.getColumnId();
		if (colId > array.size()) {
			array.add(field);
		} else {
			array.add(colId - 1, field);
		}
		return true;
	}
	
	/**
	 * remove a field
	 * 
	 * @param name
	 * @return
	 */
	public boolean remove(String name) {
		for (int index = 0; index < array.size(); index++) {
			Field field = array.get(index);
			if (field.getName().equalsIgnoreCase(name)) {
				return array.remove(index) != null;
			}
		}
		return false;
	}
	
	/**
	 * remove a field by column id
	 * @param colId
	 * @return
	 */
	public boolean remove(short colId) {
		if (colId <= array.size()) {
			Field field = array.get(colId - 1);
			if (field.getColumnId() == colId) {
				return array.remove(colId - 1) != null;
			}
		}
		for (int index = 0; index < array.size(); index++) {
			Field field = array.get(index);
			if (field.getColumnId() == colId) {
				return array.remove(index) != null;
			}
		}
		return false;
	}
	
	/**
	 * name set
	 * @return
	 */
	public Set<String> nameSet() {
		TreeSet<String> set = new TreeSet<String>();
		for(Field field : array) {
			set.add(field.getName());
		}
		return set;
	}
	
	/**
	 * column id set
	 * @return
	 */
	public Set<Short> idSet() {
		TreeSet<Short> set = new TreeSet<Short>();
		for(Field field : array) {
			set.add(field.getColumnId());
		}
		return set;
	}
	
	/**
	 * value set
	 * @return
	 */
	public Collection<Field> values() {
		return array;
	}

	/**
	 * find prime field
	 * @return
	 */
	public Field pid() {
		for (Field field : array) {
			if (field.isPrimeIndex()) {
				return field;
			}
		}
		return null;
	}
	
	/**
	 * find a field by column name
	 * @param name
	 * @return
	 */
	public Field find(String name) {
		for (Field field : array) {
			if (field.getName().equalsIgnoreCase(name)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * find a field by column id
	 * @param colId
	 * @return
	 */
	public Field find(short colId) {
		if (colId <= array.size()) {
			Field field = array.get(colId - 1);
			if (field.getColumnId() == colId) {
				return field;
			}
		}
		for (int index = 0; index < array.size(); index++) {
			Field field = array.get(index);
			if (field.getColumnId() == colId) {
				return field;
			}
		}
		return null;
	}
	
	public void reserve() {
		int size = array.size();
		if (size > 0) {
			array.ensureCapacity(size);
		}
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

	/**
	 *
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != Table.class) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		Table table = (Table) obj;
		return space != null && space.equals(table.space);
	}

	/**
	 *
	 */
	@Override
	public int hashCode() {
		if (space == null) return 0;
		return space.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Table table) {
		return space.compareTo(table.space);
	}

	private byte[] buildLayout() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 5);
		Layout node = this.layout;
		while (node != null) {
			byte[] b = Numeric.toBytes(node.getColumnId());
			buff.write(b, 0, b.length);
			buff.write(node.getType());
			node = node.getNext();
		}
		return buff.toByteArray();
	}

	/**
	 * resolve layout 
	 * @param b
	 * @param offset
	 * @param len
	 * @return
	 */
	private int resolveLayout(byte[] b, int offset, int len) {
		int off = offset;
		int end = off + len;

		// order node
		short cid = Numeric.toShort(b, off, 2);
		off += 2;
		byte type = b[off++];
		this.layout = new Layout(cid, type);
		while (off < end) {
			cid = Numeric.toShort(b, off, 2);
			off += 2;
			type = b[off];
			off += 1;

			Layout lay = new Layout(cid, type);
			layout.setLast(lay);
		}
		return off - offset;
	}

	/**
	 *
	 * @return
	 */
	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 5);
		// protocol version
		byte[] b = Numeric.toBytes(Table.VERSION);
		buff.write(b, 0, b.length);

		// database name and table name
		byte[] db_name = space.getSchema().getBytes();
		byte[] table_name = space.getTable().getBytes();
		buff.write((byte)db_name.length);
		buff.write((byte)table_name.length);
		buff.write(db_name, 0, db_name.length);
		buff.write(table_name, 0, table_name.length);
		// tag
		byte[] tags = buff.toByteArray();

		// write all field
		buff.reset();
		for (Field field : array) {
			b = field.build();
			buff.write(b, 0, b.length);
		}
		byte[] data = buff.toByteArray();
		buff.reset();
		// field elements
		short elements = (short)array.size();
		b = Numeric.toBytes(elements);
		buff.write(b, 0, b.length);
		// field byte size
		b = Numeric.toBytes(data.length);
		buff.write(b, 0, b.length);
		// write data
		buff.write(data, 0, data.length);

		// build layout
		data = buildLayout();
		int size = (data == null ? 0 : data.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			buff.write(data, 0, data.length);
		}
		
		// build copy chunk number
		data = Numeric.toBytes(copy);
		size = (data == null ? 0 : data.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			buff.write(data, 0, data.length);
		}
		// build store mode
		data = Numeric.toBytes(mode);
		size = (data == null ? 0 : data.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			buff.write(data, 0, data.length);
		}
		// build chunk size
		data = Numeric.toBytes(chunksize);
		size = (data == null ? 0 : data.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			buff.write(data, 0, data.length);
		}
		// build prime host num
		data = Numeric.toBytes(primes);
		size = (data == null ? 0 : data.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			buff.write(data, 0, data.length);
		}
		// build not catch
		data = Numeric.toBytes(caching ? 1 : 0);
		size = (data == null ? 0 : data.length);
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			buff.write(data, 0, data.length);
		}

		// build clusters
		List<String> ips = clusters.list();
		size = ips.size();
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		if (size > 0) {
			for (String ip : ips) {
				data = ip.getBytes();
				b = Numeric.toBytes(data.length);
				buff.write(b, 0, b.length);
				buff.write(data, 0, data.length);
			}
		}

		// flush all
		data = buff.toByteArray();
		// write all
		buff.reset();
		size = 4 + tags.length + data.length;
		b = Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		buff.write(tags, 0, tags.length);
		buff.write(data, 0, data.length);

		return buff.toByteArray();
	}

	/**
	 * @param b
	 * @param offset
	 * @return
	 */
	public int resolve(byte[] b, final int offset) {
		int off = offset;

		// check size
		int length = Numeric.toInteger(b, off, 4);
		if(b.length - offset < length) return -1;
		off += 4;
		// check version
		int version = Numeric.toInteger(b, off, 4);
		if(version != Table.VERSION) return -1;
		off += 4;

		// space
		int db_size = b[off++] & 0xff;
		int table_size = b[off++] & 0xff;
		if (!Space.inSchemaSize(db_size) || !Space.inTableSize(table_size)) {
			throw new IllegalArgumentException("invalid space");
		}

		space.setSchema(new String(b, off, db_size));
		off += db_size;
		space.setTable(new String(b, off, table_size));
		off += table_size;

		// count field element
		short elements = Numeric.toShort(b, off, 2);
		off += 2;
		int allsize = Numeric.toInteger(b, off, 4);
		off += 4;

		int begin = off;

		for (short i = 0; i < elements; i++) {
			Field field = null;
			byte type = b[off];
			switch (type) {
			case Type.RAW:
				field = new RawField();
				break;
			case Type.CHAR:
				field = new CharField();
				break;
			case Type.NCHAR:
				field = new NCharField();
				break;
			case Type.WCHAR:
				field = new WCharField();
				break;
			case Type.SHORT:
				field = new ShortField();
				break;
			case Type.INTEGER:
				field = new IntegerField();
				break;
			case Type.LONG:
				field = new LongField();
				break;
			case Type.REAL:
				field = new RealField();
				break;
			case Type.DOUBLE:
				field = new DoubleField();
				break;
			case Type.DATE:
				field = new DateField();
				break;
			case Type.TIME:
				field = new TimeField();
				break;
			case Type.TIMESTAMP:
				field = new TimeStampField();
				break;
			}
			if (field == null) {
				throw new IllegalArgumentException("invalid field define!");
			}
			int size = field.resolve(b, off);
			if (size < 1) {
				throw new IllegalArgumentException("field resolve error!");
			}
			off += size;
			this.add(field);
		}
		if (off - begin != allsize) {
			return -1;
		}

		// resolve layout
		int size = Numeric.toInteger(b, off, 4);
		off += 4;
		if(size > 0) {
			size = resolveLayout(b, off, size);
			off += size;
		}
		// resolve copy
		size = Numeric.toInteger(b, off, 4);
		off += 4;
		if (size > 0) {
			copy = Numeric.toInteger(b, off, 4);
			off += 4;
		}
		// resolve mode
		size = Numeric.toInteger(b, off, 4);
		off += 4;
		if (size > 0) {
			mode = Numeric.toInteger(b, off, 4);
			off += 4;
		}
		// resolve chunk size
		size = Numeric.toInteger(b, off, 4);
		off += 4;
		if(size > 0) {
			chunksize = Numeric.toInteger(b, off, 4);
			off += 4;
		}
		// resolve prime host num
		size = Numeric.toInteger(b, off, 4);
		off += 4;
		if(size > 0) {
			primes = Numeric.toInteger(b, off, 4);
			off += 4;
		}
		// resolve not cache
		size = Numeric.toInteger(b, off, 4);
		off += 4;
		if (size > 0) {
			int value = Numeric.toInteger(b, off, 4);
			off += 4;
			caching = (value == 1);
		}

		// resolve clusters
		size = Numeric.toInteger(b, off, 4);
		off += 4;
		if(size > 0) {
			for(int i = 0; i < size; i++) {
				int len = Numeric.toInteger(b, off, 4);
				off += 4;
				String ip = new String(b, off, len);
				off += len;
				clusters.add(ip);
			}
			clusters.setNumber(size);
		}
		
		// compress field
		this.reserve();

		return off - offset;
	}

//	public static void main(String[] args) {
//		Space space = new Space("Video", "Word");
//		Table table = new Table(space);
//		
//		short colid = 2;
//		for(int i = 0; i < 5; i++) {
//			String naming = String.format("SHORT_%d", colid);
//			SmallField field = new SmallField(colid, naming, Short.MAX_VALUE);
//			table.add(field);
//			colid++;
//		}
//		
//		colid = 1;
//		String naming = String.format("SHORT%d", 199);
//		SmallField field = new SmallField(colid, naming, Short.MAX_VALUE);
//		table.add(field);
//		
//		for(Field value : table.values()) {
//			System.out.printf("%s - %d\n", value.getName(), value.getColumnId());
//		}
//		
//		colid = 3;
//		Field value = table.find(colid);
//		if (value != null) {
//			System.out.printf("find:%s - %d\n", value.getName(), value.getColumnId());
//		} else {
//			System.out.printf("%d is null!\n", colid);
//		}
//		
//		Clusters cs = table.getClusters();
//		cs.addIP("www.lexst.com");
//		cs.addIP("192.168.1.22");
//		cs.addIP("10.12.233.26");
//		
//		byte[] b = table.build();
//		System.out.printf("build byte size:%d\n", b.length);
//		
//		Table t2 = new Table();
//		int len = t2.resolve(b, 0);
//		System.out.printf("resolve byte size:%d\n", len);
//	}

//	private void print(byte[] b) {
//		java.lang.StringBuilder sb = new java.lang.StringBuilder();
//		for (int i = 0; i < b.length; i++) {
//			String s = String.format("%X", b[i] & 0xff);
//			if (s.length() == 1) s = "0" + s;
//			if (sb.length() > 0) sb.append(" ");
//			sb.append(s);
//		}
//		System.out.printf("%s", sb.toString());
//	}
//
//	public static void main(String[] args) {
//		Space space = new Space("PC", "ThinkPad");
//		short colid = 1;
//		byte[] s = "ABCDEFGH".getBytes();
//
//		int dt = com.lexst.util.datetime.SimpleDate.format(new Date());
//		int st = com.lexst.util.datetime.SimpleTime.format(new Date());
//		long stamp = com.lexst.util.datetime.SimpleTimeStamp.format(new Date());
//
//		Table table = new Table(space);
//		SmallField sht = new SmallField(colid++, "SHORT", Short.MAX_VALUE);
//		sht.setIndexType(Type.PRIMARY_INDEX);
//		RawField raw = new RawField(colid++, "RAW", s);
//		raw.setIndexType(Type.SLAVE_INDEX);
//		table.add(raw); // new RawField(colid++, "RAW", s));
//		table.add(new CharField(colid++, "CHAR", s));
////		table.add(new NCharField(colid++, "NCHAR", s));
////		table.add(new WCharField(colid++, "WCHAR", s));
//		table.add(sht); // new SmallField(colid++, "SHORT", Short.MAX_VALUE));
////		table.add(new IntField(colid++, "INT", Integer.MAX_VALUE));
////		table.add(new BigField(colid++, "LONG", Long.MAX_VALUE));
////		table.add(new RealField(colid++, "REAL", 999.0f));
////		table.add(new DoubleField(colid++, "DOUBLE", 999.25f));
////		table.add(new DateField(colid++, "DATE", (byte)0, dt));
////		table.add(new TimeField(colid++, "TIME", (byte)0, st));
////		table.add(new TimeStampField(colid++, "STAMP", (byte)0, stamp));
//
//		Layout layout = new Layout( (short)1, (byte)1 );
//		for (colid = 2; colid < 10; colid++) {
//			Layout l2 = new Layout((short) 2, (byte) 1);
//			layout.setLast(l2);
//		}
//		table.setLayout(layout);
//
//		byte[] b = table.build();
////		table.print(b);
//
//		try {
//			java.io.FileOutputStream out = new java.io.FileOutputStream("c:/head.bin");
//			out.write(b, 0, b.length);
//			out.close();
//		} catch (java.io.IOException exp) {
//			exp.printStackTrace();
//		}
//
////		System.out.printf("\npath separator [%s] file seprator [%s]\n", File.pathSeparator, File.separator );
//
//		int end = table.resolve(b, 0);
//		System.out.printf("build size: %d, end is %d\n", b.length, end);
//
////		try {
////			String a = "ABC";
////			byte[] as = a.getBytes("UTF-16BE");
////			for (int i = 0; i < as.length; i++) {
////				System.out.printf("%d ", as[i] & 0xff);
////			}
////		} catch (java.lang.Exception exp) {
////			exp.printStackTrace();
////		}
//	}

}