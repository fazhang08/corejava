/**
 * 
 */
package com.lexst.remote.client.top;

import java.lang.reflect.*;

import com.lexst.remote.client.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.top.*;

public class TopExpressClient extends RemoteClient implements TopExpress {

	private static Method methodNothing;
	private static Method methodVoting;
	private static Method methodIsRunsite;

	private static Method methodDict;
	private static Method methodSingle;
	private static Method methodPid;
	private static Method methodAccounts;
	private static Method methodFindSite;

	static {
		try {
			methodNothing = (TopExpress.class).getMethod("nothing", new Class<?>[0]);
			methodVoting = (TopExpress.class).getMethod("voting", new Class<?>[] { SiteHost[].class });
			methodIsRunsite = (TopExpress.class).getMethod("isRunsite", new Class<?>[0]);
			
			methodDict = (TopExpress.class).getMethod("dict", new Class<?>[0]);
			methodSingle = (TopExpress.class).getMethod("single", new Class<?>[0]);
			methodPid = (TopExpress.class).getMethod("pid", new Class<?>[0]);
			methodAccounts = (TopExpress.class).getMethod("accounts", new Class<?>[0]);
			methodFindSite = (TopExpress.class).getMethod("findSite", new Class<?>[] { Integer.TYPE });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 * @param stream
	 */
	public TopExpressClient(boolean stream) {
		super(stream, TopExpress.class.getName());
	}

	/**
	 * @param stream
	 * @param interfaceName
	 */
	public TopExpressClient(boolean stream, String interfaceName) {
		super(stream, interfaceName);
	}

	/**
	 * @param stream
	 * @param remote
	 */
	public TopExpressClient(boolean stream, SocketHost remote) {
		this(stream);
		this.setRemote(remote);
	}
	
	/**
	 * @param remote
	 */
	public TopExpressClient(SocketHost remote) {
		this(remote.getType() == SocketHost.TCP);
		this.setRemote(remote);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		this.refreshTime();
		super.invoke(methodNothing, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.visit.naming.top.TopExpress#voting(com.lexst.util.host.SiteHost[])
	 */
	@Override
	public SiteHost voting(SiteHost[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { hosts };
		Object param = super.invoke(methodVoting, params);
		return (SiteHost) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#isRunsite()
	 */
	@Override
	public boolean isRunsite() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodIsRunsite, null);
		return ((Boolean) param).booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.visit.naming.top.TopExpress#dict()
	 */
	@Override
	public byte[] dict() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodDict, null);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#single()
	 */
	@Override
	public long single() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodSingle, null);
		return ((Long)param).longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.visit.naming.top.TopExpress#pid()
	 */
	@Override
	public byte[] pid() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodPid, null);
		return (byte[]) param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.visit.naming.top.TopExpress#accounts()
	 */
	@Override
	public byte[] accounts() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodAccounts, null);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopExpress#findSite(int)
	 */
	@Override
	public SiteHost[] findSite(int type) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type) };
		Object param = super.invoke(methodFindSite, params);
		return (SiteHost[]) param;
	}

}