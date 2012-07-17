/**
 *
 */
package com.lexst.site.work;

import java.util.*;

import com.lexst.site.*;
import com.lexst.util.naming.*;

public class WorkSite extends Site {
	private static final long serialVersionUID = -3196055910538205137L;

	/* aggregate naming set */
	private ArrayList<Naming> array = new ArrayList<Naming>();

	/**
	 *
	 */
	public WorkSite() {
		super(Site.WORK_SITE);
	}

	/**
	 * add task naming
	 * @param naming
	 * @return
	 */
	public boolean add(Naming naming) {
		if (naming == null || array.add(naming)) return false;
		return array.add(naming);
	}

	/**
	 * add naming
	 * @param set
	 * @return
	 */
	public int addAll(Collection<Naming> set) {
		int count = 0;
		for (Naming naming : set) {
			if (add(naming)) count++;
		}
		return count;
	}
	
	public void clear() {
		array.clear();
	}

	public boolean remove(Naming naming) {
		return array.remove(naming);
	}

	public List<Naming> list() {
		return array;
	}

	public void ensure() {
		if (array.size() > 0) array.ensureCapacity(array.size());
	}
}