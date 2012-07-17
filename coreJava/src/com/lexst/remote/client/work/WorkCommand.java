/**
 * 
 */
package com.lexst.remote.client.work;

import com.lexst.db.statement.*;

final class WorkCommand {

	protected WorkDelegate delegate;

	protected BasicObject object;
	
	protected byte[] data;

	/**
	 * @param delegate
	 * @param object
	 */
	public WorkCommand(WorkDelegate delegate, BasicObject object, byte[] data) {
		this.delegate = delegate;
		this.object = object;
		this.data = data;
	}

	public WorkDelegate getDelegate() {
		return this.delegate;
	}

	public BasicObject getObject() {
		return this.object;
	}

	public byte[] getData() {
		return this.data;
	}
}
