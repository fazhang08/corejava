/**
 *
 */
package com.lexst.live;

import java.io.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;

public class LiveStreamInvoker implements StreamInvoker {

	/**
	 *
	 */
	public LiveStreamInvoker() {
		super();
	}

	public String help() {
		InputStream in = getClass().getResourceAsStream("help.ini");
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		try {
			byte[] data = new byte[128];
			while(true) {
				int size = in.read(data, 0, data.length);
				if(size < 0) break;
				buff.write(data, 0, size);
			}
		} catch (IOException exp) {
			exp.printStackTrace();
		}
		byte[] data = buff.toByteArray();
		return new String(data, 0, data.length);
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.StreamCall#invoke(com.lexst.fixp.Stream, java.io.OutputStream)
	 */
	@Override
	public void invoke(Stream request, OutputStream response)
			throws IOException {
		// TODO Auto-generated method stub

	}

}