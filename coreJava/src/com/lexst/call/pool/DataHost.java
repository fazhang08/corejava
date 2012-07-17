/**
 * 
 */
package com.lexst.call.pool;

import com.lexst.site.*;
import com.lexst.util.host.*;

final class DataHost extends SiteHost {

	private static final long serialVersionUID = -1829305860636534941L;
	
	private byte rank = 0;
	
	/**
	 * 
	 */
	public DataHost() {
		super();
	}

	/**
	 * @param host
	 * @param rank
	 */
	public DataHost(SiteHost host, byte rank) {
		super(host);
		this.setRank(rank);
	}

	public void setRank(byte value) {
		this.rank = value;
	}

	public byte getRank() {
		return this.rank;
	}

	public boolean isPrime() {
		return rank == RankSite.PRIME_SITE;
	}

	public boolean isSlave() {
		return rank == RankSite.SLAVE_SITE;
	}

}