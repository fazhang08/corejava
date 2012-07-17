/**
 * 
 */
package com.lexst.data.pool;

import com.lexst.db.schema.*;

final class PrimeChunk {

	private Space space;

	private long chunkId;
	
	private long length;

	private String filename;

	/**
	 * 
	 */
	public PrimeChunk(Space space, long chunkid, long length) {
		super();
		this.setSpace(space);
		this.setChunkId(chunkid);
		this.setLength(length);
	}

	public void setSpace(Space s) {
		this.space = new Space(s);
	}

	public Space getSpace() {
		return space;
	}

	public void setChunkId(long id) {
		this.chunkId = id;
	}

	public long getChunkId() {
		return this.chunkId;
	}
	
	public void setLength(long len) {
		this.length = len;
	}

	public long getLength() {
		return this.length;
	}

	public void setFilename(String s) {
		filename = s;
	}

	public String getFilename() {
		return filename;
	}
}
