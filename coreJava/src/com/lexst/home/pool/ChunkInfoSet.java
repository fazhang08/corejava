/**
 * 
 */
package com.lexst.home.pool;

import java.util.*;

import com.lexst.db.chunk.*;

final class ChunkInfoSet {
	
	private Map<Long, Chunk> map = new TreeMap<Long, Chunk>();
	
	public ChunkInfoSet() {
		super();
	}
	
	public boolean add(Chunk info) {
		return map.put(info.getId(), info) == null;
	}

	public Chunk find(long chunkid) {
		return map.get(chunkid);
	}
}
