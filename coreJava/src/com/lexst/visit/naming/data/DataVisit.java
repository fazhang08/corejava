/**
 *
 */
package com.lexst.visit.naming.data;

import com.lexst.db.chunk.*;
import com.lexst.db.schema.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public interface DataVisit extends Visit {
	
	/**
	 * apply data site's rank
	 * @return
	 * @throws VisitException
	 */
	int applyRank() throws VisitException;

	/**
	 * find chunk information by space
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	Chunk[] findChunk(String db, String table) throws VisitException;
	
	/**
	 * send message to data site(slive node), download a chunk
	 * @param host
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param length
	 * @return
	 * @throws VisitException
	 */
	boolean distribute(SiteHost host, String db, String table, long chunkid, long length) throws VisitException;
	
	/**
	 * send message to data site(slive node), download optimize's chunk
	 * @param host
	 * @param db
	 * @param table
	 * @param oldIds
	 * @param newIds
	 * @return
	 * @throws VisitException
	 */
	boolean upgrade(SiteHost host, String db, String table, long[] oldIds, long[] newIds) throws VisitException;

	/**
	 * notify data site, check and update chunk
	 * @param db
	 * @param table
	 * @param host
	 * @return
	 * @throws VisitException
	 */
	boolean revive(String db, String table, SiteHost from) throws VisitException;

	/**
	 * request a index set
	 * @param db
	 * @param table
	 * @param pwd
	 * @return
	 * @throws VisitException
	 */
	IndexTable findIndex(String db, String table) throws VisitException;

	/**
	 * create data space
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean createSpace(Table table) throws VisitException;

	/**
	 * remove data space
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean deleteSpace(String db, String table) throws VisitException;
	
	/**
	 * optimize disk data
	 * 
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	public boolean optimize(String db, String table) throws VisitException;
	
	/**
	 * load index to memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean loadIndex(String db, String table) throws VisitException;
	
	/**
	 * release index from memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean stopIndex(String db, String table) throws VisitException;
	
	/**
	 * load chunk into memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean loadChunk(String db, String table) throws VisitException;
	
	/**
	 * clear chunk from memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean stopChunk(String db, String table) throws VisitException;
	
	/**
	 * @param jobid
	 * @param mod
	 * @param begin
	 * @param end
	 * @return
	 */
	byte[] downloadDCField(long jobid, int mod, long begin, long end) throws VisitException;
}
