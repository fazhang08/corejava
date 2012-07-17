/**
 *
 */
package com.lexst.top.pool;

import java.io.*;
import java.util.*;

import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.*;
import com.lexst.site.home.*;
import com.lexst.top.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public class HomePool extends Pool {

	// static handle
	private static HomePool selfHandle = new HomePool();

	/* space -> home host set */
	private Map<Space, SiteSet> mapSpace = new HashMap<Space, SiteSet>();

	/* site host -> home site */
	private Map<SiteHost, HomeSite> mapSite = new HashMap<SiteHost, HomeSite>();

	/* site host -> refresh time */
	private Map<SiteHost, Long> mapTime = new HashMap<SiteHost, Long>();

	/**
	 * default constructor
	 */
	private HomePool() {
		super();
	}

	/**
	 * get instance
	 * @return
	 */
	public static HomePool getInstance() {
		return HomePool.selfHandle;
	}
	
	/**
	 * @return
	 */
	public List<SiteHost> gather() {
		List<SiteHost> array = new ArrayList<SiteHost>();
		array.addAll(mapSite.keySet());
		return array;
	}
	
	/**
	 * find run-site
	 * @return
	 */
	public SiteHost findRunsite() {
		for(SiteHost host : mapSite.keySet()) {
			HomeSite site = mapSite.get(host);
			if(site.isRunsite()) return host;
		}
		return null;
	}
	
	/**
	 * @param remote
	 * @return
	 */
	private HomeClient bring(SocketHost remote) {
		HomeClient client = new HomeClient(true, remote);
		for (int i = 0; i < 3; i++) {
			try {
				client.reconnect();
				return client;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(1000);
		}
		return null;
	}
	
	private void complete(HomeClient client) {
		if(client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		}
	}

	/**
	 * @param space
	 * @return
	 */
	private List<SiteHost> findHosts(Space space) {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();

		super.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if (set != null && set.size() > 0) {
				array.addAll(set.list());
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}

		if(array.isEmpty()) return null;
		return array;
	}
	
	/**
	 * random select
	 * @param size
	 * @return
	 */
	private int choice(int size) {
		if (size > 1) {
			Random rand = new Random();
			return rand.nextInt(size);
		}
		return 0;
	}
	
	/**
	 * random choose home-site
	 * 
	 * @param size
	 * @return
	 */
	private List<SiteHost> choice_hosts(int size) {
		if (size < 1) {
			return new ArrayList<SiteHost>();
		}
		Random rand = new Random();
		int begin = rand.nextInt(size);

		boolean loop = false;
		List<SiteHost> array = new ArrayList<SiteHost>(size);
		ArrayList<SiteHost> a = new ArrayList<SiteHost>(mapSite.keySet());

		while (true) {
			if (begin >= a.size()) {
				if (loop) break;
				begin = 0;
				loop = true;
			}
			SiteHost host = a.get(begin++);
			if(!array.contains(host)) array.add(host);
		}
		return array;
	}
	
	/**
	 * @param size
	 * @param set
	 * @return
	 */
	private List<SiteHost> choice_hosts(int size, SiteSet set) {
		if (size < 1 || set.isEmpty()) {
			return new ArrayList<SiteHost>();
		}
		Random rand = new Random();
		int begin = rand.nextInt(size);

		boolean loop = false;
		List<SiteHost> array = new ArrayList<SiteHost>(size);
		ArrayList<SiteHost> a = new ArrayList<SiteHost>(set.list());

		while (true) {
			if (begin >= a.size()) {
				if (loop) break;
				begin = 0;
				loop = true;
			}
			SiteHost host = a.get(begin++);
			if(!array.contains(host)) array.add(host);
		}
		return array;
	}

	/**
	 * 从每个集群中找一个CALL节点
	 * 
	 * @param space
	 * @return
	 */
	public SiteHost[] selectCallSite(Space space) {
		Logger.debug("HomePool.selectCallSite, space is '%s', size:%d", space, mapSpace.size());

//		List<SiteHost> list = null;
//		this.lockMulti();
//		try {
//			SiteSet set = mapSpace.get(space);
//			if (set == null) {
//				Logger.warning("HomePool.selectCallSite, cannot find '%s' host", space);
//				return null;
//			}
//			list = findHosts(space);
//		} catch (Throwable exp) {
//			Logger.fatal(exp);
//		} finally {
//			this.unlockMulti();
//		}
		
		// find home host
		List<SiteHost> list = findHosts(space);
		Logger.note((list != null && !list.isEmpty()),
				"HomePool.selectCallSite, '%s' host site is:%d", space, (list == null ? -1 : list.size()));		
		if (list == null || list.isEmpty()) {
			return null;
		}

		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			try {
				if (client != null) {
					SiteHost[] hosts = client.findCallSite(space);
					if (hosts != null && hosts.length > 0) {
						int index = choice(hosts.length);
						array.add(hosts[index]); //随机取其中一个
					}
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client); // close client
		}

		int size = array.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}

	/**
	 * select a naming host
	 * @param naming
	 * @return
	 * @throws VisitException
	 */
	public SiteHost[] selectCallSite(String naming) {
		Logger.info("HomePool.selectCallSite, naming is %s, home sites:%d", naming, mapSite.size());
		
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		ArrayList<SiteHost> list = new ArrayList<SiteHost>( mapSite.keySet() );

		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			try {
				if (client != null) {
					SiteHost[] hosts = client.findCallSite(naming);
					if (hosts != null && hosts.length > 0) {
						int index = choice(hosts.length);
						array.add(hosts[index]);
					}
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client); // close client
		}

		Logger.debug("HomePool.selectCallSite, sites: %d", array.size());

		int size = array.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	public SiteHost[] selectCallSite(String naming, Space space) {
		Logger.info("HomePool.selectCallSite, naming is %s, space is %s", naming, space);
		
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		ArrayList<SiteHost> list = new ArrayList<SiteHost>( mapSite.keySet() );

		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			try {
				if (client != null) {
					SiteHost[] hosts = client.findCallSite(naming, space);
					if (hosts != null && hosts.length > 0) {
						int index = choice(hosts.length);
						array.add(hosts[index]);
					}
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client); // close client
		}

		int size = array.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	public IP[] optimize(Space space, IP[] datas) {
		Logger.debug("HomePool.optimize, space is '%s'", space);

		SiteSet set = mapSpace.get(space);
		if(set == null) return null;
		List<SiteHost> list = findHosts(space);
		if(list == null) return null;
		
		ArrayList<IP> store = new ArrayList<IP>();
		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			if(client == null) continue;
			try {
				IP[] s = client.optimize(space.getSchema(), space.getTable(), datas);
				for (int i = 0; s != null && i < s.length; i++) {
					store.add(s[i]);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch(Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client);
		}
		
		if(store.isEmpty()) return null;
		IP[] s = new IP[store.size()];
		return store.toArray(s);
	}
	
	public IP[] loadIndex(Space space, IP[] datas) {
		Logger.info("HomePool.loadIndex, space is '%s'", space);
		
		SiteSet set = mapSpace.get(space);
		if(set == null) return null;
		List<SiteHost> list = findHosts(space);
		if(list == null) return null;
		
		ArrayList<IP> array = new ArrayList<IP>();
		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			if(client == null) continue;
			try {
				IP[] s = client.loadIndex(space, datas);
				for (int i = 0; s != null && i < s.length; i++) {
					array.add(s[i]);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch(Throwable exp) {
				Logger.fatal(exp);
			}
			complete(client);
		}
		
		Logger.info("HomePool.loadIndex, result size:%d", array.size());
		
		if(array.isEmpty()) return null;
		IP[] s = new IP[array.size()];
		return array.toArray(s);
	}
	
	public IP[] stopIndex(Space space, IP[] datas) {
		Logger.debug("HomePool.stopIndex, space is '%s'", space);

		SiteSet set = mapSpace.get(space);
		if(set == null) return null;
		List<SiteHost> list = findHosts(space);
		if(list == null) return null;
		
		ArrayList<IP> store = new ArrayList<IP>();
		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			if(client == null) continue;
			try {
				IP[] s = client.stopIndex(space, datas);
				for (int i = 0; s != null && i < s.length; i++) {
					store.add(s[i]);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch(Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client);
		}
		
		if(store.isEmpty()) return null;
		IP[] s = new IP[store.size()];
		return store.toArray(s);
	}
	
	public IP[] loadChunk(Space space, IP[] hosts) {
		Logger.debug("HomePool.loadChunk, space is '%s'", space);
		
		SiteSet set = mapSpace.get(space);
		if(set == null) return null;
		List<SiteHost> list = findHosts(space);
		if(list == null) return null;
		
		ArrayList<IP> store = new ArrayList<IP>();
		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			if(client == null) continue;
			try {
				IP[] s = client.loadChunk(space, hosts);
				for (int i = 0; s != null && i < s.length; i++) {
					store.add(s[i]);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch(Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client);
		}
		
		if(store.isEmpty()) return null;
		IP[] s = new IP[store.size()];
		return store.toArray(s);
	}
	
	public IP[] stopChunk(Space space, IP[] hosts) {
		Logger.debug("HomePool.stopChunk, space is '%s'", space);
		
		SiteSet set = mapSpace.get(space);
		if(set == null) return null;
		List<SiteHost> list = findHosts(space);
		if(list == null) return null;
		
		ArrayList<IP> store = new ArrayList<IP>();
		for(SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			if(client == null) continue;
			try {
				IP[] s = client.stopChunk(space, hosts);
				for (int i = 0; s != null && i < s.length; i++) {
					store.add(s[i]);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch(Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client);
		}
		
		if(store.isEmpty()) return null;
		IP[] s = new IP[store.size()];
		return store.toArray(s);
	}
	
	public IP[] buildTask(String naming, IP[] hosts) {
		Logger.debug("HomePool.buildTask, naming is '%s'", naming);
		
		ArrayList<SiteHost> list = new ArrayList<SiteHost>(mapSite.keySet());
		ArrayList<IP> store = new ArrayList<IP>();
		for (SiteHost host : list) {
			HomeClient client = bring(host.getTCPHost());
			if (client == null) continue;
			try {
				IP[] s = client.buildTask(naming, hosts);
				for (int i = 0; s != null && i < s.length; i++) {
					store.add(s[i]);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			this.complete(client);
		}
		if (store.isEmpty()) return null;
		IP[] s = new IP[store.size()];
		return store.toArray(s);
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
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
		Logger.info("HomePool.process, into ...");
		while(!isInterrupted()) {
			this.delay(1000);
			if(isInterrupted()) break;
			// check timeout site
			this.check();
		}
		Logger.info("HomePool.process, exit");
	}

	/**
	 * add site
	 * @param site
	 * @return
	 */
	public short add(Site object) {
		if(object == null || !object.isHome()) {
			return Response.CLIENT_ERROR;
		}
		HomeSite site = (HomeSite)object;
		SiteHost host = site.getHost();
		
		Logger.info("HomePool.add, home site %s, space size is %d", host, site.listSpace().size());
		this.lockSingle();
		try {
			// check site exists
			if (mapSite.containsKey(host)) {
				Logger.error("duplicate socket host %s", host);
				return Response.IP_EXISTED;
			}
			for (Space space : site.listSpace()) {
				SiteSet set = mapSpace.get(space);
				if (set == null) {
					set = new SiteSet();
					mapSpace.put(space, set);
				}
				set.add(host);
			}
			// save host
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
	 * remove site
	 * @param host
	 * @return
	 */
	public short remove(SiteHost host) {
		Logger.info("HomePool.remove, home site %s", host);
		this.lockSingle();
		try {
			mapTime.remove(host);
			HomeSite site = mapSite.remove(host);
			if (site == null) {
				return Response.NOTACCEPTED;
			}
			for (Space space : site.listSpace()) {
				SiteSet set = mapSpace.get(space);
				if (set != null) {
					set.remove(host);
				}
				if (set == null || set.isEmpty()) {
					mapSpace.remove(space);
				}
			}
			return Response.ACCEPTED;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return Response.SERVER_ERROR;
	}

	/**
	 * update site
	 * @param site
	 * @return
	 */
	public short update(Site site) {
		Logger.info("HomePool.update, home site %s", site.getHost());
		short code = remove(site.getHost());
		if (code == Response.ACCEPTED) {
			return add(site);
		}
		return code;
	}
	
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
	 * refresh home site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			HomeSite site = mapSite.get(host);
			if (site != null) {
				mapTime.put(host, System.currentTimeMillis());
				code = Response.HOME_ISEE;
			} else {
				code = Response.NOTLOGIN;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		Logger.debug("HomePool.refresh, home site %s refresh status %d", host, code);
		return code;
	}

	/**
	 * return home site set
	 * @param db
	 * @param table
	 * @return
	 */
	public SiteHost[] find(String db, String table) {
		return find(new Space(db, table));
	}
	
	/**
	 * return home site set
	 * @param space
	 * @return
	 */
	public SiteHost[] find(Space space) {
		Logger.info("HomePool.find, space is '%s'", space);
		super.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if (set == null || set.isEmpty()) {
				return null;
			}
			return set.toArray();
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * create a space to home
	 * @param host
	 * @param table
	 * @return
	 */
	private boolean createSpace(SiteHost host, Table table) {
		HomeClient client = bring(host.getTCPHost());
		boolean success = false;
		try {
			if (client != null) {
				success = client.createSpace(table);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
		
		Logger.note(success, "HomePool.createSpace, create space '%s' to %s", table.getSpace(), host);
		
		return success;
	}

	/**
	 * create space to home site
	 * @param table
	 * @return
	 */
	public boolean createSpace(Table table) {
		if (mapSite.isEmpty()) {
			Logger.warning("HomePool.createSpace, home site set is empty");
			return false;
		}
		Space space = table.getSpace();
		Logger.info("HomePool.createSpace, space is '%s'", space);

		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		Clusters clusters = table.getClusters();
		
		if (clusters.size() > 0) {
			List<String> list = clusters.list();
			for(SiteHost host : mapSite.keySet()) {
				if(list.contains(host.getIP())) {
					array.add(host);
				}
			}
			if(array.size() < list.size()) {
				Logger.warning("HomePool.createSpace, home site missing!");
				return false;
			}
			clusters.setNumber(array.size());
		} else if (clusters.getNumber() > 0) {
			for(Space s : mapSpace.keySet()) {
				if (!space.matchSchema(s)) {
					continue;
				}
				SiteSet set = mapSpace.get(s);
				if(set == null || set.isEmpty()) continue;
				
				int left = clusters.getNumber() - array.size();
				List<SiteHost> list = choice_hosts(left, set);
				array.addAll(list);
				if (array.size() >= clusters.getNumber()) break;
			}
			// random choose
			if (array.isEmpty()) {
				List<SiteHost> list = choice_hosts(clusters.getNumber());
				array.addAll(list);
			}
			
			if(array.size() < clusters.getNumber()) {
				Logger.warning("HomePool.createSpace, home site number missing!");
				return false;
			}
			for(SiteHost host : array) {
				clusters.add(host.getIP());
			}
		} else { // random choose a home site			
			List<SiteHost> list = choice_hosts(1);
			array.addAll(list);
			if(array.size() < 1) {
				Logger.warning("HomePool.createSpace, home site absent!");
				return false;
			}
			
			clusters.setNumber(1);
			clusters.add(array.get(0).getIP());
		}
		
		ArrayList<SiteHost> backup = new ArrayList<SiteHost>();
		for(SiteHost host : array) {
			boolean success = this.createSpace(host, table);
			if(!success) {
				for(SiteHost site : backup) {
					boolean deleted = this.deleteSpace(space, site);
					Logger.note(deleted, "HomePool.createSpace, delete space %s from %s", space, site);
				}
				return false;
			}
			backup.add(host);
		}
		return true;		
	}

	/**
	 * delete space
	 * @param space
	 * @return
	 */
	public boolean deleteSpace(Space space) {
		Logger.info("HomePool.deleteSpace, space is '%s'", space);
		SiteSet set = mapSpace.get(space);
		if (set == null || set.isEmpty()) {
			Logger.error("HomePool.deleteSpace, cannot space '%s' set", space);
			return false;
		}
		for (SiteHost host : set.list()) {
			// delete space from home site
			boolean success = deleteSpace(space, host);
			if(!success) return false;
		}
		return true;
	}

	/**
	 * delete a data space 
	 * @param space
	 * @param home_host
	 * @return
	 */
	private boolean deleteSpace(Space space, SiteHost home_host) {
		HomeClient client = bring(home_host.getTCPHost());
		boolean success = false;
		try {
			if (client != null) {
				success = client.deleteSpace(space);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
		Logger.note(success, "HomePool.deleteSpace, delete space '%s' from %s", space, home_host);
		return success;
	}

	/**
	 * check timeout
	 */
	private void check() {
		int size = mapTime.size();
		if(size == 0) return;

		ArrayList<SiteHost> dels = new ArrayList<SiteHost>(size);
		ArrayList<SiteHost> notifys = new ArrayList<SiteHost>(size);
		super.lockSingle();
		try {
			long now = System.currentTimeMillis();
			for (SiteHost host : mapTime.keySet()) {
				Long value = mapTime.get(host);
				if (value == null) {
					dels.add(host);
					continue;
				}
				long time = value.longValue();
				if (now - time >= deleteTime) {
					dels.add(host);
				} else if (now - time >= refreshTimeout) {
					notifys.add(host);
				}
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		// delete timeout site
		for (SiteHost host : dels) {
			Logger.warning("HomePool.check, remove timeout site:%s", host);
			this.remove(host);
		}

		SiteHost listen = Launcher.getInstance().getLocalHost();
		// send timeout message to home site
		for (SiteHost host : notifys) {
			Logger.warning("HomePool.check, notify timeout site:%s", host);
			this.sendTimeout(host, listen, 2);
		}
	}

}