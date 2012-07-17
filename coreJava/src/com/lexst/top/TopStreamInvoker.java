/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 5/12/2010
 * @see com.lexst.top
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.top;

import java.io.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.log.client.*;

public class TopStreamInvoker implements StreamInvoker {
	
	/**
	 * 
	 */
	public TopStreamInvoker() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.StreamInvoker#invoke(com.lexst.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	public void invoke(Stream request, OutputStream output) throws IOException {
		Stream resp = null;
		Command cmd = request.getCommand();
		if (cmd.isRequest()) {
			resp = this.apply(request);
		} else if (cmd.isResponse()) {
			resp = this.reply(request);
		}
		// send reply data
		if(resp != null) {
			byte[] b = resp.build();
			output.write(b, 0, b.length);
			output.flush();
		}
	}

	private Stream apply(Stream request) throws IOException {
		Stream resp = null;
		Command cmd = request.getCommand();
		switch(cmd.getMajor()) {
		case Request.SQL:
//			resp = sqlCall.invoke(request);
			break;
		case Request.APP:
			Logger.debug("TopStreamInvoker.apply, APP command");
//			resp = methodCall.invoke(request); 
			break;
		case Request.LOGIN:
			Logger.debug("TopStreamInvoker.apply, LOGIN command");
//			resp = methodCall.login(request); break;
		case Request.LOGOUT:
			Logger.debug("TopStreamInvoker.apply, LOGOUT command");
//			resp = methodCall.logout(request); break;
		default:
			throw new FixpProtocolException("undefine request");
		}
		return resp;
	}

	private Stream reply(Stream stream) throws IOException {
		Stream resp = null;
		Command cmd = stream.getCommand();
		if (cmd.getResponse() == Response.ISEE) {
			Logger.debug("TopStreamInvokder.reply, ISEE Command!");
		} else {
			throw new FixpProtocolException("undefine response");
		}
		return resp;
	}
}