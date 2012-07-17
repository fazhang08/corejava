/**
 *
 */
package com.lexst.top.pool;

import java.util.*;

import com.lexst.db.account.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.site.*;
import com.lexst.site.live.*;
import com.lexst.top.*;
import com.lexst.top.effect.*;
import com.lexst.util.host.*;

public class LivePool extends Pool {
	
	// static handle
	private static LivePool selfHandle = new LivePool();
	
	/* live space -> site set */
	private Map<Space, SiteSet> mapSpace = new TreeMap<Space, SiteSet>();
	
	/* live host -> live site */
	private Map<SiteHost, LiveSite> mapSite = new TreeMap<SiteHost, LiveSite>();
	
	/* live host -> refresh time */
	private Map<SiteHost, Long> mapTime = new TreeMap<SiteHost, Long>();

	/**
	 * default constructor
	 */
	private LivePool() {
		super();
	}

	/**
	 * return a static handle
	 * @return
	 */
	public static LivePool getInstance() {
		return LivePool.selfHandle;
	}
	
	/**
	 * @return
	 */
	public List<SiteHost> gather() {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		array.addAll(mapSite.keySet());
		return array;
	}
	
	/**
	 * @param object
	 * @return
	 */
	public short add(Site object) {
		if (object == null || !object.isLive()) {
			return Response.CLIENT_ERROR;
		}
		LiveSite site = (LiveSite) object;
		SiteHost host = site.getHost();
		
		Logger.info("LivePool.add, live site %s", host);
		
		this.lockSingle();
		try {
			// check site exists
			if (mapSite.containsKey(host)) {
				Logger.error("LivePool.add, duplicate socket host %s", host);
				return Response.IP_EXISTED;
			}
			User user = site.getUser();
			// check account
			if (!Launcher.getInstance().isAllowUser(user)) {
				Logger.warning("LivePool.add, check failed, account %s", user);
				return Response.NOTFOUND;
			}
			// save space
			for(Space space : site.list()) {
				SiteSet set = mapSpace.get(space);
				if(set == null) {
					set = new SiteSet();
					mapSpace.put(space, set);
				}
				set.add(host);
			}
			mapSite.put(host, site);
			mapTime.put(host, System.currentTimeMillis());
			return Response.ACCEPTED;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}

	/**
	 * remove a site
	 * @param host
	 * @return
	 */
	public short remove(SiteHost host) {
		Logger.info("LivePool.remove, live site %s", host);

		this.lockSingle();
		try {
			LiveSite site = mapSite.remove(host);
			if (site == null) {
				return Response.NOTACCEPTED;
			}
			mapTime.remove(host);
			// remove space
			for (Space space : site.list()) {
				SiteSet set = mapSpace.get(space);
				if (set != null) {
					set.remove(site.getHost());
				}
				if (set == null || set.isEmpty()) {
					mapSpace.remove(space);
				}
			}
			return Response.ACCEPTED;
		} catch(Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}
	
	public short update(Site site) {
		if (remove(site.getHost()) == Response.ACCEPTED) {
			return this.add(site);
		}
		return Response.NOTACCEPTED;
	}
	
	/**
	 * check site existed
	 * @param host
	 * @return
	 */
	public boolean exists(SiteHost host) {
		super.lockMulti();
		try {
			return mapSite.containsKey(host);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	/**
	 * refresh a sql live site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site != null) {
				mapTime.put(host, System.currentTimeMillis());
				code = Response.LIVE_ISEE;
			} else {
				code = Response.NOTLOGIN;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockSingle();
		}
		Logger.debug("LivePool.refresh, live site %s refresh status %d", host, code);
		return code;
	}

	/**
	 *
	 * @param host
	 * @param schema
	 * @return
	 */
	public short createSchema(SiteHost host, Schema schema) {
		if (schema == null) {
			return Response.CLIENT_ERROR;
		}
		super.lockSingle();
		try {
			// cannot login
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return Response.NOTLOGIN;
			}
			// check permit
			UserManager manager = Launcher.getInstance().getUserManager();
			if (!manager.isDBA(site.getUser())) {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if (account == null) {
					return Response.NOTFOUND_ACCOUNT;
				}
				// 检查建立账号的权限
				if (!account.allowCreateSchema()) {
					return Response.REFUSE;
				}
			}
			boolean success = Launcher.getInstance().createSchema(schema);
			if(success) {
				if (!manager.isDBA(site.getUser())) {
					Account account = manager.findAccount(site.getUser().getHexUsername());
					account.addSchema(schema.getName());
				}
				Launcher.getInstance().flushAccount();
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return Response.NOTACCEPTED;
	}

	/**
	 * delete a database configure
	 * @param host
	 * @param db
	 * @return
	 */
	public short deleteSchema(SiteHost host, String db) {
		// 先检查主机是否注册,再检查
		if (db == null) {
			return Response.CLIENT_ERROR;
		}
		super.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if(site == null) {
				return Response.NOTLOGIN;
			}
			// check permit
			UserManager manager = Launcher.getInstance().getUserManager();
			if (!manager.isDBA(site.getUser())) {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if (account == null) {
					return Response.REFUSE;
				}
				// 检查建立账号的权限
				if (!account.allowDropSchema()) {
					return Response.REFUSE;
				}
			}
			boolean success = Launcher.getInstance().deleteSchema(db);
			if(success) {
				if(!manager.isDBA(site.getUser())) {
					Account account = manager.findAccount(site.getUser().getHexUsername());
					account.deleteSchema(db);
				}
				Launcher.getInstance().flushAccount();
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return Response.NOTACCEPTED;
	}

	/**
	 * create a account
	 * @param host
	 * @param user
	 * @return
	 */
	public short createUser(SiteHost host, User user) {
		Logger.info("LivePool.createUser, create account: %s", user.getHexUsername());

		this.lockSingle();
		try {
			// 主机必须注册
			LiveSite oldsite = mapSite.get(host);
			if(oldsite == null) {
				return Response.NOTLOGIN;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			// 不允许建立DBA同名账号
			if (manager.getDBA().isMatchUsername(user.getUsername())) {
				return Response.REFUSE;
			}
			// 用户同名账号是否存在
			Account account = manager.findAccount(user.getHexUsername());
			if (account != null) {
				return Response.ACCOUNT_EXISTED;
			}

			// 如果不是DBA账号,检查账号权限
			if (!manager.isDBA(oldsite.getUser())) {
				Account oldAccount = manager.findAccount(oldsite.getUser().getHexUsername());
				if (oldAccount == null) {
					return Response.NOTFOUND;
				}
				// 检查是否有权建立账号
				if(!oldAccount.allowCreateUser()) {
					return Response.REFUSE;
				}
			}
			// 建立一个新账号
			account = new Account(user);
			if(manager.addAccount(account)) {
				Launcher.getInstance().flushAccount();
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		// failed
		return Response.NOTACCEPTED;
	}

	/**
	 * delete account
	 * @param host
	 * @param username
	 * @return
	 */
	public short deleteUser(SiteHost host, String username) {
		Logger.info("LivePool.deleteUser, drop user: %s", username);
		
		User user = new User();
		user.setHexUsername(username);

		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if(site == null) {
				return Response.NOTLOGIN;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			// 如果是DBA账号,不允许删除
			if(manager.getDBA().isMatchUsername(user.getUsername())) {
				return Response.NOTACCEPTED;
			}
			// 检查被删除账号是否存在
			Account account = manager.findAccount(username);
			if(account == null) {
				return Response.NOTFOUND;
			}
			// 账号是DBA,或者是自己的账号,允许删除
			boolean success = false;
			if (manager.isDBA(site.getUser())) {
				success = manager.deleteAccount(username);
			} else if (site.getUser().isMatchUsername(user.getUsername())) {
				success = manager.deleteAccount(username);
				// 同时删除注册地址
				mapSite.remove(host);
				mapTime.remove(host);
			}
			if(success) {
				Launcher.getInstance().flushAccount();
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.NOTACCEPTED;
	}

	/**
	 * modify account user
	 * @param host
	 * @param other
	 * @return
	 */
	public short alterUser(SiteHost host, User other) {
		Logger.info("LivePool.alterUser, alter user: %s", other.getHexUsername());

		// DBA账号,不允许网络修改.
		// 登陆是DBA账号,或者是自己的账号,允许修改
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if(site == null) {
				return Response.NOTLOGIN;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			// 如果是DBA账号,不允许修改
			if(manager.getDBA().isMatchUsername(other.getUsername())) {
				return Response.NOTACCEPTED;
			}
			// 检查被删除账号是否存在
			Account account = manager.findAccount(other.getHexUsername());
			if(account == null) {
				return Response.NOTFOUND;
			}

			// 账号是DBA,或者是自己的账号,允许修改
			boolean success = false;
			if (manager.isDBA(site.getUser())) {
				account.setUser(other);
				success = true;
			} else if (site.getUser().isMatchUsername(other.getUsername())) {
				site.getUser().set(other);
				success = true;
			}
			if(success) {
				Launcher.getInstance().flushAccount();
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.NOTACCEPTED;
	}

	/**
	 * add permit
	 * @param host
	 * @param permit
	 * @return
	 */
	public short addPermit(SiteHost host, Permit permit) {

		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return Response.NOTLOGIN;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			// 如果不是DBA,检查用户权限
			if (!manager.isDBA(site.getUser())) {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if(account == null) {
					return Response.NOTFOUND_ACCOUNT;
				}
				if(!account.allowGrant()) {
					return Response.REFUSE;
				}
			}
			// 追加管理权限
			List<String> list = permit.getUsers();
			if (!manager.exists(list)) {
				return Response.NOTFOUND_ACCOUNT;
			}
			for (String username : list) {
				Account account = manager.findAccount(username);
				account.add(permit);
			}
			// save account
			Launcher.getInstance().flushAccount();
			return Response.ACCEPTED;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}

	/**
	 * delete permit
	 * @param host
	 * @param permit
	 * @return
	 */
	public short deletePermit(SiteHost host, Permit permit) {
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return Response.NOTLOGIN;
			}

			UserManager manager = Launcher.getInstance().getUserManager();
			// 不是DBA,检查操作权限
			if (!manager.isDBA(site.getUser())) {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if (account == null) {
					return Response.NOTFOUND_ACCOUNT;
				}
				if (!account.allowRevoke()) {
					return Response.REFUSE;
				}
			}
			// 删除管理权限
			List<String> list = permit.getUsers();
			if (!manager.exists(list)) {
				return Response.NOTFOUND_ACCOUNT;
			}
			for (String username : list) {
				Account account = manager.findAccount(username);
				account.remove(permit);
			}

			Launcher.getInstance().flushAccount();
			return Response.ACCEPTED;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}

	/**
	 * craete a table space
	 * @param host
	 * @param table
	 * @return
	 */
	public short createTable(SiteHost host, Table table) {
		if(table == null) {
			return Response.CLIENT_ERROR;
		}
		
		Space space = table.getSpace();
		Logger.info("LivePool.createTable, create table space '%s'", space);

		super.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return Response.NOTLOGIN;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			// 如果不是管理员账号,检查权限
			if(!manager.isDBA(site.getUser())) {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if (account == null) {
					return Response.NOTFOUND_ACCOUNT;
				}
				// 检查建立账号的权限
				if (!account.allowCreateTable(space.getSchema())) {
					return Response.REFUSE;
				}
			}
			// 检查表,如果表存在,不允许
			Table old = Launcher.getInstance().findTable(space);
			if(old != null) {
				return Response.TABLE_EXISTED;
			}
			// check database
			Dict dict = Launcher.getInstance().getDict();
			if (dict.findSchema(space.getSchema()) == null) {
				return Response.NOTFOUND_SCHEMA;
			}
			// create table
			boolean success = Launcher.getInstance().createSpace(table);
			if(success) {
				if(!manager.isDBA(site.getUser())) {
					Account account = manager.findAccount(site.getUser().getHexUsername());
					account.addSpace(space);
				}
				Launcher.getInstance().flushAccount();
				// return id
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return Response.NOTACCEPTED;
	}

	/**
	 * delete table
	 * @param host
	 * @param space
	 * @return
	 */
	public short deleteTable(SiteHost host, Space space) {
		Logger.info("LivePool.deleteTable, drop table '%s' from %s", space, host);
		if (space == null) return Response.CLIENT_ERROR;

		super.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return Response.NOTLOGIN;
			}

			UserManager manager = Launcher.getInstance().getUserManager();
			// 如果不是管理员账号,检查权限
			if(!manager.isDBA(site.getUser())) {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if (account == null) {
					return Response.REFUSE;
				}
				// 检查建立账号的权限
				if (!account.allowDropTable(space.getSchema())) {
					return Response.REFUSE;
				}
			}
			// 检查表,如果表存在,不允许
			Table old = Launcher.getInstance().findTable(space);
			if(old == null) {
				return Response.NOTFOUND_TABLE;
			}
			// check database
			Dict dict = Launcher.getInstance().getDict();
			if (dict.findSchema(space.getSchema()) == null) {
				return Response.NOTFOUND_SCHEMA;
			}
			boolean success = Launcher.getInstance().deleteSpace(space);
			if (success) {
				if(!manager.isDBA(site.getUser())) {
					Account account = manager.findAccount(site.getUser().getHexUsername());
					account.deleteSpace(space);
				}
				Launcher.getInstance().flushAccount();
				return Response.ACCEPTED;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.NOTACCEPTED;
	}

	/**
	 * find table
	 * @param host
	 * @param space
	 * @return
	 */
	public Table findTable(SiteHost host, Space space) {
		Logger.info("LivePool.findTable, space \'%s\', site host: %s", space, host);
		
		super.lockMulti();
		try {
			if (!mapSite.containsKey(host)) {
				Logger.error("LivePool.findTable, cannot find login site %s, when find table", host);
				return null;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockMulti();
		}
		// return table
		Table table = Launcher.getInstance().findTable(space);
		return table;
	}

	public String[] getSchemas(SiteHost host) {
		Logger.info("LivePool.getSchemas, site host %s", host);

		ArrayList<String> a = new ArrayList<String>();
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return null;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			Dict dict = Launcher.getInstance().getDict();
			if(manager.isDBA(site.getUser())) {
				Set<String> keys = dict.keys();
				for (String dbname : keys) {
					Schema db = dict.findSchema(dbname);
					if (db != null) a.add(db.getName());
				}
			} else {
				Account account = manager.findAccount(site.getUser().getHexUsername());
				if (account == null) return null;
				Set<String> key = account.dbKeys();
				for (String dbname : key) {
					Schema db = dict.findSchema(dbname);
					if (db != null) a.add(db.getName());
				}
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		if(a.isEmpty()) return null;
		String[] s = new String[a.size()];
		return a.toArray(s);
	}

	/**
	 * get table configure
	 * @param host
	 * @return
	 */
	public Table[] getTables(SiteHost host) {
		Logger.info("LivePool.getTables, site %s", host);

		ArrayList<Table> a = new ArrayList<Table>(32);
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return null;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			Dict dict = Launcher.getInstance().getDict();
			// 如果是管理员账号,取全部数据库表
			if (manager.isDBA(site.getUser())) {
				// 取全部配置
				Set<String> keys = dict.keys();
				for (String dbname : keys) {
					Schema db = dict.findSchema(dbname);
					a.addAll(db.listTable());
				}
			} else {
				// 找到匹配的账号,取出匹配的表
				Account account = manager.findAccount(site.getUser().getHexUsername());
				Set<String> key = account.dbKeys();
				for (String db : key) {
					List<Space> list = account.findSpaces(db);
					for (Space space : list) {
						Table table = dict.findTable(space);
						a.add(table);
					}
				}
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}

		if(a.isEmpty())	return null;
		Table[] s = new Table[a.size()];
		return a.toArray(s);
	}

	/**
	 * find space
	 *
	 * @param host
	 * @return
	 */
	public Permit[] getPermits(SiteHost host) {
		Logger.info("LivePool.getPermits, site %s", host);

		ArrayList<Permit> a = new ArrayList<Permit>();
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return null;
			}
			String name = site.getUser().getHexUsername();
			UserManager manager = Launcher.getInstance().getUserManager();
			Account account = manager.findAccount(name);
			if (account == null) {
				return null;
			}
			Collection<Permit> list = account.list();
			if (list != null && !list.isEmpty()) {
				a.addAll(list);
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		if (a.isEmpty()) return null;
		Permit[] all = new Permit[a.size()];
		return a.toArray(all);
	}

	/**
	 * check user identified
	 * @param host
	 * @return
	 */
	public short checkIdentified(SiteHost host) {
		Logger.info("LivePool.checkIdnetified, site %s", host);

		short type = Response.UNIDENTIFIED;
		this.lockSingle();
		try {
			LiveSite site = mapSite.get(host);
			if (site == null) {
				return Response.NOTLOGIN;
			}
			UserManager manager = Launcher.getInstance().getUserManager();
			if (manager.isDBA(site.getUser())) {
				type = Response.SQL_ADMIN;
			} else {
				String name = site.getUser().getHexUsername();
				Account account = manager.findAccount(name);
				if (account != null) {
					type = Response.SQL_CLIENT;
				}
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return type;
	}

	/**
	 * check timeout
	 */
	private void checkTimeout() {
		int size = mapSite.size();
		if (size == 0) return;

		ArrayList<SiteHost> dels = new ArrayList<SiteHost>(size);
		super.lockSingle();
		try {
			long now = System.currentTimeMillis();
			for (SiteHost host : mapTime.keySet()) {
				Long value = mapTime.get(host);
				if (value == null || now - value.longValue() >= deleteTime) {
					dels.add(host);
				}
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			super.unlockSingle();
		}
		// 删除严重超时的节点
		for (SiteHost host : dels) {
			this.remove(host);
		}
	}

	private void check() {
		this.checkTimeout();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapTime.clear();
		mapSite.clear();
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
		Logger.info("LivePool.process, into ...");
		while (!isInterrupted()) {
			this.delay(1000);
			if (isInterrupted()) break;
			this.check();
		}
		Logger.info("LivePool.process, exit");
	}
}