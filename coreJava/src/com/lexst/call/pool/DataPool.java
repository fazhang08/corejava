/**
 *
 */
package com.lexst.call.pool;

import java.io.*;
import java.util.*;

import com.lexst.db.chunk.*;
import com.lexst.db.column.Column;
import com.lexst.db.index.range.*;
import com.lexst.db.row.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.data.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.call.*;
import com.lexst.site.data.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;

public class DataPool extends LocalPool {

	private static DataPool selfHandle = new DataPool();
	
	private Map<SiteHost, ClientSet> mapClient = new TreeMap<SiteHost, ClientSet>();

	/* table space -> index set */
	private Map<Space, IndexModule> mapIndex = new TreeMap<Space, IndexModule>();

	/* chunkid -> socket address set */
	private Map<Long, SiteSet> mapChunk = new TreeMap<Long, SiteSet>();

	/* table space -> socket address set */
	private Map<Space, SiteSet> mapSpace = new HashMap<Space, SiteSet>(16);
	
	/* diffuse task naming -> data host set */
	private Map<Naming, SiteSet> mapNaming = new TreeMap<Naming, SiteSet>();

	/* table space -> table instance */
	private Map<Space, Table> mapTable = new TreeMap<Space, Table>();
	
	/* table space -> current prime host */
	private Map<Space, SiteHost> mapPrime = new HashMap<Space, SiteHost>();
	
	/* homesite notify, update datasite record*/
	private boolean refresh;

	private long localIP = 0L;

	private long number = 0L;

	/**
	 * 
	 */
	public DataPool() {
		super();
		this.refresh = false;
	}
	
	/**
	 * return a static handle
	 * @return
	 */
	public static DataPool getInstance() {
		return DataPool.selfHandle;
	}
	

	/**
	 * dc|adc identify
	 * @return
	 */
	private synchronized long nextIdentity() {
		if (localIP == 0L) {
			localIP = callInstance.getLocal().getHost().getIPValue();
			localIP <<= 32;
		}
		if (number >= 0x0FFFFFFFL) number = 0;
		number++;
		return localIP | number;
	}
	
	public void refresh() {
		this.refresh = true;
		wakeup();
	}

