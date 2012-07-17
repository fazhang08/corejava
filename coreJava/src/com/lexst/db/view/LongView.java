/**
 *
 */
package com.lexst.db.view;

import java.util.*;

import com.lexst.db.index.*;
import com.lexst.db.index.range.*;
import com.lexst.db.statement.*;
import com.lexst.util.host.*;
import com.lexst.util.range.*;

public class LongView implements View {

	/* long range( bigindex ) -> chunk identity set */
	private Map<LongRange, IdentitySet> mapSet = new TreeMap<LongRange, IdentitySet>();

	/**
	 * default constructor
	 */
	public LongView() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.view.View#add(com.lexst.util.host.SiteHost, com.lexst.db.sign.range.SignRange)
	 */
	@Override
	public boolean add(SiteHost host, IndexRange index) {
		if(index.getClass() != LongIndexRange.class) {
			throw new ClassCastException("not big index");
		}
		LongIndexRange idx = (LongIndexRange)index;
		long begin = idx.getBegin();
		long end = idx.getEnd();
		LongRange range = new LongRange(begin, end);
		long chunkId = idx.getChunkId();

		IdentitySet set = mapSet.get(range);
		if (set == null) {
			set = new IdentitySet();
			mapSet.put(range, set);
		}
		return set.add(host, chunkId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.view.View#remove(com.lexst.util.host.SiteHost)
	 */
	@Override
	public int remove(SiteHost host) {
		int size = mapSet.size();
		if(size == 0) return 0;

		int count = 0;
		ArrayList<LongRange> a = new ArrayList<LongRange>(size);
		for (LongRange range : mapSet.keySet()) {
			IdentitySet set = mapSet.get(range);
			if (set != null) {
				count += set.remove(host);
				if (set.isEmpty()) a.add(range);
			} else {
				a.add(range);
			}
		}
		for (LongRange range : a) {
			mapSet.remove(range);
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.view.View#delete(com.lexst.util.host.SiteHost)
	 */
	@Override
	public List<Long> delete(SiteHost host) {
		int size = mapSet.size();
		if( size == 0) return null;

		ArrayList<Long> array = new ArrayList<Long>(512);

		ArrayList<LongRange> a = new ArrayList<LongRange>(size);
		for (LongRange range : mapSet.keySet()) {
			IdentitySet set = mapSet.get(range);
			if (set != null) {
				set.remove(host, array);
				if (set.isEmpty()) a.add(range);
			} else {
				a.add(range);
			}
		}
		for (LongRange range : a) {
			mapSet.remove(range);
		}
		return array;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.view.View#remove(com.lexst.util.host.SiteHost, long)
	 */
	@Override
	public int remove(SiteHost host, long chunkId) {
		int size = mapSet.size();
		if(size == 0) return 0;

		int count = 0;
		ArrayList<LongRange> a = new ArrayList<LongRange>(size);
		for (LongRange range : mapSet.keySet()) {
			IdentitySet set = mapSet.get(range);
			if (set != null) {
				boolean success = set.remove(host, chunkId);
				if (success) count++;
				if (set.isEmpty()) a.add(range);
			} else {
				a.add(range);
			}
		}
		for (LongRange range : a) {
			mapSet.remove(range);
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.view.View#find(com.lexst.db.statement.Condition)
	 */
	@Override
	public Set<Long> find(Condition condi) {
		IndexColumn num = condi.getValue();
		if (num == null || num.getClass() != LongIndex.class) {
			throw new IllegalArgumentException("null pointer or invalid type!");
		}
		long value = ((LongIndex) num).getValue();

		// 找到范围分片截止点
		HashSet<Long> all = new HashSet<Long>(1024);
		// 列名在左,参数值在右(固定!, 检查可以的范围)
		switch (condi.getCompare()) {
		case Condition.EQUAL:
			for (LongRange range : mapSet.keySet()) {
				if (range.inside(value)) {
					IdentitySet set = mapSet.get(range);
					all.addAll(set.keySet());
				}
			}
			break;
		case Condition.NOT_EQUAL:
			for(LongRange range : mapSet.keySet()) {
				if (range.getBegin() != value || range.getEnd() != value) {
					IdentitySet set = mapSet.get(range);
					all.addAll(set.keySet());
				}
			}
			break;
		case Condition.LESS:
			for(LongRange range : mapSet.keySet()) {
				if (range.getBegin() < value || range.getEnd() < value) {
					IdentitySet set = mapSet.get(range);
					all.addAll(set.keySet());
				}
			}
			break;
		case Condition.LESS_EQUAL:
			for(LongRange range : mapSet.keySet()) {
				if (range.getBegin() <= value || range.getEnd() <= value) {
					IdentitySet set = mapSet.get(range);
					all.addAll(set.keySet());
				}
			}
			break;
		case Condition.GREATER:
			for(LongRange range : mapSet.keySet()) {
				if (range.getBegin() > value || range.getEnd() > value) {
					IdentitySet set = mapSet.get(range);
					all.addAll(set.keySet());
				}
			}
			break;
		case Condition.GREATER_EQUAL:
			for(LongRange range : mapSet.keySet()) {
				if (range.getBegin() >= value || range.getEnd() >= value) {
					IdentitySet set = mapSet.get(range);
					all.addAll(set.keySet());
				}
			}
			break;
		case Condition.LIKE:
			for (IdentitySet set : mapSet.values()) {
				all.addAll(set.keySet());
			}
			break;
		}
		return all;
	}

	/* (non-Javadoc)
	 * @see com.lexst.call.index.set.IndexSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return mapSet.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.lexst.call.index.set.IndexSet#size()
	 */
	@Override
	public int size() {
		return mapSet.size();
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.view.View#getChunkIds()
	 */
	@Override
	public Set<Long> getChunkIds() {
		Set<Long> all = new TreeSet<Long>();
		for (IdentitySet set : mapSet.values()) {
			all.addAll(set.keySet());
		}
		return all;
	}

}