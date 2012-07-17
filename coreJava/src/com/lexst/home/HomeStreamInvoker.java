/**
 *
 */
package com.lexst.home;

import java.io.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.thread.*;

public class HomeStreamInvoker implements StreamInvoker {


	/**
	 *
	 */
	public HomeStreamInvoker() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.StreamCall#invoke(com.lexst.fixp.Stream, java.io.OutputStream)
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
		if(resp != null) {
			byte[] b = resp.build();
			output.write(b, 0, b.length);
			output.flush();
		}
	}

	private Stream apply(Stream request) throws IOException {
		Stream resp = null;

		return resp;
	}

	private Stream reply(Stream packet) throws IOException {
//		Logger.debug("HomeStreamInvoker.reply");

		Command cmd = packet.getCommand();
		short code = cmd.getResponse();
		switch (code) {
		case Response.HOME_ISEE:
//			Logger.debug("HomeStreamInvoker.reply, ISEE Command!");
			Launcher.getInstance().refreshEndTime();
			break;
		case Response.NOTLOGIN:
			Launcher.getInstance().setOperate(BasicLauncher.LOGIN);
			break;
		}

		/*
		if (cmd.getResponse() == Response.ISEE) {
			Logger.debug("HomeStreamInvoker.reply, ISEE Command!");
		} else if(cmd.)
		*/

		return null;
	}
}