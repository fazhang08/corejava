/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;
import java.util.*;

import com.lexst.algorithm.diffuse.*;
import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.DCArea;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.work.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;

public class SQLPool extends Pool {
	
	// static instance
	private static SQLPool selfHandle = new SQLPool();
		
	private Map<SiteHost, ClientSet> mapClient = new TreeMap<SiteHost, ClientSet>();
	
	/**
	 * default constructor
	 */
	private SQLPool() {
		super();
	}

	/**
	 * @return
	 */
	public static SQLPool getInstance() {
		return SQLPool.selfHandle;
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
		Logger.info("SQLPool.process, into...");
		while(!isInterrupted()) {
			this.check();
			this.delay(20000);
		}
		Logger.info("SQLPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		stopClients();
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
					WorkClient client = (WorkClient) set.get(i);
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
	private WorkClient findClient(SiteHost host, boolean stream) {
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
			WorkClient client = (WorkClient) set.lockNext();
			if (client != null) return client;
			if (set.size() >= ClientSet.LIMIT) {
				client = (WorkClient) set.next();
				client.locking();
				return client;
			}
		}
		
		boolean success = false;
		WorkClient client = new WorkClient(stream);
		// connect to host
		SocketHost address = (stream ? host.getTCPHost() : host.getUDPHost());
		try {
			client.connect(address);
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
	
	/**
	 * connect host
	 * @param host
	 * @return
	 * @throws IOException
	 * @throws VisitException
	 */
	private WorkClient findClient(SiteHost host) {
		return findClient(host, true);
	}

	private Stream buildReply(short code) {
		Command cmd = new Command(code);
		return new Stream(cmd);
	}

	private void flushSelectResult(OutputStream resp, short code, int items, byte[] data) throws IOException {
		Stream reply = buildReply(code);
		reply.setContentItems(items);
		byte[] bytes = null;
		if (data != null && data.length > 0) {
			bytes = reply.buildHead(data.length);
		} else {
			bytes = reply.build();
		}
		resp.write(bytes, 0, bytes.length);
		if (data != null && data.length > 0) {
			resp.write(data, 0, data.length);
		}
		resp.flush();
	}

	private void flushDCResult(OutputStream resp, short code, byte[] data) throws IOException {
		Stream reply = buildReply(code);
		byte[] bytes = null;
		if (data != null && data.length > 0) {
			bytes = reply.buildHead(data.length);
		} else {
			bytes = reply.build();
		}
		resp.write(bytes, 0, bytes.length);
		if (data != null && data.length > 0) {
			resp.write(data, 0, data.length);
		}
		resp.flush();
	}

	private void dc_flush(ByteArrayOutputStream buff, DC dc, OutputStream resp) throws IOException {		
		DCTask task = (DCTask)DiffuseTaskPool.getInstance().find(dc.getFromNaming());
		if (task == null) {
			Logger.error("SQLPool.dc_flush, cannot find DC Task for '%s'", dc.getFromNaming());
			flushDCResult(resp, Response.DC_NOTFOUND, null);
			return;
		}
		
		byte[] data = null;
		if (buff != null) data = buff.toByteArray();
		// split data, from to_sites
		DCResult[] results = task.execute(dc, data); 
		// release memory
		data = null;
		if (buff != null) buff.reset();

		List<SiteHost> to_addr = dc.listToAddress();
		if (results == null || results.length == 0) { // this is error
			flushDCResult(resp, Response.DC_NOTFOUND, null);
			return;
		} else if (results.length != to_addr.size()) {
			flushDCResult(resp, Response.DC_SIZENOTMATCH, null);
			return;
		}

		WorkDelegate finder = new WorkDelegate(results.length);
		for (int i = 0; i < results.length; i++) {
			SiteHost host = to_addr.get(i);
			WorkClient client = findClient(host);
			if(client == null) { // this is error
				Logger.error("SQLPool.dc_flush, cannot connect %s", host);
				break;
			}
			
			DC clone_dc = dc.clone();
			clone_dc.defineToSites(results.length);
			// save work client handle and dc command, "aggregate" data
			finder.add(client, clone_dc, results[i].data());
		}

		if (finder.size() != results.length) {
			Logger.error("SQLPool.dc_flush, not match!");
			finder.discontinue(true);
			flushDCResult(resp, Response.DC_CLIENTERR, null);
			return;
		}
		Logger.debug("SQLPool.dc_flush, client size:%d", finder.size());

		// start jobs
		finder.execute();
		// wait jobs...
		finder.waiting();
		// get data
		data = finder.data();

		Logger.debug("SQLPool.dc_flush, from '%s' to '%s', data size %d",
				dc.getFromNaming(), dc.getToNaming(), (data == null ? -1 : data.length));
//		Logger.debug("SQLPool.dc_flush, return data:%s", print(data));

		if (data == null || data.length == 0) {
			flushDCResult(resp, Response.DC_NOTFOUND, null);
		} else {
			flushDCResult(resp, Response.DC_FOUND, data);
		}
	}

//	private String print(byte[] data) {
//		StringBuilder buff = new StringBuilder();
//		for(int i =0; data != null && i <data.length; i++) {
//			String s = String.format("%X", data[i]&0xff);
//			if(s.length()==1) s = "0" + s;
//			if(buff.length()>0) buff.append(",");
//			buff.append(s);
//		}
//		return buff.toString();
//	}

	/**
	 * @param buff
	 * @param adc
	 * @param resp
	 * @throws IOException
	 */
	private void adc_flush(ByteArrayOutputStream buff, ADC adc, OutputStream resp) throws IOException {
//		Naming naming = new Naming(adc.getFromNaming());
//		
//		Project project = mapProject.get(naming);
//		if (project == null) {
//			Logger.error("SQLPool.adc_flush, cannot find diffuse project for '%s'", naming);
//			flushDCResult(resp, Response.DC_NOTFOUND, null);
//			return;
//		}
//		// check table
//		if (adc.getFromSelect() != null) {
//			Space space = adc.getFromSelect().getSpace();
//			if (project.getTable(space) == null) {
//				Logger.error("SQLPool.adc_flush, cannot find table: '%s'", space);
//				flushDCResult(resp, Response.DC_NOTFOUND, null);
//				return;
//			}
//		}
//
//		ADCTask task = null;
//		try {
//			task = (ADCTask) Class.forName(project.getTaskClass()).newInstance();
//			task.setProject(project);
//		} catch (InstantiationException exp) {
//			Logger.error(exp);
//		} catch (IllegalAccessException exp) {
//			Logger.error(exp);
//		} catch (ClassNotFoundException exp) {
//			Logger.error(exp);
//		} catch (Throwable exp) {
//			Logger.fatal(exp);
//		}
		
		// check table
		if (adc.getFromSelect() != null) {
			Space space = adc.getFromSelect().getSpace();
			Project project = DiffuseTaskPool.getInstance().findProject(adc.getFromNaming());
			if (project.getTable(space) == null) {
				Logger.error("SQLPool.adc_flush, cannot find table: '%s'", space);
				flushDCResult(resp, Response.DC_NOTFOUND, null);
				return;
			}
		}

		// find adc-task
		ADCTask task = (ADCTask)DiffuseTaskPool.getInstance().find(adc.getFromNaming());
		if (task == null) {
			Logger.error("SQLPool.adc_flush, cannot find dctask for '%s'", adc.getFromNaming());
			flushDCResult(resp, Response.DC_NOTFOUND, null);
			return;
		}

		byte[] data = null;
		if (buff != null) data = buff.toByteArray();
		SiteHost host = Launcher.getInstance().getLocal().getHost();
		DCArea[] results = task.execute(host, DiskPool.getInstance(), adc, data);
		data = null;
		if (buff != null) buff.reset();

		ByteArrayOutputStream bi = new ByteArrayOutputStream();
		for(DCArea area : results) {
			byte[] b = area.build();
			bi.write(b, 0, b.length);
		}
		
		this.flushDCResult(resp, Response.DC_FOUND, bi.toByteArray());
	}
	
	/**
	 * query data from database
	 * @param resp
	 * @param query
	 * @throws IOException
	 */
	public void select(Select select, OutputStream resp) throws IOException {
		long time = System.currentTimeMillis();
		byte[] query = select.build();
		
		// jni query
		long result = Install.select(query);
		int rows = (int) ((result >>> 32) & 0xffffffffL); //item count
		int stamp = (int) (result & 0xffffffffL);	// query stamp

		Logger.debug("SQLPool.select, data rows:%d, stamp:%d", rows, stamp);

		// read sql data
		boolean finish = (rows == 0);
		int readsize = 1024 * 1024;
		ByteArrayOutputStream buff = new ByteArrayOutputStream(finish ? 16 : readsize);
		while (!finish) {
			byte[] b = Install.nextSelect(stamp, readsize);
			// send to
			if (b == null || b.length == 0) {
				finish = true;
			} else {
				finish = (b[0] == 1);
				buff.write(b, 1, b.length - 1);
			}
		}
		
		Logger.debug("SQLPool.select, data len:%d, query usedtime:%d", buff.size(), System.currentTimeMillis()-time);

		byte[] sqldata = buff.toByteArray();
		if (sqldata == null || sqldata.length == 0) {
			this.flushSelectResult(resp, Response.SELECT_NOTFOUND, rows, null);
		} else {
			this.flushSelectResult(resp, Response.SELECT_FOUND, rows, sqldata);
		}
	}

	/**
	 * delete data from database
	 * @param resp
	 * @param syntax
	 */
	public void delete(Delete object, OutputStream resp) throws IOException {
		// delete item
		byte[] syntax = object.build();
		byte[] ret = Install.delete(syntax);

		int off = 0;
		int items = Numeric.toInteger(ret, off, 4);
		off += 4;
		int stamp = Numeric.toInteger(ret, off, 4);
		off += 4;
		
		Logger.debug("SQLPool.delete, delete count:%d, stamp:%d", items, stamp);
		
		if (items < 0) { // error
			Stream stream = buildReply(Response.SERVER_ERROR);
			byte[] b = stream.build();
			resp.write(b, 0, b.length);
			resp.flush();
			return;
		} else if (items == 0) { // not found
			Stream stream = buildReply(Response.DELETE_NOTFOUND);
			// this is not found, send to socket
			byte[] b = stream.build();
			resp.write(b, 0, b.length);
			resp.flush();
			return;
		} else { // delete success
			// snatch
			byte[] sqldata = null;
			if (object.isSnatch()) {
				int readsize = 1024 * 1024;
				ByteArrayOutputStream buff = new ByteArrayOutputStream(readsize);
				boolean finish = false;
				while (!finish) {
					byte[] bytes = Install.nextDelete(stamp, readsize);
					// when finish, exit
					if (bytes == null || bytes.length == 0) {
						finish = true;
					} else {
						// send to socket
						finish = (bytes[0] == 1);
						buff.write(bytes, 1, bytes.length - 1);
					}
				}
				sqldata = buff.toByteArray();
			}

			Stream stream = this.buildReply(Response.DELETE_FOUND);
			stream.addMessage(new Message(Key.CONTENT_ITEMS, items));
			byte[] head = stream.buildHead(sqldata == null ? 0 : sqldata.length);
			resp.write(head, 0, head.length);
			if (sqldata != null && sqldata.length > 0) {
				resp.write(sqldata, 0, sqldata.length);
			}
			resp.flush();			
		}
		
		// backup chunk
		if(items > 0) {
			int dbsz = ret[off++] & 0xFF;
			int tabsz = ret[off++] & 0xFF;
			
			byte[] db = new byte[dbsz];
			byte[] table = new byte[tabsz];
			
			System.arraycopy(ret, off, db, 0, db.length);
			off += db.length;
			System.arraycopy(ret, off, table, 0, table.length);
			off += table.length;
			
			long cacheid = Numeric.toLong(ret, off, 8);
			off += 8;
			if(cacheid != 0L) {
				byte[] entity = Install.getCacheEntity(db, table, cacheid);
				CachePool.getInstance().update(new String(db), new String(table), cacheid, entity);
			}
			
			while (off < ret.length) {
				long chunkid = Numeric.toLong(ret, off, 8);
				off += 8;
				byte[] entity = Install.getChunkEntity(db, table, chunkid);
				ChunkPool.getInstance().update(new String(db), new String(table), chunkid, entity);
			}
		}
	}
	
	/**
	 * flush "INSERT" result
	 * @param items
	 * @param resp
	 * @throws IOException
	 */
	private void flushInsert(int items, OutputStream resp) throws IOException {
		boolean success = items > 0;
		Logger.note(success, "SQLPool.flushInsert, item count:%d", items);

		Command cmd = new Command(success ? Response.ACCEPTED : Response.DATA_INSERT_FAILED);
		Stream reply = new Stream(cmd);
		reply.setData(Numeric.toBytes(items));

		// flush to network
		byte[] data = reply.build();
		resp.write(data, 0, data.length);
		resp.flush();
	}
	
	/**
	 * sync append data to lexst db
	 * @param data
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	private boolean syncInsert(byte[] data, OutputStream resp) throws IOException {
		Logger.debug("SQLPool.syncInsert, data len:%d", data.length);
		int off = 0;
		// all size
		int allsize = Numeric.toInteger(data, off, 4);
		off += 4;
		if (data.length != allsize) {
			flushInsert(-1, resp);
			return false;
		}
		// version
		int version = Numeric.toInteger(data, off, 4);
		off += 4;
		if (version != 1) {
			flushInsert(-1, resp);
			return false;
		}
		// space
		int dbsize = data[off++];
		int tbsize = data[off++];
		
		String db = new String(data, off, dbsize);
		off += dbsize;
		String table = new String(data, off, tbsize);
		off += tbsize;
		Space space = new Space(db, table);
		// check space
		if (!Launcher.getInstance().existSpace(space)) {
			Logger.error("SQLPool.syncInsert, not found space '%s'", space);
			flushInsert(-1, resp);
			return false;
		}

		Logger.debug("SQLPool.syncInsert, flush to '%s'", space);
		long time = System.currentTimeMillis();

//		int items = 0;
//		while (true) {
//			// insert data (JNI)
//			items = Install.insert(data);
//			// check result
//			if (items > 0) {
//				break; // this is success
//			} else if (items == -3) {
//				while (true) {
//					long[] chunkIds = Launcher.getInstance().applyChunkId(5);
//					if (chunkIds != null) {
//						for (long chunkId : chunkIds) {
//							Install.addChunkId(chunkId);
//						}
//						break;
//					}
//				}
//			} else if (items == -2) {
//				// memory out
//				break;
//			} else {
//				// 添加失败,分析失败原因
//				break;
//			}
//		}
		
		int items = 0;
		while(true) {
			// insert data (JNI)
			byte[] ret = Install.insert(data);
			// check result
			off = 0;
			byte status = ret[off++];
			items = Numeric.toInteger(ret, off, 4);
			off += 4;
			if(status == 0) { // success
				int dblen = ret[off++] & 0xFF;
				int tablen = ret[off++] & 0xFF;
				byte[] db_name = new byte[dblen];
				byte[] table_name = new byte[tablen];
				// db name
				System.arraycopy(ret, off, db_name, 0, db_name.length);
				off += dblen;
				// table name
				System.arraycopy(ret, off, table_name, 0, table_name.length);
				off += tablen;
				// chunk identity
				long chunkid = Numeric.toLong(ret, off, 8);
				off += 8;
				// get cache data
				byte[] entity = Install.getCacheEntity(db_name, table_name, chunkid);
				// send to CachePool, distribute to other host
				CachePool.getInstance().update(new String(db_name), new String(table_name), chunkid, entity);
				break;
			} else {
				if (items == -3) { // get new chunkid
					while (true) {
						long[] chunkIds = Launcher.getInstance().applyChunkId(5);
						if (chunkIds != null) {
							for (long chunkId : chunkIds) {
								Install.addChunkId(chunkId);
							}
							break;
						}
					}
				} else if (items == -2) { // memory out
					break;
				} else { // other error
					break;
				}
			}
		}

		Logger.debug("SQLPool.syncInsert, flush '%s' to disk, insert count:%d, insert usedtime:%d", space, items, System.currentTimeMillis()-time);

		// response data
		flushInsert(items, resp);
		return items > 0;
	}
	
	/**
	 * async data to lexst db
	 * @param data
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	private boolean asyncInsert(byte[] data, OutputStream resp) throws IOException {
		Logger.debug("SQLPool.asyncInsert, data len:%d", data.length);
		int off = 0;
		// all size
		int allsize = Numeric.toInteger(data, off, 4);
		off += 4;
		if (data.length != allsize) {
			flushInsert(-1, resp);
			return false;
		}
		// version
		int version = Numeric.toInteger(data, off, 4);
		off += 4;
		if (version != 1) {
			flushInsert(-1, resp);
			return false;
		}
		// space
		int dbsize = data[off++];
		int tbsize = data[off++];
		
		String db = new String(data, off, dbsize);
		off += dbsize;
		String table = new String(data, off, tbsize);
		off += tbsize;
		Space space = new Space(db, table);
		// check space
		if (!Launcher.getInstance().existSpace(space)) {
			Logger.error("SQLPool.asyncInsert, cannot find space '%s'", space);
			flushInsert(-1, resp);
			return false;
		}
		
		// save to buffer
		boolean success = FlushPool.getInstance().write(data);
		// response data
		flushInsert(success ? 1 : -1, resp);
		return success;
	}
	
	/**
	 * append data to lexst db
	 * @param data
	 * @param sync
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	public boolean insert(byte[] data, boolean sync, OutputStream resp) throws IOException {
		if (sync) {
			return syncInsert(data, resp);
		} else {
			return asyncInsert(data, resp);
		}
	}
	
	/**
	 * SQL "dc" command
	 * @param dc
	 * @param resp
	 * @throws IOException
	 */
	public void dc(DC dc, OutputStream resp) throws IOException {
		if(dc.getFromSelect() != null) {
			dc_select(dc, resp);
		} else {
			dc_flush(null, dc, resp);
		}
	}

	/**
	 * SQL "adc" command, return DCArea set
	 * @param adc
	 * @param resp
	 * @throws IOException
	 */
	public void adc(ADC adc, OutputStream resp) throws IOException {
		if(adc.getFromSelect() != null) {
			adc_select(adc, resp);
		} else {
			adc_flush(null, adc, resp);
		}
	}
	
	private void dc_select(DC dc, OutputStream resp) throws IOException {
		long time = System.currentTimeMillis();
		Select select = dc.getFromSelect();
		byte[] query = select.build();

		// jni query
		long result = Install.select(query);
		int rows = (int) ((result >>> 32) & 0xffffffffL); //item count
		int stamp = (int) (result & 0xffffffffL);	// task stamp identity

		Logger.debug("SQLPool.dc_select, query count:%d, identity:%d", rows, stamp);

		// read sql data
		boolean finish = (rows == 0);
		int readsize = 1048576;
		ByteArrayOutputStream buff = new ByteArrayOutputStream(finish ? 16 : readsize);
		while (!finish) {
			byte[] b = Install.nextSelect(stamp, readsize);
			// send to
			if (b == null || b.length == 0) {
				finish = true;
			} else {
				finish = (b[0] == 1);
				buff.write(b, 1, b.length - 1);
			}
		}
		
		Logger.debug("SQLPool.dc_select, data len:%d, query usedtime:%d", buff.size(), System.currentTimeMillis()-time);

		dc_flush(buff, dc, resp);
	}

	private void adc_select(ADC adc, OutputStream resp) throws IOException {
		long time = System.currentTimeMillis();
		Select select = adc.getFromSelect();
		byte[] query = select.build();

		// jni query
		long result = Install.select(query);
		int rows = (int) ((result >>> 32) & 0xffffffffL); //item count
		int stamp = (int) (result & 0xffffffffL);	// task stamp identity

		Logger.debug("SQLPool.adc_select, query count:%d, identity:%d", rows, stamp);

		// read sql data
		boolean finish = (rows == 0);
		int readsize = 1048576;
		ByteArrayOutputStream buff = new ByteArrayOutputStream(finish ? 16 : readsize);
		while (!finish) {
			byte[] b = Install.nextSelect(stamp, readsize);
			// send to
			if (b == null || b.length == 0) {
				finish = true;
			} else {
				finish = (b[0] == 1);
				buff.write(b, 1, b.length - 1);
			}
		}
		
		Logger.debug("SQLPool.adc_select, data len:%d, query usedtime:%d", buff.size(), System.currentTimeMillis()-time);
		
		adc_flush(buff, adc, resp);
	}
	
	private void check() {
		if (mapClient.isEmpty()) return;

		long timeout = 120 * 1000;
		ArrayList<SiteHost> excludes = new ArrayList<SiteHost>();

		super.lockSingle();
		try {
			for (SiteHost host : mapClient.keySet()) {
				ClientSet set = mapClient.get(host);
				int size = set.size();
				for (int i = 0; i < size; i++) {
					WorkClient client = (WorkClient)set.get(i);
					if (client.isRefreshTimeout(timeout)) {
						set.remove(client);
						client.stop();
					}
				}
				if (set == null || set.isEmpty()) {
					excludes.add(host);
				}
			}
			for (SiteHost host : excludes) {
				mapClient.remove(host);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
	}
}