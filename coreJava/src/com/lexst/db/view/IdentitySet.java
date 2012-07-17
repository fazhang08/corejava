/**
 *
 */
package com.lexst.db.view;

import java.util.*;

import com.lexst.util.host.*;

final class IdentitySet {

	/* chunk identity -> site host set */
	private Map<Long, SiteSet> map = new TreeMap<Long, SiteSet>();

	/**
	 *
	 */
	public IdentitySet() {
		super();
	}

	/**
	 * add site host and chunk id
	 * @param host
	 * @param chunkid
	 * @return
	 */
	public boolean add(SiteHost host, long chunkid) {
		SiteSet set = map.get(chunkid);
		if (set == null) {
			set = new SiteSet();
			map.put(chunkid, set);
		}
		return set.add(host);
	}

	/**
	 * remove site host and chunk id
	 * @param host
	 * @param chunkid
	 * @return
	 */
	public boolean remove(SiteHost host, long chunkid) {
		SiteSet set = map.get(chunkid);
		if (set == null) {
			return false;
		}
		boolean success = set.remove(host);
		if (success && set.isEmpty()) {
			map.remove(chunkid);
		}
		return success;
	}

	/**
	 *
	 * @param host
	 * @return
	 */
	public int remove(SiteHost host) {
		int count = 0;
		ArrayList<Long> a = new ArrayList<Long>(map.size());
		for (long chunkid : map.keySet()) {
			SiteSet set = map.get(chunkid);
			if (set != null) {
				if (set.remove(host)) count++;
				if (set.isEmpty()) a.add(chunkid);
			} else {
				a.add(chunkid);
			}
		}
		// remove old record
		for (long chunkid : a) {
			map.remove(chunkid);
		}
		return count;
	}

	/**
	 * @param host
	 * @param array
	 * @return
	 */
	public int remove(SiteHost host, List<Long> array) {
		int count = 0;
		ArrayList<Long> a = new ArrayList<Long>(map.size());
		for (long chunkid : map.keySet()) {
			SiteSet set = map.get(chunkid);
			if (set == null) {
				a.add(chunkid);
				continue;
			}
			// remove match
			if (set.remove(host)) {
				array.add(chunkid);
				count++;
			}
			if (set.isEmpty()) {
				a.add(chunkid);
			}
		}
		for(long chunkId : a) {
			map.remove(chunkId);
		}
		return count;
	}

	/**
	 * @return
	 */
	public Set<Long> keySet() {
		return map.keySet();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public int size() {
		return map.size();
	}

}