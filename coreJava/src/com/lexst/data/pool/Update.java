/**
 * 
 */
package com.lexst.data.pool;

import java.util.*;

import com.lexst.db.schema.*;
import com.lexst.util.host.*;

final class Update implements Comparable<Update> {

	private Space space;

	private List<SiteHost> array;
	
	private int count;

	/**
	 * 
	 * @param space
	 */
	public Update(Space space) {
		super();
		array = new ArrayList<SiteHost>();
		count = Integer.MIN_VALUE;
		setSpace(space);
	}

	public void setSpace(Space s) {
		space = new Space(s);
	}

	public Space getSpace() {
		return space;
	}

	public void add(SiteHost host) {
		if (host != null && !array.contains(host)) {
			array.add(host);
		}
	}

	public List<SiteHost> list() {
		return array;
	}
	
	public void reset() {
		this.count = 0;
	}
	
	public void add(int num) {
		count += num;
	}
	
	public int count() {
		return count;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != Update.class) {
			return false;
		}
		Update object = (Update) arg;
		return space.equals(object.space);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return space.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Update object) {
		return space.compareTo(object.space);
	}
}
