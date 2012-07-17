/**
 *
 */
package com.lexst.remote.client.home;

import java.io.*;
import java.lang.reflect.*;

import com.lexst.db.charset.*;
import com.lexst.db.schema.*;
import com.lexst.remote.client.*;
import com.lexst.site.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.home.*;

/**
 *
 * HomeClient
 *
 * HomeClient是各节点向HOME节点请求时使用
 */
public class HomeClient extends RemoteClient implements HomeVisit {

	private static Method methodNothing;
	private static Method methodCurrentTime;
	private static Method methodGetSiteTimeout;
	private static Method methodHello;
	private static Method methodFindLogSite;
	private static Method methodPullSingle;
	private static Method methodPullKey;
	private static Method methodFindChunkSize;
	private static Method methodBalance;
	private static Method methodFindDataSite;
	private static Method methodFindDataSiteRank;
	private static Method methodFindDataSiteChunkId;
	private static Method methodFindCallSite1;
	private static Method methodFindCallSite2;
	private static Method methodFindCallSite3;
	private static Method methodFindWorkSite;
	
	private static Method methodBatchWorkSite;
	private static Method methodBatchDataSite;
	
	private static Method methodFindBuildSite1;
	private static Method methodFindBuildSite2;
	private static Method methodFindBuildSite3;

	private static Method methodLogin;
	private static Method methodLogout;
	private static Method methodRelogin;

	private static Method methodFindCharset;
	private static Method methodFindTable;
	
	private static Method methodCreateSpace;
	private static Method methodDeleteSpace;
	
	private static Method methodOptimize;
	private static Method methodLoadIndex;
	private static Method methodStopIndex;
	private static Method methodLoadChunk;
	private static Method methodStopChunk;

	private static Method methodBuildTask;
	
	private static Method methodAgree;
	private static Method methodPublish;
	private static Method methodUpgrade;
	private static Method methodAccede;

	private static Method methodFindBuildChunk;
	private static Method methodShowSite;
	
