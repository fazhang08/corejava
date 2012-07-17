/**
 * 
 */
package com.lexst.data.pool;

import java.util.*;


final class Bunket {
	
	private int id;
	private String filename;
	private boolean finished;
	
//	private LockArray<BunketNode> array = new LockArray<BunketNode>();
	
	private ArrayList<BunketNode> array = new ArrayList<BunketNode>(50);
	
	/**
	 * 
	 */
	public Bunket(int id, String filename) {
		super();
		this.setID(id);
		this.setFilename(filename);
		this.finished = false;
	}
	
	public void setID(int i) {
		this.id = i;
	}
	public int getID() {
		return this.id;
	}
	
	public void setFilename(String s) {
		this.filename = s;
	}
	public String getFilename() {
		return this.filename;
	}
	
	public void setFinished(boolean b) {
		this.finished = b;
	}
	public boolean isFinished() {
		return this.finished;
	}
	
	public boolean add(BunketNode trip) {
		return array.add(trip);
	}

	public BunketNode poll() {
		if (array.size() > 0) {
			return array.remove(0);
		}
		return null;
	}

	public int size() {
		return array.size();
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

}