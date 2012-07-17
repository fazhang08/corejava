/**
 * 
 */
package com.lexst.data.pool;

import com.lexst.db.schema.*;

public class EntityIdentify {

	private Space space;
	private long chunkid;
	
	/**
	 * 
	 */
	public EntityIdentify(String db, String table, long id) {
		space = new Space(db, table);
		chunkid = id;
	}

	public Space getSpace() {
		return space;
	}
	
	public long getChunkId() {
		return chunkid;
	}
	
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != EntityIdentify.class) return false;
		if (arg == this) return true;

		EntityIdentify ei = (EntityIdentify) arg;
		return chunkid == ei.chunkid && space.equals(ei.space);
	}
	
	public int hashCode() {
		return (int)(space.hashCode() ^ chunkid);
	}
}
