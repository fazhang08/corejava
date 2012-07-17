/**
 *
 */
package com.lexst.db.statement;

import java.io.ByteArrayOutputStream;
import java.util.*;

import com.lexst.db.schema.Space;

public class Sort extends SQLObject {

	private static final long serialVersionUID = 1L;

	private List<Order> array = new ArrayList<Order>();

	/**
	 *
	 */
	public Sort() {
		super();
	}

	/**
	 * @param space
	 */
	public Sort(Space space) {
		this();
		this.setSpace(space);
	}

	/**
	 *
	 * @param order
	 * @return
	 */
	public boolean add(Order order) {
		for(Order i : array) {
			if(i.getColumnId() == order.getColumnId()) {
				return false;
			}
		}
		return array.add(order);
	}

	/**
	 * list order set
	 * @return
	 */
	public List<Order> list() {
		return array;
	}

	public int size() {
		return array.size();
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * build data
	 * @return
	 */
	public byte[] build() {
		ByteArrayOutputStream a = new ByteArrayOutputStream(1024);
		for (Order order : array) {
			short colid = order.getColumnId();
			byte type = order.getType();
			byte[] b = com.lexst.util.Numeric.toBytes(colid);
			a.write(b, 0, b.length);
			a.write(type);
		}

		byte[] data = buildField(SQLObject.SDOTSET, a.toByteArray());
		byte[] space = buildSpace();

		a.reset();
		a.write(space, 0, space.length);
		a.write(data, 0, data.length);

		return this.buildMethod(SQLObject.SORT_METHOD, a.toByteArray());
	}

	public static void main(String[] args) {
		Space space = new Space("PC", "ThinkPad");
		Sort sort = new Sort(space);
		for (short colid = 1; colid <= 10; colid++) {
			if (colid % 2 == 0) {
				sort.add(new Order(colid, Order.ASC));
			} else {
				sort.add(new Order(colid, Order.DESC));
			}
		}
		byte[] b = sort.build();

		try {
			java.io.FileOutputStream out = new java.io.FileOutputStream("c:/sort.bin");
			out.write(b, 0, b.length);
			out.close();
		} catch (java.io.IOException exp) {
			exp.printStackTrace();;
		}
	}
}