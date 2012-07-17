/**
 *
 */
package com.lexst.home.pool;

import java.io.*;
import java.util.*;

import com.lexst.db.chunk.*;
import com.lexst.db.index.range.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.home.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.data.*;
import com.lexst.site.*;
import com.lexst.site.data.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;

public class DataPool extends Pool {

	private static DataPool selfHandle = new DataPool();

	/* space -> index table set (data site) */
	private Map<Space, IndexModule> mapModule = new TreeMap<Space, IndexModule>();
	
	/* diffuse task naming -> host set */
	private Map<Naming, SiteSet> mapNaming = new TreeMap<Naming, SiteSet>();
	
	/* sapce -> site set */
	private Map<Space, SiteSet> mapSpace = new TreeMap<Space, SiteSet>();
	
	// site host -> data siet
	private Map<SiteHost, DataSite> mapSite = new TreeMap<SiteHost, DataSite>();
	
	// site host -> refresh time
	private Map<SiteHost, Long> mapTime = new TreeMap<SiteHost, Long>();

	// chunkid -> chunk domain information
	private LockMap<Long, PublishChunk> mapPublish = new LockMap<Long, PublishChunk>(0, true);

	private LockArray<UpgradeSpace> aryUpgrade = new LockArray<UpgradeSpace>(32);
	
	/**
	 *
	 */
	private DataPool() {
		super();
	}

	/**
	 * @return
	 */
	public static DataPool getInstance() {
		return DataPool.selfHandle;
	}
	
