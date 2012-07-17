/**
 * 
 */
package com.lexst.call.effect;

import java.util.*;

import com.lexst.db.charset.*;
import com.lexst.util.lock.*;

public class SchemaCharset {
	
	private MutexLock lock = new MutexLock();
	
	// database name -> charset
	private Map<String, SQLCharset> mapCharset = new HashMap<String, SQLCharset>();

	/**
	 * 
	 */
	public SchemaCharset() {
		super();
	}

	/**
	 * @param db
	 * @param charset
	 * @return
	 */
	public boolean add(String db, SQLCharset charset) {
		db = db.toLowerCase();
		boolean success = false;
		lock.lockSingle();
		try {
			if (!mapCharset.containsKey(db)) {
				success = (mapCharset.put(db, charset) == null);
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return success;
	}
	
	public boolean remove(String db) {
		db = db.toLowerCase();
		boolean success = false;
		lock.lockSingle();
		try {
			success = (mapCharset.remove(db) != null);
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return success;
	}
	
	public SQLCharset find(String db) {
		db = db.toLowerCase();
		lock.lockMulti();
		try {
			return mapCharset.get(db);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return null;
	}
	
	public boolean exists(String db) {
		db = db.toLowerCase();
		lock.lockMulti();
		try {
			return mapCharset.containsKey(db);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return false;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}

	public int size() {
		lock.lockMulti();
		try {
			return mapCharset.size();
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return 0;
	}

}
