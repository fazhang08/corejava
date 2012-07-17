/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * lexst database interface
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 12/11/2009
 * 
 * @see com.lexst.data
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.data;

public final class Install {

	public static boolean loaded = false;

	static {
		try {
			System.loadLibrary("lexstdb");
			Install.loaded = true;
		} catch (Throwable exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * append to file last
	 * @param filename
	 * @param data
	 * @param off
	 * @param len
	 * @return
	 */
	public native static long append(byte[] filename, byte[] data, int off, int len);
	
	/**
	 * write data to file offset
	 * @param filename
	 * @param fileoff
	 * @param data
	 * @param off
	 * @param len
	 * @return
	 */
	public native static long write(byte[] filename, long fileoff, byte[] data, int off, int len);

	/**
	 * read data from file
	 * @param filename
	 * @param fileoff
	 * @param len
	 * @return
	 */
	public native static byte[] read(byte[] filename, long fileoff, int len);
	
	/**
	 * get file length
	 * @param filename
	 * @return
	 */
	public native static long filesize(byte[] filename);
	

	/* sql function, begin */
	
	/**
	 * save sql data to lexst db
	 * on success, resolve data and call xxxCacheEntity
	 * on failed, other process
	 */
	public native static byte[] insert(byte[] data);
	
	/**
	 * get current cache chunk (block) data (prime site call)
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	public native static byte[] getCacheEntity(byte[] db, byte[] table, long chunkid);
	
	/**
	 * save cache data (block) to disk
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param entity
	 * @return
	 */
	public native static int setCacheEntity(byte[] db, byte[] table, long chunkid, byte[] entity);
	
	/**
	 * delete cache data from disk
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	public native static int deleteCacheEntity(byte[] db, byte[] table, long chunkid);
	
	/**
	 * get current chunk data (prim site call)
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	public native static byte[] getChunkEntity(byte[] db, byte[] table, long chunkid);
	
	/**
	 * save chunk data to disk (slave site)
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param entity
	 * @return
	 */
	public native static int setChunkEntity(byte[] db, byte[] table, long chunkid, byte[] entity);

	/**
	 * query sql
	 * @param query
	 * @return
	 */
	public native static long select(byte[] query);

	/**
	 * query and reply data
	 * @param stamp
	 * @return
	 */
	public native static byte[] nextSelect(int stamp, int size);
	
	/**
	 * delete data
	 * @param query
	 * @return
	 */
	public native static byte[] delete(byte[] query);

	/**
	 * get delete's data
	 * @param stamp
	 * @param size
	 * @return
	 */
	public native static byte[] nextDelete(int stamp, int size);

	/**
	 * force to chunk
	 * on success, >=0; otherwise <0
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int rush(byte[] db, byte[] table);

	/* sql function, end */

	/**
	 * initialize database
	 * success return 0, otherwise other value
	 */
	public native static int initialize();

	/**
	 * optimize database space
	 * on success, result byte return 
	 * on failed, 0 byte return
	 *
	 * @param db
	 * @param table
	 * @return
	 */
	public native static byte[] optimize(byte[] db, byte[] table);

	/**
	 * set job threads
	 * @param num
	 * @return
	 */
	public native static int setWorker(int num);

	/**
	 * build directory
	 * @param path
	 * @return
	 */
	public native static int setBuildRoot(byte[] path);

	/**
	 * cache directory
	 * @param path
	 * @return
	 */
	public native static int setCacheRoot(byte[] path);

	/**
	 * chunk directory
	 * @param path
	 * @return
	 */
	public native static int setChunkRoot(byte[] path);

	/**
	 * create a space
	 * @param schema
	 * @return
	 */
	public native static int createSpace(byte[] schema, boolean prime);

	/**
	 * delete space directory and all chunk
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int deleteSpace(byte[] db, byte[] table);
	
	/**
	 * set data space, not start
	 * @param schema
	 * @return
	 */
	public native static int initSpace(byte[] schema, boolean prime);

	/**
	 * set data space and start it
	 * @param schema
	 * @return
	 */
	public native static int loadSpace(byte[] schema, boolean prime);

	/**
	 * stop and close space(cannot delete chunk)
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int stopSpace(byte[] db, byte[] table);

	/**
	 * check a space exists
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int findSpace(byte[] db, byte[] table);

	/**
	 * list all space name
	 * @return
	 */
	public native static byte[] listSpaces();

	/**
	 * load database and start thread
	 * @return
	 */
	public native static int launch();

	/**
	 * stop job thread and stop database
	 * @return
	 */
	public native static int stop();

	/**
	 * add a chunk id to jni server
	 * @param chunkId
	 */
	public native static int addChunkId(long chunkId);

	/**
	 * count free chunk identity number
	 * @return
	 */
	public native static int countFreeChunkIds();
	
	/**
	 * get all free chunk identity
	 * @return
	 */
	public native static long[] getFreeChunkIds();
	
	/**
	 * get all used chunk identity
	 * @return
	 */
	public native static long[] getTotalUsedChunkIds();
	
	/**
	 * return all chunk and chunk index
	 * @return
	 */
	public native static byte[] pullChunkIndex();

	/**
	 * flush index set by a space
	 * @param db
	 * @param table
	 * @return
	 */
	public native static byte[] findChunkIndex(byte[] db, byte[] table);

	/**
	 * check update
	 * @return
	 */
	public native static boolean isRefreshing();

	/**
	 * count disk space size
 	 * index 0: free size
 	 * index 1: used size
	 * @return
	 */
	public native static long[] getDiskSpace();

	/**
	 * set chunk size by a space
	 * @param db
	 * @param table
	 * @param size
	 * @return
	 */
	public native static int setChunkSize(byte[] db, byte[] table, int size);
	
	/**
	 * return a finish chunk 
	 * style : id(8 byte), db, table, chunk path
	 * @return
	 */
	public native static byte[] nextFinishChunk();

	/**
	 * return a chunk path
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	public native static byte[] findChunkPath(byte[] db, byte[] table, long chunkid);

	/**
	 * return a cache chunk path
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	public native static byte[] findCachePath(byte[] db, byte[] table, long chunkid);
	
	/**
	 * load a chunk file
	 * @param db
	 * @param table
	 * @param filename (disk file)
	 * @return
	 */
	public native static int loadChunk(byte[] db, byte[] table, byte[] filename);

	/**
	 * find a chunk file
	 * @param db
	 * @param table
	 * @param chunkId
	 * @return
	 */
	public native static int findChunk(byte[] db, byte[] table, long chunkId);

	/**
	 * @param db
	 * @param table
	 * @param chunkId
	 * @return
	 */
	public native static int deleteChunk(byte[] db, byte[] table, long chunkId);
	
	/**
	 * modify chunk to prime mode
	 * 
	 * @param db
	 * @param table
	 * @param chunkId
	 * @return
	 */
	public native static int toPrime(byte[] db, byte[] table, long chunkId);
	
	/**
	 * modify chunk to slave mode
	 * 
	 * @param db
	 * @param table
	 * @param chunkId
	 * @return
	 */
	public native static int toSlave(byte[] db, byte[] table, long chunkId);
	
	/**
	 * get all chunk identity(used) for space
	 * @param db
	 * @param table
	 * @return
	 */
	public native static long[] getChunkIds(byte[] db, byte[] table);
	
	/**
	 * get cache chunk identity(used) for space
	 * @param db
	 * @param table
	 * @return
	 */
	public native static long getCacheId(byte[] db, byte[] table);

	/**
	 * when chunk not exists, choose a chunk filename
	 * 
	 * @param db
	 * @param table
	 * @param chunkId
	 * @return
	 */
	public native static byte[] defineChunkPath(byte[] db, byte[] table, long chunkId);

	/**
	 * set site rank
	 * @param rank
	 */
	public native static void setRank(int rank);

	/**
	 * load index into memory
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int inrush(byte[] db, byte[] table);

	/**
	 * clear index from memory
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int uninrush(byte[] db, byte[] table);

	/**
	 * load chunk into memory
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int afflux(byte[] db, byte[] table);

	/**
	 * clear chunk from memory
	 * @param db
	 * @param table
	 * @return
	 */
	public native static int unafflux(byte[] db, byte[] table);

	/**
	 * find build root path by a space 
	 * @param db
	 * @param table
	 * @return
	 */
	public native static byte[] getBuildPath(byte[] db, byte[] table);
	
	/**
	 * find cache root path by a space
	 * @param db
	 * @param table
	 * @return
	 */
	public native static byte[] getCachePath(byte[] db, byte[] table);
	
	/**
	 * find chunk root path by a space
	 * @param db
	 * @param table
	 * @param index
	 * @return
	 */
	public native static byte[] getChunkPath(byte[] db, byte[] table, int index);
	
	/**
	 * marshal all chunk
	 * @param db
	 * @param table
	 * @return
	 */
	public native static long[] marshal(byte[] db, byte[] table);
	
	/**
	 * export record
	 * @param db
	 * @param table
	 * @param size
	 * @return
	 */
	public native static byte[] educe(byte[] db, byte[] table, int size);
}