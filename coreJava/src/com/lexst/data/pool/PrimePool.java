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
import com.lexst.remote.client.home.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

/**
 * 
 * 当CHUNK生成时，通知HOME节点。由HOME节点分配N个从节点，从节点下载CHUNK
 * 
 */
public class PrimePool extends LocalPool {

	private static PrimePool selfHandle = new PrimePool();

	/**
	 * 
	 */
	private PrimePool() {
		super();
	}

	/**
	 * @return
	 */
	public static PrimePool getInstance() {
		return PrimePool.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("PrimePool.process, into...");
		while (!super.isInterrupted()) {
			check();
			delay(10000);
		}
		Logger.info("PrimePool.process, exit");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}

	private void check() {
		ArrayList<PrimeChunk> array = new ArrayList<PrimeChunk>();

		while (true) {
			byte[] b = Install.nextFinishChunk();
			if (b == null || b.length == 0) {
				break;
			}
			// split chunk
			int off = 0;
			long chunkid = Numeric.toLong(b, off, 8);
			off += 8;

			int dbsz = b[off++] & 0xff;
			int tabsz = b[off++] & 0xff;
			int filesz = Numeric.toShort(b, off, 2);
			off += 2;

			String db = new String(b, off, dbsz);
			off += dbsz;
			String table = new String(b, off, tabsz);
			off += tabsz;
			String filename = new String(b, off, filesz);
			off += filesz;

			Space space = new Space(db, table);
			long length = 0L;
			File file = new File(filename);
			if (file.exists() && file.isFile()) {
				length = file.length();
			}
			
			PrimeChunk chunk = new PrimeChunk(space, chunkid, length);
			array.add(chunk);
			
			// notify cache pool, delete a backup
			CachePool.getInstance().delete(db, table, chunkid);
			
			Logger.info("PrimePool.check, save '%s' - %x - %d - %s", space, chunkid, length, filename);
		}

		if (array.isEmpty()) return;
		
		// refresh space, and relogin to home site
		Launcher.getInstance().setUpdateModule(true);

		// 通知HOME节点,分发以下CHUNK到从节点
		SiteHost home = Launcher.getInstance().getHome();
		HomeClient client = this.bring(home);
		if (client == null) {
			Logger.error("PrimePool.check, cannot connect to %s", home);
			return;
		}
		// notify home site
		SiteHost local = Launcher.getInstance().getLocal().getHost();
		for (PrimeChunk chunk : array) {
			Space space = chunk.getSpace();
			long chunkid = chunk.getChunkId();
			long length = chunk.getLength();
			try {
				boolean success = client.publish(local, space, chunkid, length);
				Logger.note(success, "PrimePool.check, publish '%s' - %x to %s", space, chunkid, home);
				continue;
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
		}
		// close client
		complete(client);
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
		Logger.info("PrimePool.upload, space '%s' chunkid:%x", space, chunkId);

		// find chunk and send to remote site
		DataUploader uploader = new DataUploader();
		byte[] b = Install.findChunkPath(db.getBytes(), table.getBytes(), chunkId);
		// when error, send a null
		if (b == null || b.length == 0) {
			Logger.error("PrimePool.upload, cannot find '%s' path", space);
			uploader.execute(space, chunkId, 0, null, resp);
			return false;
		}
		String filename = new String(b);
		boolean success = uploader.execute(space, chunkId, breakpoint, filename, resp);
		Logger.note(success, "PrimePool.upload, send '%s'", filename);
		return success;
	}
}