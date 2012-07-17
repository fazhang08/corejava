/**
 *
 */
package com.lexst.live.pool;

import java.io.*;
import java.security.interfaces.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.db.account.*;
import com.lexst.db.charset.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.live.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.top.*;
import com.lexst.security.*;
import com.lexst.site.*;
import com.lexst.site.live.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.xml.*;
import com.lexst.pool.Pool;

public class TopPool extends Pool {

	private static TopPool selfHandle = new TopPool();
	
	private LiveListener listener;

	// local site
	private LiveSite local;
	// top site host
	private SiteHost remote;
	// into check status
	private boolean into;
	// site timeout
	private int siteTimeout;
	// authority set
	private List<Permit> permits = new ArrayList<Permit>();
	// database name set	
	private Set<String> schemas = new HashSet<String>();
	// table set
	private Map<Space, Table> tables = new HashMap<Space, Table>();

	// login user property(dba or user)
	private short userRank;
	// sql charset
	private SQLCharmap charmap;
	// database name -> char set
	private Map<String, SQLCharset> mapCharset = new HashMap<String, SQLCharset>();

	private long iseeTime;
	
	/**
	 *
	 */
	private TopPool() {
		// cannot check
		into = false;
		// default site timeout
		siteTimeout = 15000;
		// unknown user rank
		userRank = 0;
	}

	/**
	 * @return
	 */
	public static TopPool getInstance() {
		return TopPool.selfHandle;
	}
	
	public void setLocal(LiveSite site) {
		this.local = site;
	}

	public LiveSite getLocal() {
		return this.local;
	}
	
	public void setRemote(SiteHost host) {
		this.remote = new SiteHost(host);
	}
	public SiteHost getRemote() {
		return this.remote;
	}
	
	public void setLiveListener(LiveListener listener) {
		this.listener = listener;
	}
	
	public List<String> listSchema() {
		return new ArrayList<String>(schemas);
	}

	public SQLCharmap getSQLCharMap() {
		return this.charmap;
	}

	public Table findTable(Space space) {
		return tables.get(space);
	}
	
	public List<Table> listTable() {
		ArrayList<Table> a = new ArrayList<Table>();
		a.addAll(tables.values());
		return a;
	}
	
	public boolean existsTable(Space space) {
		return findTable(space) != null;
	}
	
	public boolean addCharset(String db, SQLCharset set) {
		String low = db.toLowerCase();
		return mapCharset.put(low, set) == null;
	}

	public SQLCharset findCharset(String db) {
		String low = db.toLowerCase();
		return mapCharset.get(low);
	}

	public void resetUser() {
		userRank = 0;
		permits.clear();
		schemas.clear();
		tables.clear();
		if (charmap != null) {
			charmap.clear();
		}
		mapCharset.clear();
	}

	/**
	 * reset all
	 */
	public void reset() {
		into = false;
		resetUser();
	}

	public void setInto(boolean b) {
		this.into = b;
	}
	public boolean isInto() {
		return this.into;
	}

	public boolean addPermit(TopClient client, Site local, Permit permit) {
		boolean nullable = (client == null);
		if (nullable) client = fetch();
		boolean success = false;
		try {
			
			if (client != null) {
				success = client.addPermit(local.getHost(), permit);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return success;
	}

	public boolean deletePermit(TopClient client, Site local, Permit permit) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			success = client.deletePermit(local.getHost(), permit);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return success;
	}

	public boolean createUser(TopClient client, Site local, User user) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if(nullable) client = fetch();
			if (client != null) {
				success = client.createUser(local.getHost(), user);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return success;
	}

	public boolean deleteUser(TopClient client, Site local, String username) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.deleteUser(local.getHost(), username);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return success;
	}

	public boolean alterUser(TopClient client, Site local, User user) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.alterUser(local.getHost(), user);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return success;
	}

	/**
	 * create a database
	 * @param client
	 * @param local
	 * @param schema
	 * @return
	 */
	public boolean createSchema(TopClient client, Site local, Schema schema) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.createSchema(local.getHost(), schema);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);

