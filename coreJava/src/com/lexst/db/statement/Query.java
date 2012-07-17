/**
 *
 */
package com.lexst.db.statement;

import java.io.*;
import java.util.*;

import com.lexst.db.*;
import com.lexst.db.column.*;
import com.lexst.db.index.*;
import com.lexst.util.*;

public class Query extends SQLObject {
	private static final long serialVersionUID = 1L;

	// query chunk id set
	protected long[] chunkIds;
	// sql query condition
	protected Condition condition;

	/**
	 *
	 */
	protected Query() {
		super();
	}

	/**
	 * @param method
	 */
	protected Query(byte method) {
		super(method);
	}
	
	public void addChunkId(long[] all) {
		if (chunkIds == null) {
			chunkIds = new long[all.length];
			System.arraycopy(all, 0, chunkIds, 0, all.length);
		} else {
			long[] a = new long[chunkIds.length];
			System.arraycopy(chunkIds, 0, a, 0, chunkIds.length);

			chunkIds = new long[a.length + all.length];
			System.arraycopy(a, 0, chunkIds, 0, a.length);
			System.arraycopy(all, 0, chunkIds, a.length, all.length);
		}
	}

	public void setChunkId(Collection<java.lang.Long> all) {
		if (all == null || all.isEmpty()) {
			chunkIds = null;
		} else {
			chunkIds = new long[all.size()];
			int index = 0;
			for (java.lang.Long value : all) {
				chunkIds[index++] = value.longValue();
			}
		}
	}

	public void setChunkId(long[] all) {
		if (all == null) {
			chunkIds = null;
		} else {
			chunkIds = new long[all.length];
			for (int i = 0; i < all.length; i++) {
				chunkIds[i] = all[i];
			}
		}
	}

	public long[] getChunkId() {
		return chunkIds;
	}

	public void setCondition(Condition value) {
		if (this.condition == null) {
			this.condition = value;
		} else {
			condition.setLast(value);
		}
	}

	public Condition getCondition() {
		return this.condition;
	}

