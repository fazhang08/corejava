/**
 * 
 */
package com.lexst.data.pool;

import com.lexst.db.schema.*;
import com.lexst.util.host.*;

final class Upgrade {

	// data site(prime node)
	private SiteHost host;
	// table space
	private Space space;

	// old chunkid
	private long[] oldIds;

	// new chunkid
	private long[] newIds;

	/**
	 * 
	 */
	public Upgrade(SiteHost host, Space space, long[] olds, long[] news) {
		super();
		this.setHost(host);
		this.setSpace(space);
		this.setOldChunkIds(olds);
		this.setNewChunkIds(news);
	}

	public void setHost(SiteHost s) {
		this.host = s;
	}

	public SiteHost getHost() {
		return this.host;
	}
	
	public void setSpace(Space s) {
		this.space = new Space(s);
	}

	public Space getSpace() {
		return this.space;
	}

	public void setOldChunkIds(long[] olds) {
		this.oldIds = olds;
	}

	public long[] getOldChunkIds() {
		return this.oldIds;
	}

	public void setNewChunkIds(long[] news) {
		this.newIds = news;
	}

	public long[] getNewChunkIds() {
		return this.newIds;
	}
}
