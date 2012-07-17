/**
 *
 */
package com.lexst.db.statement;

import java.io.*;

import com.lexst.db.schema.*;
import com.lexst.util.*;

public class Select extends Query {
	private static final long serialVersionUID = 1L;

	// query range
	private int begin, end;
	// return column set, default is null, query all
	private short[] selectIds;
	// sql order by
	private Order sequence;

	/**
	 * construct method
	 */
	public Select(int capacity) {
		super(BasicObject.SELECT_METHOD);
		begin = end = 0;
		if (capacity < 5) {
			capacity = 5;
		}
	}

	/**
	 *
	 */
	public Select() {
		this(5);
	}

	/**
	 * @param s
	 */
	public Select(Space s) {
		this();
		this.setSpace(s);
	}

	/**
	 * @param begin
	 * @param end
	 */
	public void setRange(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	public int getBegin() {
		return this.begin;
	}

	public int getEnd() {
		return this.end;
	}

	public void setSelectId(short[] ids) {
		if (ids == null || ids.length == 0) {
			this.selectIds = null;
			return;
		}
		selectIds = new short[ids.length];
		for (int i = 0; i < ids.length; i++) {
			selectIds[i] = ids[i];
		}
	}

	public short[] getSelectId() {
		return selectIds;
	}

	public void setOrder(Order object) {
		if (this.sequence == null) {
			this.sequence = object;
		} else {
			sequence.setLast(object);
		}
	}

	public Order getOrder() {
		return this.sequence;
	}

	public Select clone() {
		Select select = new Select();
		select.space = new Space(this.space);
		select.setChunkId(this.chunkIds);
		select.condition = new Condition(this.condition);

		select.begin = this.begin;
		select.end = this.end;
		select.setSelectId(this.selectIds);
		select.setOrder(this.sequence);

		return select;
	}

	private byte[] buildColumns() {
		int items = (selectIds == null ? 0 : selectIds.length);
		ByteArrayOutputStream out = new ByteArrayOutputStream(128);
		byte[] b = Numeric.toBytes(items);
		out.write(b, 0, b.length);
		for (int i = 0; i < items; i++) {
			b = Numeric.toBytes(selectIds[i]);
			out.write(b, 0, b.length);
		}
		byte[] data = out.toByteArray();
		return buildField(Query.COLUMNS, data);
	}

	protected int splitColumns(byte[] data, int offset, int len) {
		int off = 0;
		int count = Numeric.toInteger(data, off, 4);
		off += 4;
		if (count > 0) {
			this.selectIds = new short[count];
			for (int i = 0; i < count; i++) {
				selectIds[i] = Numeric.toShort(data, off, 2);
				off += 2;
			}
		}
		return off - offset;
	}

	private byte[] buildOrderBy() {
		if (sequence == null) {
			return null;
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		Order order = this.sequence;
		while (order != null) {
			byte[] b = Numeric.toBytes(order.getColumnId());
			buff.write(b, 0, b.length);
			buff.write(order.getType());
			order = order.getNext();
		}
		byte[] data = buff.toByteArray();
		return buildField(Query.ORDERBY, data);
	}

	private int splitOrderBy(byte[] data, int offset, int len) {
		int off = offset, end = offset + len;
		while (off < end) {
			short cid = Numeric.toShort(data, off, 2);
			off += 2;
			byte type = data[off];
			off += 1;
			// set order
			Order order = new Order(cid, type);
			this.setOrder(order);
		}
		return off - offset;
	}

	/**
	 * @return
	 */
	private byte[] buildRange() {
		if (begin == 0 && end == 0) {
			return null;
		}
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		byte[] b = Numeric.toBytes(begin);
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(end);
		buff.write(b, 0, b.length);
		b = buff.toByteArray();
		return buildField(Query.RANGE, b);
	}

	private int splitRange(byte[] data, int offset, int len) {
		if (len < 8) {
			throw new IllegalArgumentException("invalid range size");
		}
		int off = offset;
		this.begin = Numeric.toInteger(data, off, 4);
		off += 4;
		this.end = Numeric.toInteger(data, off, 4);
		off += 4;
		return off - offset;
	}

	/**
	 * build
	 *
	 * @return
	 */
	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		// space field
		byte[] b = this.buildSpace();
		buff.write(b, 0, b.length);

		// condition field
		b = this.buildCondition();
		buff.write(b, 0, b.length);
		// select column
		b = this.buildColumns();
		buff.write(b, 0, b.length);
		// all chunk id
		b = this.buildChunkId();
		buff.write(b, 0, b.length);
		// order by
		b = this.buildOrderBy();
		if (b != null) {
			buff.write(b, 0, b.length);
		}
		// select range
		b = this.buildRange();
		if (b != null) {
			buff.write(b, 0, b.length);
		}
		// field body
		byte[] data = buff.toByteArray();

		// reset and re-write
		buff.reset();
		int size = 5 + data.length;
		b = Numeric.toBytes(size);
		// all size
		buff.write(b, 0, b.length);
		// select method identity
		buff.write(Query.SELECT_METHOD);
		// write body
		buff.write(data, 0, data.length);

		return buff.toByteArray();
	}

	/**
	 * resolve bytes
	 * @param b
	 * @param offset
	 * @param len
	 * @return
	 */
	public int resolve(byte[] b, final int offset, final int len) {
		int off = offset;

		// all length
		int length = Numeric.toInteger(b, off, 4);
		if(b.length - offset < length) return -1;
		off += 4;
		// check method tag
		byte method = b[off++];
		if (method != Query.SELECT_METHOD) {
			throw new IllegalArgumentException("invalid select identity!");
		}

		// check field
		int end = offset + length;
		while (off < end) {
			Body body = super.splitField(b, off, end - off);
			off += body.length();

			byte[] data = body.data;
			switch( body.id ) {
			case Query.SPACE:
				splitSpace(data, 0, data.length); break;
			case Query.CONDITION:
				splitCondition(data, 0, data.length); break;
			case Query.COLUMNS:
				splitColumns(data, 0, data.length); break;
			case Query.CHUNKS:
				splitChunkId(data, 0, data.length); break;
			case Query.ORDERBY:
				splitOrderBy(data, 0, data.length); break;
			case Query.RANGE:
				splitRange(data, 0, data.length); break;
			}
		}
		return off - offset;
	}

//	public static void main(String[] args) {
//		short cid = 1;
//		//Char ch = new Char(colid, "pentium".getBytes());
//		Raw ch = new Raw(cid, "abcd".getBytes());
//		com.lexst.db.sign.BigSign index = new com.lexst.db.sign.BigSign(0x1020304050607080L, ch);
//		Condition condi = new Condition();
//		condi.setCompare(Condition.EQUAL);
//		condi.setValue(index);
//
//		Raw ch1 = new Raw(cid, "dcba".getBytes());
//		com.lexst.db.sign.BigSign index1 = new com.lexst.db.sign.BigSign(0x8070605040302010L, ch1);
//		Condition sub = new Condition();
//		sub.setCompare(Condition.EQUAL);
//		sub.setValue(index1);
//		condi.addFriend(sub);
//
//		short colid = 1;
//		Order order = new Order( colid++, Order.ASC );
//		Order order2 = new Order( colid++, Order.DESC );
//
//		Space space = new Space("Video", "Word");
//		Select select = new Select(space);
//		select.setCondition(condi);
//		select.setRange(1, 1000);
//		select.setOrder(order);
//		select.setOrder(order2);
//
////		select.setSelectId(new short[] { Short.MIN_VALUE, Short.MAX_VALUE });
////		select.setChunkId(new long[] { Long.MAX_VALUE, Long.MIN_VALUE });
//
//
//		byte[] b = select.build();
//		System.out.printf("select build size %d\n", b.length);
//
//		try {
////			java.io.FileOutputStream out = new java.io.FileOutputStream("c:/select.bin");
////			out.write(b, 0, b.length);
////			out.close();
//			File file = new File("c:/select.bin");
//			b = new byte[(int)file.length()];
//			java.io.FileInputStream in = new java.io.FileInputStream(file);
//			in.read(b);
//			in.close();
//		} catch (java.io.IOException exp) {
//			exp.printStackTrace();
//		}
//
//		Select select2 = new Select();
//		int ret = select2.resolve(b, 0, b.length);
//		System.out.printf("resolve ret is %d\n", ret);
//	}

//	public static void main(String[] args) {
//		short colid = 1;
//		Order order = new Order( colid++, Order.ASC );
//		Order order2 = new Order( colid++, Order.DESC );
//
//		Select select = new Select();
//		select.setOrder(order);
//		select.setOrder(order2);
//
//		byte[] b = select.buildOrderBy();
//		System.out.printf("order byte size %d\n", b.length);
//
//		Select select2 = new Select();
//		int size = select2.splitOrderBy(b, 0, b.length);
//		System.out.printf("split size %d\n", size);
//	}
}