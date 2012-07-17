/**
 * 
 */
package com.lexst.remote.client.data;

import com.lexst.db.statement.*;

final class DataTask {

	protected DataClient client;

	protected BasicObject object;

	/**
	 * @param client
	 * @param object
	 */
	public DataTask(DataClient client, BasicObject object) {
		this.client = client;
		this.object = object;
	}

	public void release() {
		client = null;
		object = null;
	}
}