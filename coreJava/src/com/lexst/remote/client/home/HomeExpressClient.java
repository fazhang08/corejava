/**
 * 
 */
package com.lexst.remote.client.home;

import java.lang.reflect.Method;
import com.lexst.remote.client.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.home.*;


public class HomeExpressClient extends RemoteClient implements HomeExpress {

	private static Method methodNothing;
	private static Method methodVoting;
	private static Method methodIsRunsite;
	
	private static Method methodChunkid;
	private static Method methodSpaces;
	private static Method methodCharsets;
	private static Method methodFindSite;

	static {
		try {
			methodNothing = (HomeExpress.class).getMethod("nothing", new Class<?>[0]);
			methodVoting = (HomeExpress.class).getMethod("voting", new Class<?>[] { SiteHost[].class });
			methodIsRunsite = (HomeExpress.class).getMethod("isRunsite", new Class<?>[0]);
			
			methodChunkid = (HomeExpress.class).getMethod("chunkid", new Class<?>[0]);
			methodSpaces = (HomeExpress.class).getMethod("spaces", new Class<?>[0]);
			methodCharsets = (HomeExpress.class).getMethod("charsets", new Class<?>[0]);
			methodFindSite = (HomeExpress.class).getMethod("findSite", new Class<?>[] { Integer.TYPE });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 * @param stream
	 */
	public HomeExpressClient(boolean stream) {
		super(stream, HomeExpress.class.getName());
	}

	/**
	 * @param stream
	 * @param interfaceName
	 */
	public HomeExpressClient(boolean stream, String interfaceName) {
		super(stream, interfaceName);
	}

	/**
	 * @param stream
	 * @param remote
	 */
	public HomeExpressClient(boolean stream, SocketHost remote) {
		this(stream);
		this.setRemote(remote);
	}
	
	/**
	 * @param remote
	 */
	public HomeExpressClient(SocketHost remote) {
		this(remote.getType() == SocketHost.TCP);
		this.setRemote(remote);
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		this.refreshTime();
		super.invoke(methodNothing, null);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#voting(com.lexst.util.host.SiteHost[])
	 */
	@Override
	public SiteHost voting(SiteHost[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { hosts };
		Object param = super.invoke(methodVoting, params);
		return (SiteHost) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#isRunsite()
	 */
	@Override
	public boolean isRunsite() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodIsRunsite, null);
		return ((Boolean) param).booleanValue();
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#chunkid()
	 */
	@Override
	public byte[] chunkid() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodChunkid, null);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#spaces()
	 */
	@Override
	public byte[] spaces() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodSpaces, null);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#charsets()
	 */
	@Override
	public byte[] charsets() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodCharsets, null);
		return (byte[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeExpress#findSite(int)
	 */
	@Override
	public SiteHost[] findSite(int type) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type) };
		Object param = super.invoke(methodFindSite, params);
		return (SiteHost[]) param;
	}

}