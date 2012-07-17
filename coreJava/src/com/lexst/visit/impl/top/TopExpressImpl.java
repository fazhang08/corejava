/**
 * 
 */
package com.lexst.visit.impl.top;

import java.util.*;

import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.top.*;
import com.lexst.site.*;
import com.lexst.top.Launcher;
import com.lexst.top.pool.*;

public class TopExpressImpl implements TopExpress {

	/**
	 * 
	 */
	public TopExpressImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		Launcher.getInstance().nothing();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#voting(com.lexst.util.host.SiteHost[])
	 */
	@Override
	public SiteHost voting(SiteHost[] hosts) throws VisitException {
		return Launcher.getInstance().voting(hosts);
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#isRunsite()
	 */
	@Override
	public boolean isRunsite() throws VisitException {
		return Launcher.getInstance().isRunsite();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#dict()
	 */
	@Override
	public byte[] dict() throws VisitException {
		return Launcher.getInstance().getDict().buildXML();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#single()
	 */
	@Override
	public long single() throws VisitException {
		return Launcher.getInstance().getSingle().getBegin();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#pid()
	 */
	@Override
	public byte[] pid() throws VisitException {
		return Launcher.getInstance().getSpaceKey().buildXML();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#accounts()
	 */
	@Override
	public byte[] accounts() throws VisitException {
		return Launcher.getInstance().getUserManager().buildXML();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#findSite(int)
	 */
	@Override
	public SiteHost[] findSite(int type) throws VisitException {
		List<SiteHost> array = null;
		switch (type) {
		case Site.HOME_SITE:
			array = HomePool.getInstance().gather();
			break;
		case Site.LIVE_SITE:
			array = LivePool.getInstance().gather();
			break;
		}
		
		SiteHost[] hosts = null;
		if (array != null && array.size() > 0) {
			hosts = new SiteHost[array.size()];
			array.toArray(hosts);
		}
		return hosts;
	}

}