/**
 * 
 */
package com.lexst.remote.client.data;

import java.io.*;
import java.util.zip.*;

import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.util.*;
import com.lexst.util.datetime.*;
import com.lexst.util.host.*;


public class DataDownloader {

	/**
	 * 
	 */
	public DataDownloader() {
		super();
	}
	
	private synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {
			
		}
	}

	/**
	 * @param address
	 * @return
	 */
	private DataClient apply(SocketHost address) {
		DataClient client = new DataClient(true, address);
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

	/**
	 * @param client
	 */
	private void complete(DataClient client) {
		if(client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
	}

	/**
	 * @param host
	 * @param space
	 * @param chunkId
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public boolean execute(SocketHost host, Space space, long chunkId, String filename) {
		DataClient client = this.apply(host);
		if (client == null) {
			return false;
		}
		// execute download
		boolean success = execute(client, space, chunkId, filename);
		// exit and close socket
		this.complete(client);
		return success;
	}

	/**
	 * @param host
	 * @param space
	 * @param chunkId
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public boolean execute(SiteHost host, Space space, long chunkId, String filename) {
		return this.execute(host.getTCPHost(), space, chunkId, filename);
	}

	/**
	 * execute download
	 * @param client
	 * @param space
	 * @param chunkId
	 * @param filename
	 * @return
	 */
	public boolean execute(DataClient client, Space space, long chunkId, String filename) {
		long breakpoint = 0L;
		// checksum and data
		byte[] head = new byte[12];
		byte[] data = new byte[10240];
		CRC32 checker = new CRC32();

		boolean success = false;
		while(!success) {
			Logger.info("DataDownloader.execute, from %s donwload '%s' - %x - %d to %s", 
				client.getRemote(),	space, chunkId, breakpoint, filename);
			// connect data site
			Stream reply = connect(client, space, chunkId, breakpoint);
			if (reply == null) {
				this.complete(client);
				this.delay(2000);
				continue;
			}

			Command cmd = reply.getCommand();
			int code = cmd.getResponse();
			if (code != Response.ACCEPTED) {
				Logger.error("DataDownloader.execute, cannot accpeted! code %d", code);
				return false;
			}

			// chunk identity
			Message msg = reply.findMessage(Key.CHUNK_ID);
			if (msg == null) return false;
			long cid = msg.longValue();
			// chunk size
			msg = reply.findMessage(Key.CHUNK_LENGTH);
			if (msg == null) return false;
			long length = msg.longValue();
			// chunk resume point
			msg = reply.findMessage(Key.CHUNK_BREAKPOINT);
			if(msg == null) return false;
			long resume = msg.longValue();
			// chunk last modified date
			msg = reply.findMessage(Key.CHUNK_LASTMODIFIED);
			if(msg == null) return false;
			long modified = msg.longValue(); 
			java.util.Date date = SimpleTimeStamp.format(modified);
			modified = date.getTime();
			msg = reply.findMessage(Key.SCHEMA);
			if (msg == null) return false;
			String db = msg.stringValue();
			msg = reply.findMessage(Key.TABLE);
			if (msg == null) return false;
			String table = msg.stringValue();
			// check space, chunkid, breakpoint
			Space sp = new Space(db, table);
			if (!space.equals(sp) || chunkId != cid || breakpoint != resume) {
				Logger.error("DataDownloader.execute, not match! [%s - %s] [%x - %x] [%d - %d]",
						space, sp, chunkId, cid, breakpoint, resume);
				return false;
			}

			// save to disk
			try {
				while (breakpoint < length) {
					// read head
					reply.readFull(head, 0, head.length);
					int size = Numeric.toInteger(head, 0, 4);
					long sum = Numeric.toLong(head, 4, 8);
					if(size > data.length) {
						data = null;
						data = new byte[size];
					}
					// read data field
					reply.readFull(data, 0, size);
					// checksum
					checker.reset();
					checker.update(data, 0, size);
					long checksum = checker.getValue();
					if (sum != checksum) {
						Logger.error("DataDownloader.execute, checksum error! [%x-%x]", sum, checksum);
						break;
					}
					// JNI write
					Install.append(filename.getBytes(), data, 0, size);
					
					breakpoint += size;
				}
			} catch (IOException exp) {
				Logger.error(exp);
			}

			Logger.debug("DataDownloader.execute, download finished! breakpoint:%d, length:%d", breakpoint, length);

			success = (breakpoint >= length);
			
			// when success, set last-modified and exit
			if(success) {
				File file = new File(filename);
				file.setLastModified(modified);
			} else { // if error, retry
				this.complete(client);
				this.delay(2000);
			}
		}

		return success;
	}

	public Stream connect(DataClient client, Space space, long chunkId, long breakpoint)  {
		Stream reply = null;
		try {
			reply = client.download(space, chunkId, breakpoint);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return reply;
	}
}