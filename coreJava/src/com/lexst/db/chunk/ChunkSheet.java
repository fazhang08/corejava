/**
 *
 */
package com.lexst.db.chunk;

import java.io.*;
import java.util.*;

import com.lexst.db.*;
import com.lexst.db.index.range.*;

/**
 *
 * a chunk column information
 */
public final class ChunkSheet implements Serializable, Comparable<ChunkSheet> {

	private static final long serialVersionUID = 1L;

	// chunk identity
	private long chunkid;
	// chunk rank
	private byte rank;
	// chunk status
	private byte status;
	// column number -> index range
	private Map<Short, IndexRange> mapIndex = new TreeMap<Short, IndexRange>();

	/*
	 *
	 */
	protected ChunkSheet() {
		super();
		chunkid = 0L; // invalid id
		rank = 0;
		status = 0;
	}

	/**
	 * @param id
	 */
	public ChunkSheet(long id) {
		this();
		this.setId(id);
	}

	/**
	 * @param id
	 * @param rank
	 * @param status
	 */
	public ChunkSheet(long id, byte rank, byte status) {
		this();
		this.setId(id);
		this.setRank(rank);
		this.setStatus(status);
	}

	public void setId(long id) {
		this.chunkid = id;
	}
	public long getId() {
		return this.chunkid;
	}

	public void setRank(byte value) {
		if (value == Type.PRIME_CHUNK || value == Type.SLAVE_CHUNK) {
			rank = value;
		} else {
			throw new IllegalArgumentException("invalid rank!");
		}
	}

	public byte getRank() {
		return this.rank;
	}

	public void setStatus(byte value) {
		if (value == Type.COMPLETE_CHUNK || value == Type.INCOMPLETE_CHUNK) {
			this.status = value;
		} else {
			throw new IllegalArgumentException("invalid status!");
		}
	}

	public byte getStatus() {
		return this.status;
	}
	
	public boolean isComplete() {
		return status == Type.COMPLETE_CHUNK;
	}

	public boolean isIncomplete() {
		return status == Type.INCOMPLETE_CHUNK;
	}

	/**
	 * add a sign-range
	 * @param range
	 * @return
	 */
	public boolean add(IndexRange range) {
		short columnId = range.getColumnId();
		if (mapIndex.containsKey(columnId)) {
			return false;
		}
		return mapIndex.put(columnId, range) == null;
	}

	/**
	 * list index
	 * @return
	 */
	public Collection<IndexRange> list() {
		return mapIndex.values();
	}

	/**
	 *
	 * @return
	 */
	public Set<Short> keys() {
		return mapIndex.keySet();
	}

	/**
	 * find a index
	 * @param columnId
	 * @return
	 */
	public IndexRange find(short columnId) {
		return mapIndex.get(columnId);
	}

	public void clear() {
		mapIndex.clear();
	}

	public boolean isEmpty() {
		return mapIndex.isEmpty();
	}

	public int size() {
		return mapIndex.size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ChunkSheet chunk) {
		return (chunkid < chunk.chunkid ? -1 : (chunkid == chunk.chunkid ? 0 : 1));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return false;
		}
		if (obj == null || obj.getClass() != ChunkSheet.class) {
			return false;
		}
		ChunkSheet frame = (ChunkSheet) obj;
		return chunkid == frame.chunkid;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (int) (chunkid >>> 32 ^ chunkid);
	}
}