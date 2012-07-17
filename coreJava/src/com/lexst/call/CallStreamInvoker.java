/**
 * 
 */
package com.lexst.call;

import java.io.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;


public class CallStreamInvoker implements StreamInvoker {
	
	/**
	 * defalut constructor
	 */
	public CallStreamInvoker() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.invoke.StreamCall#invoke(com.lexst.fixp.Stream,
	 * java.io.OutputStream)
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
		if (resp != null) {
			byte[] b = resp.build();
			output.write(b, 0, b.length);
			output.flush();
		}
	}

	private Stream apply(Stream request) throws IOException {

		return null;
	}

	private Stream reply(Stream resp) throws IOException {

		return null;
	}
}