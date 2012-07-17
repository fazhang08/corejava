/**
 * 
 */
package com.lexst.home.pool;

import com.lexst.db.schema.*;
import com.lexst.util.host.*;

final class UpgradeSpace {
	
	private SiteHost host;
	private Space space;
	private long[] oldIds;
	private long[] newIds;
	
	/**
	 * 
	 */
	public UpgradeSpace(SiteHost host, Space space, long[] oldids, long[] newids) {
		super();
		this.setHost(host);
		this.setSpace(space);
		this.setOldChunkIds(oldids);
		this.setNewChunkIds(newids);
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

	public void setOldChunkIds(long[] s) {
		this.oldIds = s;
	}
	public long[] getOldChunkIds() {
		return this.oldIds;
	}

	public void setNewChunkIds(long[] s) {
		this.newIds = s;
	}
	public long[] getNewChunkIds() {
		return this.newIds;
	}

}
