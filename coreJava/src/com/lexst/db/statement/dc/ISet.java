/**
 * 
 */
package com.lexst.db.statement.dc;

import java.util.*;

final class ISet {

	private Set<Integer> array = new TreeSet<Integer>();

	/**
	 * 
	 */
	public ISet() {
		super();
	}
	
	public ISet(int mod) {
		this();
		this.add(mod);
	}
	
	public ISet(int mod1, int mod2) {
		this();
		this.add(mod1);
		this.add(mod2);
	}
	
	public ISet(ISet set, int mod) {
		this(mod);
		this.add(set);
	}

	public boolean add(int value) {
		return array.add(value);
	}
	
	public boolean add(ISet set) {
		return array.addAll(set.array);
	}

	public Set<Integer> set() {
		return array;
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public int size() {
		return array.size();
	}
}
