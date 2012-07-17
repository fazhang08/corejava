/**
 * 
 */
package com.lexst.log.server;

import java.io.IOException;
import java.io.OutputStream;

import com.lexst.fixp.Stream;
import com.lexst.invoke.StreamInvoker;

public class LogStreamInvoker implements StreamInvoker {

	/**
	 * 
	 */
	public LogStreamInvoker() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.StreamCall#invoke(com.lexst.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	public void invoke(Stream request, OutputStream response)
			throws IOException {

	}

}
