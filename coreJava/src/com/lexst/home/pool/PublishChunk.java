/**
 * 
 */
package com.lexst.home.pool;

import com.lexst.db.schema.*;
import com.lexst.util.host.*;


final class PublishChunk {

	private SiteHost remote;
	private Space space;
	private long chunkId;
	private long length;
	
	/**
	 * 
	 */
	public PublishChunk(SiteHost host, Space space, long chunkId, long length) {
		super();
		this.setHost(host);
		this.setSpace(space);
		this.setChunkId(chunkId);
		this.setLength(length);
	}
	
	public void setHost(SiteHost host) {
		remote = new SiteHost(host);
	}
	public SiteHost getHost() {
		return remote;
	}
	
	public void setSpace(Space s) {
		this.space = new Space(s);
	}
	public Space getSpace() {
		return this.space;
	}
	
	public void setChunkId(long cid) {
		this.chunkId = cid;
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
}