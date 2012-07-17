/**
 * 
 */
package com.lexst.db.statement.dc;

import java.util.*;

public class DCModule {

	/* mod -> match set */
	private Map<Integer, DCTable> mapTable = new TreeMap<Integer, DCTable>();

	/**
	 * 
	 */
	public DCModule() {
		super();
	}

	/**
	 * @param area
	 */
	public DCModule(DCArea area) {
		this();
		this.add(area);
	}

	/**
	 * @param list
	 */
	public DCModule(List<DCArea> list) {
		this();
		this.add(list);
	}

	public int add(DCArea area) {
		int count = 0;
		for (DCField field : area.list()) {
			int mod = field.getMod();
			DCTable table = mapTable.get(mod);
			if (table == null) {
				table = new DCTable();
				mapTable.put(mod, table);
			}
			boolean success = table.add(area.getHost(), area.getIdentity(), field);
			if (success) count++;
		}
		return count;
	}
	
	public int add(List<DCArea> list) {
		int count = 0;
		for (DCArea file : list) {
			count += add(file);
		}
		return count;
	}

	public Set<Integer> keySet() {
		return mapTable.keySet();
	}

	public DCTable get(int mod) {
		return mapTable.get(mod);
	}

	/**
	 * count file size
	 * @return
	 */
	public long length() {
		long count = 0L;
		for (DCTable table : mapTable.values()) {
			count += table.length();
		}
		return count;
	}

	/**
	 * balance split
	 * 
	 * 实现数据平衡
	 */
	public DCTable[] split(final int sites) {
		if (sites < 1) {
			throw new IllegalArgumentException("invalid sites:" + sites);
		}

		long total = length();
		long scale = total / sites;
		if (total % sites != 0) scale++;
		
		// mod -> table length
		Map<Integer, Long> map1 = new TreeMap<Integer, Long>();
		for (int mod : mapTable.keySet()) {
			DCTable table = mapTable.get(mod);
			map1.put(mod, table.length());
		}
		
		ArrayList<ISet> sets = new ArrayList<ISet>();
		ArrayList<Integer> store = new ArrayList<Integer>();
		ArrayList<Integer> array = new ArrayList<Integer>(map1.keySet());
		Map<Long, ISet> map2 = new TreeMap<Long, ISet>();
		
		for(int i = 0; i < array.size(); i++) {
			int mod1 = array.get(i);
			if(store.contains(mod1)) continue;
			
			map2.clear();
			long length1 = map1.get(mod1);
			
			if (length1 == scale) {
				map2.put(length1, new ISet(mod1));
			} else {
				for (int j = 0; j < array.size(); j++) {
					if (i == j) continue;
					int mod2 = array.get(j);
					if (store.contains(mod2)) continue;
					long length2 = map1.get(mod2);

					// 如果其中之一匹配就保存
					if (length2 == scale) {
						map2.clear();
						map2.put(length2, new ISet(mod2));
						i--;
						break;
					}

					long len = length1 + length2;
					if (len == scale) { // 最佳匹配,其它全部清空,保存退出
						map2.clear();
						map2.put(len, new ISet(mod1, mod2));
						break;
					}

					if (map2.isEmpty()) { // 空状态,保存
						map2.put(len, new ISet(mod1, mod2));
					} else {
						Map<Long, ISet> map3 = new TreeMap<Long, ISet>();
						for (long length : map2.keySet()) {
							// 在原基础上增加
							long newlen = length + length2;
							if (newlen < 1) continue; // 数溢出,不处理

							ISet set2 = map2.get(length);
							map3.put(newlen, new ISet(set2, mod2));
						}
						map2.putAll(map3);
					}
				}
			}

			if(map2.isEmpty()) {
				// 唯一,保留它
				store.add(mod1);
				sets.add(new ISet(mod1));
			} else {
				// 找到大于等于平均值,但是又是最小的
				long length = 0;
				for(long len : map2.keySet()) {
//					if (length == 0 || length > len) length = len;
					
					if(length == 0) length = len;
					else if (len >= scale && (length < scale || length > len)) length = len;
				}
				ISet set = map2.get(length);
				store.addAll(set.set());
				sets.add(set);
			}
		}

		DCTable[] tables = new DCTable[sets.size()];
		int index = 0;
		for (ISet set : sets) {
			tables[index] = new DCTable();
			for (int mod : set.set()) {
				DCTable table = mapTable.get(mod);
				tables[index].add(table);
			}
			index++;
		}
		return tables;
	}
	
	public boolean isEmpty() {
		return mapTable.isEmpty();
	}

	public int size() {
		return mapTable.size();
	}

}