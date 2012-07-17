/**
 *
 */
package com.lexst.db.row;

import java.io.*;
import java.util.*;

import com.lexst.db.*;
import com.lexst.db.column.*;
import com.lexst.db.field.*;
import com.lexst.db.schema.*;
import com.lexst.util.*;

public class Row implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final static byte VALID = 1;
	public final static byte DELETE = 2;

	private byte status;
	private int checksum;	// check sum, default is 0
	private long chunkid;	// chunk identity, default is 0
	private int fileoff;	// item offset, default is 0
	private int all_size;	// column byte and value byte count
	private short col_count;// column count
	private short head_size;// column byte size
	private int value_size;	// value byte size

	private ArrayList<Column> array = new ArrayList<Column>();

	/**
	 *
	 */
	public Row() {
		super();
	}
	
	/**
	 * @param capacity
	 */
	public Row(int capacity) {
		this();
		if (capacity > 0) {
			array.ensureCapacity(capacity);
		}
	}
	
	/**
	 * compress array
	 */
	public void reserve() {
		int size = array.size();
		if (size > 0) {
			array.ensureCapacity(size);
		}
	}

	private final int tagsize() {
		return 29;
	}

	public int getFileOffset() {
		return this.fileoff;
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public int size() {
		return array.size();
	}
	
	public Set<java.lang.Short> keySet() {
		TreeSet<java.lang.Short> set = new TreeSet<java.lang.Short>();
		for(Column column : array) {
			set.add(column.getId());
		}
		return set;
	}
	
	public Column get(short colId) {
		if (colId <= array.size()) {
			Column column = array.get(colId - 1);
			if (column.getId() == colId) {
				return column;
			}
		}
		for (int index = 0; index < array.size(); index++) {
			Column column = array.get(index);
			if (column.getId() == colId) {
				return column;
			}
		}
		return null;
	}
	
	/**
	 * add a field
	 * @param column
	 * @return
	 */
	public boolean add(Column column) {
		short colId = column.getId();
		if (colId > array.size()) {
			array.add(column);
		} else {
			array.add(colId - 1, column);
		}
		return true;
	}
	
	/**
	 * remove a field by column id
	 * @param colId
	 * @return
	 */
	public boolean remove(short colId) {
		if (colId <= array.size()) {
			Column column = array.get(colId - 1);
			if (column.getId() == colId) {
				return array.remove(colId - 1) != null;
			}
		}
		for (int index = 0; index < array.size(); index++) {
			Column column = array.get(index);
			if (column.getId() == colId) {
				return array.remove(index) != null;
			}
		}
		return false;
	}

	/**
	 * @param column
	 * @return
	 */
	public boolean replace(Column column) {
		short colid = column.getId();
		if(remove(colid)) {
			return this.add(column);
		}
		return false;
	}
	
	public Collection<Column> list() {
		return array;
	}
	
	public void clear() {
		array.clear();
	}
	

	private void init() {
		this.status = Row.VALID;	//valid status
		this.checksum = 0;
		this.chunkid = 0L; // undefine
	}

	/**
	 * build a item
	 * @param filend
	 * @return
	 */
	public byte[] build(int filend) {
		this.init();
		this.fileoff = filend;

		ByteArrayOutputStream head = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream body = new ByteArrayOutputStream(1024);
		// build all column
		for (Column col : array) {
			col.build(head, body);
		}
		// head + value
		byte[] headBytes = head.toByteArray();
		this.head_size = (short)headBytes.length;
		byte[] bodyBytes = body.toByteArray();
		this.value_size = bodyBytes.length;

		this.col_count = (short) array.size();
		this.all_size = tagsize() + head_size + value_size;

		ByteArrayOutputStream buff = new ByteArrayOutputStream(all_size);
		buff.write(status);
		byte[] b = Numeric.toBytes(checksum);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(chunkid);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(fileoff);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(all_size);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(col_count);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(head_size);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(value_size);
		buff.write(b, 0, b.length);

		buff.write(headBytes, 0, headBytes.length);
		buff.write(bodyBytes, 0, bodyBytes.length);

		return buff.toByteArray();
	}
	
	/**
	 * @param filend
	 * @param out
	 * @return
	 */
	public int build(int filend, ByteArrayOutputStream out) {
		this.init();
		this.fileoff = filend;

		ByteArrayOutputStream head = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream body = new ByteArrayOutputStream(1024);
		// build all column
		for(Column col : array) {
			col.build(head, body);
		}
		// head + value
		byte[] headBytes = head.toByteArray();
		this.head_size = (short)headBytes.length;
		byte[] bodyBytes = body.toByteArray();
		this.value_size = bodyBytes.length;

		this.col_count = (short) array.size();
		this.all_size = tagsize() + head_size + value_size;

		ByteArrayOutputStream buff = new ByteArrayOutputStream(all_size);
		buff.write(status);
		byte[] b = Numeric.toBytes(checksum);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(chunkid);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(fileoff);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(all_size);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(col_count);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(head_size);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(value_size);
		buff.write(b, 0, b.length);

		buff.write(headBytes, 0, headBytes.length);
		buff.write(bodyBytes, 0, bodyBytes.length);
		
		byte[] data = buff.toByteArray();
		out.write(data, 0, data.length);
		return data.length;
	}

	/**
	 * resolve data to a item
	 * return resolve size
	 * @param sheet
	 * @param b
	 * @param offset
	 * @return
	 */
	public int resolve(Sheet sheet, byte[] b, final int offset) {
		int off = offset;

		status = b[off++];
		checksum = Numeric.toInteger(b, off, 4);
		off += 4;
		chunkid = Numeric.toLong(b, off, 8);
		off += 8;
		fileoff = Numeric.toInteger(b, off, 4);
		off += 4;
		all_size = Numeric.toInteger(b, off, 4);
		off += 4;
		col_count = Numeric.toShort(b, off, 2);
		off += 2;
		head_size = Numeric.toShort(b, off, 2);
		off += 2;
		value_size = Numeric.toInteger(b, off, 4);
		off += 4;
		if (tagsize() + head_size + value_size != all_size) {
			return -1;
		}

		int end = offset + all_size;
		int headoff = off;
		int bodyoff = off + head_size;
		for (int index = 0; off < end; index++) {
			Field field = sheet.find(index);
			if(field == null) return -1;
			byte tag = b[headoff];
			byte type = (byte) (tag & 0x3f);

			Column column = null;
			switch (type) {
			case Type.RAW:
				column = new Raw();break;
			case Type.CHAR:
				column = new Char();break;
			case Type.NCHAR:
				column = new NChar();break;
			case Type.WCHAR:
				column = new WChar(); break;
			case Type.SHORT:
				column = new com.lexst.db.column.Short(); break;
			case Type.INTEGER:
				column = new com.lexst.db.column.Integer();break;
			case Type.LONG:
				column = new com.lexst.db.column.Long();break;
			case Type.REAL:
				column = new Real();break;
			case Type.DOUBLE:
				column = new com.lexst.db.column.Double();break;
			case Type.DATE:
				column = new com.lexst.db.column.Date();break;
			case Type.TIME:
				column = new com.lexst.db.column.Time();break;
			case Type.TIMESTAMP:
				column = new com.lexst.db.column.TimeStamp();break;
			default:
				return -1;
			}
			int[] a = column.resolve(b, headoff, bodyoff);
			if(a == null) {
				return -1;
			}
			headoff += a[0];
			bodyoff += a[1];
			off += (a[0] + a[1]);

			// set column id and save it
			column.setId(field.getColumnId());
			this.add(column);
		}
		return off - offset;
	}

	/**
	 * resolve data to a item
	 * return resolve size
	 * @param table
	 * @param b
	 * @param offset
	 * @return
	 */
	public int resolve(Table table, byte[] b, final int offset) {
		int off = offset;

		status = b[off++];
		checksum = Numeric.toInteger(b, off, 4);
		off += 4;
		chunkid = Numeric.toLong(b, off, 8);
		off += 8;
		fileoff = Numeric.toInteger(b, off, 4);
		off += 4;
		all_size = Numeric.toInteger(b, off, 4);
		off += 4;
		col_count = Numeric.toShort(b, off, 2);
		off += 2;
		head_size = Numeric.toShort(b, off, 2);
		off += 2;
		value_size = Numeric.toInteger(b, off, 4);
		off += 4;
		if (tagsize() + head_size + value_size != all_size) {
			return -1;
		}

		short colsize = (short)table.size();
//		int end = offset + all_size;
		int headoff = off;
		int bodyoff = off + head_size;
		for (short index = 1; index <= colsize; index++) {
			Field field = table.find(index);
			if(field == null) return -1;
			byte tag = b[headoff];
			byte type = (byte) (tag & 0x3f);

			Column column = null;
			switch (type) {
			case Type.RAW:
				column = new Raw(); break;
			case Type.CHAR:
				column = new Char(); break;
			case Type.NCHAR:
				column = new NChar(); break;
			case Type.WCHAR:
				column = new WChar(); break;
			case Type.SHORT:
				column = new com.lexst.db.column.Short(); break;
			case Type.INTEGER:
				column = new com.lexst.db.column.Integer();break;
			case Type.LONG:
				column = new com.lexst.db.column.Long();break;
			case Type.REAL:
				column = new com.lexst.db.column.Real();break;
			case Type.DOUBLE:
				column = new com.lexst.db.column.Double();break;
			case Type.DATE:
				column = new com.lexst.db.column.Date();break;
			case Type.TIME:
				column = new com.lexst.db.column.Time();break;
			case Type.TIMESTAMP:
				column = new com.lexst.db.column.TimeStamp();break;
			default:
				return -1;
			}
			int[] a = column.resolve(b, headoff, bodyoff);
			if(a == null) {
				return -1;
			}
			headoff += a[0];
			bodyoff += a[1];
			off += (a[0] + a[1]);
			// set column id and save it
			column.setId(field.getColumnId());
			this.add(column);
		}
		this.reserve();
		return off - offset;
	}

	
//	public static void main(String[] args) {
//		Row row = new Row();
//
//		Sheet sheet = new Sheet();
//		for (short i = 1; i <= 10; i++) {
////			Int col = new Int(i, i * 2);
////			row.add(col);
//
//			Char c2 = new Char(i, String.format("value is:%d", i).getBytes());
//			row.add(c2);
//
////			sheet.add(new com.lexst.db.head.HeadColumn(i, "so_so"));
//			sheet.add(i-1, new com.lexst.db.field.CharField(i, "so_so"));
//		}
//
//		long time = System.currentTimeMillis();
//		byte[] b = row.build(0);
//		long usedtime = System.currentTimeMillis() - time;
//		System.out.printf("byte size is:%d, usedtime:%d\n", b.length, usedtime);
//
//		Row row2 = new Row();
//		time = System.currentTimeMillis();
//		int end = row2.resolve(sheet, b, 0);
//		usedtime = System.currentTimeMillis() - time;
//		System.out.printf("resolve usedtime:%d, end is %d\n", usedtime, end);
////		for(short cid : row.keySet()) {
////			Column col = row.get(cid);
////			//System.out.printf("%d - value is:%d\n", cid, ((Int)col).getValue() );
////			String s = new String( ((Char)col).getValue() );
////			System.out.printf("%d - %s\n", cid, s );
////		}
//	}

}