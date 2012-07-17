/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * chunk entity class
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 2/2/2009
 * 
 * @see com.lexst.db.chunk
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.chunk;

/**
 * chunk information
 *
 */
public class Chunk extends ChunkIdentity {
	
	private static final long serialVersionUID = -7964429659909147714L;

	/* chunk file length */
	private long length;

	/* chunk file last time */
	private long lastModified;
	
	/* md5 identity */
	private long md5low;
	private long md5high;

	/**
	 * 
	 */
	public Chunk() {
		super();
	}
	
	/**
	 * @param id
	 * @param length
	 * @param modified
	 */
	public Chunk(long id, long length, long modified) {
		this();
		this.setId(id);
		this.setLength(length);
		this.setLastModified(modified);
	}
	
	public void setLength(long len) {
		this.length = len;
	}
	public long getLength() {
		return this.length;
	}
	
	public void setLastModified(long time) {
		this.lastModified = time;
	}

	public long getLastModified() {
		return this.lastModified;
	}
	
	public void setMD5(long low, long high) {
		this.md5low = low;
		this.md5high = high;
	}

	public long getMD5High() {
		return this.md5high;
	}

	public long getMD5Low() {
		return this.md5low;
	}

}