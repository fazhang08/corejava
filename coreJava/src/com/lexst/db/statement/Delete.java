/**
 *
 */
package com.lexst.db.statement;

import java.io.*;

import com.lexst.db.schema.*;

public class Delete extends Query {
	
	private static final long serialVersionUID = 1L;

	// if true, snatch delete item
	private boolean snatch;

	/**
	 *
	 */
	public Delete() {
		super(BasicObject.DELETE_METHOD);
		this.snatch = false;
	}

	/**
	 *
	 * @param space
	 */
	public Delete(Space space) {
		this();
		super.setSpace(space);
	}

	public void setSnatch(boolean b) {
		this.snatch  = b;
	}

	public boolean isSnatch() {
		return this.snatch ;
	}

	protected byte[] buildSnatch() {
		byte bit = (byte) (this.snatch ? 1 : 0);
		byte[] data = new byte[] { bit };
		return buildField(Query.SNATCH, data);
	}

	protected int splitSnatch(byte[] data, int off, int len) {
		if (data.length != 1 || off != 0) {
			throw new IllegalArgumentException("delete field missing!");
		}
		this.snatch = (data[0] == 1);
		return 1;
	}

	/**
	 * @return
	 */
	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024);
		// snatch field
		byte[] b = this.buildSnatch();
		buff.write(b, 0, b.length);
		// space field
		b = this.buildSpace();
		buff.write(b, 0, b.length);
		// condition field
		b = this.buildCondition();
		buff.write(b, 0, b.length);
		// all chunk id
		b = this.buildChunkId();
		buff.write(b, 0, b.length);

		byte[] data = buff.toByteArray();
		// combine to
		buff.reset();
		int size = 5 + data.length;
		b = com.lexst.util.Numeric.toBytes(size);
		buff.write(b, 0, b.length);
		buff.write(Query.DELETE_METHOD);
		buff.write(data, 0, data.length);

		return buff.toByteArray();
	}

	/**
	 * @param b
	 * @param offset
	 * @param len
	 * @return
	 */
	public int resolve(byte[] b, int offset, int len) {
		int off = offset;

		int length = com.lexst.util.Numeric.toInteger(b, off, 4);
		off += 4;
		if(length < len) {
			throw new IllegalArgumentException("delete field missing!");
		}
		byte method = b[off++];
		if(method != SQLObject.DELETE_METHOD) {
			throw new IllegalArgumentException("invalid delete identity!");
		}
		// unit field
		int end = offset + length;
		while(off < end) {
			Body body = super.splitField(b, off, end - off);
			off += body.length();

			byte[] data = body.data;
			switch(body.id) {
			case Query.SPACE:
				splitSpace(data, 0, data.length); break;
			case Query.SNATCH:
				splitSnatch(data, 0, data.length); break;
			case Query.CONDITION:
				splitCondition(data, 0, data.length); break;
			case Query.CHUNKS:
				splitChunkId(data, 0, data.length); break;
			}
		}
		return off - offset;
	}

	public Delete clone() {
		Delete delete = new Delete();
		delete.snatch = this.snatch;
		delete.space = new Space(this.space);
		delete.setChunkId(this.chunkIds);
		delete.condition = new Condition(this.condition);
		return delete;
	}

//	public static void main(String[] args) {
//		Space space = new Space("PC", "ThinkPad");
//		Delete dele = new Delete(space);
//		dele.setSnatch(true);
//
//		dele.setChunkId(new long[] { Long.MAX_VALUE, Long.MAX_VALUE - 1 });
//
//		short cid = 1;
//		//Char ch = new Char(colid, "pentium".getBytes());
//		Raw raw = new Raw(cid++, "pentium".getBytes());
//		com.lexst.db.sign.BigSign index = new com.lexst.db.sign.BigSign(0x1020304050607080L, raw);
//		Condition condi = new Condition();
//		condi.setCompare(Condition.EQUAL);
//		condi.setValue(index);
//
//		for (int i = 0; i < 3; i++) {
//			com.lexst.db.column.Char s = new com.lexst.db.column.Char(cid++, "pentium".getBytes());
//			com.lexst.db.sign.BigSign idx = new com.lexst.db.sign.BigSign(0x1020304050607080L, s);
//
//			Condition sub = new Condition();
//			sub.setValue(idx);
//			sub.setCompare( Condition.EQUAL);
//			sub.setRelate( Condition.AND );
//			condi.addFriend(sub);
//		}
//
//		com.lexst.db.column.Int in = new com.lexst.db.column.Int(cid++, 1000);
//		com.lexst.db.sign.IntSign is = new com.lexst.db.sign.IntSign(1000, in);
//		Condition con = new Condition();
//		con.setValue(is);
//		con.setOutsideRelate(Condition.AND);
////		con.setRelate(Condition.OR);
//		con.setCompare(Condition.EQUAL);
//		condi.setLast(con);
//
//		dele.setCondition(condi);
//
//		byte[] b = dele.build();
//
//		System.out.printf("build size is %d\n", b.length);
//
//		Delete d2 = new Delete();
//		int ret = d2.resolve(b, 0, b.length);
//		System.out.printf("size is %d\n", ret);
//
//		try {
//			java.io.FileOutputStream out = new java.io.FileOutputStream("c:/delete.bin");
//			out.write(b, 0, b.length);
//			out.close();
//		} catch (java.io.IOException exp) {
//			exp.printStackTrace();
//		}
//
//	}

}