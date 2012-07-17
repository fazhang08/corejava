/**
 * 
 */
package com.lexst.remote.client.data;

import com.lexst.db.statement.*;

final class DataCommand {

	protected DataDelegate delegate;

	protected BasicObject object;

	/**
	 * @param delegate
	 * @param object
	 */
	public DataCommand(DataDelegate delegate, BasicObject object) {
		this.delegate = delegate;
		this.object = object;
	}

}
