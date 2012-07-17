/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com, All rights reserved
 * 
 * index database
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
 * 一台主机的数据块集合 
 */
public class IndexSchema implements Serializable {
	
	private static final long serialVersionUID = -3447481520473031275L;
	
	/* table space -> index table set */
	private Map<Space, IndexTable> mapTable = new TreeMap<Space, IndexTable>();

	/**
	 *
	 */
	public IndexSchema() {
		super();
	}

	/**
	 * add a space
	 * @param space
	 * @return
	 */
	public boolean add(Space space) {
		IndexTable table = mapTable.get(space);
		if (table == null) {
			table = new IndexTable(space);
			mapTable.put(space, table);
			return true;
		}
		return false;
	}

	/**
	 * @param sheet
	 * @return
	 */
	public boolean add(Space space, ChunkSheet sheet) {
		IndexTable table = mapTable.get(space);
		if (table == null) {
			table = new IndexTable(space);
			mapTable.put(space, table);
		}
		return table.add(sheet);
	}

	/**
	 * @param chunkid
	 * @return
	 */
	public boolean remove(Space space, long chunkid) {
		IndexTable table = mapTable.get(space);
		if (table != null) {
			table.remove(chunkid);
			if (table.isEmpty()) {
				mapTable.remove(space);
			}
			return true;
		}
		return false;
	}

	/**
	 * remove a space
	 * @param space
	 * @return
	 */
	public boolean remove(Space space) {
		return mapTable.remove(space) != null;
	}

	/**
	 * @param space
	 * @param chunkid
	 * @return
	 */
	public boolean contains(Space space, long chunkid) {
		IndexTable table = mapTable.get(space);
		if (table != null) {
			return table.contains(chunkid);
		}
		return false;
	}

	/**
	 * @param space
	 * @return
	 */
	public boolean contains(Space space) {
		return mapTable.containsKey(space);
	}

	/**
	 * @return
	 */
	public Set<Space> keySet() {
		HashSet<Space> set = new HashSet<Space>();
		set.addAll(mapTable.keySet());
		return set;
	}

	/**
	 * @param space
	 * @return
	 */
	public IndexTable find(Space space) {
		return mapTable.get(space);
	}

	public Collection<IndexTable> list() {
		return mapTable.values();
	}
	
	public int countChunk() {
		int count = 0;
		for (IndexTable table : mapTable.values()) {
			count += table.size();
		}
		return count;
	}

	/**
	 * clear data
	 */
	public void clear() {
		mapTable.clear();
	}

	public int size() {
		return mapTable.size();
	}

	public boolean isEmpty() {
		return mapTable.isEmpty();
	}
}