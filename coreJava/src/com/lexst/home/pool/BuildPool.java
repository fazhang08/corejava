/**
 * 
 */
package com.lexst.home.pool;

import java.io.*;
import java.util.*;

import com.lexst.db.chunk.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.home.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.build.*;
import com.lexst.site.*;
import com.lexst.site.build.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;

public class BuildPool extends Pool {
	
	private static BuildPool selfHandle = new BuildPool();

	/* naming set */
	private Map<Naming, SiteSet> mapNaming = new TreeMap<Naming, SiteSet>();

	/* build host -> site instance */
	private Map<SiteHost, BuildSite> mapSite = new TreeMap<SiteHost, BuildSite>();

	/* build host -> register time */
	private Map<SiteHost, Long> mapTime = new TreeMap<SiteHost, Long>();

	/* chunk identity set */	
	private ChunkIdentitySet array = new ChunkIdentitySet();
	
	private Map<Space, ChunkInfoSet> mapSet = new HashMap<Space, ChunkInfoSet>(32);

	/**
	 * 
	 */
	private BuildPool() {
		super();
	}
	
	/**
	 * @return
	 */
	public static BuildPool getInstance() {
		return BuildPool.selfHandle;
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
	 * @param name
	 * @return
	 */
	public long[] findBuildChunk(String name) {
		Logger.info("BuildPool.findBuildChunk, naming is %s", name);
		
		Naming naming = new Naming(name);
		ArrayList<Long> a = new ArrayList<Long>();

		super.lockMulti();
		try {
			SiteSet set = mapNaming.get(naming);
			if (set == null) return null;
			for (SiteHost host : set.list()) {
				BuildSite site = mapSite.get(host);
				if(site == null) continue;
				BuildSpace bs = site.find(naming);
				if (bs == null) continue;
				for (Space space : bs.keySet()) {
					ChunkIdentitySet cis = bs.find(space);
					a.addAll(cis.list());
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		int size = a.size();
		if(size == 0) return null;
		
		long[] ids = new long[size];
		for(int i = 0; i <ids.length; i++) {
			ids[i] = a.get(i);
		}
		return null;
		
	}
	
	private BuildClient fetch(SocketHost host) {
		BuildClient client = new BuildClient(true, host);
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
	
	private void complete(BuildClient client) {
		if(client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		}
	}
	
	/**
	 * find site by space
	 * @param space
	 * @return
	 */
	public SiteHost[] find(Space space) {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		
		super.lockMulti();
		try {
			for (SiteHost host : mapSite.keySet()) {
				BuildSite site = mapSite.get(host);
				for (Naming naming : site.keySet()) {
					BuildSpace ts = site.find(naming);
					if (ts.exists(space)) {
						if (!array.contains(host))
							array.add(host);
					}
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		
		int size = array.size();
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	/**
	 * find site by naming
	 * @param naming
	 * @return
	 */
	public SiteHost[] find(String naming) {
		Naming tn = new Naming(naming);
		SiteHost[] hosts = null;
		super.lockMulti();
		try {
			SiteSet set = mapNaming.get(tn);
			if (set != null) {
				int size = set.size();
				if (size > 0) {
					hosts = new SiteHost[size];
					set.list().toArray(hosts);
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return hosts;
	}
	
	/**
	 * get all site host
	 * @return
	 */
	public SiteHost[] getHosts() {
		SiteHost[] hosts = null;
		super.lockMulti();
		try {
			int size = mapSite.size();
			if (size > 0) {
				hosts = new SiteHost[size];
				mapSite.keySet().toArray(hosts);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return hosts;
	}
	
	private boolean match(SiteHost host, IP[] hosts) {
		if (hosts == null || hosts.length == 0) {
			return true;
		}
		for (IP ip : hosts) {
			if (host.getIPValue() == ip.getValue()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * build a naming task
	 * @param name
	 * @param hosts
	 * @return
	 */
	public IP[] buildTask(String name, IP[] hosts) {
		Logger.info("BuildPool.buildTask, naming is %s", name);
		
		Naming naming = new Naming(name);
		SiteSet set = mapNaming.get(naming);
		if (set == null || set.isEmpty()) {
			Logger.error("BuildPool.buildTask, cannot found %s", name);
			return null;
		}
		
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		super.lockSingle();
		try {
			array.addAll(set.list());
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		
		// rpc to build site
		ArrayList<IP> store = new ArrayList<IP>();
		for(SiteHost host : array) {
			// choose ip address
			if (!match(host, hosts)) continue;
			// send "build task" command
			BuildClient client = fetch(host.getTCPHost());
			if(client == null) continue;
			try {
				boolean success = client.build(name);
				if (success) store.add(new IP(host.getIPValue()));
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.error(exp);
			}
			this.complete(client);
		}
		IP[] s = new IP[store.size()];
		return store.toArray(s);
	}
	
	/**
	 * choose algorithm
	 * @param chunkid
	 * @param host
	 * @return
	 */
	public boolean agree(long chunkid, SiteHost host) {
		if (array.exists(chunkid)) {
			return false;
		}

		boolean allow = false;
		super.lockMulti();
		try {
			if (mapSite.containsKey(host)) {
				ArrayList<Integer> array = new ArrayList<Integer>();
				for (SiteHost site : mapSite.keySet()) {
					array.add(site.getIPValue());
				}
				java.util.Collections.sort(array);
				int remainder = (int) (chunkid % array.size());
				if(remainder < 0) remainder = Math.abs(remainder);
				int ip = array.get(remainder);
				allow = (ip == host.getIPValue());
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		
		Logger.info("BuildPool.empower, chunkid:%x, host:%s, size:%d, allow:%b",
				chunkid, host, mapSite.size(), allow);
		return allow;
	}
	
	/**
	 * data site(prime site) query home site, allow download from build site
	 * @param space
	 * @param chunkid
	 * @param length
	 * @param modified
	 * @return
	 */
	public boolean accede(Space space, long chunkid, long length,
			long modified) {
		
		Logger.info("BuildPool.accede, space:'%s', chunkid:%x, length:%d, modified:%d",
				space, chunkid, length, modified);

		Chunk info = new Chunk(chunkid, length, modified);
		boolean allow = false;
		super.lockSingle();
		try {
			ChunkInfoSet set = mapSet.get(space);
			if (set == null) {
				set = new ChunkInfoSet();
				mapSet.put(space, set);
			}
			Chunk backup = set.find(chunkid);
			if (backup == null) {
				set.add(info);
				allow = true;
			} else if (info.getLength() != backup.getLength()
					|| info.getLastModified() != backup.getLastModified()) {
				set.add(info);
				allow = true;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return allow;
	}
	
	/**
	 * refresh work site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			BuildSite site = mapSite.get(host);
			if (site != null) {
				mapTime.put(host, System.currentTimeMillis());
				code = Response.ISEE;
			} else {
				code = Response.NOTLOGIN;
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		Logger.debug("BuildPool.refresh, site %s refresh status %d", host, code);
		return code;
	}

	/**
	 * add site
	 * @param object
	 * @return
	 */
	public boolean add(Site object) {
		if(!object.isBuild()) return false;
		
		SiteHost host = object.getHost();
		BuildSite site = (BuildSite)object;
		boolean success = false;
		super.lockSingle();
		try {
			if (!mapSite.containsKey(host)) {
				mapSite.put(host, site);
				mapTime.put(host, System.currentTimeMillis());
				// save space
				for (Naming naming : site.keySet()) {
					BuildSpace bs = site.find(naming);
					if (bs == null || bs.isEmpty()) continue;
					for (Space space : bs.keySet()) {						
						ChunkIdentitySet cis = bs.find(space);
						array.add(cis.list());
					}
				}
				// save task naming
				for(Naming naming : site.keySet()) {
					SiteSet set = mapNaming.get(naming);
					if(set == null) {
						set = new SiteSet();
						mapNaming.put(naming, set);
					}
					set.add(host);
				}
				success = true;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		Logger.note(success, "BuildPool.add, site host %s", host);
		return success;
	}
	
	/**
	 * remove site
	 * @param host
	 * @return
	 */
	public boolean remove(SiteHost host) {
		boolean success = false;
		super.lockSingle();
		try {
			mapTime.remove(host);
			BuildSite site = mapSite.remove(host);
			if (site != null) {
				// remove space
				for (Naming naming : site.keySet()) {
					BuildSpace bs = site.find(naming);
					if (bs == null || bs.isEmpty()) continue;
					for (Space space : bs.keySet()) {
						ChunkIdentitySet tis = bs.find(space);
						array.remove(tis.list());
					}
				}
				// remove naming
				for(Naming naming : site.keySet()) {
					SiteSet set = mapNaming.get(naming);
					if (set == null) continue;
					set.remove(host);
					if (set.isEmpty()) mapNaming.remove(naming);
				}
				success = true;
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		Logger.note(success, "BuildPool.remove, site host %s", host);
		
		return success;
	}

	/**
	 * update site
	 * @param site
	 * @return
	 */
	public boolean update(Site site) {
		Logger.info("BuildPool.update, site host:%s", site.getHost());
		boolean success = remove(site.getHost());
		if(success) {
			success = add(site);
		}
		return success;
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
		Logger.info("BuildPool.process, into ...");
		while (!isInterrupted()) {
			this.check();
			this.delay(3000);
		}
		Logger.info("BuildPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapSite.clear();
		mapTime.clear();
		array.clear();
	}

	/**
	 * check site timeout
	 */
	private void check() {
		int size = mapSite.size();
		if (size == 0) return;
		
		long now = System.currentTimeMillis();
		ArrayList<SiteHost> array = new ArrayList<SiteHost>(size);
		ArrayList<SiteHost> dele = new ArrayList<SiteHost>(size);
		
		this.lockSingle();
		try {
			for (SiteHost host : mapTime.keySet()) {
				Long time = mapTime.get(host);
				if (now - time.longValue() >= deleteTime) {
					dele.add(host);
				} else if (now - time.longValue() >= refreshTimeout) {
					array.add(host);
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		// delete host
		for (SiteHost host : dele) {
			Logger.error("BuildPool.check, delete timeout site: %s", host);
			this.remove(host);
		}
		// notify
		SiteHost listen = Launcher.getInstance().getLocalHost();
		for (SiteHost host : array) {
			Logger.warning("BuildPool.check, notify timeout site:%s", host);
			this.sendTimeout(host, listen, 2);
		}
	}
}