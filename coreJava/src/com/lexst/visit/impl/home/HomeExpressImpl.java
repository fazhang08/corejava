/**
 * 
 */
package com.lexst.visit.impl.home;

import java.util.List;

import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.home.*;
import com.lexst.home.Launcher;
import com.lexst.home.effect.*;
import com.lexst.home.pool.*;

public class HomeExpressImpl implements HomeExpress {

	/**
	 * 
	 */
	public HomeExpressImpl() {
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
	 * @see com.lexst.visit.naming.home.HomeExpress#voting(com.lexst.util.host.SiteHost[])
	 */
	@Override
	public SiteHost voting(SiteHost[] hosts) throws VisitException {
		return Launcher.getInstance().voting(hosts);
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#isRunsite()
	 */
	public boolean isRunsite() throws VisitException {
		return Launcher.getInstance().isRunsite();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#chunkid()
	 */
	@Override
	public byte[] chunkid() throws VisitException {
		IdentitySet set = Launcher.getInstance().getIdentitySet();
		return set.createXML();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#spaces()
	 */
	@Override
	public byte[] spaces() throws VisitException {
		UserSpace instance = Launcher.getInstance().getUserSpace();
		return instance.createXML();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#charsets()
	 */
	@Override
	public byte[] charsets() throws VisitException {
		UserCharset instance = Launcher.getInstance().getUserCharset();
		return instance.createXML();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#findSite(int)
	 */
	@Override
	public SiteHost[] findSite(int type) throws VisitException {
		List<SiteHost> array = null;
		switch (type) {
		case Site.DATA_SITE:
			array = DataPool.getInstance().gather();
			break;
		case Site.BUILD_SITE:
			array = BuildPool.getInstance().gather();
			break;
		case Site.WORK_SITE:
			array = WorkPool.getInstance().gather();
			break;
		case Site.CALL_SITE:
			array = CallPool.getInstance().gather();
			break;
		case Site.LOG_SITE:
			array = LogPool.getInstance().gather();
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