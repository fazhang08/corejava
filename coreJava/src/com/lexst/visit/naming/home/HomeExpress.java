/**
 * 
 */
package com.lexst.visit.naming.home;

import com.lexst.visit.*;
import com.lexst.util.host.SiteHost;

public interface HomeExpress extends Visit {

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
	 * @return
	 * @throws VisitException
	 */
	byte[] chunkid() throws VisitException;

	/**
	 * @return
	 * @throws VisitException
	 */
	byte[] spaces() throws VisitException;

	/**
	 * @return
	 * @throws VisitException
	 */
	byte[] charsets() throws VisitException;
	
	/**
	 * @param type
	 * @return
	 * @throws VisitException
	 */
	SiteHost[] findSite(int type) throws VisitException;
}