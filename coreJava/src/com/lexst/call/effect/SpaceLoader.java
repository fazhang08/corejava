/**
 *
 */
package com.lexst.call.effect;

import java.util.*;

import com.lexst.db.schema.*;
import com.lexst.util.lock.*;

public class SpaceLoader {

	// space set
	private MutexLock lock = new MutexLock();

	private Map<Space, Table> mapTable = new HashMap<Space, Table>();

	/**
	 *
	 */
	public SpaceLoader() {
		super();
	}

	/**
	 * save a space
	 * @param space
	 * @return
	 */
	public boolean add(Space space) {
		boolean success = false;
		lock.lockSingle();
		try {
			if (!mapTable.containsKey(space)) {
				mapTable.put(space, null);
				success = true;
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return success;
	}
	
	/**
	 * remove a space and table
	 * @param space
	 * @return
	 */
	public boolean remove(Space space) {
		boolean success = false;
		lock.lockSingle();
		try {
			if (mapTable.containsKey(space)) {
				mapTable.remove(space);
				success = true;
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return success;
	}

	/**
	 * update table
	 * @param space
	 * @param table
	 * @return
	 */
	public boolean update(Space space, Table table) {
		boolean success = false;
		lock.lockSingle();
		try {
			mapTable.put(space, table);
			success = true;
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return success;
	}
	
	public List<Space> keySet() {
		List<Space> array = new ArrayList<Space>();
		lock.lockSingle();
		try {
			if (!mapTable.isEmpty()) {
				array.addAll(mapTable.keySet());
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return array;
	}
	
	public Table find(Space space) {
		Table table = null;
		lock.lockMulti();
		try {
			table = mapTable.get(space);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return table;
	}

	public boolean exists(Space space) {
		boolean success = false;
		lock.lockMulti();
		try {
			success = mapTable.containsKey(space);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return success;
	}

	public void clear() {
		lock.lockSingle();
		try {
			mapTable.clear();
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public int size() {
		lock.lockMulti();
		try {
			return mapTable.size();
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return 0;
	}

}