/**
 *
 */
package com.lexst.site.log;

import java.util.*;

import com.lexst.site.*;
import com.lexst.util.host.*;

public class LogSite extends Site {
	
	private static final long serialVersionUID = 6178473587173788189L;

	// node id -> node address
	private Map<Integer, LogNode> mapNode = new TreeMap<Integer, LogNode>();

	// refresh count
	private int count;

	/**
	 *
	 */
	public LogSite() {
		super(Site.LOG_SITE);
		this.count = 0;
	}
	
	public LogSite(SiteHost host) {
		this();
		this.setHost(host);
	}

	public void addCount(int num) {
		count += num;
	}
	public int getCount() {
		return count;
	}

	public boolean add(LogNode node) {
		return mapNode.put(node.getType(), node) == null;
	}

	public boolean remove(int type) {
		return mapNode.remove(type) != null;
	}

	public LogNode find(int type) {
		return mapNode.get(type);
	}

	public boolean contains(int type) {
		return mapNode.containsKey(type);
	}

	public Collection<LogNode> list() {
		return mapNode.values();
	}

	public boolean isEmpty() {
		return mapNode.isEmpty();
	}

	public int size() {
		return mapNode.size();
	}

	public void clear() {
		mapNode.clear();
	}

}
