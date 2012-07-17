/**
 *
 */
package com.lexst.db.statement;

import java.io.Serializable;


public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	public final static byte ASC = 1;
	public final static byte DESC = 2;

	private short columnId;
	private byte type;

	// next order
	private Order next;

	/**
	 *
	 */
	public Order() {
		super();
	}

	/**
	 * @param columnId
	 * @param type
	 */
	public Order(short columnId, byte type) {
		this();
		this.set(columnId, type);
	}

	/**
	 * @param order
	 */
	public Order(Order order) {
		this();
		this.columnId = order.columnId;
		this.type = order.type;
		if (order.next != null) {
			this.next = new Order(order.next);
		}
	}

	public void set(short columnId, byte type) {
		if (type != Order.ASC && type != Order.DESC) {
			throw new java.lang.IllegalArgumentException("");
		}
		this.columnId = columnId;
		this.type = type;
	}

	public short getColumnId() {
		return this.columnId;
	}

	public byte getType() {
		return type;
	}

	public Order clone() {
		return new Order(this);
	}

	public void setLast(Order object) {
		if (this.next == null) {
			this.next = object;
		} else {
			this.next.setLast(object);
		}
	}

	public Order getLast() {
		if(next != null) {
			return next.getLast();
		}
		return this;
	}

	public Order getNext() {
		return this.next;
	}


}
