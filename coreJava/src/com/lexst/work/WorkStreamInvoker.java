/**
 * 
 */
package com.lexst.work;

import java.io.*;

import com.lexst.algorithm.aggregate.*;
import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.*;
import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.log.client.*;
import com.lexst.remote.*;

public class WorkStreamInvoker implements StreamInvoker {

	private TaskTrigger trigger;

	/**
	 * default constructor
	 */
	public WorkStreamInvoker() {
		super();
	}

	/**
	 * @param instance
	 */
	public WorkStreamInvoker(TaskTrigger instance) {
		this();
		this.setTrigger(instance);
	}
	
	public void setTrigger(TaskTrigger instance) {
		this.trigger = instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.invoke.StreamInvoker#invoke(com.lexst.fixp.Stream,
	 * java.io.OutputStream)
	 */
	@Override
	public void invoke(Stream request, OutputStream output) throws IOException {
		Stream resp = null;

		Command cmd = request.getCommand();
		byte major = cmd.getMajor();
		byte minor = cmd.getMinor();

		if (major == Request.SQL && minor == Request.SQL_DC) {
			resp = dc(request);
		} else if (major == Request.SQL && minor == Request.SQL_ADC) {
			resp = adc(request);
		}

		if (resp != null) {
			byte[] b = resp.build();
			output.write(b, 0, b.length);
			output.flush();
		}
	}

	/**
	 * "dc" command
	 * 
	 * @param request
	 * @return
	 */
	private Stream dc(Stream request) {
		DCPair object = new DCPair(request);
		// execute "dc"
		this.trigger.dc(object);
		// waiting...
		object.waiting();

		Stream resp = (Stream) object.getResponse();
		if (resp == null) {
			Command cmd = new Command(Response.NOTFOUND);
			resp = new Stream(cmd);
		}
		
		Logger.debug("WorkStreamInvoker.dc, response code:%d", resp.getCommand().getResponse());
		
		return resp;
	}

	/**
	 * "adc" command
	 * 
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private Stream adc(Stream request) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (int index = 0; true; index++) {
			byte[] b = request.findBinary(Key.ADC_OBJECT, index);
			if (b == null) break;
			buff.write(b, 0, b.length);
		}

		ADC adc = null;
		try {
			Apply apply = Apply.resolve(buff.toByteArray());
			adc = (ADC) apply.getParameters()[0];
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		}
		if(adc == null) {
			// this is error
			return new Stream(new Command(Response.DC_SERVERERR));
		}

		byte[] data = request.readContent();
		DCTable table = new DCTable();
		table.resolve(data, 0, data.length);
		
		// "adc" command
		Stream resp = (Stream)this.trigger.adc(adc, table, true);
		if(resp == null) {
			Command cmd = new Command(Response.NOTFOUND);
			resp = new Stream(cmd);
		}
		return resp;
	}
}