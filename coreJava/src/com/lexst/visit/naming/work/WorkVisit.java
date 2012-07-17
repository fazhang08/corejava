/**
 *
 */
package com.lexst.visit.naming.work;

import com.lexst.visit.*;
import com.lexst.util.host.*;

public interface WorkVisit extends Visit {

	int getRefreshTime() throws VisitException;

	boolean login(SocketHost local, SiteHost monitor) throws VisitException;

	boolean logout(SocketHost local) throws VisitException;

	boolean relogin(SocketHost local, SiteHost monitor) throws VisitException;
	
	boolean refresh(SocketHost local) throws VisitException;
}