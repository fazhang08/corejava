/**
 *
 */
package com.lexst.visit.naming.call;

import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public interface CallVisit extends Visit {

	/**
	 * create a table space
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean createSpace(Table table) throws VisitException;

	/**
	 * delete table space
	 * @param db
	 * @param table
	 * @return
	 * @throws VisitException
	 */
	boolean deleteSpace(String db, String table) throws VisitException;

	/**
	 * insert a item
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	int insert(Insert object, boolean sync) throws VisitException;

	/**
	 * insert all item
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	int inject(Inject object, boolean sync) throws VisitException;

	/**
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	byte[] select(Select object) throws VisitException;

	/**
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	byte[] dc(DC object) throws VisitException;

	/**
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	byte[] adc(ADC object) throws VisitException;
	
	/**
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	long delete(Delete object) throws VisitException;

	/**
	 * @param object
	 * @return
	 * @throws VisitException
	 */
	long update(Update object) throws VisitException;
	
	/**
	 * login to call site
	 * @param site
	 * @return
	 * @throws VisitException
	 */
	boolean login(Site site) throws VisitException;
	
	/**
	 * logout from call site
	 * @param type
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	boolean logout(int type, SiteHost local) throws VisitException;
	
	/**
	 * relogin 
	 * @param site
	 * @return
	 * @throws VisitException
	 */
	boolean relogin(Site site) throws VisitException;

	/**
	 * active
	 * @param type
	 * @param local
	 * @return
	 * @throws VisitException
	 */
	boolean refresh(int type, SiteHost local) throws VisitException;
}