/**
 *
 */
package com.lexst.live;

import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.invoke.*;
import com.lexst.log.client.*;
import com.lexst.util.host.*;

public class LivePacketInvoker implements PacketInvoker {

	private IPacketListener packetListener;
	
	private LiveListener liveListener;

	/**
	 * @param listener
	 */
	public LivePacketInvoker(IPacketListener listener) {
		super();
		this.packetListener = listener;
	}
	
	public LivePacketInvoker(LiveListener listener, IPacketListener reply) {
		super();
		this.liveListener = listener;
		this.packetListener = reply;
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.PacketCall#invoke(com.lexst.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		Command cmd = packet.getCommand();
		if (cmd.isRequest()) {
			return this.apply(packet);
		} else if (cmd.isResponse()) {
			return this.reply(packet);
		}
		return null;
	}

	/**
	 * apply a job
	 * @param packet
	 * @return
	 */
	private Packet apply(Packet packet) {
		Packet resp = null;

		Command cmd = packet.getCommand();
		if (cmd.isShutdown()) {
			shutdown(packet);
		} else if(cmd.isComeback()) {
//			launcher.comeback();
		}

		return resp;
	}

	/**
	 * reply a job
	 * @param packet
	 * @return
	 */
	private Packet reply(Packet packet) {
		Command cmd = packet.getCommand();
		if(cmd.getResponse() == Response.LIVE_ISEE) {
			Logger.debug("LivePacketInvoker.reply, LIVE ISEE Command!");
			// 通知窗口启动TOP图标闪烁
			liveListener.flicker();
		}
		return null;
	}

	private void shutdown(Packet request) {
		SocketHost remote = request.getRemote();
		for (int i = 0; i < 3; i++) {
			Command cmd = new Command(Response.OKAY);
			Packet resp = new Packet(cmd);
			resp.addMessage(Key.SPEAK, "goodbye!");
			// send shutdown reply
			packetListener.send(remote, resp);
		}
		// shutdown service
		liveListener.shutdown();
	}
}