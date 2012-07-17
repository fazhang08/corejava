/**
 *
 */
package com.lexst.visit.naming.top;

import com.lexst.db.charset.*;
import com.lexst.db.schema.*;
import com.lexst.db.account.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public interface TopVisit extends Visit {

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
	 * active top site
	 * @param type	(site type)
	 * @param local	(host address)
	 * @return
	 * @throws VisitException
	 */
	int hello(int type, SiteHost local) throws VisitException;

	/**
	 * login to top (home site and live site)
	 * @param site
	 * @return
	 * @throws VisitException
	 */
	boolean login(Site site) throws VisitException;

	/**
	 * logout home site
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
	 * request chunk id, only data site
	 * @param num
	 * @return
	 * @throws VisitException
	 */
	long[] applyChunkId(int num) throws VisitException;

	/**
	 * apply table primary key
	 * @param db
	 * @param table
	 * @param num
	 * @return
	 * @throws VisitException
	 */
	Number[] pullKey(String db, String table, int num) throws VisitException;

	/**
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	int findChunkSize(String db, String table) throws VisitException;

	/**
	 * set chunk file size
	 * @param db
	 * @param table
	 * @param size
	 * @return
	 * @throws VisitException
	 */
	boolean setChunkSize(String db, String table, int size) throws VisitException;

	/**
	 * @param db
	 * @return
	 * @throws VisitException
	 */
	SQLCharset findCharset(String db) throws VisitException;

	/**
	 * get sql char map
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	SQLCharmap getCharmap(SiteHost local) throws VisitException;

	/**
	 * find home site by space
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findHomeSite(String db, String table) throws VisitException;

	/**
	 * @param naming
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] selectCallSite(String naming) throws VisitException;

	/**
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] selectCallSite(String db, String table) throws VisitException;
	
	/**
	 * @param naming
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] selectCallSite(String naming, String db, String table) throws VisitException;
	
	/**
	 * create a database
	 * @param local (live site address)
	 * @param schema
	 * @return
	 * @throws VisitException
	 */
	boolean createSchema(SiteHost local, Schema schema) throws VisitException;

	/**
	 * delete a database
	 * @param local
	 * @param db
	 * @return
	 * @throws VisitException
	 */
	boolean deleteSchema(SiteHost local, String db) throws VisitException;

	/**
	 * get all database name
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	String[] getSchemas(SiteHost local) throws VisitException;

	/**
	 * create a user information
	 * @param local
	 * @param user
	 * @return
	 * @throws VisitException
	 */
	boolean createUser(SiteHost local, User user) throws VisitException;

	/**
	 * delete a user
	 * @param local
	 * @param username
	 * @return
	 * @throws VisitException
	 */
	boolean deleteUser(SiteHost local, String username) throws VisitException;

	/**
	 * modify user information
	 * @param local
	 * @param user
	 * @return
	 * @throws VisitException
	 */
	boolean alterUser(SiteHost local, User user) throws VisitException;

	/**
	 * add a user permit
	 * @param local
	 * @param permit
	 * @return
	 * @throws VisitException
	 */
	boolean addPermit(SiteHost local, Permit permit) throws VisitException;

	/**
	 * delete a user permit
	 * @param local
	 * @param permit
	 * @return
	 * @throws VisitException
	 */
	boolean deletePermit(SiteHost local, Permit permit) throws VisitException;

	/**
	 * get user permit
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	Permit[] getPermits(SiteHost local) throws VisitException;

	/**
	 * create a table to top site
	 * @param local
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean createTable(SiteHost local, Table table) throws VisitException;

	/**
	 * find a table information
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	Table findTable(SiteHost local, String db, String table) throws VisitException;

	/**
	 * @param local
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean deleteTable(SiteHost local, String db, String table) throws VisitException;

	/**
	 * get all table by a user 
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	Table[] getTables(SiteHost local) throws VisitException;

	/**
	 * check login user type, admin or normal user
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	short checkIdentified(SiteHost local) throws VisitException;

	/**
	 * system optimize time
	 * @param db
	 * @param table
	 * @param type
	 * @param time
	 * @return
	 * @throws VisitException
	 */
	boolean setOptimizeTime(String db, String table, int type, long time) throws VisitException;

	/**
	 * execute optimize command
	 * @param local
	 * @param db
	 * @param table
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] optimize(SiteHost local, String db, String table, String[] hosts) throws VisitException;

	/**
	 * load chunk index to memory
	 * @param local
	 * @param db
	 * @param table
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] loadIndex(SiteHost local, String db, String table, String[] hosts) throws VisitException;

	/**
	 * release chunk index
	 * @param local
	 * @param db
	 * @param table
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] stopIndex(SiteHost local, String db, String table, String[] hosts) throws VisitException;

	/**
	 * load chunk data
	 * @param local
	 * @param db
	 * @param table
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] loadChunk(SiteHost local, String db, String table, String[] hosts) throws VisitException;

	/**
	 * release chunk data
	 * @param local
	 * @param db
	 * @param table
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] stopChunk(SiteHost local, String db, String table, String[] hosts) throws VisitException;

	/**
	 * build a task to build site
	 * @param local
	 * @param naming
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	IP[] buildTask(SiteHost local, String naming, String[] hosts) throws VisitException;

	/**
	 * show site address
	 * @param site
	 * @param from
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] showSite(int site, String from) throws VisitException;
}