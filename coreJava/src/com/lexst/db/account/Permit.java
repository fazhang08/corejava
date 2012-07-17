/**
 *
 */
package com.lexst.db.account;

import java.io.Serializable;
import java.util.*;

import org.w3c.dom.Element;

/**
 *
 * 说明，DBA拥有所有操作权限，其它权限是平级的。即如果允许建表，不等于可以查询。可以查询，不等于可以删除
 *
 */
public abstract class Permit implements Serializable {
	private static final long serialVersionUID = 1L;

	/* permit list */
	public final static int USER_PERMIT = 1;

	public final static int SCHEMA_PERMIT = 2;

	public final static int TABLE_PERMIT = 3;

	/* permit option */
	protected int rank;

	// check user (sql using)
	private List<String> users = new ArrayList<String>();

	/**
	 *
	 */
	public Permit() {
		super();
		rank = 0;
	}

	/**
	 * @param rank
	 */
	protected Permit(int rank) {
		this();
		this.setRank(rank);
	}

	public void setRank(int i) {
		this.rank = i;
	}

	public int getRank() {
		return this.rank;
	}

	public int setUsers(String[] all) {
		int count = 0;
		for (int i = 0; all != null && i < all.length; i++) {
			if (addUser(all[i]))
				count++;
		}
		return count;
	}

	public boolean addUser(String username) {
		if (username == null) return false;
		username = username.trim();
		if (username.isEmpty()) return false;
		for (String s : users) {
			if (s.equalsIgnoreCase(username)) {
				return false;
			}
		}
		return users.add(username);
	}

	public List<String> getUsers() {
		return users;
	}

	public abstract boolean add(Permit permit);

	public abstract boolean remove(Permit permit);

	public abstract boolean isEmpty();

	public abstract boolean isAllow(int id);

	public abstract String buildXML();

	public abstract boolean parseXML(Element elem);
}
