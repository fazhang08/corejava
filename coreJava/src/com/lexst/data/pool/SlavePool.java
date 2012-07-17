/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;
import java.util.*;

import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.data.*;
import com.lexst.util.host.*;

public class SlavePool extends LocalPool {
	
	private static SlavePool selfHandle = new SlavePool();

	private Vector<SlaveChunk> chunks = new Vector<SlaveChunk>();
	
	private Vector<Upgrade> upgrades = new Vector<Upgrade>();
	
	/**
	 * 
	 */
	private SlavePool() {
		super();
	}
	
	/**
	 * @return
	 */
	public static SlavePool getInstance() {
		return SlavePool.selfHandle;
	}

	/**
	 * add a distribute object
	 * @param host
	 * @param chunkId
	 * @return
	 */
	public boolean distribute(SiteHost host, Space space, long chunkId, long length) {
		boolean slave = Launcher.getInstance().getLocal().isSlave();
		if (!slave) {
			Logger.error("SlavePool.distribute, cannot accpeted!");
			return false;
		}

		Logger.info("SlavePool.distribute, host %s, space '%s', chunkid:%x", host, space, chunkId);
		SlaveChunk chunk = new SlaveChunk(host, space, chunkId, length);
		
		if (!chunks.contains(chunk)) {
			chunks.add(chunk);
			wakeup();
			return true;
		}
		return false;
	}

