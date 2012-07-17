/**
 * 
 */
package com.lexst.algorithm.diffuse;

import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.*;
import com.lexst.util.host.SiteHost;

public abstract class ADCTask extends DiffuseTask {

	/**
	 * 
	 */
	public ADCTask() {
		super();
	}

	/**
	 * @param trustor
	 * @param adc
	 * @param data
	 * @return
	 */
	public abstract DCArea[] execute(SiteHost host, ADCTrustor trustor, ADC adc, byte[] data);

}