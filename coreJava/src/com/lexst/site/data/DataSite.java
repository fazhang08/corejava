/**
 *
 */
package com.lexst.site.data;

import java.util.*;

import com.lexst.db.chunk.*;
import com.lexst.db.schema.*;
import com.lexst.site.*;
import com.lexst.util.naming.*;

public class DataSite extends RankSite {

	private static final long serialVersionUID = -4596350053905504921L;

	/* disk space size */
	private long usedSize;

	private long freeSize;

	/* database index */
	private IndexSchema schema = new IndexSchema();
	
	/* diffuse task naming */
	private List<Naming> array = new ArrayList<Naming>();

	/**
	 * default constructor
	 */
	public DataSite() {
		super(Site.DATA_SITE);
		usedSize = freeSize = 0L;
	}

	/**
	 * @param rank
	 */
	public DataSite(byte rank) {
		this();
		this.setRank(rank);
	}
	
	public boolean addNaming(String naming) {
		Naming s = new Naming(naming);
		if (!array.contains(s)) {
			return array.add(s);
		}
		return false;
	}

	public boolean removeNaming(String naming) {
		return array.remove(new Naming(naming));
	}

	public int addAllNaming(Collection<Naming> set) {
		int count = 0;
		for (Naming naming : set) {
			array.remove(naming);
			if (array.add(naming)) count++;
		}
		return count;
	}
	
	public void clearAllNaming() {
		array.clear();
	}
	
	public List<Naming> listNaming() {
		return array;
	}

	public long getUsable() {
		return this.usedSize;
	}
	public void setUsable(long s) {
		this.usedSize = s;
	}

	public long getFree() {
		return this.freeSize;
	}
	public void setFree(long s) {
		this.freeSize = s;
	}

	public void setIndexSchema(IndexSchema db) {
		this.schema = db;
	}

	public IndexSchema getIndexSchema() {
		return this.schema;
	}

	/**
	 * @param space
	 * @return
	 */
	public boolean contains(Space space) {
		return schema.contains(space);
	}

	/**
	 * clear database index
	 */
	public void clear() {
		schema.clear();
	}
}