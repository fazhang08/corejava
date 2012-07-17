/**
 *
 */
package com.lexst.visit.naming.home;

import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public interface LogVisit extends Visit {

	/**
	 * get server current time
	 * @return
	 * @throws VisitException
	 */
	long currentTime() throws VisitException;

	int getSiteTimeout(int type) throws VisitException;

	boolean login(Site site) throws VisitException;

	boolean logout(int type, SiteHost host) throws VisitException;

	boolean relogin(Site site) throws VisitException;
}
