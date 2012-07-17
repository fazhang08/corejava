/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com, All rights reserved
 * 
 * index table
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 1/2/2010
 * 
 * @see com.lexst.db.chunk
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.chunk;

import java.io.*;
import java.util.*;

import com.lexst.db.schema.*;

/**
 *
 * 一台主机的单表索引数据
 */
public final class IndexTable implements Serializable {

	private static final long serialVersionUID = 1L;

	/* table space */
	private Space space;

	/* chunk identity -> chunk index set */
	private Map<Long, ChunkSheet> mapSheet = new TreeMap<Long, ChunkSheet>();

	/**
	 *
	 */
	public IndexTable() {
		super();
	}

	/**
	 * @param s
	 */
	public IndexTable(Space s) {
		this();
		this.setSpace(s);
	}

	public void setSpace(Space s) {
		this.space = new Space(s);
	}

	public Space getSpace() {
		return this.space;
	}

	/**
	 * @param sheet
	 * @return
	 */
	public boolean add(ChunkSheet sheet) {
		long chunkId = sheet.getId();
		if (mapSheet.containsKey(chunkId)) {
			return false;
		}
		return mapSheet.put(chunkId, sheet) == null;
	}

	/**
	 * @param chunkId
	 * @return
	 */
	public boolean remove(long chunkId) {
		return mapSheet.remove(chunkId) != null;
	}

	/**
	 * @param chunkId
	 * @return
	 */
	public boolean contains(long chunkId) {
		return mapSheet.get(chunkId) != null;
	}

	/**
	 * @return
	 */
	public Set<Long> keys() {
		return mapSheet.keySet();
	}

	/**
	 * @param chunkId
	 * @return
	 */
	public ChunkSheet find(long chunkId) {
		return mapSheet.get(chunkId);
	}

	/**
	 *
	 * @return
	 */
	public Collection<ChunkSheet> list() {
		return mapSheet.values();
	}

	/**
	 * clear data
	 */
	public void clear() {
		mapSheet.clear();
	}

	public int size() {
		return mapSheet.size();
	}

	public boolean isEmpty() {
		return mapSheet.isEmpty();
	}
}