/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 lexst.com. All rights reserved
 * 
 * dc and adc basic class
 * 
 * @author scott.liu lexst@126.com
 * 
 * @version 1.0 6/12/2011
 * 
 * @see com.lexst.db.statement
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.statement;

public class ADC extends BasicComputing {

	private static final long serialVersionUID = 7132141428913005815L;

	private int from_allsite;

	private int from_index;
	
	/**
	 * 
	 */
	public ADC() {
		super(BasicObject.ADC_METHOD);
		from_allsite = from_index = 0;
	}

	public void defineFromSites(int i) {
		this.from_allsite = i;
	}

	public int getDefineFromSites() {
		return this.from_allsite;
	}

	public void defineFromIndex(int i) {
		this.from_index = i;
	}

	public int getDefineFromIndex() {
		return this.from_index;
	}

	/**
	 * copy object
	 */
	public ADC clone() {
		ADC adc = new ADC();
		super.set(adc);
		adc.from_allsite = this.from_allsite;
		adc.from_index = this.from_index;
		return adc;
	}
}
