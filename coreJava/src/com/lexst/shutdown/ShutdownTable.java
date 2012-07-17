/**
 *
 */
package com.lexst.shutdown;

import java.util.ArrayList;
import java.util.List;


public final class ShutdownTable {

	// static handle
	private static ShutdownTable selfHandle = new ShutdownTable();

	// accepted ip address
	private List<String> array = new ArrayList<String>(20);

	/**
	 *
	 */
	private ShutdownTable() {
		super();
	}

	/**
	 * return a static instance
	 * @return
	 */
	public static ShutdownTable getInstance() {
		return ShutdownTable.selfHandle;
	}

	public List<String> list() {
		return this.array;
	}

	public boolean contains(String ip) {
		return this.array.contains(ip);
	}

	public void add(String ip) {
		this.array.add(ip);
	}

	public void add(String[] ips) {
		for(int i =0; i<ips.length; i++) {
			array.add(ips[i]);
		}
	}

	public void clear() {
		array.clear();
	}

	public int size() {
		return array.size();
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}
}
