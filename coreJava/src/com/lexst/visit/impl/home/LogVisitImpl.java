/**
 *
 */
package com.lexst.visit.impl.home;

import com.lexst.home.*;
import com.lexst.home.pool.*;
import com.lexst.site.*;
import com.lexst.util.datetime.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.home.*;

public class LogVisitImpl implements LogVisit {

	/**
	 *
	 */
	public LogVisitImpl() {
		super();
	}

	/*
	 * get server system time
	 * @see com.lexst.visit.Visit#currentTime()
	 */
	public long currentTime() throws VisitException {
		return SystemTime.get();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#getSiteTimeout(int)
	 */
	@Override
	public int getSiteTimeout(int type) throws VisitException {
		int second = 20; // default is 20 second
		switch (type) {
		case Site.LOG_SITE:
			second = LogPool.getInstance().getSiteTimeout();
			break;
		case Site.DATA_SITE:
			second = DataPool.getInstance().getSiteTimeout();
			break;
		case Site.CALL_SITE:
			second = CallPool.getInstance().getSiteTimeout();
			break;
		case Site.WORK_SITE:
			second = WorkPool.getInstance().getSiteTimeout();
			break;
		case Site.BUILD_SITE:
			second = BuildPool.getInstance().getSiteTimeout();
			break;
		}
		second -= 2;
		if (second < 10) second = 10;
		return second;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#login(com.lexst.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		boolean success = false;
		switch (site.getType()) {
		case Site.LOG_SITE:
			success = LogPool.getInstance().add(site);
			break;
		case Site.DATA_SITE:
			success = DataPool.getInstance().add(site);
			break;
		case Site.CALL_SITE:
			success = CallPool.getInstance().add(site);
			break;
		case Site.WORK_SITE:
			success = WorkPool.getInstance().add(site);
			break;
		case Site.BUILD_SITE:
			success = BuildPool.getInstance().add(site);
			break;
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#logout(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean logout(int type, SiteHost host) throws VisitException {
		boolean success = false;
		switch(type) {
		case Site.LOG_SITE:
			success = LogPool.getInstance().remove(host);
			break;
		case Site.DATA_SITE:
			success = DataPool.getInstance().remove(host);
			break;
		case Site.CALL_SITE:
			success = CallPool.getInstance().remove(host);
			break;
		case Site.WORK_SITE:
			success = WorkPool.getInstance().remove(host);
			break;
		case Site.BUILD_SITE:
			success = BuildPool.getInstance().remove(host);
			break;
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#relogin(com.lexst.site.Site)
	 */
	@Override
	public boolean relogin(Site site) throws VisitException {
		boolean success = false;
		switch(site.getType()) {
		case Site.LOG_SITE:
			success = LogPool.getInstance().update(site);
			break;
		case Site.DATA_SITE:
			success = DataPool.getInstance().update(site);
			break;
		case Site.CALL_SITE:
			success = CallPool.getInstance().update(site);
			break;
		case Site.WORK_SITE:
			success = WorkPool.getInstance().update(site);
			break;
		case Site.BUILD_SITE:
			success = BuildPool.getInstance().update(site);
			break;
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		Launcher.getInstance().nothing();
	}

}
