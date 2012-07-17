/**
 * 
 */
package com.lexst.algorithm.aggregate;

import com.lexst.fixp.Entity;
import com.lexst.db.statement.ADC;
import com.lexst.db.statement.dc.DCTable;

public interface TaskTrigger {

	boolean removeTask(long identity);

	void dc(DCPair object);

	Entity adc(ADC adc, DCTable table, boolean stream);
}