//		if(success) {
//			schemas.add(schema.getName());
//			
////			schema.getCharset();
////			try {
////				SQLCharset set = client.findCharset(names[i]);
////				if (set != null) {
////					this.addCharset(names[i], set);
////				}
////			} catch (VisitException exp) {
////				Logger.error(exp);
////			}
//
//		}
		
		Logger.note(success, "TopPool.createSchema, create database %s", schema.getName());
		return success;
	}

	/**
	 * delete a database
	 * @param client
	 * @param local
	 * @param db
	 * @return
	 */
	public boolean deleteSchema(TopClient client, Site local, String db) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.deleteSchema(local.getHost(), db);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		
		Logger.note(success, "TopPool.deleteSchema, delete database %s", db);
		return success;
	}

	/**
	 * create a database table
	 * @param table
	 * @return
	 */
	public boolean createTable(TopClient client, Site local, Table table) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			success = client.createTable(local.getHost(), table);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		
		if (success) {
			Space space = table.getSpace();
			tables.put(space, table);
		}
		
		Logger.note(success, "TopPool.createTable, create '%s'", table.getSpace());
		return success;
	}

	/**
	 * delete a database table
	 * @param space
	 * @return
	 */
	public boolean deleteTable(TopClient client, Site local, Space space) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.deleteTable(local.getHost(), space.getSchema(), space.getTable());
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		
		Logger.note(success, "TopPool.deleteTable, delete '%s'", space);
		return success;
	}

	/**
	 * get all database configure for a user
	 * @return
	 */
	public int getSchemas(TopClient client, Site local) {
		boolean nullable = (client == null);
		int count = 0;
		try {
			if(nullable) client = fetch();
			String[] names = client.getSchemas(local.getHost());
			for (int i = 0; names != null && i < names.length; i++) {
				if (names[i] == null) continue;
				this.schemas.add(names[i]);
				// get database charset
				try {
					SQLCharset set = client.findCharset(names[i]);
					if (set != null) {
						this.addCharset(names[i], set);
					}
				} catch (VisitException exp) {
					Logger.error(exp);
				}
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		
		return count;
	}

	/**
	 * get all table configure for a user
	 * @return
	 */
	public int getTables(TopClient client, Site local) {
		boolean nullable = (client == null);
		int count = 0;
		try {
			if(nullable) client = fetch();
			if(client != null) {
				Table[] all = client.getTables(local.getHost());
				for (int i = 0; all != null && i < all.length; i++) {
					if (all[i] != null) {
						tables.put(all[i].getSpace(), all[i]);
					}
				}
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return count;
	}
	
	/**
	 * @param client
	 * @param local
	 * @param space
	 * @param size
	 * @return
	 */
	public boolean setChunkSize(TopClient client, Site local, Space space, int size) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.setChunkSize(space.getSchema(), space.getTable(), size);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return success;
	}
	
	/**
	 * set chunk optimze time
	 * @param client
	 * @param local
	 * @param space
	 * @param type
	 * @param time
	 * @return
	 */
	public boolean setOptimizeTime(TopClient client, Site local, Space space, int type, long time) {
		boolean nullable = (client == null);
		boolean success = false;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				success = client.setOptimizeTime(space.getSchema(), space.getTable(), type, time);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if(nullable) complete(client);
		return success;
	}

	/**
	 * @param space
	 * @return
	 */
	public SiteHost[] findHomeSite(TopClient client, Site local, Space space) {
		boolean nullable = (client == null);
		SiteHost[] hosts = null;
		try {
			if (nullable) client = fetch();
			if (client != null) {
				hosts = client.findHomeSite(space.getSchema(), space.getTable());
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return hosts;
	}

	public short checkIdentified(TopClient client, Site local) {
		boolean nullable = (client == null);
		try {
			if (nullable) client = fetch();
			userRank = client.checkIdentified(local.getHost());
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);

		return userRank;
	}

	public void loadCharmap(TopClient client, Site local) {
		boolean nullable = (client == null);
		// get all char set
		try {
			if (nullable) client = fetch();
			if (client != null) {
				charmap = client.getCharmap(local.getHost());
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
	}
	
	public IP[] optimize(TopClient client, Site local, Space space, String[] hosts) throws VisitException {
		boolean nullable = (client == null);
		IP[] s = null;
		if (nullable) client = fetch();
		if (client != null) {
			s = client.optimize(local.getHost(), space, hosts);
		}
		if (nullable) complete(client);
		return s;
	}
	
	public IP[] loadIndex(TopClient client, Site local, Space space, String[] hosts) throws VisitException {
		boolean nullable = (client == null);
		IP[] s = null;
		if (nullable) client = fetch();
		if (client != null) {
			s = client.loadIndex(local.getHost(), space, hosts);
		}
		if (nullable) complete(client);
		return s;
	}
	
	public IP[] stopIndex(TopClient client, Site local, Space space, String[] hosts) throws VisitException {
		boolean nullable = (client == null);
		IP[] s = null;
		if (nullable) client = fetch();
		if (client != null) {
			s = client.stopIndex(local.getHost(), space, hosts);
		}
		if (nullable) complete(client);
		return s;
	}

	/**
	 * load chunk data to memory
	 * @param client
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] loadChunk(TopClient client, Site local, Space space, String[] hosts) throws VisitException {
		boolean nullable = (client == null);
		IP[] s = null;
		if (nullable) client = fetch();
		if (client != null) {
			s = client.loadChunk(local.getHost(), space, hosts);
		}
		if (nullable) complete(client);
		return s;
	}

	/**
	 * release chunk data
	 * @param client
	 * @param local
	 * @param space
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] stopChunk(TopClient client, Site local, Space space, String[] hosts) throws VisitException {
		boolean nullable = (client == null);
		IP[] s = null;
		if (nullable) client = fetch();
		if (client != null) {
			s = client.stopChunk(local.getHost(), space, hosts);
		}
		if (nullable) complete(client);
		return s;
	}
	
	/**
	 * send "build task" command to site
	 * @param client
	 * @param local
	 * @param naming
	 * @param hosts
	 * @return
	 * @throws VisitException
	 */
	public IP[] buildTask(TopClient client, Site local, String naming, String[] hosts) throws VisitException {
		boolean nullable = (client == null);
		IP[] all = null;
		if (nullable) client = fetch();
		try {
			if (client != null) {
				all = client.buildTask(local.getHost(), naming, hosts);
			}
		} catch (VisitException exp) {
			throw exp;
		} catch (Throwable exp) {
			throw new VisitException(exp);
		} finally {
			if (nullable) complete(client);
		}
		return all;
	}
	
	public SiteHost[] showSite(TopClient client, Site local, int site, String from) throws VisitException {
		boolean nullable = (client == null);
		SiteHost[] hosts = null;
		if (nullable) client = fetch();
		try {
			if (client != null) {
				hosts = client.showSite(site, from);
			}
		} catch (VisitException exp) {
			throw exp;
		} catch (Throwable exp) {
			throw new VisitException(exp);
		} finally {
			if (nullable) complete(client);
		}
		return hosts;
	}
	
	public boolean isDBA() {
		return userRank == Response.SQL_ADMIN;
	}

	public boolean isClient() {
		return userRank == Response.SQL_CLIENT;
	}
	
	/**
	 * apply a top client handle
	 * @param stream
	 * @return
	 */
	private TopClient fetch(boolean stream) {
		if (remote == null) {
			Logger.error("TopPool.fetch, top site is null");
			return null;
		}

		SocketHost host = (stream ? remote.getTCPHost() : remote.getUDPHost());
		TopClient client = new TopClient(stream, host);
		try {
			client.reconnect();
			return client;
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return null;
	}

	/**
	 * apply a top client (stream mode)
	 * @return
	 */
	private TopClient fetch() {
		return fetch(true);
	}

	/**
	 * release connect and close socket
	 * @param client
	 */
	private void complete(TopClient client) {
		if (client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			
		}
	}
	
	private RSAPublicKey generate() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		try {
			InputStream in = com.lexst.live.LivePacketInvoker.class.getResourceAsStream("rsakey.public");
			byte[] data = new byte[1024];
			do {
				int size = in.read(data);
				if (size == -1) break;
				buff.write(data, 0, size);
			} while (true);
			in.close();
		} catch (IOException exp) {

		}
		
		byte[] b = buff.toByteArray();
		XMLocal local = new XMLocal();
		Document doc = local.loadXMLSource(b);
		
		NodeList list = doc.getElementsByTagName("code");
		Element element = (Element)list.item(0);
		
		String modulus = local.getXMLValue(element.getElementsByTagName("modulus"));
		String exponent = local.getXMLValue(element.getElementsByTagName("exponent"));

		return SecureGenerator.buildRSAPublicKey(modulus, exponent);
	}
	
	/**
	 * login to top site
	 * @param server
	 * @param local
	 * @return
	 */
	public boolean login(SiteHost server, LiveSite local) {
		this.remote = new SiteHost(server);
		
		TopClient client = fetch(false);
		if(client == null) {
			Logger.error("TopPool.login, cannot connect %s", remote);
			return false;
		}
		
		boolean success = false;
		try {
			//1. safe check
			String algo = local.getAlgorithm();
			if (algo != null) {
				RSAPublicKey key = generate();
//				byte[] pwd = local.getUser().getPassword().getBytes();
				byte[] pwd = local.getUser().getPassword();
				boolean safe = client.initSecure(key, algo, pwd);
				Logger.note("TopPool.login, init security", safe);
				if (!safe) {
					this.complete(client);
					return false;
				}
			}

			//2. login to top server
			success = client.login(local);
			Logger.note(success, "TopPool.login, login to %s", remote);
			if (success) {
				int second = client.getSiteTimeout(local.getType());
				siteTimeout = second * 1000;
				Logger.info("TopPool.login, site timeout %d", second);
				loadCharmap(client, local);
				checkIdentified(client, local);
				int ret1 = getPermits(client, local);
				int ret2 = getSchemas(client, local);
				int ret3 = getTables(client, local);
				if (ret1 >= 0 && ret2 >= 0 && ret3 >= 0) {

				}
				this.into = true;
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch(Throwable exp) {
			Logger.fatal(exp);
		}
		
		complete(client);

		return success;
	}

	/**
	 * logout from home site
	 * @return
	 */
	public boolean logout(LiveSite site) {
		TopClient client = fetch(false);
		if (client == null) {
			Logger.error("TopPool.logout, cannot connect %s", remote);
			return false;
		}

		boolean success = false;
		try {
			boolean safe = true;
			String algo = site.getAlgorithm();
			if (algo != null) {
//				byte[] pwd = site.getUser().getPassword().getBytes();
				byte[] pwd = site.getUser().getPassword();
				RSAPublicKey key = this.generate();
				safe = client.initSecure(key, algo, pwd);
				Logger.note("TopPool.logout, init security", safe);
			}
			if (safe) {
				success = client.logout(site.getType(), site.getHost());
				Logger.note(success, "TopPool.logout, logout from %s", remote);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
		if (success) {
			this.reset();
		}
		return success;
	}

	/**
	 * apply all options
	 * @return
	 */
	public int getPermits(TopClient client, Site local) {
		boolean nullable = (client == null);
		// clear old
		permits.clear();
		// get table space
		try {
			if (nullable) client = fetch();
			Permit[] auths = client.getPermits(local.getHost());
			if (auths != null) {
				for (int i = 0; i < auths.length; i++) {
					permits.add(auths[i]);
				}
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (nullable) complete(client);
		return permits.size();
	}

	public void replyActive() {
		iseeTime = System.currentTimeMillis();
		this.wakeup();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// logout service
		this.logout(local);
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		long endtime = 0L;
		iseeTime = System.currentTimeMillis();
		while (!isInterrupted()) {
			delay(1000);
			if (isInterrupted()) break;

			// when into check status
			if (into) {
				if (System.currentTimeMillis() >= endtime) {
					Logger.debug("TopPool.process, active %s", remote);
					listener.active(1, remote.getUDPHost());
					endtime = System.currentTimeMillis() + siteTimeout;
				}
				if(System.currentTimeMillis() - iseeTime >= siteTimeout * 2) {
					listener.active(3, remote.getUDPHost());
					endtime = System.currentTimeMillis() + siteTimeout;
				}
				if(System.currentTimeMillis() - iseeTime >= siteTimeout * 5) {
					// notify launcher, site timeout, close service, exit!
					this.listener.disconnect();
					this.into = false;
				}
			}
		}
	}

}