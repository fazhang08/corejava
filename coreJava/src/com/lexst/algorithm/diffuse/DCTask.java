/**
 * 
 */
package com.lexst.algorithm.diffuse;

import com.lexst.db.statement.*;

public abstract class DCTask extends DiffuseTask {

	/**
	 * default
	 */
	public DCTask() {
		super();
	}

	/**
	 * execute "dc" command
	 * 
	 * @param dc
	 * @param data
	 * @return
	 */
	public abstract DCResult[] execute(DC dc, byte[] data);

}