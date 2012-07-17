/**
 *
 */
package com.lexst.remote.client.top;

import java.io.*;
import java.lang.reflect.*;

import com.lexst.db.account.*;
import com.lexst.db.charset.*;
import com.lexst.db.schema.*;
import com.lexst.remote.client.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.top.*;


public class TopClient extends RemoteClient implements TopVisit {

	private static Method methodNothing;
	private static Method methodCurrentTime;
	private static Method methodGetSiteTimeout;
	private static Method methodHello;
	private static Method methodLogin;
	private static Method methodLogout;
	private static Method methodRelogin;
	
	private static Method methodApplyChunkId;
	private static Method methodPullKey;
	
	private static Method methodFindChunkSize;
	private static Method methodSetChunkSize;
	private static Method methodFindCharset;
	private static Method methodGetCharmap;
	
	private static Method methodFindHomeSite;
	private static Method methodSelectCallSite1;
	private static Method methodSelectCallSite2;
	private static Method methodSelectCallSite3;
	
	private static Method methodCreateSchema;
	private static Method methodDeleteSchema;
	private static Method methodGetSchemas;
	
	private static Method methodCreateUser;
	private static Method methodDeleteUser;
	private static Method methodAlterUser;
	private static Method methodAddPermit;
	private static Method methodDeletePermit;
	private static Method methodGetPermits;
	
	private static Method methodCreateTable;
	private static Method methodFindTable;
	private static Method methodDeleteTable;
	private static Method methodGetTables;
	private static Method methodCheckIdentified;
	
	private static Method methodSetOptimizeTime;
	private static Method methodOptimize;
	private static Method methodLoadIndex;
	private static Method methodStopIndex;
	private static Method methodLoadChunk;
	private static Method methodStopChunk;
	private static Method methodBuildTask;
	private static Method methodShowSite;
	
