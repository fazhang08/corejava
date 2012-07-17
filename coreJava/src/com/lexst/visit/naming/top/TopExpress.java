/**
 * 
 */
package com.lexst.visit.naming.top;

import com.lexst.visit.*;
import com.lexst.util.host.SiteHost;

public interface TopExpress extends Visit {

	/**
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	SiteHost voting(SiteHost[] hosts) throws VisitException;
	
	/**
	 * @return
	 * @throws VisitException
	 */
	boolean isRunsite() throws VisitException;

	/**
	 * request dict of top, return byte array
	 * @return
	 * @throws VisitException
	 */
	byte[] dict() throws VisitException;
	
	
	/**
	 * @return
	 * @throws VisitException
	 */
	long single() throws VisitException;
		
	/**
	 * @return
	 * @throws VisitException
	 */
	byte[] pid() throws VisitException;
	
	/**
	 * @return
	 * @throws VisitException
	 */
	byte[] accounts() throws VisitException;
	
	/**
	 * @param type
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findSite(int type) throws VisitException;
}