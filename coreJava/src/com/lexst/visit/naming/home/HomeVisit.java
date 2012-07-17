/**
 *
 */
package com.lexst.visit.naming.home;

import com.lexst.db.charset.*;
import com.lexst.db.schema.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public interface HomeVisit extends Visit {

	/**
	 * get server current time
	 * @return
	 * @throws VisitException
	 */
	long currentTime() throws VisitException;

	/**
	 * handshake time
	 * @param type	(site type)
	 * @return
	 * @throws VisitException
	 */
	int getSiteTimeout(int type) throws VisitException;

	/**
	 * notify home site, site is active
	 * @param type	(site type)
	 * @param host	(host address)
	 * @return
	 * @throws VisitException
	 */
	int hello(int type, SiteHost host) throws VisitException;

	/**
	 * @return
	 * @throws VisitException
	 */
	Site[] batchWorkSite() throws VisitException;
	
	/**
	 * @return
	 * @throws VisitException
	 */
	Site[] batchDataSite() throws VisitException;
	
	/**
	 * request a log site by target site, include: home, work, data, call
	 * @param type	(site type)
	 * @return log site address
	 * @throws VisitException
	 */
	SiteHost findLogSite(int type) throws VisitException;
	
	/**
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findDataSite(String db, String table) throws VisitException;
	
	/**
	 * @param db
	 * @param table
	 * @param rank
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findDataSite(String db, String table, int rank) throws VisitException;

	/**
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findDataSite(String db, String table, long chunkid) throws VisitException;
	
	/**
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findCallSite(String db, String table) throws VisitException;

	/**
	 * @param naming
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findCallSite(String naming) throws VisitException;

	/**
	 * @param naming
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findCallSite(String naming, String db, String table) throws VisitException;
	
	/**
	 * find work site by naming
	 * @param naming
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findWorkSite(String naming) throws VisitException;

	/**
	 * find build site by space
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findBuildSite(String db, String table) throws VisitException;
	
	/**
	 * find build site by naming
	 * @param naming
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findBuildSite(String naming) throws VisitException;

	/**
	 * get all build site
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findBuildSite() throws VisitException;
	
	/**
	 * request chunk id, only data site
	 * @param num
	 * @return
	 * @throws VisitException
	 */
	long[] pullSingle(int num) throws VisitException;
	
	/**
	 * request table pid
	 * @param db
	 * @param table
	 * @param num
	 * @return
	 * @throws VisitException
	 */
	Number[] pullKey(String db, String table, int num) throws VisitException;

	/**
	 * apply table space, call site request
	 * @return
	 * @throws VisitException
	 */
	Space[] balance(int num) throws VisitException;
	
	/**
	 * apply chunk size 's table
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	int findChunkSize(String db, String table) throws VisitException;

	/**
	 * login a site (include: log site, call site, data site, query site)
	 * @param site
	 * @return
	 * @throws VisitException
	 */
	boolean login(Site site) throws VisitException;

	/**
	 * logout site
	 * @param type
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	boolean logout(int type, SiteHost local) throws VisitException;

	/**
	 * re-login site
	 * @param site
	 * @return
	 * @throws VisitException
	 */
	boolean relogin(Site site) throws VisitException;

	/**
	 * find charset (sql)
	 * @param db
	 * @return
	 * @throws VisitException
	 */
	SQLCharset findCharset(String db) throws VisitException;

	/**
	 * find a table struct (sql)
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	Table findTable(String db, String table) throws VisitException;
	
	/**
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean createSpace(Table table) throws VisitException;
	
	/**
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
	IP[] optimize(String db, String table, IP[] hosts) throws VisitException;
	
	/**
	 * load index to memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	IP[] loadIndex(String db, String table, IP[] hosts) throws VisitException;
	
	/**
	 * release index from memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	IP[] stopIndex(String db, String table, IP[] hosts) throws VisitException;
	
	/**
	 * load chunk to memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	IP[] loadChunk(String db, String table, IP[] hosts) throws VisitException;
	
	/**
	 * release chunk from memory
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	IP[] stopChunk(String db, String table, IP[] hosts) throws VisitException;
	
	/**
	 * build task to build site
	 * @param naming
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] buildTask(String naming, IP[] hosts) throws VisitException;
	
	/**
	 * query home site, allow download 
	 * @param chunkId
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	boolean agree(long chunkId, SiteHost local) throws VisitException;
	
	/**
	 * publish a new chunk
	 * @param local
	 * @param db
	 * @param table
	 * @param chunkId
	 * @param length
	 * @return
	 * @throws VisitException
	 */
	boolean publish(SiteHost local, String db, String table, long chunkId, long length) throws VisitException;

	/**
	 * prime site send to home site, upgrade chunk
	 * @param local
	 * @param db
	 * @param table
	 * @param oldIds
	 * @param newIds
	 * @return
	 * @throws VisitException
	 */
	boolean upgrade(SiteHost local, String db, String table, long[] oldIds, long[] newIds) throws VisitException;

	/**
	 * query home site(build pool), allow download from build site
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param length
	 * @param modified
	 * @return
	 * @throws VisitException
	 */
	boolean	accede(String db, String table, long chunkid, long length, long modified) throws VisitException;
	
	/**
	 * @param naming
	 * @return
	 * @throws VisitException
	 */
	long[] findBuildChunk(String naming) throws VisitException;
	
	/**
	 * show match address
	 * @param site
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] showSite(int site) throws VisitException;
}