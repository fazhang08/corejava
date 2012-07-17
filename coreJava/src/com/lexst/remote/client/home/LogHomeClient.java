/**
 *
 */
package com.lexst.remote.client.home;

import java.io.*;
import java.lang.reflect.*;

import com.lexst.remote.client.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.home.*;


public class LogHomeClient extends RemoteClient implements LogVisit {

	private static Method methodNothing;
	private static Method methodCurrentTime;
	private static Method methodGetSiteTimeout;

	private static Method methodLogin;
	private static Method methodLogout;
	private static Method methodRelogin;

	static {
		try {
			methodNothing = (LogVisit.class).getMethod("nothing", new Class<?>[0]);
			methodCurrentTime = (LogVisit.class).getMethod("currentTime", new Class<?>[0]);
			methodGetSiteTimeout = (LogVisit.class).getMethod("getSiteTimeout", new Class<?>[] { Integer.TYPE });

			methodLogin = (LogVisit.class).getMethod("login", new Class<?>[] { Site.class });
			methodLogout = (LogVisit.class).getMethod("logout", new Class<?>[] { Integer.TYPE, SiteHost.class });
			methodRelogin = (LogVisit.class).getMethod("relogin", new Class<?>[] { Site.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 *
	 */
	public LogHomeClient(boolean stream) {
		super(stream, LogVisit.class.getName());
	}

	/**
	 * @param host (home site address)
	 * @throws IOException
	 */
	public LogHomeClient(boolean stream, SocketHost host)  {
		this(stream);
		this.setRemote(host);
	}

	/**
	 *
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public LogHomeClient(boolean stream, String ip, int port) {
		this(stream);
		super.setRemote(ip, port);
	}

	/*
	 * 
	 */
	@Override
	public long currentTime() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodCurrentTime, null);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#getSiteTimeout(int)
	 */
	@Override
	public int getSiteTimeout(int type) throws VisitException {
		Object[] params = new Object[] { new Integer(type) };
		Object param = super.invoke(LogHomeClient.methodGetSiteTimeout, params);
		return  ((Integer)param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#login(com.lexst.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		Object[] params = new Object[] {site};
		Object param = super.invoke(LogHomeClient.methodLogin, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#logout(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean logout(int type, SiteHost host) throws VisitException {
		Object[] params = new Object[] { new java.lang.Integer(type), host};
		Object param = super.invoke(LogHomeClient.methodLogout, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.LogHomeVisit#relogin(com.lexst.site.Site)
	 */
	@Override
	public boolean relogin(Site site) throws VisitException {
		Object[] params = new Object[] {site};
		Object param = super.invoke(LogHomeClient.methodRelogin, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		// TODO Auto-generated method stub
		super.invoke(LogHomeClient.methodNothing, null);
	}

}