	/**
	 * combin a condition
	 * @param condi
	 * @return
	 */
	private byte[] combin_condition(Condition condi) {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		// 1. outside relations
		buff.write(condi.getOutsideRelate());
		// 2. previous relations
		buff.write(condi.getRelate());
		// 3. compare symbol
		buff.write(condi.getCompare());
		// 4. index type
		IndexColumn value = condi.getValue();
		byte indexType = value.getType();
		buff.write(indexType);
		// 5. index serial
		byte[] b = null;
		switch(indexType) {
		case Type.SHORT_INDEX: {
			com.lexst.db.index.ShortIndex index = (com.lexst.db.index.ShortIndex) value;
			b = Numeric.toBytes(index.getValue());
		}
			break;
		case Type.INTEGER_INDEX: {
			com.lexst.db.index.IntegerIndex index = (com.lexst.db.index.IntegerIndex)value;
			b = Numeric.toBytes(index.getValue());
		}
			break;
		case Type.LONG_INDEX: {
			com.lexst.db.index.LongIndex index = (com.lexst.db.index.LongIndex)value;
			b = Numeric.toBytes(index.getValue());
		}
			break;
		case Type.REAL_INDEX: {
			com.lexst.db.index.RealIndex index = (com.lexst.db.index.RealIndex)value;
			float num = index.getValue();
			b = Numeric.toBytes( Float.floatToIntBits(num) );
		}
			break;
		case Type.DOUBLE_INDEX: {
			com.lexst.db.index.DoubleIndex index = (com.lexst.db.index.DoubleIndex)value;
			double num = index.getValue();
			b = Numeric.toBytes(java.lang.Double.doubleToLongBits(num));
		}
			break;
		default:
			throw new java.lang.IllegalArgumentException("invalid index column");
		}
		buff.write(b, 0, b.length);
		//6. column id
		Column column = value.getColumn();
		short cid = column.getId();
		b = Numeric.toBytes(cid);
		buff.write(b, 0, b.length);
		//7. column data
		ByteArrayOutputStream head = new ByteArrayOutputStream();
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		column.build(head, body);
		b = body.toByteArray();
		head.write(b, 0, b.length);
		b = head.toByteArray();
		buff.write(b, 0, b.length);

		// friend condition
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Condition sub : condi.getFriends()) {
			b = combin_condition(sub);
			out.write(b, 0, b.length);
		}
		byte[] data = out.toByteArray();
		int friendSize = (data == null ? 0 : data.length);
		b = Numeric.toBytes(friendSize);
		buff.write(b, 0, b.length);
		if (friendSize > 0) {
			buff.write(data, 0, data.length);
		}
		// friend condition list
		return buff.toByteArray();
	}

	protected byte[] buildCondition() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);

		Condition condi = this.condition;
		while (condi != null) {
			byte[] b = combin_condition(condi);
			buff.write(b, 0, b.length);
			// next condition
			condi = condi.getNext();
		}
		byte[] data = buff.toByteArray();
		return buildField(Query.CONDITION, data);
	}

	private int splitCondition(byte[] b, int offset, int len, boolean friend) {
		int off = offset;
		Condition condi = new Condition();

		// outside relate
		condi.setOutsideRelate(b[off++]);
		// relate
		condi.setRelate(b[off++]);
		// compare
		condi.setCompare(b[off++]);
		
		// condition type
		byte type = b[off++];

		IndexColumn index = null;
		switch(type) {
		case Type.SHORT_INDEX: {
			short value = Numeric.toShort(b, off, 2);
			off += 2;
			index = new com.lexst.db.index.ShortIndex(value);
		}
			break;
		case Type.INTEGER_INDEX: {
			int value = Numeric.toInteger(b, off, 4);
			off += 4;
			index = new com.lexst.db.index.IntegerIndex(value);
		}
			break;
		case Type.LONG_INDEX: {
			long value = Numeric.toLong(b, off, 8);
			off += 8;
			index = new com.lexst.db.index.LongIndex(value);
		}
			break;
		case Type.REAL_INDEX: {
			int value = Numeric.toInteger(b, off, 4);
			off += 4;
			float num = Float.intBitsToFloat(value);
			index = new com.lexst.db.index.RealIndex(num);
		}
			break;
		case Type.DOUBLE_INDEX: {
			long bits = Numeric.toLong(b, off, 8);
			off += 8;
			double num = java.lang.Double.longBitsToDouble(bits);
			index = new com.lexst.db.index.DoubleIndex(num);
		}
			break;
		default:
			throw new IllegalArgumentException("invalid index number");
		}
		// column 2
		short cid = Numeric.toShort(b, off, 2);
		off += 2;
		// column
		Column column = null;
		byte tag = b[off];
		boolean nullable = ((tag >>> 6) & 0x3) == 1;
		byte valType = (byte) (tag & 0x3f);
		int bodyoff = off + 1;
		switch(valType) {
		case Type.RAW:
			if(!nullable) bodyoff += 4;
			column = new Raw(); break;
		case Type.CHAR:
			if(!nullable) bodyoff += 4;
			column = new Char(); break;
		case Type.NCHAR:
			if(!nullable) bodyoff += 4;
			column = new NChar(); break;
		case Type.WCHAR:
			if(!nullable) bodyoff += 4;
			column = new WChar(); break;
		case Type.SHORT:
			column = new com.lexst.db.column.Short(); break;
		case Type.INTEGER:
			column = new com.lexst.db.column.Integer(); break;
		case Type.LONG:
			column = new com.lexst.db.column.Long(); break;
		case Type.REAL:
			column = new Real(); break;
		case Type.DOUBLE:
			column = new com.lexst.db.column.Double(); break;
		case Type.DATE:
			column = new com.lexst.db.column.Date(); break;
		case Type.TIME:
			column = new com.lexst.db.column.Time(); break;
		case Type.TIMESTAMP:
			column = new com.lexst.db.column.TimeStamp(); break;
		default:
			throw new IllegalArgumentException("invalid column");
		}

		column.setId(cid);
		int[] lens = column.resolve(b, off, bodyoff);
		int allen = lens[0] + lens[1];
		off += allen;
		index.setColumn(column);
		condi.setValue(index);

		// if firend conditon
		if (friend) {
			Condition head = getCondition().getLast();
			head.addFriend(condi);
		} else {
			this.setCondition(condi);
		}

		// split firend condtion
		int friendSize = Numeric.toInteger(b, off, 4);
		off += 4;

		for (int count = 0; count < friendSize;) {
			int size = splitCondition(b, off, friendSize - count, true);
			count += size;
			off += size;
		}

		return off - offset;
	}

	/**
	 * split condition set
	 * @param b
	 * @param offset
	 * @param len
	 * @return
	 */
	protected int splitCondition(byte[] b, int offset, int len) {
		int off = offset, end = offset + len;
		while(off < end) {
			int size = splitCondition(b, off, end - off, false);
			off += size;
		}
		return off - offset;
	}

	protected byte[] buildChunkId() {
		ByteArrayOutputStream out = new ByteArrayOutputStream(256);
		int count = (chunkIds == null ? 0 : chunkIds.length);

		byte[] b = Numeric.toBytes(count);
		out.write(b, 0, b.length);
		for (int i = 0; i < count; i++) {
			b = Numeric.toBytes(chunkIds[i]);
			out.write(b, 0, b.length);
		}
		byte[] data = out.toByteArray();
		return this.buildField(Query.CHUNKS, data);
	}

	protected int splitChunkId(byte[] data, int offset, int len) {
		int off = offset;
		int count = Numeric.toInteger(data, off, 4);
		off += 4;
		if (count > 0) {
			this.chunkIds = new long[count];
			for (int i = 0; i < count; i++) {
				chunkIds[i] = Numeric.toLong(data, off, 8);
				off += 8;
			}
		}
		return off - offset;
	}

}