/**
 *
 */
package com.lexst.db.charset;

import java.io.Serializable;
import java.util.*;


public class SQLCharmap implements Serializable {
	private static final long serialVersionUID = 1L;

	// keyword -> sql char type
	private Map<String, SQLCharType> mapType = new HashMap<String, SQLCharType>();

	/**
	 *
	 */
	public SQLCharmap() {
		super();
	}

	/**
	 * save a object
	 * @param type
	 * @return
	 */
	public boolean add(SQLCharType type) {
		String name = type.getName();
		String low = name.toLowerCase();
		return mapType.put(low, type) == null;
	}

	/**
	 * find a object
	 * @param name
	 * @return
	 */
	public SQLCharType find(String name) {
		if (name == null) return null;
		String low = name.toLowerCase();
		return mapType.get(low);
	}

	public SQLCharType findClass(String clsname) {
		for(SQLCharType mode : mapType.values()) {
			String name = mode.getClassName();
			if(clsname.equals(name)) {
				return mode;
			}
		}
		return null;
	}

	public Set<String> keys() {
		return mapType.keySet();
	}

	public void clear() {
		mapType.clear();
	}

	public boolean isEmpty() {
		return mapType.isEmpty();
	}

	public int size() {
		return mapType.size();
	}
}