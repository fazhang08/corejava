/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com, All rights reserved
 * 
 * index module
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 1/2/2010
 * 
 * @see com.lexst.db.chunk
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.chunk;

import java.io.*;
import java.util.*;

import com.lexst.db.index.*;
import com.lexst.db.index.range.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.db.view.*;
import com.lexst.log.client.*;
import com.lexst.util.host.*;

/**
 *
 * 同一个表,但是分布于不同主机上的索引记录
 */
public class IndexModule implements Serializable {
	
	private static final long serialVersionUID = -6439624911505613690L;

	/* user table space */
	private Space space;

	/* column id -> index set */
	private Map<Short, View> mapView = new TreeMap<Short, View>();

	/**
	 *
	 */
	public IndexModule() {
		super();
	}

	/**
	 * @param space
	 */
	public IndexModule(Space space) {
		this();
		this.setSpace(space);
	}

	/**
	 * index space
	 * @param s
	 */
	public void setSpace(Space s) {
		space = new Space(s);
	}
	public Space getSpace() {
		return space;
	}

	public Set<Short> keySet() {
		return mapView.keySet();
	}

	/**
	 * @param condi
	 * @param all
	 * @return
	 */
	public int find(Condition condi, ChunkIdentitySet all) {
		int count = 0;
		for(int i = 0; condi != null; i++) {
			ChunkIdentitySet set = new ChunkIdentitySet();
			int size = select(condi, set);

			switch (condi.getOutsideRelate()) {
			case Condition.AND:
				if (size < 0) return -1;
				all.AND(set);
				count += size;
				break;
			case Condition.OR:
				if (size > 0) {
					all.OR(set);
					count += size;
				}
				break;
			default:
				if (i == 0) {
					all.add(set);
					count = size;
				} else {
					throw new IllegalArgumentException("invalid condition relate");
				}
			}
			// next level condition
			condi = condi.getNext();
		}
		return count;
	}

	private int select(Condition condi, ChunkIdentitySet all) {
		int count = 0;
		while (condi != null) {
			IndexColumn column = condi.getValue();
			short columnId = column.getColumnId();
			View view = mapView.get(columnId);
			if (view == null) {
				Logger.error("IndexModule.select, cannot find %d column id", columnId);
				return 0;
			}
			Set<Long> set = view.find(condi);
			if (!set.isEmpty()) {
				if (condi.isAND()) {
					// 保留相同chunk identity
					all.AND(set);
				} else if (condi.isOR()) {
					// 累加
					all.OR(set);
				} else {
					all.add(set);
				}
				count = set.size();
			}
			// 检查子集
			for (Condition sub : condi.getFriends()) {
				int size = this.select(sub, all);
				if (size < 1) return -1;
				count += size;
			}
			condi = condi.getNext();
		}
		return count;
	}

	/**
	 * @param host
	 * @param index
	 * @return
	 */
	public boolean add(SiteHost host, IndexRange index) {
		short columnId = index.getColumnId();
		View view = mapView.get(columnId);
		if (index.isShort()) {
			if (view == null) {
				view = new ShortView();
				mapView.put(columnId, view);
			}
		} else if (index.isInteger()) {
			if (view == null) {
				view = new IntegerView();
				mapView.put(columnId, view);
			}
		} else if (index.isLong()) {
			if (view == null) {
				view = new LongView();
				mapView.put(columnId, view);
			}
		} else if (index.isReal()) {
			if (view == null) {
				view = new RealView();
				mapView.put(columnId, view);
			}
		} else if (index.isDouble()) {
			if (view == null) {
				view = new DoubleView();
				mapView.put(columnId, view);
			}
		} else {
			throw new ClassCastException("illegal index class!");
		}
		// save index
		return view.add(host, index);
	}

	/**
	 * @param host
	 * @return
	 */
	public int remove(SiteHost host) {
		int size = mapView.size();
		if (size == 0) return 0;

		int count = 0;
		ArrayList<Short> a = new ArrayList<Short>(size);
		for (short columnId : mapView.keySet()) {
			View view = mapView.get(columnId);
			if (view != null) {
				count += view.remove(host);
				if (view.isEmpty()) a.add(columnId);
			} else {
				a.add(columnId);
			}
		}
		for (short columnId : a) {
			mapView.remove(columnId);
		}
		return count;
	}

	/**
	 * @param host
	 * @return
	 */
	public List<Long> delete(SiteHost host) {
		int size = mapView.size();
		if(size == 0) return null;

		ArrayList<Long> array = new ArrayList<Long>(1024);
		ArrayList<Short> a = new ArrayList<Short>(size);
		for(short columnId : mapView.keySet()) {
			View view = mapView.get(columnId);
			if(view == null) {
				a.add(columnId);
			} else {
				List<Long> list = view.delete(host);
				if (list != null) {
					array.addAll(list);
				}
				if(view.isEmpty()) a.add(columnId);
			}
		}
		for (short columnId : a) {
			mapView.remove(columnId);
		}
		return array;
	}

	/**
	 * @param columnId
	 * @return
	 */
	public View find(short columnId) {
		return mapView.get(columnId);
	}

	public int size() {
		return mapView.size();
	}

	public boolean isEmpty() {
		return mapView.isEmpty();
	}
}