/**
 *
 */
package com.lexst.db.statement;

import java.io.Serializable;
import java.util.*;

import com.lexst.db.index.*;

public class Condition implements Serializable {
	private static final long serialVersionUID = 1L;

	// 与下一个比较单元的关系
	public final static byte NONE = 0;
	public final static byte AND = 1;
	public final static byte OR = 2;

	public final static byte EQUAL = 1;
	public final static byte NOT_EQUAL = 2;
	public final static byte LESS = 3;
	public final static byte LESS_EQUAL = 4;
	public final static byte GREATER = 5;
	public final static byte GREATER_EQUAL = 6;
	public final static byte LIKE = 7;

	// column name [ symbol(=, !=, <, <=, >=, >) ] value

	// 外部关联单元
	private byte outsideRelated;
	// 与下一比较单元的关系
	private byte related;
	// 比较符(等于,不等于,大于,大于等于,小于,小于等于)
	private byte compare;
	// column name
	private String columnName;
	// query value
	private IndexColumn value;
	// friend condition
	private List<Condition> friends = new ArrayList<Condition>();
	// next condition
	private Condition next;

	/**
	 *
	 */
	public Condition() {
		super();
		this.outsideRelated = Condition.NONE;
		this.related = Condition.NONE;
		this.compare = 0;
		this.next = null;
	}

	/**
	 * @param condi
	 */
	public Condition(Condition condi) {
		this();
		this.outsideRelated = condi.outsideRelated;
		this.related = condi.related;
		this.compare = condi.compare;
		this.columnName = condi.columnName;
		this.value = condi.value.clone();
		for (Condition tion : condi.friends) {
			friends.add(tion.clone());
		}
		if (condi.next != null) {
			next = new Condition(condi.next);
		}
	}

	/**
	 * @param colName
	 * @param compare
	 * @param value
	 */
	public Condition(String colName, byte compare, IndexColumn value) {
		this();
		this.setColumnName(colName);
		this.setCompare(compare);
		this.setValue(value);
	}

	/**
	 * @param related
	 * @param colName
	 * @param compare
	 * @param value
	 */
	public Condition(byte related, String colName, byte compare, IndexColumn value) {
		this();
		this.setRelate(related);
		this.setColumnName(colName);
		this.setCompare(compare);
		this.setValue(value);
	}

	public void setCompare(byte b) {
		if (Condition.EQUAL <= b && b <= Condition.LIKE) {
			this.compare = b;
		} else {
			throw new IllegalArgumentException("invalid compare value!");
		}
	}

	public byte getCompare() {
		return this.compare;
	}

	public static byte getCompare(String word) {
		if (">".equals(word)) {
			return Condition.GREATER;
		} else if (">=".equals(word)) {
			return Condition.GREATER_EQUAL;
		} else if ("<".equals(word)) {
			return Condition.LESS;
		} else if ("<=".equals(word)) {
			return Condition.LESS_EQUAL;
		} else if ("=".equals(word)) {
			return Condition.EQUAL;
		} else if ("<>".equals(word) || "!=".equals(word)) {
			return Condition.NOT_EQUAL;
		} else if ("LIKE".equalsIgnoreCase(word)) {
			return Condition.LIKE;
		} else {
			throw new IllegalArgumentException("invalid compare!");
		}
	}

	public void setOutsideRelate(byte b) {
		if (Condition.NONE <= b && b <= Condition.OR) {
			this.outsideRelated = b;
		} else {
			throw new IllegalArgumentException("invalid outside relate");
		}
	}

	public byte getOutsideRelate() {
		return this.outsideRelated;
	}

	public void setRelate(byte b) {
		if (Condition.NONE <= b && b <= Condition.OR) {
			this.related = b;
		} else {
			throw new IllegalArgumentException("invalid relate");
		}
	}
	public byte getRelate() {
		return this.related;
	}

	public static byte getRelated(String word) {
		if ("AND".equalsIgnoreCase(word)) {
			return Condition.AND;
		} else if ("OR".equalsIgnoreCase(word)) {
			return Condition.OR;
		} else {
			throw new IllegalArgumentException("invalid related keyword: " + word);
		}
	}

	public boolean isAND() {
		return this.related == Condition.AND;
	}

	public boolean isOR() {
		return this.related == Condition.OR;
	}

	public boolean isNotRelate() {
		return this.related == Condition.NONE;
	}

	public void setColumnName(String name) {
		this.columnName = name;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public short getColumnId() {
		return value.getColumnId();
	}

	public void setValue(IndexColumn num) {
		this.value = num;
	}
	public IndexColumn getValue() {
		return this.value;
	}

	public void addFriend(Condition condi) {
		this.friends.add(condi);
	}
	public List<Condition> getFriends() {
		return this.friends;
	}

	public void setLast(Condition condi) {
		if(next == null) {
			this.next = condi;
		} else {
			next.setLast(condi);
		}
	}

	public Condition getNext() {
		return next;
	}

	public Condition getLast() {
		if(next != null) {
			return next.getLast();
		}
		return this;
	}

	public Condition clone() {
		return new Condition(this);
	}
}