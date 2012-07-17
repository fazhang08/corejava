/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * chunk identity class(global only one)
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

import java.io.*;


public class ChunkIdentity implements Serializable, Comparable<ChunkIdentity> {

	private static final long serialVersionUID = -1405898991090363938L;
	
	/* chunk identity */
	private long identity;

	/**
	 * 
	 */
	public ChunkIdentity() {
		super();
		this.identity = 0L;
	}

	/**
	 * 
	 * @param id
	 */
	public ChunkIdentity(long id) {
		this();
		this.setId(id);
	}

	/**
	 * only one
	 * @param id
	 */
	public void setId(long id) {
		this.identity = id;
	}

	public long getId() {
		return this.identity;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if(arg == this) return true;
		else if(arg == null || !(arg instanceof ChunkIdentity)) {
			return false;
		}
		ChunkIdentity ci = (ChunkIdentity)arg;
		return ci.identity == identity;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (int) ((identity >>> 32) ^ identity);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ChunkIdentity arg) {
		return (identity < arg.identity ? -1 : (identity > arg.identity ? 1 : 0));
	}

}