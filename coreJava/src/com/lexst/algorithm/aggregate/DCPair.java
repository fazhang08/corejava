/**
 * 
 */
package com.lexst.algorithm.aggregate;

import com.lexst.fixp.*;

public class DCPair {

	/* request instance, stream or packet */
	private Entity request;

	/* response instance, stream or packet */
	private Entity resp;

	private boolean finished;

	/**
	 * 
	 */
	protected DCPair() {
		super();
		finished = false;
	}

	/**
	 * @param stream
	 */
	public DCPair(Stream stream) {
		this();
		request = stream;
	}

	/**
	 * @param packet
	 */
	public DCPair(Packet packet) {
		this();
		this.request = packet;
	}

	public boolean isStream() {
		return request != null && request.getClass() == Stream.class;
	}

	public boolean isPacket() {
		return request != null && request.getClass() == Packet.class;
	}

	public Entity getRequest() {
		return request;
	}

	public void setResponse(Entity entity) {
		this.resp = entity;
	}

	public Entity getResponse() {
		return this.resp;
	}

	private synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {

		}
	}

	public synchronized void finish() {
		finished = true;
		try {
			this.notify();
		} catch (IllegalMonitorStateException exp) {

		}
	}

	public void waiting() {
		while (!finished) {
			this.delay(20L);
		}
	}

}