	/**
	 * stop all client
	 */
	private void stopClients() {
		if(mapClient.isEmpty()) return;
		
		super.lockSingle();
		try {
			ArrayList<SiteHost> array = new ArrayList<SiteHost>(mapClient.keySet());
			for (SiteHost host : array) {
				ClientSet set = mapClient.remove(host);
				int size = set.size();
				for (int i = 0; i < size; i++) {
					DataClient client = (DataClient) set.get(i);
					client.stop();
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * find a work client
	 * @param host
	 * @param stream
	 * @return
	 */
	private DataClient findClient(SiteHost host, boolean stream) {
		ClientSet set = null;
		super.lockMulti();
		try {
			set = mapClient.get(host);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		
		if (set != null && set.size() > 0) {
			DataClient client = (DataClient) set.lockNext();
			if (client != null) return client;
			// when not lock-client
			if (set.size() >= ClientSet.LIMIT) {
				client = (DataClient) set.next();
				client.locking();
				return client;
			}
		}
		
		boolean success = false;
		// connect to host
		SocketHost address = (stream ? host.getTCPHost() : host.getUDPHost());
		DataClient client = new DataClient(address);
		try {
			client.reconnect();
			success = true;
		} catch (IOException exp) {
			Logger.error(exp);
		}
		if (!success) {
			client.close();
			return null;
		}

		client.locking();	// locked client
		client.start(); 	// start client thread

		super.lockSingle();
		try {
			if(set == null) {
				set = new ClientSet();
				mapClient.put(host, set);
			}
			set.add(client);
		} catch(Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		return client;
	}
	
	private DataClient findClient(SiteHost host) {
		return findClient(host, true);
	}

	/**
	 * find primary data client
	 * @param space
	 * @return
	 */
	private DataClient findPrime(Space space) {
		DataHost current = null, previous = null;
		SiteSet set = null;
		super.lockMulti();
		try {
			// before address
			previous = (DataHost)mapPrime.get(space);
			set = mapSpace.get(space);	
		} catch(Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		
		if (set == null) {
			Logger.error("DataClient.findPrime, cannot find space '%s'", space);
			return null;
		}
		
		int size = set.size();
		Logger.debug("DataPool.findPrime, '%s' host size %d", space, size);
		for (int i = 0; i < size; i++) {
			DataHost host = (DataHost) (previous != null && i == 0 ? set.next(previous) : set.next());
			if (host != null && host.isPrime()) {
				current = host;
				break;
			}
		}

		if (current != null) {
			super.lockSingle();
			try {
				mapPrime.put(space, current);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				super.unlockSingle();
			}
			// get client handle
			return findClient(current);
		}
		
		return null;
	}

	private Table findTable(Space space) {
		Table table = null;
		super.lockMulti();
		try {
			table = mapTable.get(space);
		} catch(Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if(table != null) return table;

		HomeClient client = super.bring(false);
		if(client == null) return null;
		try {
			table = client.findTable(space);
		} catch (VisitException exp) {
			Logger.error(exp);
		}
		this.complete(client);
		if (table == null) return null;

		super.lockSingle();
		try {
			mapTable.put(space, table);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return table;
	}
	
	/**
	 * 根据查询条件进行搜索
	 * 提示: 
	 * 1. table 可以自定义,或者由系统,不需要与table.space保持一致
	 * 2. select 中的 space 与 dc 的space 保持一致
	 * @param select
	 */
	public byte[] select(Select select) {
		long time = System.currentTimeMillis();

		// 检查删除锁定,如果存在,必须等待,直到结束
		Space space = select.getSpace();
		IndexModule module = null;
		super.lockMulti();
		try {
			module = mapIndex.get(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (module == null) {
			Logger.error("DataPool.select, cannot find index module for '%s'", space);
			return null;
		}
		ChunkIdentitySet tokens = new ChunkIdentitySet();
		int count = module.find(select.getCondition(), tokens);
		if (count < 0) {
			Logger.warning("DataPool.select, cannot find match chunk");
			return null;
		}
		
		// 按照chunk id,找到匹配的主机地址		
		HashMap<SiteHost, ChunkIdentitySet> map = new HashMap<SiteHost, ChunkIdentitySet>();
		for(long chunkId : tokens.list()) {
			//debug code, start
			Logger.debug("DataPool.select, check chunk id [%x - %d]", chunkId, chunkId);
			//debug code, end
			
			SiteSet set = null;
			super.lockMulti();
			try {
				set = mapChunk.get(chunkId);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				super.unlockMulti();
			}
			if(set == null) {
				Logger.error("DataPool.select, cannot find chunk id [%d - %x]", chunkId, chunkId);
				continue;
			}
			SiteHost host = set.next();
			if(host == null) {
				Logger.error("DataPool.select, cannot find host %s", host);
				continue;
			}
			// 分片
			ChunkIdentitySet idset = map.get(host);
			if(idset == null) {
				idset = new ChunkIdentitySet();
				map.put(host, idset);
			}
			idset.add(chunkId);
		}

		DataDelegate finder = new DataDelegate(map.size());
		for(SiteHost host : map.keySet()) {
			ChunkIdentitySet idset = map.get(host);
			// find data-client
			DataClient client = findClient(host);
			if(client == null) {
				Logger.error("DataPool.select, cannot find client:%s", host);
				break;
			}
			Select clone_select = select.clone();
			long[] chunkIds = idset.toArray();
			clone_select.setChunkId(chunkIds);
			finder.add(client, clone_select);
		}

		Logger.debug("DataPool.select, pre-select usedtime %d", System.currentTimeMillis() - time);

		// occur error
		if (map.size() != finder.size()) {
			finder.discontinue(false);
			return null;
		}
		
		// start query
		finder.execute();
		// wait and get data
		finder.waiting();
		byte[] data = finder.data();
		
		Logger.debug("DataPool.select, complete select usedtime %d", System.currentTimeMillis()-time);
		
		return data;
	}

	/**
	 * @param delete
	 * @return
	 */
	public long delete(Delete delete) {
		Space space = delete.getSpace();
		Logger.error("DataPool.delete, space is '%s'", space);
		
		//1. 将所有 "主节点" 主机全部锁定. 后面检索只限于对"主节点"删除.删除完毕后,再删除副节点
		// 检查删除锁定,如果存在,必须等待,直到结束
		IndexModule module = null;
		super.lockMulti();
		try {
			module = mapIndex.get(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (module == null) {
			Logger.error("DataPool.delete, cannot find '%s' index module", space);
			return -1;
		}
		ChunkIdentitySet tokens = new ChunkIdentitySet();
		int count = module.find(delete.getCondition(), tokens);
		if (count < 0) {
			Logger.warning("DataPool.delete, cannot find match chunk");
			return 0;
		}

		// 按照chunkid,找到匹配的主机地址
		HashMap<SiteHost, ChunkIdentitySet> map = new HashMap<SiteHost, ChunkIdentitySet>();
		for(long chunkId : tokens.list()) {
			SiteSet set = null;
			super.lockMulti();
			try {
				set = mapChunk.get(chunkId);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				super.unlockMulti();
			}
			if(set == null) {
				Logger.warning("DataPool.delete, cannot find chunk id by %d", chunkId);
				continue;
			}
			SiteHost host = set.next();
			if(host == null) {
				Logger.warning("DataPool.delete, cannot find host");
				continue;
			}
			// 分片
			ChunkIdentitySet idset = map.get(host);
			if(idset == null) {
				idset = new ChunkIdentitySet();
				map.put(host, idset);
			}
			idset.add(chunkId);
		}

		DataDelegate finder = new DataDelegate(map.size());
		for (SiteHost host : map.keySet()) {
			ChunkIdentitySet idset = map.get(host);
			DataClient client = findClient(host);
			if(client == null) {
				Logger.error("DataPool.delete, cannot connect %s", host);
				break;
			}
			Delete next = delete.clone();
			long[] chunkIds = idset.toArray();
			next.setChunkId(chunkIds);
			client.delete(finder, next);
		}
		
		if(map.size() != finder.size()) {
			finder.discontinue(true);
			return -1;
		}
		
		finder.execute();
		finder.waiting();
		return finder.getItems();
	}

	/**
	 * insert a row to data site
	 * @param insert
	 * @param sync (sync mode or async mode)
	 * @return
	 */
	public int insert(Insert insert, boolean sync) {
		Space space = insert.getSpace();
		Logger.debug("DataPool.insert, flush to '%s'", space);
		// find a master site(data site), insert a record		
		DataClient client = findPrime(space);
		if (client == null) {
			Logger.error("DataPool.insert, cannot find client for '%s'", space);
			return -1;
		}
		// change to byte array, insert
		byte[] data = insert.build();
		return client.insert(data, sync);
	}

	/**
	 * insert any row to data site
	 * @param inject
	 * @param sync (sync mode or async mode)
	 * @param previous (previous address)
	 * @return
	 */
	public int inject(Inject inject, boolean sync) {
		Space space = inject.getSpace();
		// find a mster site(data site), insert n record
		DataClient client = findPrime(space);
		if(client == null) {
			Logger.error("DataPool.inject, space '%s', cannot find client!", space);
			return -1;
		}		
		Logger.debug("DataPool.inject, flush '%s' to %s", space, client.getRemote());
		// change to byte array, insert
		byte[] data = inject.build();
		return client.insert(data, sync);
	}

	/**
	 * update row 
	 * @param update
	 * @return
	 */
	public long update(Update update) {
		Space space = update.getSpace();
		Condition condi = update.getCondition();
		
		Table table = findTable(space);
		if (table == null) {
			Logger.error("DataPool.update, cannot find '%s' table", space);
			return -1;
		}
		
		Logger.debug("DataPool.update, space is %s", space);

		//1. 将所有 "主节点" 主机全部锁定. 后面检索只限于对"主节点"删除.删除完毕后,再删除副节点
		// 检查删除锁定,如果存在,必须等待,直到结束
		IndexModule module = null;
		super.lockMulti();
		try {
			module = mapIndex.get(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (module == null) {
			Logger.error("DataPool.update, cannot find '%s' index module", space);
			return -1;
		}
		ChunkIdentitySet tokens = new ChunkIdentitySet();
		int count = module.find(condi, tokens);
		if (count < 0) {
			Logger.warning("DataPool.update, cannot find match chunk");
			return 0;
		}

		// 按照chunkid,找到匹配的主机地址 
		HashMap<SiteHost, ChunkIdentitySet> map = new HashMap<SiteHost, ChunkIdentitySet>();
		for(long chunkId : tokens.list()) {
			SiteSet set = null;
			super.lockMulti();
			try {
				set = mapChunk.get(chunkId);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				super.unlockMulti();
			}
			if(set == null) {
				Logger.warning("DataPool.update, cannot find chunk id by %d", chunkId);
				continue;
			}
			SiteHost host = set.next();
			if(host == null) {
				Logger.warning("DataPool.update, cannot find host");
				continue;
			}
			// 分片
			ChunkIdentitySet id_set = map.get(host);
			if(id_set == null) {
				id_set = new ChunkIdentitySet();
				map.put(host, id_set);
			}
			id_set.add(chunkId);
		}

		DataDelegate finder = new DataDelegate(map.size());
		for (SiteHost host : map.keySet()) {
			ChunkIdentitySet idset = map.get(host);
			DataClient client = findClient(host);
			if(client == null) {
				Logger.error("DataPool.update, cannot connect %s", host);
				break;
			}
			Delete delete = new Delete(space);
			delete.setSnatch(true);
			delete.setCondition(condi);
			delete.setChunkId(idset.list());
			// execute "delete" command
			client.delete(finder, delete);
		}
		
		if(map.size() != finder.size()) {
			finder.discontinue(true);
			return -1;
		}

		// execute job
		finder.execute();
		// wait select
		finder.waiting();
		
		Inject inject = new Inject(table);
		List<Column> values = update.values();

		byte[] data = finder.data();
		int off = 0;
		while(off < data.length) {
			Row row = new Row();
			int len = row.resolve(table, data, off);
			if(len < 1) break;
			off += len;

			// replace column
			for(Column column : values) {
				row.replace(column);
			}
			
			inject.add(row);
		}

		// insert item
		return this.inject(inject, false);
	}
	
	/**
	 * @param dc
	 * @return
	 */
	public byte[] dc(DC dc) {
		return dc(dc, null);
	}
	
	/**
	 * @param dc
	 * @param filteChunkIds
	 * @return
	 */
	public byte[] dc(DC dc, long[] filteChunkIds) {
		// set dc id
		dc.setIdentity(nextIdentity());

		// 如果没有定义work地址,在这里分配
		List<SiteHost> list = dc.listToAddress();
		if (list.isEmpty()) {
			int sites = dc.getToSites();
			String naming = dc.getToNaming();
			Logger.debug("DataPool.dc, aggregate naming: '%s'", naming);

			List<SiteHost> all = WorkPool.getInstance().find(naming);
			if (all == null || all.isEmpty()) {
				Logger.error("DataPool.dc, cannot find worksite by '%s'", naming);
				return null;
			}

			if (sites < 1 || sites < all.size()) sites = all.size();
			for (int i = 0; i < sites; i++) {
				dc.addToAddress(all.get(i));
			}
		}
		
		Select select = dc.getFromSelect();
		if(select != null) { // 如果没有"select"语句, 直接连接一个命名主机
			return dc_select(dc, filteChunkIds);
		} else {
			return dc_notselect(dc);
		}
	}
	
	private byte[] dc_notselect(DC dc) {
		//1. 找到需要数量的命名主机
		int from_sites = dc.getFromSites();
		if(from_sites < 1) from_sites = 1;
		Naming naming = new Naming(dc.getFromNaming());
		SiteSet set = null;
		super.lockMulti();
		try {
			set = mapNaming.get(naming);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (set == null) {
			Logger.error("DataPool.dc_notselect, cannot find naming:%s", naming);
			return null;
		} else if (set.size() < from_sites) {
			Logger.error("DataPool.dc_notselect, site missing!");
			return null;
		}
		
		ArrayList<SiteHost> hosts = new ArrayList<SiteHost>();
		for (int i = 0; i < from_sites; i++) {
			hosts.add(set.next());
		}
		
		//2. 在克隆DC中给每台主机分配一个索引号
		dc.defineFromSites(from_sites);
		DataDelegate finder = new DataDelegate(from_sites);

		int siteIndex = 1;
		for (SiteHost host : hosts) {
			DataClient client = findClient(host);
			if (client == null) {
				Logger.error("DataPool.dc_notselect, cannot connect %s", host);
				break;
			}
			DC clone_dc = dc.clone();
			clone_dc.defineFromIndex(siteIndex++);
			finder.add(client, clone_dc);
		}
		
		if(dc.getDefineFromSites() != finder.size()) {
			Logger.error("DataPool.dc_notselect, not match, exit!");
			finder.discontinue(true);
			return null;
		}

		// start query
		finder.execute();
		// wait and get data
		finder.waiting();
		// dc result
		byte[] data = finder.data();
		return data;
	}
	
	
	/**
	 * "dc" jobs
	 * @param dc
	 * @param filteChunkIds
	 * @return
	 */
	private byte[] dc_select(DC dc, long[] filteChunkIds) {
		Select select = dc.getFromSelect();
		Space space = select.getSpace();
		IndexModule module = null;
		super.lockMulti();
		try {
			module = mapIndex.get(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (module == null) {
			Logger.fatal("DataPool.dc_select, cannot find index module for '%s'", space);
			return null;
		}
		ChunkIdentitySet tokens = new ChunkIdentitySet();
		int count = module.find(select.getCondition(), tokens);
		if (count <= 0) {
			Logger.warning("DataPool.dc_select, cannot find chunk identity");
			return null;
		}
		
		// 删除冗余的chunkid
		if(filteChunkIds != null) {
			tokens.remove(filteChunkIds);
			if(tokens.isEmpty()) {
				Logger.warning("DataPool.dc, null chunkid set");
				return null;
			}
		}

		// 按照chunkid,找到匹配的主机地址
		Map<SiteHost, ChunkIdentitySet> map = new HashMap<SiteHost, ChunkIdentitySet>();
		for(long chunkId : tokens.list()) {
			SiteSet set = null;
			super.lockMulti();
			try {
				set = mapChunk.get(chunkId);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				super.unlockMulti();
			}
			if(set == null) {
				Logger.error("DataPool.dc, cannot find chunk id [%d - %x]", chunkId, chunkId);
				continue;
			}
			SiteHost host = set.next();
			if(host == null) {
				Logger.error("DataPool.dc, cannot find host %s", host);
				continue;
			}
			// 分割chunkid
			ChunkIdentitySet id_set = map.get(host);
			if(id_set == null) {
				id_set = new ChunkIdentitySet();
				map.put(host, id_set);
			}
			id_set.add(chunkId);
		}
		
		int fromsites = map.size();
		if(fromsites == 0) {
			Logger.warning("DataPool.dc, cannot find host!");
			return null;
		}
		dc.defineFromSites(fromsites);
		DataDelegate finder = new DataDelegate(fromsites);
		
		int siteIndex = 1;
		for(SiteHost host : map.keySet()) {
			ChunkIdentitySet id_set = map.get(host);
			// find data-client
			DataClient client = findClient(host);
			if(client == null) {
				Logger.error("DataPool.dc, cannot find client:%s", host);
				break;
			}
			Select clone_select = select.clone();
			long[] chunkIds = id_set.toArray();
			clone_select.setChunkId(chunkIds);
			DC clone_dc = dc.clone();
			clone_dc.setFromSelect(clone_select);
			clone_dc.defineFromIndex(siteIndex++);
			finder.add(client, clone_dc);
		}

		if(dc.getDefineFromSites() != finder.size()) {
			Logger.error("DataPool.dc, not match, exit!");
			finder.discontinue(true);
			return null;
		}

		// start query
		finder.execute();
		// wait and get data
		finder.waiting();
		// dc result
		byte[] data = finder.data();
		return data;
	}

	/**
	 * 异步分布计算
	 * 
	 * @param adc
	 * @return
	 */
	public byte[] adc(ADC adc) {
		return adc(adc, null);
	}

	/**
	 * @param adc
	 * @param filteChunkIds
	 * @return
	 */
	public byte[] adc(ADC adc, long[] filteChunkIds) {
		adc.setIdentity(this.nextIdentity());
		
		if (adc.getFromSelect() != null) {
			return adc_select(adc, filteChunkIds);
		} else {
			return adc_notselect(adc);
		}
	}
	
	private byte[] adc_select(ADC adc, long[] filteChunkIds) {
		Select select = adc.getFromSelect();
		Space space = select.getSpace();
		IndexModule module = null;
		super.lockMulti();
		try {
			module = mapIndex.get(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (module == null) {
			Logger.fatal("DataPool.adc_select, cannot find index module for '%s'", space);
			return null;
		}
		ChunkIdentitySet tokens = new ChunkIdentitySet();
		int count = module.find(select.getCondition(), tokens);
		if (count <= 0) {
			Logger.warning("DataPool.adc_select, cannot find chunk identity");
			return null;
		}
		
		// 删除冗余的chunkid
		if(filteChunkIds != null) {
			tokens.remove(filteChunkIds);
			if(tokens.isEmpty()) {
				Logger.warning("DataPool.adc_select, null chunkid set");
				return null;
			}
		}

		// 按照chunkid,找到匹配的主机地址
		Map<SiteHost, ChunkIdentitySet> map = new HashMap<SiteHost, ChunkIdentitySet>();
		for(long chunkId : tokens.list()) {
			SiteSet set = null;
			super.lockMulti();
			try {
				set = mapChunk.get(chunkId);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				super.unlockMulti();
			}
			if(set == null) {
				Logger.error("DataPool.adc_select, cannot find chunk id [%d - %x]", chunkId, chunkId);
				continue;
			}
			SiteHost host = set.next();
			if(host == null) {
				Logger.error("DataPool.adc_select, cannot find host %s", host);
				continue;
			}
			// 分割chunkid
			ChunkIdentitySet id_set = map.get(host);
			if(id_set == null) {
				id_set = new ChunkIdentitySet();
				map.put(host, id_set);
			}
			id_set.add(chunkId);
		}
		
		int fromsites = map.size();
		if(fromsites == 0) {
			Logger.warning("DataPool.adc_select, cannot find host!");
			return null;
		}
		DataDelegate finder = new DataDelegate(fromsites);
		
		for(SiteHost host : map.keySet()) {
			ChunkIdentitySet id_set = map.get(host);
			// find data-client
			DataClient client = findClient(host);
			if(client == null) {
				Logger.error("DataPool.adc_select, cannot find client:%s", host);
				break;
			}
			Select clone_select = select.clone();
			long[] chunkIds = id_set.toArray();
			clone_select.setChunkId(chunkIds);
			ADC clone_adc = adc.clone();
			clone_adc.setFromSelect(clone_select);
			finder.add(client, clone_adc);
		}

		if(fromsites != finder.size()) {
			Logger.error("DataPool.adc_select, not match, exit!");
			finder.discontinue(true);
			return null;
		}

		// start query
		finder.execute();
		// wait and get data
		finder.waiting();
		// dc result
		byte[] data = finder.data();
		if (data == null || data.length == 0) {
			Logger.warning("DataPool.adc_select, cannot find data!");
			return null;
		}

		// resolve DCArea
		ArrayList<DCArea> areas = new ArrayList<DCArea>();
		for (int off = 0; off < data.length;) {
			DCArea area = new DCArea();
			int len = area.resolve(data, off);
			off += len;
			areas.add(area);
		}
		
		return WorkPool.getInstance().adc(adc, areas);
	}
	
	/**
	 * @param adc
	 * @return
	 */
	private byte[] adc_notselect(ADC adc) {
		//1. find naming host, and check host number
		int from_sites = adc.getFromSites();
		if(from_sites < 1) from_sites = 1;
		Naming naming = new Naming(adc.getFromNaming());
		SiteSet set = null;
		super.lockMulti();
		try {
			set = mapNaming.get(naming);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if (set == null) {
			Logger.error("DataPool.adc_notselect, cannot find naming:%s", naming);
			return null;
		} else if (set.size() < from_sites) {
			Logger.error("DataPool.adc_notselect, host missing!");
			return null;
		}
		
		ArrayList<SiteHost> hosts = new ArrayList<SiteHost>();
		for (int i = 0; i < from_sites; i++) {
			hosts.add(set.next());
		}
		
		//2. find data-client, clone "ADC", and set a index(from 1)\
		adc.defineFromSites(from_sites);
		DataDelegate finder = new DataDelegate(from_sites);

		int siteIndex = 1;
		for (SiteHost host : hosts) {
			DataClient client = findClient(host);
			if (client == null) {
				Logger.error("DataPool.adc_notselect, cannot connect %s", host);
				break;
			}
			ADC clone_adc = adc.clone();
			clone_adc.defineFromIndex(siteIndex++);
			finder.add(client, clone_adc);
		}
		
		if(from_sites != finder.size()) {
			Logger.error("DataPool.adc_notselect, not match, exit!");
			finder.discontinue(true);
			return null;
		}

		// "diffuse" job
		finder.execute();
		// wait job
		finder.waiting();
		// dc result
		byte[] data = finder.data();
		if(data == null || data.length ==0) {
			Logger.warning("DataPool.adc_notselect, cannot find data!");
			return null;
		}
		
		// resolve "DCArea" set
		ArrayList<DCArea> array = new ArrayList<DCArea>();
		for(int off = 0; off < data.length; ) {
			DCArea area = new DCArea();
			int len = area.resolve(data, off);
			off += len;
			array.add(area);
		}
		
		// "aggregate" job
		return WorkPool.getInstance().adc(adc, array);
	}

	/**
	 * update all datasite
	 */
	private void refreshSite() {		
		HomeClient client = super.bring(false);
		if(client == null) {
			Logger.error("DataPool.refreshSite, cannot connect home-site:%s", home);
			return;
		}
		
		boolean error = true;
		DataSite[] sites = null;
		try {
			sites = (DataSite[]) client.batchDataSite();
			error = false;
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);

		if (error) {
			Logger.warning("DataPool.refreshSite, visit error!");
			return;
		}
		
		Logger.debug("DataPool.refreshSite, data site size:%d", (sites == null ? -1 : sites.length));
		
		// check space
		Map<Naming, SiteSet> map_naming = new TreeMap<Naming, SiteSet>();
		Map<Space, SiteSet> map_space = new TreeMap<Space, SiteSet>();
		Map<Long, SiteSet> map_chunk = new TreeMap<Long, SiteSet>();
		Map<Space, IndexModule> map_index = new TreeMap<Space, IndexModule>();
		List<SiteHost> allsite = new ArrayList<SiteHost>();
		
		for (int i = 0; sites != null && i < sites.length; i++) {
			DataHost host = new DataHost(sites[i].getHost(), sites[i].getRank());
			allsite.add(host);
			// save all naming(diffuse naming)
			for (Naming naming : sites[i].listNaming()) {
				SiteSet set = map_naming.get(naming);
				if (set == null) {
					set = new SiteSet();
					map_naming.put(naming, set);
				}
				set.add(host);
			}

			IndexSchema schema = sites[i].getIndexSchema();
			for(Space space : schema.keySet()) {
				if (!callInstance.containsSpace(space)) {
					continue;
				}
				
				IndexTable indexTable = schema.find(space);
				// save space -> host address
				SiteSet set = map_space.get(space);
				if (set == null) {
					set = new SiteSet();
					map_space.put(space, set);
				}
				set.add(host);
				// save module
				IndexModule module = map_index.get(space);
				if(module == null) {
					module = new IndexModule(space);
					map_index.put(space, module);
				}
				for(long chunkId : indexTable.keys()) {
					ChunkSheet sheet = indexTable.find(chunkId);
					for (short columnId : sheet.keys()) {
						IndexRange index = sheet.find(columnId);
						module.add(host, index);
					}
					// save data site
					set = map_chunk.get(chunkId);
					if (set == null) {
						set = new SiteSet();
						map_chunk.put(chunkId, set);
					}
					set.add(host);
				}
			}
		}

		// update all record
		super.lockSingle();
		try {
			mapNaming.clear();
			mapChunk.clear();
			mapIndex.clear();
			mapSpace.clear();
			mapPrime.clear();

			mapNaming.putAll(map_naming);
			mapSpace.putAll(map_space);
			mapChunk.putAll(map_chunk);
			mapIndex.putAll(map_index);
			
			ArrayList<SiteHost> excludes = new ArrayList<SiteHost>();
			for (SiteHost host : mapClient.keySet()) {
				if (!allsite.contains(host)) excludes.add(host);
			}

			for (SiteHost host : excludes) {
				ClientSet set = mapClient.remove(host);
				int size = set.size();
				for (int i = 0; i < size; i++) {
					DataClient ds = (DataClient) set.get(i);
					ds.stop();
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		CallSite site = super.callInstance.getLocal();
		site.clearDiffuseNaming();
		for (Naming naming : map_naming.keySet()) {
			Logger.debug("DataPool.refreshSite, add naming:%s", naming);
			site.addDiffuseNaming(naming.toString());
		}
		super.callInstance.setOperate(BasicLauncher.RELOGIN);
	}

	/**
	 * 停止这个空间下的所有连接
	 * @param space
	 */
	public int stopSpace(Space space) {
		int count = 0;
		super.lockSingle();
		try {
			mapTable.remove(space);
			mapPrime.remove(space);
			
			SiteSet set = mapSpace.remove(space);
			if (set == null) return -1;
			IndexModule module = mapIndex.remove(space);
			if(module == null) return -1;
			
			for(SiteHost host : set.list()) {
				List<Long> list = module.delete(host);
				if(list == null || list.isEmpty()) continue;
				for(long chunkId : list) {
					SiteSet sub = mapChunk.get(chunkId);
					if(sub != null) {
						if(sub.remove(host)) count++;
					}
					if(sub == null || sub.isEmpty()) {
						mapChunk.remove(chunkId);
					}
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		Logger.info("DataPool.stopSpace, remove chunkid count:%d", count);
		return count;
	}
	
	private void check() {
		if (refresh) {
			refresh = false;
			this.refreshSite();
		}
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

		this.delay(10000);
		this.refresh = true;
		
		while(!super.isInterrupted()) {
			this.check();
			this.delay(5000);
		}
		Logger.info("DataPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		stopClients();
	}

}