/**
 *
 */
package com.lexst.visit.impl.call;

import com.lexst.call.*;
import com.lexst.call.pool.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.log.client.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.call.*;

public class CallVisitImpl implements CallVisit {

	private static CallLauncher callInstance;

	public static void setInstance(CallLauncher instance) {
		CallVisitImpl.callInstance = instance;
	}

	/**
	 *
	 */
	public CallVisitImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		callInstance.nothing();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#createSpace(com.lexst.db.schema.Table)
	 */
	@Override
	public boolean createSpace(Table table) throws VisitException {
		return callInstance.createSpace(table);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#deleteSpace(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteSpace(String db, String table) throws VisitException {
		Space space = new Space(db, table);
		return callInstance.deleteSpace(space);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#insert(com.lexst.db.statement.Insert)
	 */
	@Override
	public int insert(Insert object, boolean sync) throws VisitException {
		return DataPool.getInstance().insert(object, sync);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#inject(com.lexst.db.statement.Inject)
	 */
	@Override
	public int inject(Inject object, boolean sync) throws VisitException {
		return DataPool.getInstance().inject(object, sync);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#select(com.lexst.db.statement.Select)
	 */
	@Override
	public byte[] select(Select object) throws VisitException {
		return DataPool.getInstance().select(object);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#dc(com.lexst.db.statement.DC, boolean)
	 */
	@Override
	public byte[] dc(DC object) throws VisitException {
		return DataPool.getInstance().dc(object);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#adc(com.lexst.db.statement.ADC)
	 */
	@Override
	public byte[] adc(ADC object) throws VisitException {
		return DataPool.getInstance().adc(object);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#delete(com.lexst.db.statement.Delete)
	 */
	@Override
	public long delete(Delete object) throws VisitException {
		return DataPool.getInstance().delete(object);
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#update(com.lexst.db.statement.Update)
	 */
	@Override
	public long update(Update object) throws VisitException {
		return DataPool.getInstance().update(object);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#login(com.lexst.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		Logger.info("CallVisitImpl.login, site type %s", Site.translate(site.getType()));
		if (site.isLive()) {
			return LivePool.getInstance().add(site);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#logout(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean logout(int type, SiteHost host) throws VisitException {
		Logger.info("CallVisitImpl.logout, site type %s", Site.translate(type));
		if (type == Site.LIVE_SITE) {
			return LivePool.getInstance().remove(host);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#relogin(com.lexst.site.Site)
	 */
	@Override
	public boolean relogin(Site site) throws VisitException {
		Logger.info("CallVisitImpl.relogin, site type %s", Site.translate(site.getType()));
		if (site.isLive()) {
			return LivePool.getInstance().update(site);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#refresh(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean refresh(int type, SiteHost host) throws VisitException {
		if (type == Site.LIVE_SITE) {
			return LivePool.getInstance().refresh(host);
		}
		return false;
	}

}