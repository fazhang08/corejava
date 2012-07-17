/**
 *
 */
package com.lexst.util;

import java.util.*;

import com.lexst.util.lock.*;

/**
 *
 * 这个集合中的数据,每一个操作都是锁定.
 * 可以防止并发操作异常的现象发生
 */
public class LockArray<E>  {
	
	private MutexLock lock = new MutexLock();

	private ArrayList<E> array;

	/*
	 * construct method
	 */
	public LockArray(int size) {
		super();
		lock.setTimeout(10L);
		if (size < 3) {
			size = 3;
		}
		array = new ArrayList<E>(size);
	}

	/*
	 * construct method
	 */
	public LockArray() {
		this(10);
	}

	public void setTimeout(long millisecond) {
		lock.setTimeout(millisecond);
	}
	public long getTimeout() {
		return lock.getTimeout();
	}

	public boolean add(E e) {
		lock.lockSingle();
		try {
			return array.add(e);
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			lock.unlockSingle();
		}
		return false;
	}

	public boolean addAll(Collection<? extends E> c) {
		lock.lockSingle();
		try {
			return array.addAll(c);
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return false;
	}

	public boolean remove(E e) {
		lock.lockSingle();
		try {
			return array.remove(e);
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return false;
	}

	/**
	 * @param index
	 * @return
	 */
	public E remove(int index) {
		lock.lockSingle();
		try {
			if (0 <= index && index < array.size()) {
				return array.remove(index);
			}
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return null;
	}

	public boolean removeAll(Collection<?> coll) {
		lock.lockSingle();
		try {
			return array.removeAll( coll );
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return false;
	}

	public int indexOf(E e) {
		lock.lockMulti();
		try {
			return array.indexOf(e);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return -1;
	}

	public int lastIndexOf(Object o) {
		lock.lockMulti();
		try {
			return array.lastIndexOf(o);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return -1;
	}

	public boolean contains(E e) {
		lock.lockMulti();
		try {
			return array.contains(e);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return false;
	}

	public List<E> list() {
		List<E> all = new ArrayList<E>();
		lock.lockSingle();
		try {
			if (!array.isEmpty()) {
				all.addAll(array);
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			lock.unlockSingle();
		}
		return all;
	}

	public List<E> pollAll() {
		List<E> all = new ArrayList<E>();
		lock.lockSingle();
		try {
			if(!array.isEmpty()){
				all.addAll(array);
				array.clear();
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			lock.unlockSingle();
		}
		return all;
	}

	public E poll() {
		E object = null;
		lock.lockSingle();
		try {
			if (!array.isEmpty()) {
				object = array.remove(0);
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			lock.unlockSingle();
		}
		return object;
	}

	public List<E> poll(int size) {
		ArrayList<E> all = new ArrayList<E>(size);
		lock.lockSingle();
		try {
			for (int i = 0; i < size; i++) {
				if (array.isEmpty()) break;
				all.add(array.remove(0));
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			lock.unlockSingle();
		}
		return all;
	}

	public Iterator<E> iterator() {
		return array.iterator();
	}
	
	public void enruse() {
		lock.lockSingle();
		try {
			array.ensureCapacity(array.size());
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
	}

	public void clear() {
		lock.lockSingle();
		try {
			array.clear();
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
	}

	public E get(int index) {
		E object = null;
		lock.lockMulti();
		try {
			if (0 <= index && index < array.size()) {
				object = array.get(index);
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			lock.unlockMulti();
		}
		return object;
	}

	public <T> T[] toArray(T[] all) {
		lock.lockMulti();
		try {
			return array.toArray(all);
		} catch (Throwable exp) {

		} finally {
			lock.unlockMulti();
		}
		return null;
	}

	/**
	 * @return
	 */
	public int size() {
		lock.lockSingle();
		try {
			return array.size();
		} catch (Throwable exp) {

		} finally {
			lock.unlockSingle();
		}
		return 0;
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

}