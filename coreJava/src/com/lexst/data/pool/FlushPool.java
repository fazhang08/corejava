/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;
import java.util.*;

import com.lexst.data.*;
import com.lexst.log.client.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.lock.*;

public class FlushPool extends VirtualThread {
	
	private final static long MAX_BUNKETSIZE = 1024 * 1024 * 64;

	private static FlushPool selfHandle = new FlushPool();

	private SingleLock lock = new SingleLock();

	private Bunket current;
	
	private int sequenceIndex;

	private Map<Integer, Bunket> mapBunket = new TreeMap<Integer, Bunket>();
	
	private String tempPath;

	/**
	 * 
	 */
	private FlushPool() {
		super();
		this.setSleep(5);
		sequenceIndex = 0;
	}

	/**
	 * @return
	 */
	public static FlushPool getInstance() {
		return FlushPool.selfHandle;
	}

	public void setTempPath(String s) {
		if (s != null && s.trim().length() > 0) {
			this.tempPath = s.trim();
		}
	}

	public String getTempPath() {
		return this.tempPath;
	}
	
	private boolean mkdir() {
		if (tempPath == null) return true;
		File file = new File(tempPath);
		if (!file.exists() || !file.isDirectory()) {
			boolean success = file.mkdirs();
			Logger.note(success, "FlushPool.mkdir, directory %s", tempPath);
			return success;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		if (!mkdir()) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("FlushPool.process, into...");
		while (!isInterrupted()) {
			this.sleep();
			this.execute();
		}
		// close file and set "finished" tag
		this.complete();
		// append left 
		this.execute();

		Logger.info("FlushPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		
	}
	
	public void complete() {
		// set 'finished' tag
		if (current != null) {
			current.setFinished(true);
			current = null;
		}
	}

	/**
	 * apply a Bunket
	 * @return
	 */
	private Bunket apply() {
		String path = (tempPath != null ? tempPath : System.getProperty("java.io.tmpdir"));
		if(path == null) path = "";
		if (path.length() > 0 && path.charAt(path.length() - 1) != File.separatorChar) {
			path += File.separator;
		}
		for (sequenceIndex++; sequenceIndex < Integer.MAX_VALUE; sequenceIndex++) {
			if (sequenceIndex == Integer.MAX_VALUE) {
				sequenceIndex = 1;
			}
			String filename = String.format("%sblock%d.lxas", path, sequenceIndex);
			File file = new File(filename);
			if (!file.exists()) {
				Logger.info("FlushPool.apply, filename %s", filename);
				return new Bunket(sequenceIndex, filename);
			}
		}
		return null;
	}
	
	/**
	 * write data to disk
	 * @param data
	 * @return
	 */
	public boolean write(byte[] data) {
		boolean success = false;
		lock.lock();
		try {
			if (current == null) {
				current = this.apply(); 
				mapBunket.put(current.getID(), current);
			}
			
			String filename = current.getFilename();
			// JNI write
			long time = System.currentTimeMillis();
			long fileoff = Install.append(filename.getBytes(), data, 0, data.length);
			Logger.note(fileoff >= 0, "FlushPool.write, insert %s offset %d, usedtime:%d",
					filename, fileoff, System.currentTimeMillis() - time);
			if (fileoff >= 0L) {
				BunketNode node = new BunketNode(fileoff, data.length);
				// save node
				current.add(node);
				// build new 
				if (fileoff + data.length >= FlushPool.MAX_BUNKETSIZE) {
					current.setFinished(true);
					current = null;
				}
				success = true;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
		// notify thread
		if(success) wakeup();
		return success;
	}
	
	private void remove(int id) {
		lock.lock();
		try {
			mapBunket.remove(id);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
	}

	private Bunket get(int id) {
		lock.lock();
		try {
			return mapBunket.get(id);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
		return null;
	}

	private List<Integer> list() {
		List<Integer> array = new ArrayList<Integer>();
		lock.lock();
		try {
			if (mapBunket.size() > 0) {
				array.addAll(mapBunket.keySet());
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
		if (array.size() > 0) {
			Collections.sort(array);
		}
		return array;
	}

	/**
	 * inset buffer data to lexst db
	 */
	private void execute() {
		if (mapBunket.isEmpty()) return;
		// get identity
		for (int id : list()) {
			Bunket bunket = get(id);
			if (bunket.size() > 0) {
				insert(bunket);
			}
			if (bunket.isFinished() && bunket.isEmpty()) {
				String filename = bunket.getFilename();
				File file = new File(filename);
				boolean success = file.delete();
				Logger.note(success, "FlushPool.execute, delete file %s", filename);
				remove(id);
			}
		}
	}

	/**
	 * append data to lexst db
	 * @param bunket
	 */
	private void insert(Bunket bunket) {
		String filename = bunket.getFilename();
		while(true) {
			BunketNode node = null;
			lock.lock();
			try {
				node = bunket.poll();
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				lock.unlock();
			}
			if(node == null) break;

			// JNI read
			boolean success = false;
			long time = System.currentTimeMillis();
			byte[] data = Install.read(filename.getBytes(), node.getOffset(), node.getLength());
			
			Logger.debug("FlushPool.insert, read [%s %d - %d | %d], usedtime:%d",
					filename, node.getOffset(), node.getLength(),
					(data == null ? -1 : data.length), System.currentTimeMillis() - time);
			if (data != null && data.length > 0) {
				success = this.jniInsert(data);
			}
			Logger.note(success, "FlushPool.insert, insert [%d - %d] to lexst db", node.getOffset(), node.getLength());
			data = null;
		}
	}
	
	private boolean jniInsert(byte[] data) {
		Logger.info("FlushPool.jniInsert, data len:%d", data.length);
		long time = System.currentTimeMillis();
		
//		int items = 0;
//		while (true) {
//			// insert data (JNI)
//			items = Install.insert(data);
//			// check result
//			if (items > 0) {
//				break; // this is success
//			} else if (items == -3) {
//				while (true) {
//					long[] chunkIds = Launcher.getInstance().applyChunkId(5);
//					if (chunkIds != null) {
//						for (long chunkId : chunkIds) {
//							Install.addChunkId(chunkId);
//						}
//						break;
//					}
//				}
//			} else if (items == -2) {
//				// memory out
//				break;
//			} else {
//				// 添加失败,分析失败原因
//				break;
//			}
//		}
		
		int items = 0;
		while(true) {
			// insert data (JNI)
			byte[] ret = Install.insert(data);
			// check result
			int off = 0;
			byte status = ret[off++];
			items = Numeric.toInteger(ret, off, 4);
			off += 4;
			if(status == 0) { // success
				int dblen = ret[off++] & 0xFF;
				int tablen = ret[off++] & 0xFF;
				byte[] db_name = new byte[dblen];
				byte[] table_name = new byte[tablen];
				// db name
				System.arraycopy(ret, off, db_name, 0, db_name.length);
				off += dblen;
				// table name
				System.arraycopy(ret, off, table_name, 0, table_name.length);
				off += tablen;
				// chunk identity
				long chunkid = Numeric.toLong(ret, off, 8);
				off += 8;
				// get cache data
				byte[] entity = Install.getCacheEntity(db_name, table_name, chunkid);
				// send to CachePool, distribute to other host
				CachePool.getInstance().update(new String(db_name), new String(table_name), chunkid, entity);
				break;
			} else {
				if (items == -3) { // get new chunkid
					while (true) {
						long[] chunkIds = Launcher.getInstance().applyChunkId(5);
						if (chunkIds != null) {
							for (long chunkId : chunkIds) {
								Install.addChunkId(chunkId);
							}
							break;
						}
					}
				} else if (items == -2) { // memory out
					break;
				} else { // other error
					break;
				}
			}
		}
		
		Logger.info("FlushPool.jniInsert, data len:%d, item count:%d, insert time:%d",
				data.length, items, System.currentTimeMillis() - time);
		return items > 0;
	}

}