	static {
		try {
			methodNothing = (HomeVisit.class).getMethod("nothing", new Class<?>[0]);
			methodCurrentTime = (HomeVisit.class).getMethod("currentTime", new Class<?>[0]);
			methodGetSiteTimeout = (HomeVisit.class).getMethod("getSiteTimeout", new Class<?>[] { Integer.TYPE });
			methodHello = (HomeVisit.class).getMethod("hello", new Class<?>[] {Integer.TYPE, SiteHost.class });
			methodFindLogSite = (HomeVisit.class).getMethod("findLogSite", new Class<?>[] { Integer.TYPE });

			methodBatchWorkSite = (HomeVisit.class).getMethod("batchWorkSite", new Class<?>[0]);
			methodBatchDataSite = (HomeVisit.class).getMethod("batchDataSite", new Class<?>[0]);

			methodPullSingle = (HomeVisit.class).getMethod("pullSingle", new Class<?>[] { Integer.TYPE });
			methodPullKey = (HomeVisit.class).getMethod("pullKey", new Class<?>[] { String.class, String.class, Integer.TYPE });
			methodFindChunkSize = (HomeVisit.class).getMethod("findChunkSize", new Class<?>[] { String.class, String.class });

			methodBalance = (HomeVisit.class).getMethod("balance", new Class<?>[] { Integer.TYPE });
			methodFindDataSite = (HomeVisit.class).getMethod("findDataSite", new Class<?>[] { String.class, String.class });
			methodFindDataSiteRank = (HomeVisit.class).getMethod("findDataSite", new Class<?>[] { String.class, String.class, Integer.TYPE });
			methodFindDataSiteChunkId = (HomeVisit.class).getMethod("findDataSite", new Class<?>[] { String.class, String.class, Long.TYPE });
			methodFindCallSite1 = (HomeVisit.class).getMethod("findCallSite", new Class<?>[] { String.class, String.class });
			methodFindCallSite2 = (HomeVisit.class).getMethod("findCallSite", new Class<?>[] { String.class });
			methodFindCallSite3 = (HomeVisit.class).getMethod("findCallSite", new Class<?>[] { String.class, String.class, String.class});
			methodFindWorkSite = (HomeVisit.class).getMethod("findWorkSite", new Class<?>[] { String.class });

			methodFindBuildSite1 = (HomeVisit.class).getMethod("findBuildSite", new Class<?>[] { String.class, String.class });
			methodFindBuildSite2 = (HomeVisit.class).getMethod("findBuildSite", new Class<?>[] { String.class });
			methodFindBuildSite3 = (HomeVisit.class).getMethod("findBuildSite", new Class<?>[0]);

			methodLogin = (HomeVisit.class).getMethod("login", new Class<?>[] { Site.class });
			methodLogout = (HomeVisit.class).getMethod("logout", new Class<?>[] { Integer.TYPE, SiteHost.class });
			methodRelogin = (HomeVisit.class).getMethod("relogin", new Class<?>[] { Site.class });

			methodFindCharset = (HomeVisit.class).getMethod("findCharset", new Class<?>[] { String.class });
			methodFindTable = (HomeVisit.class).getMethod("findTable", new Class<?>[] { String.class, String.class });
			
			methodCreateSpace = (HomeVisit.class).getMethod("createSpace", new Class<?>[] { Table.class });
			methodDeleteSpace = (HomeVisit.class).getMethod("deleteSpace", new Class<?>[] { String.class, String.class });
			
			methodOptimize = (HomeVisit.class).getMethod("optimize", new Class<?>[] { String.class, String.class, IP[].class });
			methodLoadIndex = (HomeVisit.class).getMethod("loadIndex", new Class<?>[] { String.class, String.class, IP[].class });
			methodStopIndex = (HomeVisit.class).getMethod("stopIndex", new Class<?>[] { String.class, String.class, IP[].class });
			methodLoadChunk = (HomeVisit.class).getMethod("loadChunk", new Class<?>[] { String.class, String.class, IP[].class });
			methodStopChunk = (HomeVisit.class).getMethod("stopChunk", new Class<?>[] { String.class, String.class, IP[].class });
			
			methodBuildTask = (HomeVisit.class).getMethod("buildTask", new Class<?>[] { String.class, IP[].class });
			
			methodAgree = (HomeVisit.class).getMethod("agree", new Class<?>[] { Long.TYPE, SiteHost.class });
			methodPublish = (HomeVisit.class).getMethod("publish", new Class<?>[] { SiteHost.class, String.class, String.class, Long.TYPE, Long.TYPE });
			methodUpgrade = (HomeVisit.class).getMethod("upgrade", new Class<?>[] { SiteHost.class, String.class, String.class, long[].class, long[].class });
			methodAccede = (HomeVisit.class).getMethod("accede", new Class<?>[] { String.class, String.class, Long.TYPE, Long.TYPE, Long.TYPE });
			
			methodFindBuildChunk = (HomeVisit.class).getMethod("findBuildChunk", new Class<?>[] { String.class });
			methodShowSite = (HomeVisit.class).getMethod("showSite", new Class<?>[] { Integer.TYPE });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	/**
	 *
	 */
	public HomeClient(boolean stream) {
		super(stream, HomeVisit.class.getName());
	}

	/**
	 * @param host (home site address)
	 * @throws IOException
	 */
	public HomeClient(boolean stream, SocketHost host)  {
		this(stream);
		this.setRemote(host);
	}

	/**
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public HomeClient(boolean stream, String ip, int port) {
		this(stream);
		super.setRemote(ip, port);
	}

	/**
	 * @param remote
	 */
	public HomeClient(SocketHost remote) {
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

	/*
	 * 
	 */
	@Override
	public long currentTime() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodCurrentTime, null);
		return ((Long) param).longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.visit.naming.home.HomeVisit#getTimeout(int)
	 */
	@Override
	public int getSiteTimeout(int siteType) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(siteType) };
		Object param = super.invoke(HomeClient.methodGetSiteTimeout, params);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#hello(int, java.lang.String, int, int)
	 */
	@Override
	public int hello(int siteType, SiteHost host) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(siteType), host };
		Object param = super.invoke(HomeClient.methodHello, params);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#batchWorkSite()
	 */
	@Override
	public Site[] batchWorkSite() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(HomeClient.methodBatchWorkSite, null);
		return (Site[])param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#batchDataSite()
	 */
	@Override
	public Site[] batchDataSite() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(HomeClient.methodBatchDataSite, null);
		return (Site[])param;
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.lead.LeadVisit#applyChunkId(int)
	 */
	@Override
	public long[] pullSingle(int num) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(num) };
		Object param = super.invoke(methodPullSingle, params);
		return (long[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#pullKey(java.lang.String, java.lang.String, int)
	 */
	@Override
	public Number[] pullKey(String db, String table, int num) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, new Integer(num) };
		Object param = super.invoke(methodPullKey, params);
		return (Number[]) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#balance(int)
	 */
	@Override
	public Space[] balance(int num) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(num) };
		Object param = super.invoke(methodBalance, params);
		return (Space[]) param;
	}

	/**
	 * apply table chunk size
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public int findChunkSize(Space space) throws VisitException {
		return findChunkSize(space.getSchema(), space.getTable());
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#getChunkSize(java.lang.String, java.lang.String)
	 */
	@Override
	public int findChunkSize(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodFindChunkSize, params);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#applyLogSite()
	 */
	@Override
	public SiteHost findLogSite(int siteType) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(siteType) };
		Object param = super.invoke(HomeClient.methodFindLogSite, params);
		return (SiteHost) param;
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] findDataSite(Space space) throws VisitException {
		return findDataSite(space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findDatSite(java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] findDataSite(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {db, table};
		Object param = super.invoke(HomeClient.methodFindDataSite, params);
		return (SiteHost[])param;
	}
	
	/**
	 * @param space
	 * @param rank
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] findDataSite(Space space, int rank) throws VisitException {
		return findDataSite(space.getSchema(), space.getTable(), rank);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findDataSite(java.lang.String, java.lang.String, int)
	 */
	@Override
	public SiteHost[] findDataSite(String db, String table, int rank) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {db, table, new java.lang.Integer(rank)};
		Object param = super.invoke(HomeClient.methodFindDataSiteRank, params);
		return (SiteHost[])param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findDataSite(java.lang.String, java.lang.String, long)
	 */
	@Override
	public SiteHost[] findDataSite(String db, String table, long chunkid) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, new java.lang.Long(chunkid) };
		Object param = super.invoke(HomeClient.methodFindDataSiteChunkId, params);
		return (SiteHost[]) param;
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] findCallSite(Space space) throws VisitException {
		return findCallSite(space.getSchema(), space.getTable());
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findCallSite(java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] findCallSite(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {db, table};
		Object param = super.invoke(HomeClient.methodFindCallSite1, params);
		return (SiteHost[])param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findCallSite(java.lang.String)
	 */
	@Override
	public SiteHost[] findCallSite(String naming) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming };
		Object param = super.invoke(HomeClient.methodFindCallSite2, params);
		return (SiteHost[])param;
	}

	/**
	 * @param naming
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] findCallSite(String naming, Space space) throws VisitException {
		return findCallSite(naming, space.getSchema(), space.getTable());
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findCallSite(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] findCallSite(String naming, String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming, db, table };
		Object param = super.invoke(HomeClient.methodFindCallSite3, params);
		return (SiteHost[]) param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findWorkSite(java.lang.String)
	 */
	@Override
	public SiteHost[] findWorkSite(String naming) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming };
		Object param = super.invoke(HomeClient.methodFindWorkSite, params);
		return (SiteHost[]) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findBuildSite(java.lang.String, java.lang.String)
	 */
	@Override
	public SiteHost[] findBuildSite(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(HomeClient.methodFindBuildSite1, params);
		return (SiteHost[]) param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findBuildSite(java.lang.String)
	 */
	@Override
	public SiteHost[] findBuildSite(String naming) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming };
		Object param = super.invoke(HomeClient.methodFindBuildSite2, params);
		return (SiteHost[]) param;		
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findBuildSite()
	 */
	@Override
	public SiteHost[] findBuildSite() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(HomeClient.methodFindBuildSite3, null);
		return (SiteHost[]) param;
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#login(com.lexst.site.Site)
	 */
	@Override
	public boolean login(Site site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {site};
		Object param = super.invoke(HomeClient.methodLogin, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#logout(int, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean logout(int type, SiteHost host) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new java.lang.Integer(type), host};
		Object param = super.invoke(HomeClient.methodLogout, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#relogin(com.lexst.site.Site)
	 */
	@Override
	public boolean relogin(Site site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {site};
		Object param = super.invoke(HomeClient.methodRelogin, params);
		return ((Boolean)param).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findCharset(java.lang.String)
	 */
	public SQLCharset findCharset(String db) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db };
		Object param = super.invoke(HomeClient.methodFindCharset, params);
		return (SQLCharset) param;
	}
	
	/**
	 * find table configure by space
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public Table findTable(Space space) throws VisitException {
		return findTable(space.getSchema(), space.getTable());
	}

	/*
	 * find a table configure
	 * @see com.lexst.visit.naming.home.HomeVisit#findTable(java.lang.String, java.lang.String)
	 */
	@Override
	public Table findTable(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(HomeClient.methodFindTable, params);
		return (Table) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#createSpace(com.lexst.db.schema.Table)
	 */
	@Override
	public boolean createSpace(Table table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { table };
		Object param = super.invoke(HomeClient.methodCreateSpace, params);
		return ((Boolean)param).booleanValue();
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
	 * @see com.lexst.visit.naming.home.HomeVisit#deleteSpace(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteSpace(String db, String table) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(HomeClient.methodDeleteSpace, params);
		return ((Boolean)param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#optimize(java.lang.String, java.lang.String, com.lexst.util.host.IP[])
	 */
	@Override
	public IP[] optimize(String db, String table, IP[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, hosts };
		Object param = super.invoke(HomeClient.methodOptimize, params);
		return (IP[]) param;
	}

	/**
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] loadIndex(Space space, IP[] hosts) throws VisitException {
		return loadIndex(space.getSchema(), space.getTable(), hosts);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#loadIndex(java.lang.String, java.lang.String, com.lexst.util.host.IP[])
	 */
	@Override
	public IP[] loadIndex(String db, String table, IP[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, hosts };
		Object param = super.invoke(HomeClient.methodLoadIndex, params);
		return (IP[]) param;
	}
	
	/**
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] stopIndex(Space space, IP[] hosts) throws VisitException {
		return stopIndex(space.getSchema(), space.getTable(), hosts);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#stopIndex(java.lang.String, java.lang.String, com.lexst.util.host.IP[])
	 */
	@Override
	public IP[] stopIndex(String db, String table, IP[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, hosts };
		Object param = super.invoke(HomeClient.methodStopIndex, params);
		return (IP[]) param;
	}
	
	/**
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] loadChunk(Space space, IP[] hosts) throws VisitException {
		return loadChunk(space.getSchema(), space.getTable(), hosts);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#loadChunk(java.lang.String, java.lang.String, com.lexst.util.host.IP[])
	 */
	@Override
	public IP[] loadChunk(String db, String table, IP[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, hosts };
		Object param = super.invoke(HomeClient.methodLoadChunk, params);
		return (IP[]) param;
	}
	
	/**
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] stopChunk(Space space, IP[] hosts) throws VisitException {
		return stopChunk(space.getSchema(), space.getTable(), hosts);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#stopChunk(java.lang.String, java.lang.String, com.lexst.util.host.IP[])
	 */
	@Override
	public IP[] stopChunk(String db, String table, IP[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, hosts };
		Object param = super.invoke(HomeClient.methodStopChunk, params);
		return (IP[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#buildTask(java.lang.String, com.lexst.util.host.IP[])
	 */
	public IP[] buildTask(String naming, IP[] hosts) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] {naming, hosts};
		Object param = super.invoke(HomeClient.methodBuildTask, params);
		return (IP[]) param;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#agree(long, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean agree(long chunkId, SiteHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new java.lang.Long(chunkId), local};
		Object param = super.invoke(HomeClient.methodAgree, params);
		return ((Boolean)param).booleanValue();
	}
	
	/**
	 * publish data
	 * @param local
	 * @param space
	 * @param chunkId
	 * @return
	 * @throws VisitException
	 */
	public boolean publish(SiteHost local, Space space, long chunkId, long length) throws VisitException {
		return publish(local, space.getSchema(), space.getTable(), chunkId, length);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#publish(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, long)
	 */
	@Override
	public boolean publish(SiteHost local, String db, String table, long chunkId, long length)
			throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, new Long(chunkId), new Long(length)};
		Object param = super.invoke(HomeClient.methodPublish, params);
		return ((Boolean)param).booleanValue();
	}

	/**
	 * notify home site, upgrade chunk
	 * @param local
	 * @param space
	 * @param oldIds
	 * @param newIds
	 * @return
	 * @throws VisitException
	 */
	public boolean upgrade(SiteHost local, Space space, long[] oldIds, long[] newIds) throws VisitException {
		return upgrade(local, space.getSchema(), space.getTable(), oldIds, newIds);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#upgrade(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, long[], long[])
	 */
	@Override
	public boolean upgrade(SiteHost local, String db, String table,
			long[] oldIds, long[] newIds) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, db, table, oldIds, newIds };
		Object param = super.invoke(HomeClient.methodUpgrade, params);
		return ((Boolean) param).booleanValue();
	}
	
	/**
	 * @param space
	 * @param chunkid
	 * @param length
	 * @param modified
	 * @return
	 * @throws VisitException
	 */
	public boolean accede(Space space, long chunkid, long length, long modified)
			throws VisitException {
		return accede(space.getSchema(), space.getTable(), chunkid, length, modified);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#accede(java.lang.String, java.lang.String, long, long, long)
	 */
	@Override
	public boolean accede(String db, String table, long chunkid, long length,
			long modified) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { db, table, chunkid, length, modified };
		Object param = super.invoke(HomeClient.methodAccede, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#findBuildChunk(java.lang.String)
	 */
	@Override
	public long[] findBuildChunk(String naming) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { naming };
		Object param = super.invoke(HomeClient.methodFindBuildChunk, params);
		return (long[]) param;
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.home.HomeVisit#showSite(int)
	 */
	@Override
	public SiteHost[] showSite(int site) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { new Integer(site) };
		Object param = super.invoke(HomeClient.methodShowSite, params);
		return (SiteHost[]) param;
	}

}