	static {
		try {
			methodNothing = (TopVisit.class).getMethod("nothing", new Class<?>[0]);
			methodCurrentTime = (TopVisit.class).getMethod("currentTime", new Class<?>[0]);
			methodGetSiteTimeout = (TopVisit.class).getMethod("getSiteTimeout", new Class<?>[]{Integer.TYPE}); 
			methodHello = (TopVisit.class).getMethod("hello", new Class<?>[] {Integer.TYPE, SiteHost.class });

			methodLogin = (TopVisit.class).getMethod("login", new Class<?>[] { Site.class });
			methodLogout = (TopVisit.class).getMethod("logout", new Class<?>[] {Integer.TYPE, SiteHost.class });
			methodRelogin = (TopVisit.class).getMethod("relogin", new Class<?>[] { Site.class });
			
			methodApplyChunkId = (TopVisit.class).getMethod("applyChunkId", new Class<?>[] { Integer.TYPE });
			methodPullKey = (TopVisit.class).getMethod("pullKey", new Class<?>[] {String.class, String.class, Integer.TYPE});
			
			methodFindChunkSize = (TopVisit.class).getMethod("findChunkSize", new Class<?>[] {String.class, String.class});
			methodSetChunkSize = (TopVisit.class).getMethod("setChunkSize", new Class<?>[] { String.class, String.class, Integer.TYPE});
			
			methodFindCharset = (TopVisit.class).getMethod("findCharset", new Class<?>[] { String.class});
			methodGetCharmap = (TopVisit.class).getMethod("getCharmap", new Class<?>[] { SiteHost.class });

			methodFindHomeSite = (TopVisit.class).getMethod("findHomeSite", new Class<?>[] { String.class, String.class});
			methodSelectCallSite1 = (TopVisit.class).getMethod("selectCallSite", new Class<?>[] { String.class });
			methodSelectCallSite2 = (TopVisit.class).getMethod("selectCallSite", new Class<?>[] { String.class, String.class });
			methodSelectCallSite3 = (TopVisit.class).getMethod("selectCallSite", new Class<?>[] { String.class, String.class, String.class });

			methodCreateSchema = (TopVisit.class).getMethod("createSchema", new Class<?>[] { SiteHost.class, Schema.class });
			methodDeleteSchema = (TopVisit.class).getMethod("deleteSchema", new Class<?>[] {SiteHost.class, String.class});
			methodGetSchemas = (TopVisit.class).getMethod("getSchemas", new Class<?>[] {SiteHost.class });
			
			methodCreateUser = (TopVisit.class).getMethod("createUser", new Class<?>[]{SiteHost.class, User.class});
			methodDeleteUser = (TopVisit.class).getMethod("deleteUser", new Class<?>[]{SiteHost.class, String.class});
			methodAlterUser = (TopVisit.class).getMethod("alterUser", new Class<?>[] {SiteHost.class, User.class});
			methodAddPermit = (TopVisit.class).getMethod("addPermit", new Class<?>[] { SiteHost.class, Permit.class});
			methodDeletePermit = (TopVisit.class).getMethod("deletePermit", new Class<?>[] { SiteHost.class, Permit.class});
			methodGetPermits = (TopVisit.class).getMethod("getPermits", new Class<?>[] { SiteHost.class});

			methodCreateTable = (TopVisit.class).getMethod("createTable", new Class<?>[] { SiteHost.class, Table.class});
			methodFindTable = (TopVisit.class).getMethod("findTable", new Class<?>[] { SiteHost.class, String.class, String.class});
			methodDeleteTable = (TopVisit.class).getMethod("deleteTable", new Class<?>[] { SiteHost.class, String.class, String.class});
			methodGetTables = (TopVisit.class).getMethod("getTables", new Class<?>[] { SiteHost.class});
	
			methodCheckIdentified = (TopVisit.class).getMethod("checkIdentified", new Class<?>[] { SiteHost.class });
			
			methodSetOptimizeTime = (TopVisit.class).getMethod("setOptimizeTime", new Class<?>[] { String.class, String.class, Integer.TYPE, Long.TYPE });
			
			methodOptimize = (TopVisit.class).getMethod("optimize", new Class<?>[] { SiteHost.class, String.class, String.class, String[].class });
			methodLoadIndex = (TopVisit.class).getMethod("loadIndex", new Class<?>[] { SiteHost.class, String.class, String.class, String[].class });
			methodStopIndex = (TopVisit.class).getMethod("stopIndex", new Class<?>[] { SiteHost.class, String.class, String.class, String[].class });
			methodLoadChunk = (TopVisit.class).getMethod("loadChunk", new Class<?>[] { SiteHost.class, String.class, String.class, String[].class });
			methodStopChunk = (TopVisit.class).getMethod("stopChunk", new Class<?>[] { SiteHost.class, String.class, String.class, String[].class });
			
			methodBuildTask = (TopVisit.class).getMethod("buildTask", new Class<?>[] { SiteHost.class, String.class, String[].class });

			methodShowSite = (TopVisit.class).getMethod("showSite", new Class<?>[] { Integer.TYPE, String.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 *
	 */
	public TopClient(boolean stream) {
		super(stream, TopVisit.class.getName());
	}

	/**
	 *
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public TopClient(boolean stream, String ip, int port) {
		this(stream);
		super.setRemote(ip, port);
	}
	
	public TopClient(boolean stream, SocketHost remote) {
		this(stream);
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
	 * @see com.lexst.visit.naming.top.TopVisit#currentTime()
	 */
	@Override
	public long currentTime() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodCurrentTime, null);
		return ((Long) param).longValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#getSiteTimeout(int)
	 */
	@Override
	public int getSiteTimeout(int type) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type) };
		Object param = super.invoke(methodGetSiteTimeout, params);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#hello(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public int hello(int type, SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type), local };
		Object param = super.invoke(methodHello, params);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#login(com.lexst.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { site };
		Object param = super.invoke(methodLogin, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#logout(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean logout(int type, SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(type), local };
		Object param = super.invoke(methodLogout, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#relogin(com.lexst.site.Site)
	 */
	@Override
	public boolean relogin(Site site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { site };
		Object param = super.invoke(methodRelogin, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#applyChunkId(int)
	 */
	@Override
	public long[] applyChunkId(int num) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(num)};
		Object param = super.invoke(methodApplyChunkId, params);
		return (long[])param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#pullKey(java.lang.String, java.lang.String, int)
	 */
	@Override
	public Number[] pullKey(String db, String table, int num) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, new Integer(num)};
		Object param = super.invoke(methodPullKey, params);
		return (Number[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#findChunkSize(java.lang.String, java.lang.String)
	 */
	@Override
	public int findChunkSize(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table};
		Object param = super.invoke(methodFindChunkSize, params);
		return ((Integer)param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#setChunkSize(java.lang.String, java.lang.String, int)
	 */
	@Override
	public boolean setChunkSize(String db, String table, int size) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, new Integer(size) };
		Object param = super.invoke(methodSetChunkSize, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#findCharset(java.lang.String)
	 */
	@Override
	public SQLCharset findCharset(String db) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db };
		Object param = super.invoke(methodFindCharset, params);
		return (SQLCharset)param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#getCharset(com.lexst.util.host.SiteHost)
	 */
	@Override
	public SQLCharmap getCharmap(SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodGetCharmap, params);
		return (SQLCharmap)param;
	}
	
	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] findHomeSite(Space space) throws VisitException {
		return findHomeSite(space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#findHomeSite(java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] findHomeSite(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table};
		Object param = super.invoke(methodFindHomeSite, params);
		return (SiteHost[])param;
	}
	
	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] selectCallSite(Space space) throws VisitException {
		return selectCallSite(space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#selectCallSite(java.lang.String)
	 */
	@Override
	public SiteHost[] selectCallSite(String naming) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming };
		Object param = super.invoke(methodSelectCallSite1, params);
		return (SiteHost[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#selectCallSite(java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] selectCallSite(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodSelectCallSite2, params);
		return (SiteHost[]) param;
	}
	
	/**
	 * @param naming
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] selectCallSite(String naming, Space space) throws VisitException {
		return selectCallSite(naming, space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#selectCallSite(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] selectCallSite(String naming, String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming, db, table };
		Object param = super.invoke(methodSelectCallSite3, params);
		return (SiteHost[]) param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#createSchema(com.lexst.util.host.SiteHost, com.lexst.db.schema.Schema)
	 */
	@Override
	public boolean createSchema(SiteHost local, Schema schema) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, schema};
		Object param = super.invoke(methodCreateSchema, params);
		return ((Boolean)param).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#deleteSchema(com.lexst.util.host.SiteHost, java.lang.String)
	 */
	@Override
	public boolean deleteSchema(SiteHost local, String db) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db};
		Object param = super.invoke(methodDeleteSchema, params);
		return ((Boolean)param).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#getSchemas(com.lexst.util.host.SiteHost)
	 */
	@Override
	public String[] getSchemas(SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodGetSchemas, params);
		return (String[])param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#createUser(com.lexst.util.host.SiteHost, com.lexst.db.account.User)
	 */
	@Override
	public boolean createUser(SiteHost local, User user) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, user};
		Object param = super.invoke(methodCreateUser, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#deleteUser(com.lexst.util.host.SiteHost, java.lang.String)
	 */
	@Override
	public boolean deleteUser(SiteHost local, String username) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, username };
		Object param = super.invoke(methodDeleteUser, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#alterUser(com.lexst.util.host.SiteHost, com.lexst.db.account.User)
	 */
	@Override
	public boolean alterUser(SiteHost local, User user) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, user };
		Object param = super.invoke(methodAlterUser, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#addPermit(com.lexst.util.host.SiteHost, com.lexst.db.account.Permit)
	 */
	@Override
	public boolean addPermit(SiteHost local, Permit permit) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, permit};
		Object param = super.invoke(methodAddPermit, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#deletePermit(com.lexst.util.host.SiteHost, com.lexst.db.account.Permit)
	 */
	@Override
	public boolean deletePermit(SiteHost local, Permit permit) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, permit };
		Object param = super.invoke(methodDeletePermit, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#getPermits(com.lexst.util.host.SiteHost)
	 */
	@Override
	public Permit[] getPermits(SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodGetPermits, params);
		return (Permit[])param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#createTable(com.lexst.util.host.SiteHost, com.lexst.db.schema.Table)
	 */
	@Override
	public boolean createTable(SiteHost local, Table table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, table};
		Object param = super.invoke(methodCreateTable, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#findTable(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String)
	 */
	@Override
	public Table findTable(SiteHost local, String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table };
		Object param = super.invoke(methodFindTable, params);
		return (Table)param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#deleteTable(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteTable(SiteHost local, String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table };
		Object param = super.invoke(methodDeleteTable, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#getTables(com.lexst.util.host.SiteHost)
	 */
	@Override
	public Table[] getTables(SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodGetTables, params);
		return (Table[])param;
	}

	/* 
	 * live user's level
	 * 
	 * @see com.lexst.visit.naming.top.TopVisit#checkIdentified(com.lexst.util.host.SiteHost)
	 */
	@Override
	public short checkIdentified(SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodCheckIdentified, params);
		return ((Short)param).shortValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#setOptimizeTime(java.lang.String, java.lang.String, int, long)
	 */
	@Override
	public boolean setOptimizeTime(String db, String table, int type, long time) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, new Integer(type), new Long(time)};
		Object param = super.invoke(methodSetOptimizeTime, params);
		return ((Boolean)param).booleanValue();
	}

	/**
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] optimize(SiteHost local, Space space, String[] hosts) throws VisitException {
		return optimize(local, space.getSchema(), space.getTable(), hosts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#optimize(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public IP[] optimize(SiteHost local, String db, String table, String[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, hosts };
		Object param = super.invoke(methodOptimize, params);
		return (IP[])param;
	}
	
	/**
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] loadIndex(SiteHost local, Space space, String[] hosts) throws VisitException {
		return loadIndex(local, space.getSchema(), space.getTable(), hosts);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#loadIndex(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public IP[] loadIndex(SiteHost local, String db, String table, String[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, hosts };
		Object param = super.invoke(methodLoadIndex, params);
		return (IP[])param;
	}

	/**
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] stopIndex(SiteHost local, Space space, String[] hosts) throws VisitException {
		return stopIndex(local, space.getSchema(), space.getTable(), hosts);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#stopIndex(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public IP[] stopIndex(SiteHost local, String db, String table, String[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, hosts };
		Object param = super.invoke(methodStopIndex, params);
		return (IP[])param;
	}

	/**
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] loadChunk(SiteHost local, Space space, String[] hosts) throws VisitException {
		return loadChunk(local, space.getSchema(), space.getTable(), hosts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#loadChunk(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public IP[] loadChunk(SiteHost local, String db, String table, String[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, hosts };
		Object param = super.invoke(methodLoadChunk, params);
		return (IP[])param;
	}

	/**
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] stopChunk(SiteHost local, Space space, String[] hosts) throws VisitException {
		return stopChunk(local, space.getSchema(), space.getTable(), hosts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#stopChunk(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public IP[] stopChunk(SiteHost local, String db, String table, String[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, hosts };
		Object param = super.invoke(methodStopChunk, params);
		return (IP[])param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#buildTask(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String[])
	 */
	@Override
	public IP[] buildTask(SiteHost local, String naming, String[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, naming, hosts };
		Object param = super.invoke(methodBuildTask, params);
		return (IP[])param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.top.TopVisit#showSite(int, java.lang.String)
	 */
	@Override
	public SiteHost[] showSite(int site, String from) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(site), from };
		Object param = super.invoke(methodShowSite, params);
		return (SiteHost[])param;
	}

}