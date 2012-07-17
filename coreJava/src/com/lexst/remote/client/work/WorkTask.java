/**
 * 
 */
package com.lexst.remote.client.work;

import com.lexst.db.statement.*;

final class WorkTask {

	protected WorkClient client;

	protected BasicObject object;

	protected byte[] data;

	/**
	 * @param client
	 * @param object
	 */
	public WorkTask(WorkClient client, BasicObject object, byte[] data) {
		this.client = client;
		this.object = object;
		this.data = data;
	}

	public WorkClient getClient() {
		return this.client;
	}

	public BasicObject getObject() {
		return this.object;
	}

	public byte[] getData() {
		return this.data;
	}
}