	/**
	 * add a upgrade object, when after optmize
	 * @param from
	 * @param space
	 * @param oldIds
	 * @param newIds
	 * @return
	 */
	public boolean upgrade(SiteHost from, Space space, long[] oldIds, long[] newIds) {
		boolean slave = Launcher.getInstance().getLocal().isSlave();
		if (!slave) {
			Logger.error("SlavePool.upgrade, cannot accpeted!");
			return false;
		}

		Logger.info("SlavePool.upgrade, host:%s, space:%s", from, space);
		Upgrade object = new Upgrade(from, space, oldIds, newIds);
		return upgrades.add(object);
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
		Logger.info("SlavePool.process, into...");
		while (!super.isInterrupted()) {
			while (!chunks.isEmpty()) {
				SlaveChunk object = chunks.remove(0);
				if(object == null) {
					Logger.error("SlavePool.process, null SlaveChunk object, size:%d", chunks.size());
					continue;
				}
				this.download(object);
			}
			while (!upgrades.isEmpty()) {
				Upgrade object = upgrades.remove(0);
				if(object == null) {
					Logger.error("SlavePool.process, null Upgrade object, size:%d", upgrades.size());
					continue;
				}
				this.upgrade(object);
			}
			this.delay(1000);
		}
		Logger.info("SlavePool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * download a chunk from data site(prime node)
	 * @param object
	 * @return
	 */
	private boolean download(SlaveChunk object) {
		SiteHost host = object.getHost();
		Space space = object.getSpace();
		long chunkid = object.getChunkId();
		long length = object.getLength();
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		
		Logger.debug("SalvePool.download, host %s, space '%s', chunkid:%x", host, space, chunkid);
		
		byte[] path = null;
		for (int index = 0; true; index++) {
			path = Install.getChunkPath(db, table, index);
			if (path == null || path.length == 0) {
				Logger.error("SlavePool.download, cannot get '%s' directory", space);
				return false;
			}
			File dir = new File(new String(path));
			if (dir.isDirectory() && dir.getFreeSpace() > length) {
				break;
			}
		}
		// build filename
		String filename = buildFilename(new String(path), chunkid);
		// download chunk
		DataDownloader downloader = new DataDownloader();
		boolean success = downloader.execute(host, space, chunkid, filename);
		Logger.note(success,"SlavePool.download, from %s download '%s' - %x to %s", host,
				space, chunkid, filename);
		// when success, load chunk to jni
		if(success) {
			byte[] b = filename.getBytes();
			int ret = Install.loadChunk(db, table, b);
			success = (ret == 0);
			Logger.note(success, "SlavePool.download, load chunk '%s' - %s", space, filename);
		}
		// modify chunk rank
		if (success) {
			int ret = Install.toSlave(db, table, chunkid);
			success = (ret == 0);
			Logger.note("SlavePool.download, update chunk rank", success);
		}
		if(success) {
			Launcher.getInstance().setUpdateModule(true);
		}
		return success;
	}

	/**
	 * 处理流程
	 * 1. 连接上指定的DATA主机，拿到全部CHUNK
	 * 2. 删除被主节点主机被清除的CHUNK
	 * 3. 更新主节点优化后仍保存的，并且本地也有的CHUNK
	 * @param upgrade
	 */
	private void upgrade(Upgrade upgrade) {
		SiteHost prime = upgrade.getHost();
		Space space = upgrade.getSpace();
		long[] oldIds = upgrade.getOldChunkIds();
		long[] newIds = upgrade.getNewChunkIds();
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		
		Logger.info("SlavePool.upgrade, space is '%s'", space);

		// read jni chunkid
		long[] localIds = Install.getChunkIds(db, table);
		if (localIds == null || localIds.length == 0) {
			upgradeAll(prime, space, newIds);
			return; //下载全部
		}
		
		// choose match chunkid (old chunkid)
		ArrayList<Long> array = new ArrayList<Long>();
		for (long chunkid : localIds) {
			for (long oldid : oldIds) {
				if (chunkid == oldid) {
					array.add(chunkid);
				}
			}
		}
		// when not found, exit
		if(array.isEmpty()) {
			upgradeAll(prime, space, newIds);
			return; //下载一部分,直到空间用完
		}
		
		// 与新CHUNK比较，相同的保存，不同删除
		ArrayList<Long> deletes = new ArrayList<Long>();
		for(long chunkid : array) {
			boolean found = false;
			for(long newid : newIds) {
				found = (newid == chunkid);
				if(found) break;
			}
			if (!found) {
				deletes.add(chunkid);
				int ret = Install.deleteChunk(db, table, chunkid);
				boolean success = (ret == 0);
				Logger.note(success, "SlavePool.upgrade, delete chunk '%s' - %x", space, chunkid);
			}
		}
		array.removeAll(deletes);

		// connect data site(prime node) and update
		DataClient client = apply(prime);
		if (client == null) {
			Logger.error("SlavePool.upgrade, cannot connect %s", prime);
			return;
		}
		// download and reload
		DataDownloader downloader = new DataDownloader();
		for (long chunkid : array) {
			// find local path
			byte[] path = Install.findChunkPath(db, table, chunkid);
			if (path == null || path.length == 0) {
				Logger.error("SlavePool.upgrade, cannot find '%s' - %x", space, chunkid);
				continue;
			}
			// delete chunk
			int ret = Install.deleteChunk(db, table, chunkid);
			boolean success = (ret == 0);
			Logger.note(success, "SlavePool.upgrade, delete chunk '%s' - %x, result code %d", space, chunkid, ret);
			// download new chunk
			if (success) {
				String filename = new String(path);
				success = downloader.execute(client, space, chunkid, filename);
				Logger.note(success, "SlavePool.upgrade, download '%s' - %s", space, filename);
			}
			// when success, reload chunk
			if (success) {
				ret = Install.loadChunk(db, table, path);
				success = (ret >= 0);
				Logger.note(success, "SlavePool.upgrade, load new chunk '%s' - %x, result code:%d", space, chunkid, ret);
			}
			// modify chunk rank
			if(success) {
				ret = Install.toSlave(db, table, chunkid);
				success = (ret == 0);
				Logger.note("SlavePool.upgrade, update chunk rank", success);
			}
		}
		// close client
		complete(client);
		
		// update index and relogin
		Launcher.getInstance().setUpdateModule(true);
	}
	
	private void upgradeAll(SiteHost host, Space space, long[] newIds) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		
		Logger.info("SlavePool.upgrade, space '%s' from %s", space, host);

		// connect data site(prime node) and update
		DataClient client = apply(host);
		if (client == null) {
			Logger.error("SlavePool.upgradeAll, cannot connect %s", host);
			return;
		}
		
		long G = 1024 * 1024 * 1024;
		// download and reload
		DataDownloader downloader = new DataDownloader();
		for (long chunkid : newIds) {
			byte[] path = null;
			for (int index = 0; true; index++) {
				byte[] b = Install.getChunkPath(db, table, index);
				if (b == null || b.length == 0) break;
				File root = new File(new String(b));
				if (root.exists() && root.isDirectory() && root.getFreeSpace() > G) {
					path = b;
					break;
				}
			}
			if (path == null) {
				Logger.warning("SlavePool.upgradeAll, disk space missing");
				return;
			}
			// download chunk
			String filename = buildFilename(new String(path), chunkid);
			boolean success = downloader.execute(client, space, chunkid, filename);
			Logger.note(success, "SlavePool.upgradeAll, download '%s' - %s", space, filename);
			// when success, reload chunk
			if (success) {
				byte[] b = filename.getBytes();
				int ret = Install.loadChunk(db, table, b);
				success = (ret >= 0);
				Logger.note(success, "SlavePool.upgradeAll, load new chunk '%s' - %x, result code:%d", space, chunkid, ret);
			}
			// modify chunk rank
			if(success) {
				int ret = Install.toSlave(db, table, chunkid);
				success = (ret == 0);
				Logger.note("SlavePool.upgradeAll, update chunk rank", success);
			}
		}
		// close client
		complete(client);
		
		// update index and relogin
		Launcher.getInstance().setUpdateModule(true);

	}
	
	/**
	 * upload chunk to other site
	 * @param request
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	public boolean upload(Stream request, OutputStream resp) throws IOException {
		// chunk identity
		Message msg = request.findMessage(Key.CHUNK_ID);
		if (msg == null) return false;
		long chunkId = msg.longValue();
		// chunk resume breakpoint
		long breakpoint = 0L;
		msg = request.findMessage(Key.CHUNK_BREAKPOINT);
		if(msg != null) breakpoint = msg.longValue();
		// space
		msg = request.findMessage(Key.SCHEMA);
		if (msg == null) return false;
		String db = msg.stringValue();
		msg = request.findMessage(Key.TABLE);
		if (msg == null) return false;
		String table = msg.stringValue();
		Space space = new Space(db, table);
		Logger.info("SlavePool.upload, space '%s' chunkid:%x breakpoint:%d", space, chunkId, breakpoint);

		// find chunk and send to remote site
		DataUploader uploader = new DataUploader();
		byte[] b = Install.findChunkPath(db.getBytes(), table.getBytes(), chunkId);
		// when error, send a null
		if (b == null || b.length == 0) {
			Logger.error("SlavePool.upload, cannot find '%s' path", space);
			uploader.execute(space, chunkId, 0, null, resp);
			return false;
		}
		String filename = new String(b);
		boolean success = uploader.execute(space, chunkId, breakpoint, filename, resp);
		Logger.note(success, "SlavePool.upload, send '%s'", filename);
		return success;
	}
}