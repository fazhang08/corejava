/**
 *
 */
package com.lexst.remote.client.call;

import java.lang.reflect.*;
import java.util.*;

import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.remote.client.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.call.*;


public class CallClient extends RemoteClient implements CallVisit {

	private static Method methodNothing;
	private static Method methodCreateSpace;
	private static Method methodDeleteSpace;

	private static Method methodInsert;
	private static Method methodInject;
	private static Method methodDelete;
	private static Method methodSelect;
	private static Method methodUpdate;
	private static Method methodDC;
	private static Method methodADC;
	
	private static Method methodLogin;
	private static Method methodLogout;
	private static Method methodRelogin;
	private static Method methodRefresh;

	static {
		try {
			methodNothing = (CallVisit.class).getMethod("nothing", new Class<?>[0]);

			methodCreateSpace = (CallVisit.class).getMethod("createSpace", new Class<?>[] { Table.class });
			methodDeleteSpace = (CallVisit.class).getMethod("deleteSpace", new Class<?>[] { String.class, String.class });

			methodInsert = (CallVisit.class).getMethod("insert", new Class<?>[] { Insert.class, Boolean.TYPE });
			methodInject = (CallVisit.class).getMethod("inject", new Class<?>[] { Inject.class, Boolean.TYPE });
			methodDelete = (CallVisit.class).getMethod("delete", new Class<?>[] { Delete.class });
			methodSelect = (CallVisit.class).getMethod("select", new Class<?>[] { Select.class});
			methodUpdate = (CallVisit.class).getMethod("update", new Class<?>[] { Update.class });
			methodDC = (CallVisit.class).getMethod("dc", new Class<?>[] { DC.class });
			methodADC = (CallVisit.class).getMethod("adc", new Class<?>[] { ADC.class });
			
			methodLogin = (CallVisit.class).getMethod("login", new Class<?>[] { Site.class });
			methodLogout = (CallVisit.class).getMethod("logout", new Class<?>[] { Integer.TYPE, SiteHost.class });
			methodRelogin = (CallVisit.class).getMethod("relogin", new Class<?>[] { Site.class });
			methodRefresh = (CallVisit.class).getMethod("refresh",new Class<?>[] { Integer.TYPE, SiteHost.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	// table space
	private List<Space> spaces = new ArrayList<Space>();

	/**
	 *
	 */
	public CallClient(boolean stream) {
		super(stream, CallVisit.class.getName());
	}

	/**
	 * @param host
	 */
	public CallClient(boolean stream, SocketHost host) {
		this(stream);
		this.setRemote(host);
	}

	/**
	 * add space
	 * @param space
	 * @return
	 */
	public boolean add(Space space) {
		if (spaces.contains(space)) {
			return false;
		}
		return spaces.add(space);
	}

	public List<Space> list() {
		return spaces;
	}

	public boolean remove(Space space) {
		return spaces.remove(space);
	}

	public boolean isEmpty() {
		return spaces.isEmpty();
	}

	public int size() {
		return spaces.size();
	}

	/* rpc nothing
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		this.refreshTime();
		super.invoke(methodNothing, null);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#createSpace(com.lexst.db.schema.Table)
	 */
	@Override
	public boolean createSpace(Table table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { table };
		Object param = super.invoke(CallClient.methodCreateSpace, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean deleteSpace(Space space) throws VisitException {
		return deleteSpace(space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#deleteSpace(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteSpace(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(CallClient.methodDeleteSpace, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#insert(com.lexst.db.statement.Insert)
	 */
	@Override
	public int insert(Insert insert, boolean sync) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { insert, new Boolean(sync) };
		Object param = super.invoke(CallClient.methodInsert, params);
		return ((Integer) param).intValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#inject(com.lexst.db.statement.Inject)
	 */
	@Override
	public int inject(Inject inject, boolean sync) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { inject, new Boolean(sync) };
		Object param = super.invoke(CallClient.methodInject, params);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#select(com.lexst.db.statement.Select)
	 */
	@Override
	public byte[] select(Select select) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { select };
		Object param = super.invoke(CallClient.methodSelect, params);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#dc(com.lexst.db.statement.DC, boolean)
	 */
	@Override
	public byte[] dc(DC dc) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { dc };
		Object param = super.invoke(CallClient.methodDC, params);
		return (byte[]) param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#adc(com.lexst.db.statement.ADC)
	 */
	@Override
	public byte[] adc(ADC adc) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { adc };
		Object param = super.invoke(CallClient.methodADC, params);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#delete(com.lexst.db.statement.Delete)
	 */
	@Override
	public long delete(Delete delete) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { delete };
		Object param = super.invoke(CallClient.methodDelete, params);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#update(com.lexst.db.statement.Update)
	 */
	@Override
	public long update(Update update) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { update };
		Object param = super.invoke(CallClient.methodUpdate, params);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#login(com.lexst.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {site};
		Object param = super.invoke(CallClient.methodLogin, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#logout(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean logout(int type, SiteHost host) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type), host };
		Object param = super.invoke(CallClient.methodLogout, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#relogin(com.lexst.site.Site)
	 */
	@Override
	public boolean relogin(Site site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { site };
		Object param = super.invoke(CallClient.methodRelogin, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.call.CallVisit#refresh(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean refresh(int type, SiteHost host) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type), host };
		Object param = super.invoke(CallClient.methodRefresh, params);
		return ((Boolean) param).booleanValue();
	}


}