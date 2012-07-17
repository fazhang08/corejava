/**
 * 
 */
package com.lexst.data.pool;

import java.util.*;

import com.lexst.log.client.*;
import com.lexst.pool.*;

/**
 * 
 * count "select" request
 *
 */
public class SelectPool extends Pool {
	
	private static SelectPool selfHandle = new SelectPool();
	
	private Map<Long, SelectChunk> mapChunks = new TreeMap<Long, SelectChunk>();

	/**
	 * 
	 */
	private SelectPool() {
		super();
	}
	
	/**
	 * @return
	 */
	public static SelectPool getInstance() {
		return SelectPool.selfHandle;
	}
	
	public void add(long chunkid) {
		SelectChunk chunk = mapChunks.get(chunkid);
		if(chunk == null) {
			chunk = new SelectChunk(chunkid);
			mapChunks.put(chunkid, chunk);
		} else {
			chunk.increase();
		}
	}
	
	public void add(long chunkid, long usedtime) {
		
	}
	
	public void add(long[] chunkIds, long usedtime) {
		
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("SelectPool.process, into...");
		while (!super.isInterrupted()) {
			this.delay(5000);
		}
		Logger.info("SelectPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {

	}

}