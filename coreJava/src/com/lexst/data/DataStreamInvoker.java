/**
 *
 */
package com.lexst.data;

import java.io.*;
import java.util.zip.*;

import com.lexst.data.pool.*;
import com.lexst.db.statement.*;
import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.log.client.*;
import com.lexst.remote.*;
import com.lexst.util.host.*;

public class DataStreamInvoker implements StreamInvoker {

	/**
	 *
	 */
	public DataStreamInvoker() {
		super();
	}
	
	/**
	 * build response packet
	 * @param code
	 * @return
	 */
	private Stream buildResp(SocketHost remote, short code) {
		Command cmd = new Command(code);
		return new Stream(remote, cmd);
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.StreamCall#invoke(com.lexst.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	public void invoke(Stream request, OutputStream resp)
			throws IOException {
		Command cmd = request.getCommand();
		byte major = cmd.getMajor();
		byte minor = cmd.getMinor();

		switch(major) {
		case Request.LOGIN:
		case Request.LOGOUT:
		case Request.NOTIFY:
		case Request.RPC:
			break; //以上都不执行
		case Request.DATA:
			switch (minor) {
			case Request.DOWNLOAD_CHUNK:
				if (PrimePool.getInstance().isRunning()) {
					PrimePool.getInstance().upload(request, resp);
				} else if (SlavePool.getInstance().isRunning()) {
					SlavePool.getInstance().upload(request, resp);
				}
				break;
			}
			break;
		case Request.SQL:	//可以执行
			switch (minor) {
			case Request.SQL_CREATE_SCHEMA:
				break;
			case Request.SQL_CREATETABLE:
				break;
			case Request.SQL_SELECT:
				this.select(request, resp); break;
			case Request.SQL_DC:
				this.dc(request, resp); break;
			case Request.SQL_ADC:
				this.adc(request, resp); break;
			case Request.SQL_DELETE:
				this.delete(request, resp); break;
			case Request.SQL_INSERT:
				this.insert(request, resp); break;
			case Request.SQL_UPDATE:
				break; //不执行
			}
		}
	}
	
	private Apply resolve(byte[] b) {
		Apply apply = null;
		try {
			apply = Apply.resolve(b);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return apply;
	}
	
	/**
	 * execute SQL "select" command
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private void select(Stream request, OutputStream resp) throws IOException {
		Logger.debug("DataStreamInvokder.select, site from %s", request.getRemote());
		
		byte[] query = request.readContent();
		Apply apply = resolve(query);
		Select select = (Select) apply.getParameters()[0];
		// query
		SQLPool.getInstance().select(select, resp);
	}

	/**
	 * execute SQL "dc" command
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private void dc(Stream request, OutputStream resp) throws IOException {
		Logger.debug("DataStreamInvoker.dc, site from %s", request.getRemote());

		byte[] data = request.readContent();
		Apply apply = this.resolve(data);
		DC dc = (DC)apply.getParameters()[0];
		SQLPool.getInstance().dc(dc, resp);
	}
	
	/**
	 * execute SQL "adc" command
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private void adc(Stream request, OutputStream resp) throws IOException {
		Logger.debug("DataStreamInvoker.adc, site from %s", request.getRemote());
		
		byte[] data = request.readContent();
		Apply apply = this.resolve(data);
		ADC adc = (ADC)apply.getParameters()[0];
		SQLPool.getInstance().adc(adc, resp);
	}

	/**
	 * execute SQL "delete" method
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private void delete(Stream request, OutputStream resp) throws IOException {
		Logger.debug("DataStreamInvokder.delete, site from %s", request.getRemote());
		
		byte[] query = request.readContent();
		Apply apply = this.resolve(query);
		Delete dele = (Delete)apply.getParameters()[0];
		// delete data
		SQLPool.getInstance().delete(dele, resp);
	}

	/**
	 * execute SQL "insert" method
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private void insert(Stream request, OutputStream resp) throws IOException {
		byte[] data = request.readContent();
		
		CRC32 checksum = new CRC32();
		checksum.update(data, 0, data.length);
		long sum = checksum.getValue();

		Message msg = request.findMessage(Key.CHECKSUM_CRC32);
		if(msg != null) {
			long value = msg.longValue();
			if(sum != value) { // this is error
				Logger.error("DataStreamInvoker.insert, data len:%d, checksum error: %d - %d", data.length, sum, value);
				Stream reply = buildResp(request.getRemote(), Response.DATA_CHECKSUM_ERROR);
				byte[] b = reply.build();
				resp.write(b, 0, b.length);
				resp.flush();
				return;
			}
		}
		
		boolean sync = true;
		msg = request.findMessage(Key.INSERT_MODE);
		if(msg != null) {
			int value = msg.intValue();
			sync = (value == Value.INSERT_SYNC);
		}
		
		Logger.debug("DataStreamInvoker.insert, from %s, CRC32 %d, data len %d", request.getRemote(), sum, data.length);
	
		// insert to disk
		SQLPool.getInstance().insert(data, sync, resp);
	}

}