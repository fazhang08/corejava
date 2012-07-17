/**
 * 
 */
package com.lexst.algorithm.aggregate;

import com.lexst.db.statement.ADC;
import com.lexst.db.statement.dc.*;

public abstract class ADCTask extends AggregateTask {

	protected ADC adc;

	/**
	 * 
	 */
	public ADCTask() {
		super();
	}

	public void setADC(ADC obj) {
		this.adc = obj;
	}

	public ADC getADC() {
		return this.adc;
	}

	/**
	 * save data
	 * 
	 * @param field
	 * @param data
	 * @return
	 */
	public abstract boolean add(DCField field, byte[] data);

	/**
	 * execute "adc" command
	 */
	public abstract byte[] execute();
}