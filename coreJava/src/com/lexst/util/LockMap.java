/**
 *
 */
package com.lexst.util;

import java.io.*;
import java.util.*;

import com.lexst.util.lock.*;

public class LockMap<K, V> implements Serializable {
	private static final long serialVersionUID = 1L;

	private Map<K, V> map;
	private MutexLock lock = new MutexLock();
	
	/**
	 * @param capacity
	 * @param tree
	 */
	public LockMap(int capacity, boolean tree) {
		super();
		if (tree) {
			map = new TreeMap<K, V>();
		} else {
			if (capacity < 16) capacity = 16;
			map = new HashMap<K, V>(capacity);
		}
	}

	/**
	 *
	 */
	public LockMap() {
		this(0, true);
	}

	/**
	 * constract a space
	 * @param capacity
	 */
	public LockMap(int capacity) {
		this(capacity, false);
	}

	/**
	 * return a set
	 * @return
	 */
	public Set<K> keySet() {
		Set<K> set = new TreeSet<K>();
		lock.lockSingle();
		try {
			if (!map.isEmpty()) {
				set.addAll(map.keySet());
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return set;
	}

	public Collection<V> values() {
		List<V> array = null;
		lock.lockSingle();
		try {
			if (map.isEmpty()) {
				array = new ArrayList<V>();
			} else {
				array = new ArrayList<V>(map.values());
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return array;
	}

	public boolean containsKey(Object key) {
		lock.lockMulti();
		try {
			return map.containsKey(key);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return false;
	}

	public boolean containsValue(Object value) {
		lock.lockMulti();
		try {
			return map.containsValue(value);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return false;
	}

	public V get(Object key) {
		lock.lockMulti();
		try {
			return map.get(key);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return null;
	}

	public V put(K key, V value) {
		lock.lockSingle();
		try {
			return map.put(key, value);
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return null;
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		lock.lockSingle();
		try {
			map.putAll(m);
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
	}

	public V remove(Object key) {
		lock.lockSingle();
		try {
			return map.remove(key);
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return null;
	}

	public void clear() {
		lock.lockSingle();
		try {
			map.clear();
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
	}

	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K, V>> set = null;
		lock.lockSingle();
		try {
			if(map.getClass() == HashMap.class) {
				set = new HashSet<Map.Entry<K, V>>(map.entrySet());
			} else {
				set = new TreeSet<Map.Entry<K, V>>(map.entrySet());
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return set;
	}

	public int size() {
		lock.lockSingle();
		try {
			return map.size();
		} catch (Throwable expt) {

		} finally {
			lock.unlockSingle();
		}
		return 0;
	}

	public boolean isEmpty() {
		return size() == 0;
	}
}