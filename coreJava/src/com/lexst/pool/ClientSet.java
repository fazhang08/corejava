/**
 * 
 */
package com.lexst.pool;

import java.util.*;

import com.lexst.remote.client.RemoteClient;

public class ClientSet {
	
	public static int LIMIT = 3;

	private List<RemoteClient> array = new ArrayList<RemoteClient>(ClientSet.LIMIT);

	private int index;

	/**
	 * 
	 */
	public ClientSet() {
		super();
		index = 0;
	}

	public boolean add(RemoteClient client) {
		return array.add(client);
	}

	public boolean remove(RemoteClient client) {
		return array.remove(client);
	}

	public List<RemoteClient> list() {
		return array;
	}
	
	/**
	 * @param i
	 * @return
	 */
	public RemoteClient get(int i) {
		if (i < 0 || i >= array.size()) {
			throw new ArrayIndexOutOfBoundsException("invald index:" + i);
		}
		return array.get(i);
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}
	
	public int size() {
		return array.size();
	}
	
	public RemoteClient[] toArray() {
		int size = array.size();
		if (size == 0) return null;
		RemoteClient[] all = new RemoteClient[size];
		return array.toArray(all);
	}

	/**
	 * apply next client object
	 * @return
	 */
	public synchronized RemoteClient next() {
		int size = array.size();
		if (size > 0) {
			if (index >= size) index = 0;
			return array.get(index++);
		}
		return null;
	}
	
	/**
	 * request next client and lock it
	 * @return
	 */
	public RemoteClient lockNext() {
		int size = array.size();
		for(int i = 0; i < size; i++) {
			RemoteClient client = next();
			if (client != null && !client.isLocked()) {
				if (client.lock()) return client;
			}
		}
		return null;
	}
	
	/**
	 * apply client object
	 * @param excludes
	 * @return
	 */
	public RemoteClient next(Collection<RemoteClient> excludes) {
		// find first object
		RemoteClient first = next();
		boolean match = false;
		for (RemoteClient client : excludes) {
			if (match = (client == first)) break;
		}
		if (!match) return first;
		// find second or since object
		while (true) {
			RemoteClient second = next();
			if (second == first) break;
			match = false;
			for (RemoteClient client : excludes) {
				if(match = (client == second)) break;
			}
			if (!match) return second;
		}
		return null;
	}

}