	public Map<Space, Integer> measure() {
		Map<Space, Integer> map = new HashMap<Space, Integer>();
		super.lockMulti();
		try {
			for (Space space : mapSpace.keySet()) {
				SiteSet set = mapSpace.get(space);
				int num = set.size();
				map.put(space, num);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return map; 
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
	 * @return
	 */
	public DataSite[] batch() {
		DataSite[] sites = null;
		super.lockMulti();
		try {
			int size = mapSite.size();
			if (size > 0) {
				sites = new DataSite[size];
				mapSite.values().toArray(sites);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return sites;
	}
	
	public int countSite() {
		return mapSite.size();
	}
	
	private DataClient fetch(SocketHost host) {
		DataClient client = new DataClient(true, host);
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
	
	private void complete(DataClient client) {
		if(client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		}
	}
	
	/**
	 * find all site by space
	 * @param space
	 * @param hosts
	 * @param primary
	 * @return
	 */
	private List<SiteHost> findHosts(Space space, IP[] hosts, boolean primary) {
		List<SiteHost> array = new ArrayList<SiteHost>();
		super.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			List<SiteHost> list = set.list();
			if (list == null || list.isEmpty()) return null;
			
			if (hosts == null || hosts.length == 0) {
				for (SiteHost host : list) {
					DataSite site = mapSite.get(host);
					if (!site.contains(space)) continue;
					if (primary) {
						if (site.isPrime()) array.add(host);
					} else {
						array.add(host);
					}
				}
			} else {
				for (IP ip : hosts) {
					for (SiteHost host : list) {
						DataSite site = mapSite.get(host);
						if (!site.contains(space)) continue;
						if (host.getIPValue() == ip.getValue()) {
							if (primary) {
								if (site.isPrime()) array.add(host);
							} else {
								array.add(host);
							}
							break;
						}
					}
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return array;
	}

	/**
	 * TOP请求, 找到主节点,执行优化操作
	 * @param space
	 * @param hosts
	 * @return
	 */
	public IP[] optimize(Space space, IP[] hosts) {
		Logger.debug("DataPool.optimize, space '%s', ip size %d", space, (hosts == null ? 0 : hosts.length));

		List<SiteHost> array = findHosts(space, hosts, true);
		ArrayList<IP> results = new ArrayList<IP>();
		for (SiteHost host : array) {
			DataClient client = fetch(host.getTCPHost());
			if (client != null) {
				try {
					boolean success = client.optimize(space);
					if (success) {
						results.add(new IP(host.getIPValue()));
					}
				} catch (VisitException exp) {
					Logger.error(exp);
				} catch (Throwable exp) {
					Logger.fatal(exp);
				}
			}
			this.complete(client);
		}
		if (results.isEmpty()) return null;
		IP[] s = new IP[results.size()];
		return results.toArray(s);
	}

	/**
	 * load index
	 * @param space
	 * @param hosts
	 * @return
	 */
	public IP[] loadIndex(Space space, IP[] hosts) {
		Logger.debug("DataPool.loadIndex, space '%s', ip size %d", space, (hosts == null ? 0 : hosts.length));

		List<SiteHost> array = findHosts(space, hosts, false);
		ArrayList<IP> results = new ArrayList<IP>();
		for (SiteHost host : array) {
			DataClient client = fetch(host.getTCPHost());
			if (client != null) {
				try {
					boolean success = client.loadIndex(space);
					if (success) {
						results.add(new IP(host.getIPValue()));
					}
				} catch (IOException exp) {
					Logger.error(exp);
				}
			}
			complete(client);
		}
		if (results.isEmpty()) return null;
		IP[] s = new IP[results.size()];
		return results.toArray(s);
	}

	/**
	 * stop index
	 * @param space
	 * @param hosts
	 * @return
	 */
	public IP[] stopIndex(Space space, IP[] hosts) {
		Logger.debug("DataPool.stopIndex, space '%s', ip size %d", space, (hosts == null ? 0 : hosts.length));

		List<SiteHost> array = findHosts(space, hosts, false);
		ArrayList<IP> results = new ArrayList<IP>();
		for (SiteHost host : array) {
			DataClient client = fetch(host.getTCPHost());
			if (client != null) {
				try {
					boolean success = client.stopIndex(space);
					if (success) {
						results.add(new IP(host.getIPValue()));
					}
				} catch (IOException exp) {
					Logger.error(exp);
				}
			}
			complete(client);
		}
		if(results.isEmpty()) return null;
		IP[] s = new IP[results.size()];
		return results.toArray(s);
	}

	/**
	 * load chunk to memory
	 * @param space
	 * @param hosts
	 * @return
	 */
	public IP[] loadChunk(Space space, IP[] hosts) {
		Logger.debug("DataPool.loadChunk, space '%s', ip size %d", space, (hosts == null ? 0 : hosts.length));
		
		List<SiteHost> array = findHosts(space, hosts, false);
		ArrayList<IP> results = new ArrayList<IP>();
		for (SiteHost host : array) {
			DataClient client = fetch(host.getTCPHost());
			if (client != null) {
				try {
					boolean success = client.loadChunk(space);
					if (success) {
						results.add(new IP(host.getIPValue()));
					}
				} catch (IOException exp) {
					Logger.error(exp);
				}
			}
			complete(client);
		}
		if(results.isEmpty()) return null;
		IP[] s = new IP[results.size()];
		return results.toArray(s);
	}

	/**
	 * release chunk from memory
	 * @param space
	 * @param hosts
	 * @return
	 */
	public IP[] stopChunk(Space space, IP[] hosts) {
		Logger.debug("DataPool.stopChunk, space '%s', ip size %d", space, (hosts == null ? 0 : hosts.length));

		List<SiteHost> array = findHosts(space, hosts, false);
		ArrayList<IP> results = new ArrayList<IP>();
		for (SiteHost host : array) {
			DataClient client = fetch(host.getTCPHost());
			if (client != null) {
				try {
					boolean success = client.stopChunk(space);
					if (success) {
						results.add(new IP(host.getIPValue()));
					}
				} catch (IOException exp) {
					Logger.error(exp);
				}
			}
			complete(client);
		}
		if(results.isEmpty()) return null;
		IP[] s = new IP[results.size()];
		return results.toArray(s);
	}

	/**
	 * data site(prime node) send message, upgrade chunk
	 * @param from
	 * @param space
	 * @param oldids
	 * @param newids
	 * @return
	 */
	public boolean upgrade(SiteHost from, Space space, long[] oldids,
			long[] newids) {
		Logger.info("DataPool.upgrade, from %s, space '%s', oldid size %d, newid size %d",
				from, space, oldids.length, newids.length);
		UpgradeSpace object = new UpgradeSpace(from, space, oldids, newids);
		aryUpgrade.add(object);
		return true;
	}
	
	/**
	 * distribute a chunk to slave site
	 * @param space
	 * @param chunkId
	 */
	public boolean distribute(SiteHost host, Space space, long chunkId, long length) {
		Logger.info("DataPool.distribute, add host %s - space '%s' - chunkid:%x - len:%d",
				host, space, chunkId, length);

		PublishChunk chunk = new PublishChunk(host, space, chunkId, length);
		// save it
		boolean success = false;
		if (!mapPublish.containsKey(chunk.getChunkId())) {
			success = (mapPublish.put(chunk.getChunkId(), chunk) == null);
		}
		return success;
	}

	/**
	 * check host
	 * @param host
	 * @return
	 */
	public boolean exists(SiteHost host) {
		boolean exist = false;
		super.lockMulti();
		try {
			exist = mapSite.containsKey(host);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return exist;
	}
	
	/**
	 * refresh data site
	 * @param host
	 * @return
	 */
	public short refresh(SiteHost host) {
		short code = Response.SERVER_ERROR;
		this.lockSingle();
		try {
			DataSite site = mapSite.get(host);
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
		Logger.debug("DataPool.refresh, data site %s refresh status %d", host, code);
		return code;
	}
	
	/**
	 * save index record
	 * @param object
	 * @return
	 */
	public boolean add(Site object) {
		return add(object, true);
	}
	
	/**
	 * add a site 
	 * @param object
	 * @param notify
	 * @return
	 */
	private boolean add(Site object, boolean notify) {
		if(object == null || !object.isData()) {
			Logger.error("DataPool.add, invalid data site");
			return false;
		}
		DataSite site = (DataSite)object;
		SiteHost host = site.getHost();
		IndexSchema schema = site.getIndexSchema();
		
		Logger.info("DataPool.add, data site %s, rank %d, index space size %d", host, site.getRank(), schema.size());

		int count = 0;
		this.lockSingle();
		try {
			if (mapSite.containsKey(host)) {
				Logger.warning("DataPool.add, %s existed", host);
				return false;
			}
			// save data site
			mapSite.put(host, site);
			mapTime.put(host, System.currentTimeMillis());
			// save naming
			for (Naming naming : site.listNaming()) {
				SiteSet set = mapNaming.get(naming);
				if (set == null) {
					set = new SiteSet();
					mapNaming.put(naming, set);
				}
				set.add(host);
			}
			// save space
			for(Space space : schema.keySet()) {
				Logger.debug("DataPool.add, login space '%s'", space);
				// save host
				SiteSet set = mapSpace.get(space);
				if(set == null) {
					set = new SiteSet();
					mapSpace.put(space, set);
				}
				set.add(host);
				// save index module
				IndexTable table = schema.find(space);
				IndexModule module = mapModule.get(space);
				if (module == null) {
					module = new IndexModule(space);
					mapModule.put(space, module);
				}
				// save index
				for (long chunkId : table.keys()) {
					ChunkSheet sheet = table.find(chunkId);
					for (short columnId : sheet.keys()) {
						IndexRange index = sheet.find(columnId);
						boolean success = module.add(host, index);
						if (success) count++;
					}
				}
			}
			// 通知所有CALL节点,更新DATA站点
			if (notify) {
				CallPool.getInstance().refreshDataSite();
			}
			Logger.debug("DataPool.add, site count %d", mapSite.size());
			return true;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			this.unlockSingle();
		}
		return false;
	}

	/**
	 * @param host
	 */
	public boolean remove(SiteHost host) {
		return remove(host, true);
	}
	
	/**
	 * 删除某一个节点的数据, 删除完毕,通过CallPool,向保有这个data节点的call节点,请求删除它
	 * @param host
	 * @param notify
	 * @return
	 */
	private boolean remove(SiteHost host, boolean notify) {
		Logger.info("DataPool.remove, data site %s", host);

		this.lockSingle();
		try {
			DataSite site = mapSite.remove(host);
			if(site == null) return false;
			IndexSchema schema = site.getIndexSchema();
			if (schema == null) return false;
			mapTime.remove(host);

			// remove naming
			for (Naming naming : site.listNaming()) {
				SiteSet set = mapNaming.get(naming);
				if (set != null) set.remove(host);
				if (set == null || set.isEmpty()) mapNaming.remove(naming);
			}
			// remove space
			for(Space space : schema.keySet()) {
				// delete host
				SiteSet set = mapSpace.get(space);
				if(set != null) set.remove(host);
				if(set == null || set.isEmpty()) {
					mapSpace.remove(space);
				}
				// delete index module by itself
				IndexModule module = mapModule.get(space);
				if (module != null) {
					module.remove(host);
					if(module.isEmpty()) mapModule.remove(space);
				}
			}
			// 向所有CALL节点更新DATA站点消息
			if (notify) {
				CallPool.getInstance().refreshDataSite();
			}
			return true;
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockSingle();
		}
		return false;
	}

	/**
	 * update data site
	 * @param site
	 * @return
	 */
	public boolean update(Site site) {
		if (site == null || !site.isData()) {
			return false;
		}
		
		SiteHost host = site.getHost();
		Logger.info("DataPool.update, data site %s", host);
		
		boolean removed = remove(host, false);
		if (removed) {
			boolean added = add(site, true);
			return added;
		}
		return false;
	}

	/**
	 * find data site by space
	 * @param db
	 * @param table
	 * @return
	 */
	public SiteHost[] findSite(String db, String table) {
		return findSite(new Space(db, table));
	}
	
	/**
	 * find data site by space
	 * @param space
	 * @return
	 */
	public SiteHost[] findSite(Space space) {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>(10);
		this.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if (set != null) {
				array.addAll(set.list());
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockMulti();
		}
		
		int size = array.size();
		Logger.info("DataPool.findSite, find data site by '%s', size %d", space, size);
		if (size > 0) {
			SiteHost[] hosts = new SiteHost[size];
			return array.toArray(hosts);
		}
		return null;
	}
	
	/**
	 * fin data site by space and rank
	 * @param space
	 * @param rank
	 * @return
	 */
	public SiteHost[] findSite(Space space, int rank) {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>(10);
		this.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if(set != null) {
				for(SiteHost host : set.list()) {
					DataSite site = mapSite.get(host);
					if(site.getRank() == rank) {
						array.add(host);
					}
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockMulti();
		}
		
		int size = array.size();
		Logger.info("DataPool.findSite, find data site by '%s' - %d, size %d", space, rank, size);
		if (size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	/**
	 * @param space
	 * @param chunkid
	 * @return
	 */
	public SiteHost[] findSite(Space space, long chunkid) {
		ArrayList<SiteHost> array = new ArrayList<SiteHost>(10);
		this.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if (set != null) {
				for (SiteHost host : set.list()) {
					DataSite site = mapSite.get(host);
					IndexSchema db = site.getIndexSchema();
					IndexTable it = db.find(space);
					if (it.contains(chunkid)) {
						array.add(host);
					}
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlockMulti();
		}
		int size = array.size();
		Logger.info("DataPool.findSite, find data site by '%s %X'", space, chunkid);
		if(size == 0) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	/**
	 * find prime site
	 * @param table
	 * @return
	 */
	private SiteHost[] findPrime(Table table) {
		int primes = table.getPrimes();
		int mode = table.getMode();
		long leftsize = 1024 * 1024 * 1024;
		
		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		
		if (mode == Table.EXCLUSIVE) {
			for (SiteHost host : mapSite.keySet()) {
				DataSite site = mapSite.get(host);
				Logger.debug("DataPool.findPrime, exclusive mode, used space:%d, free space:%d", site.getUsable(), site.getFree());
				
				if (site.getUsable() != 0 || site.getFree() < leftsize) {
					continue;
				}
				if(site.isPrime()) {
					array.add(host);
					if(array.size() >= primes) break;
				}
			}
		} else if (mode == Table.SHARE) {
			for(SiteHost host : mapSite.keySet()) {
				DataSite site = mapSite.get(host);
				
				Logger.debug("DataPool.findPrime, share mode, used space:%d, free space:%d", site.getUsable(), site.getFree());
				// free space missing, next!
				if(site.getFree() < leftsize) continue;
				// table is exclusive mode, next!
				boolean exclusive = false;
				Set<Space> sets = site.getIndexSchema().keySet();
				for(Space sp : sets) {
					Table backup = Launcher.getInstance().findTable(sp);
					exclusive = backup.isExclusive();
					if (exclusive) break;
				}
				if (exclusive) continue;
				
				if(site.isPrime()) {
					array.add(host);
					if(array.size() >= primes) break;
				}
			}
		}
		
		int size = array.size();
		if(size < primes) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}

	/**
	 * find slave site
	 * @param table
	 * @return
	 */
	private SiteHost[] findSlave(Table table) {
		int primes = table.getPrimes();
		int copy = table.getCopy();
		int slaves = (copy * primes) - primes;
		if(slaves == 0) return null;

		int mode = table.getMode();
		long leftsize = 1024 * 1024 * 1024;

		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		
		if (mode == Table.EXCLUSIVE) {
			for (SiteHost host : mapSite.keySet()) {
				DataSite site = mapSite.get(host);
				Logger.debug("DataPool.findSlave, exclusive mode, used space:%d, free space:%d", site.getUsable(), site.getFree());
				
				if (site.getUsable() != 0 || site.getFree() < leftsize) {
					continue;
				}
				if(site.isSlave()) {
					array.add(host);
					if(array.size() >= slaves) break;
				}
			}
		} else if (mode == Table.SHARE) {
			for(SiteHost host : mapSite.keySet()) {
				DataSite site = mapSite.get(host);
				
				Logger.debug("DataPool.findSlave, share mode, used space:%d, free space:%d", site.getUsable(), site.getFree());
				// free space missing, next!
				if(site.getFree() < leftsize) continue;
				// table is exclusive mode, next!
				boolean exclusive = false;
				Set<Space> sets = site.getIndexSchema().keySet();
				for(Space sp : sets) {
					Table backup = Launcher.getInstance().findTable(sp);
					exclusive = backup.isExclusive();
					if (exclusive) break;
				}
				if (exclusive) continue;
				
				if(site.isSlave()) {
					array.add(host);
					if(array.size() >= slaves) break;
				}
			}
		}

		int size = array.size();
		if(size < slaves) return null;
		SiteHost[] hosts = new SiteHost[size];
		return array.toArray(hosts);
	}
	
	/**
	 * send command to data site, create data space
	 * @param table
	 * @return
	 */
	public SiteHost[] createSpace(Table table) {
		// alloc site
		SiteHost[] primes = findPrime(table);
		if (primes == null) {
			Logger.warning("DataPool.createSpace, prime site missing");
			return null;
		}
		
//		SiteHost[] slaves = findSlave(table);
//		if (slaves == null) {
//			Logger.warning("DataPool.createSpace, slave site missing");
//			return null;
//		}
		
		SiteHost[] slaves = null;
		int slave_num = table.getCopy() * table.getPrimes() - table.getPrimes();
		if (slave_num > 0) {
			slaves = findSlave(table);
			if (slaves == null) {
				Logger.warning("DataPool.createSpace, slave site missing");
				return null;
			}
		}

		SiteHost[] hosts = new SiteHost[primes.length + (slaves == null ? 0 : slaves.length)];
		System.arraycopy(primes, 0, hosts, 0, primes.length);
		if (slaves != null) {
			System.arraycopy(slaves, 0, hosts, primes.length, slaves.length);
		}

		ArrayList<SiteHost> array = new ArrayList<SiteHost>();
		Space space = table.getSpace();
		
		for(SiteHost host: hosts) {
			SocketHost socket = host.getTCPHost();
			DataClient client = new DataClient(true, socket);
			boolean success = false;
			try {
				client.reconnect();
				success = client.createSpace(table);
				client.exit();
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			
			Logger.note(success, "DataPool.createSpace, '%s' to '%s'(data site)", space, host);
			if (!success) break;
			array.add(host);
		}
		
		// when error, delete table
		if(array.size() < hosts.length) {
			for(SiteHost host : array) {
				SocketHost socket = host.getTCPHost();
				DataClient client = new DataClient(true, socket);
				try {
					client.reconnect();
					boolean success = client.deleteSpace(space);
					Logger.note(success, "delete space '%s' from '%s'(data site)", space, host);
					client.exit();
				} catch (VisitException exp) {
					Logger.error(exp);
				} catch (Throwable exp) {
					Logger.fatal(exp);
				}
				client.close();
			}
			return null;
		}
		
		Logger.debug("DataPool.createSpace, create success");
		return hosts;
	}

	/**
	 * delete table space from data site
	 * @param space
	 * @return
	 */
	public boolean deleteSpace(Space space) {
		SiteHost[] sites = findSite(space);
		if (sites == null || sites.length == 0) {
			Logger.error("DataPool.deleteSpace, cannot found space '%s'", space);
			return false;
		}
		// notify data site, delete all table space
		int count = 0;
		for (SiteHost host : sites) {
			DataClient client = new DataClient(true);
			try {
				client.connect(host.getTCPHost());
				boolean success = client.deleteSpace(space);
				if (success) count++;
				client.exit();
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
		}
		Logger.info("DataPool.deleteSpace, delete table space '%s', delete count:%d , host count:%d", space, count, sites.length);
		return count == sites.length;
	}

	/**
	 * delete space by host
	 * @param host
	 * @param space
	 * @return
	 */
	public boolean deleteSpace(SiteHost host, Space space) {
		Logger.info("DataPool.deleteSpace, delete space '%s' from %s", space, host);
		
		SocketHost socket = host.getTCPHost();
		DataClient client = new DataClient(true, socket);
		boolean success = false;
		try {
			client.reconnect();
			success = client.deleteSpace(space);
			client.exit();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		client.close();
		if(success) {
			DataSite site = mapSite.get(host);
			if (site != null) site.getIndexSchema().remove(space);
			SiteSet set = mapSpace.get(space);
			if (set != null) set.remove(host);
		}
		
		Logger.note(success, "DataPool.deleteSpace, delete space '%s'", space);
		return success;
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
		Logger.info("DataPool.process, into...");

		long overloadTimeout = 3600 * 1000;
		long overloadEndtime = System.currentTimeMillis() + overloadTimeout;
		long siteTimeout = 5000;
		long siteEndtime = System.currentTimeMillis() + siteTimeout;
		
		while (!isInterrupted()) {
			// 超载分发检查(超载由用户定义,否则取默认)
			if (System.currentTimeMillis() >= overloadEndtime) {
				checkOverload();
				overloadEndtime += overloadTimeout;
			}
			// check data site timeout
			if(System.currentTimeMillis() >= siteEndtime) {
				checkTimeout();
				siteEndtime += siteTimeout;
			}
			// 正常检查
			this.check();

			this.delay(1000);
		}
		Logger.info("DataPool.process, exit");
	}

	private void check() {
		this.distribute();
		this.upgrade();
	}
	
	/**
	 * check overload site, and distribute chunk
	 */
	private void checkOverload() {
		
	}

	/**
	 * check timeout site
	 */
	private void checkTimeout() {
		int size = mapSite.size();
		if (size == 0) return;

		ArrayList<SiteHost> disables = new ArrayList<SiteHost>(size);
		ArrayList<SiteHost> timeouts = new ArrayList<SiteHost>(size);
		super.lockSingle();
		try {
			long now = System.currentTimeMillis();
			for (SiteHost host : mapTime.keySet()) {
				Long value = mapTime.get(host);
				if (value == null) {
					timeouts.add(host);
					continue;
				}
				long time = value.longValue();
				if (now - time >= deleteTime) {
					disables.add(host);
				} else if (now - time >= refreshTimeout) {
					timeouts.add(host);
				}
			}
		} catch (Throwable exp) {
			exp.printStackTrace();
		} finally {
			super.unlockSingle();
		}
		// delete all disable site
		for (SiteHost host : disables) {
			Logger.error("DataPool.checkTimeout, delete timeout site:%s", host);
			this.remove(host);
		}

		SiteHost listen = Launcher.getInstance().getLocalHost();
		for (SiteHost host : timeouts) {
			Logger.warning("DataPool.checkTimeout, notify timeout site:%s", host);
			this.sendTimeout(host, listen, 2);
		}
	}

	/**
	 * distribute chunk information to slave site
	 */
	private void distribute() {
		if (mapPublish.isEmpty()) return;

		Logger.debug("DataPool.distribute, chunk size %d", mapPublish.size());

		// 这两个参数做成可配置的文件
		final long min_size = 1024 * 1024 * 1024;
		final int max_chunks = 4096;
		
		for(long chunkid : mapPublish.keySet()) {
			PublishChunk chunk = mapPublish.get(chunkid);
			Space space = chunk.getSpace();
			Table table = Launcher.getInstance().findTable(space);
			int backups = table.getCopy() - 1;
			if (backups < 1) {
				mapPublish.remove(chunkid);
				continue;
			}

			List<SiteHost> hosts = findSlaves(space);
			if (hosts == null || hosts.isEmpty()) {
				Logger.warning("DataPool.distribute, cannot find '%s' slave site", space);
				continue;
			}
			// 找到最少块的主机和最多空间的主机
			Map<Integer, SiteSet> mapWeight = new TreeMap<Integer, SiteSet>();
			for (SiteHost host : hosts) {
				DataSite site = mapSite.get(host);
				IndexSchema db = site.getIndexSchema();
				if (site.getFree() <= min_size) continue; //小于1G的空间不处理
				int chunks = db.countChunk();
				if (chunks < max_chunks) {
					SiteSet set = mapWeight.get(chunks);
					if(set == null) {
						set = new SiteSet();
						mapWeight.put(chunks, set);
					}
					set.add(host);
				}
			}
			ArrayList<Integer> array = new ArrayList<Integer>(mapWeight.keySet());
			Collections.sort(array);
			int sends = 0;
			for(int weight : array) {
				SiteSet set = mapWeight.get(weight);
				for(SiteHost host : set.list()) {
					boolean success = distribute(host, chunk);
					Logger.note(success, "DataPool.distribute, send '%s' to %s", space, host);
					if(success) sends++;
					if (sends >= backups) break;
				}
				if (sends >= backups) break;
			}
			Logger.info("DataPool.distribute, send '%s' count %d", space, sends);
			mapPublish.remove(chunkid);
		}
	}

	/**
	 * send chunk information to data site(slave node)
	 * @param host
	 * @param chunk
	 * @return
	 */
	private boolean distribute(SiteHost host, PublishChunk chunk) {
		SocketHost address = host.getTCPHost();
		DataClient client = this.fetch(address);
		if(client == null) {
			Logger.error("DataPool.distribute, cannot send to %s", host);
			return false;
		}

		SiteHost from = chunk.getHost();
		Space space = chunk.getSpace();
		long chunkid = chunk.getChunkId();
		long length = chunk.getLength();

		boolean success = false;
		try {
			success = client.distribute(from, space, chunkid, length);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
		
		Logger.note(success, "DataPool.distribute, send site:%s, space:'%s' - %x to %s", from, space, chunkid, host);
		return success;
	}
	
	private void upgrade() {
		//found all slave site
		if(aryUpgrade.isEmpty()) return;
		
		Logger.debug("DataPool.upgrade, size is %d", aryUpgrade.size());
		
		// find all slave site
		ArrayList<SiteHost> array = new ArrayList<SiteHost>(128);
		super.lockMulti();
		try {
			for (SiteHost host : mapSite.keySet()) {
				DataSite site = mapSite.get(host);
				if (site.isSlave()) array.add(host);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		
		// send message to all
		List<UpgradeSpace> list = aryUpgrade.pollAll();
		for(SiteHost host : array) {
			this.upgrade(host, list);
		}
	}
	
	private void upgrade(SiteHost host, List<UpgradeSpace> list) {
		DataClient client = this.fetch(host.getTCPHost());
		if (client == null) {
			Logger.error("DataPool.upgrade, cannot connect %s", host);
			return;
		}
		
		try {
			for (UpgradeSpace object : list) {
				SiteHost from = object.getHost();
				Space space = object.getSpace();
				long[] oldIds = object.getOldChunkIds();
				long[] newIds = object.getNewChunkIds();
				boolean success = client.upgrade(from, space, oldIds, newIds);
				Logger.note(success, "DataPool.upgrade, send %s - '%s'  to %s", from, space, host);
			}
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
	}
	
	/**
	 * find all slave site 
	 * @param space
	 * @return
	 */
	private List<SiteHost> findSlaves(Space space) {
		List<SiteHost> array = new ArrayList<SiteHost>();
		super.lockMulti();
		try {
			SiteSet set = mapSpace.get(space);
			if (set != null) {
				for (SiteHost host : set.list()) {
					DataSite site = mapSite.get(host);
					if (site.isSlave()) array.add(host);
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return array;
	}
	
}