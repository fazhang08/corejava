/**
 * 
 */
package com.lexst.data.pool;


public class SelectChunk implements Comparable<SelectChunk> {
	
	// chunkid
	private long chunkid;
	// begin time
	private long begin;
	// "select" count
	private int count;

	/**
	 * 
	 */
	public SelectChunk(long id) {
		chunkid = id;
		begin = System.currentTimeMillis();
		count++;
	}
	
	public void setId(long id) {
		chunkid = id;
	}
	
	public long getId() {
		return chunkid;
	}
	
	public long lapse() {
		return System.currentTimeMillis() - begin;
	}
	
	public int increase() {
		return count++;
	}
	
	public int count() {
		return count;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SelectChunk o) {
		if(chunkid < o.chunkid) {
			return -1;
		} else if(chunkid > o.chunkid) {
			return 1;
		}
		return 0;
	}

}
