/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;

import com.lexst.db.schema.*;
import com.lexst.util.host.*;

final class SlaveChunk implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private SiteHost host;

	private Space space;

	private long chunkid;
	
	private long length;

	/**
	 * 
	 */
	public SlaveChunk(SiteHost host, Space space, long chunkId, long length) {
		super();
		this.setHost(host);
		this.setSpace(space);
		this.setChunkId(chunkId);
		this.setLength(length);
	}

	public void setHost(SiteHost s) {
		this.host = s;
	}

	public SiteHost getHost() {
		return this.host;
	}

	public void setSpace(Space s) {
		space = new Space(s);
	}

	public Space getSpace() {
		return space;
	}

	public void setChunkId(long id) {
		this.chunkid = id;
	}

	public long getChunkId() {
		return this.chunkid;
	}
	
	public void setLength(long len) {
		this.length = len;
	}

	public long getLength() {
		return this.length;
	}

	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != SlaveChunk.class) {
			return false;
		}
		SlaveChunk chunk = (SlaveChunk) arg;
		return chunkid == chunk.chunkid;
	}

	public int hashCode() {
		return (int) ((chunkid >>> 32) ^ chunkid);
	}
}
