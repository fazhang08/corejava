/**
 *
 */
package com.lexst.db.chunk;

import java.util.*;
import java.io.Serializable;

public class ChunkIdentitySet implements Serializable {

	private static final long serialVersionUID = 2952715349606849839L;

	/* chunk identity array */
	private Set<Long> array = new TreeSet<Long>();

	/**
	 *
	 */
	public ChunkIdentitySet() {
		super();
	}

	/**
	 * @param chunkId
	 */
	public ChunkIdentitySet(long chunkId) {
		this();
		this.add(chunkId);
	}

	/**
	 * @param chunkIds
	 */
	public ChunkIdentitySet(long[] chunkIds) {
		this();
		this.add(chunkIds);
	}

	public boolean add(long chunkId) {
		return chunkId != 0L && array.add(chunkId);
	}

	public boolean add(Set<Long> set) {
		return array.addAll(set);
	}

	public int add(long[] chunkIds) {
		int count = 0;
		for (int i = 0; chunkIds != null && i < chunkIds.length; i++) {
			if (chunkIds[i] != 0L && array.add(chunkIds[i]))
				count++;
		}
		return count;
	}

	public boolean add(ChunkIdentitySet set) {
		return array.addAll(set.array);
	}

	/**
	 * 保留相同的,不同的取消
	 * 
	 * @param set
	 */
	public void AND(Set<Long> set) {
		List<Long> all = new ArrayList<Long>(array.size());
		for (long chunkId : set) {
			if (array.contains(chunkId)) {
				all.add(chunkId);
			}
		}
		array.addAll(all);
	}

	public void AND(ChunkIdentitySet set) {
		this.AND(set.array);
	}

	/**
	 * 重叠的保留一个,不重叠的也保留
	 * 
	 * @param set
	 */
	public void OR(Set<Long> set) {
		array.addAll(set);
	}

	public void OR(ChunkIdentitySet set) {
		this.OR(set.array);
	}

	public Set<Long> list() {
		return array;
	}

	public boolean exists(long chunkId) {
		return array.contains(chunkId);
	}

	public boolean contains(long chunkId) {
		return array.contains(chunkId);
	}

	public boolean remove(long chunkId) {
		return array.remove(chunkId);
	}

	public int remove(long[] chunkIds) {
		int count = 0;
		for (int i = 0; chunkIds != null && i < chunkIds.length; i++) {
			if (array.remove(chunkIds[i]))
				count++;
		}
		return count;
	}

	public int remove(Set<Long> set) {
		int count = 0;
		for (long chunkId : set) {
			if (array.remove(chunkId))
				count++;
		}
		return count;
	}

	public void clear() {
		array.clear();
	}

	public int size() {
		return array.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public long[] toArray() {
		ArrayList<Long> a = new ArrayList<Long>(array);
		long[] s = new long[a.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = a.get(i);
		}
		return s;
	}

}