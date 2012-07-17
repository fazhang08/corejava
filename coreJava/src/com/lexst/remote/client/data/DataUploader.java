/**
 * 
 */
package com.lexst.remote.client.data;

import java.io.*;
import java.util.zip.*;

import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.util.*;
import com.lexst.util.datetime.*;

public class DataUploader {
	
	/**
	 * 
	 */
	public DataUploader() {
		super();
	}
	
	/**
	 * build a stream
	 * @param code
	 * @return
	 */
	private Stream build(short code) {
		Command cmd = new Command(code);
		Stream reply = new Stream(cmd);
		return reply;
	}

	/**
	 * flush all 
	 * @param code
	 * @param resp
	 * @throws IOException
	 */
	private void flush(short code, OutputStream resp) throws IOException {
		Stream reply = build(code);
		byte[] b = reply.build();
		resp.write(b, 0, b.length);
		resp.flush();
	}
	
	/**
	 * execute upload
	 * @param space
	 * @param chunkId
	 * @param filename (disk file)
	 * @param resp
	 * @return
	 * @throws IOException
	 */
	public boolean execute(Space space, long chunkId, long breakpoint, String filename, OutputStream resp) throws IOException {
		File file = null;
		if (filename != null) {
			file = new File(filename);
		}
		if (file == null || !file.exists()) {
			flush(Response.NOTFOUND, resp);
			return false;
		}
//		long length = file.length();
		long length = Install.filesize(filename.getBytes());
		if (length < 0) {
			flush(Response.CHUNK_SIZEOUT, resp);
			return false;
		}
		if (breakpoint < 0 || breakpoint >= length) {
			flush(Response.CHUNK_SIZEOUT, resp);
			return false;
		}
		long modified = file.lastModified();
		modified = SimpleTimeStamp.format(new java.util.Date(modified));
		
		Stream reply = build(Response.ACCEPTED);
		reply.addMessage(new Message(Key.CHUNK_ID, chunkId));
		reply.addMessage(new Message(Key.CONTENT_TYPE, Value.CHUNK_DATA));
		reply.addMessage(new Message(Key.CHUNK_LENGTH, length));
		reply.addMessage(new Message(Key.CHUNK_BREAKPOINT, breakpoint));
		reply.addMessage(new Message(Key.CHUNK_LASTMODIFIED, modified));
		reply.addMessage(new Message(Key.SCHEMA, space.getSchema()));
		reply.addMessage(new Message(Key.TABLE, space.getTable()));
		
		// send stream head
		byte[] head = reply.build();
		resp.write(head, 0, head.length);
		resp.flush();

		// send stream body
		CRC32 checker = new CRC32();
		final int len = 10240;
		for (long fileoff = breakpoint; fileoff < length;) {
			// JNI read
			byte[] data = Install.read(filename.getBytes(), fileoff, len);
			int size = ((data == null || data.length == 0) ? 0 : data.length);
			if(size < 1) break;
			fileoff += size;

			checker.reset();
			checker.update(data, 0, size);
			long sum = checker.getValue();

			// send verify head
			byte[] b = Numeric.toBytes(size);
			resp.write(b, 0, b.length);
			b = Numeric.toBytes(sum);
			resp.write(b, 0, b.length);
			// send data
			resp.write(data, 0, size);
		}
		resp.flush();

		return true;
	}

}