/**
 *
 */
package com.lexst.remote.client.data;

import java.io.*;
import java.util.*;

import com.lexst.db.statement.*;
import com.lexst.log.client.*;
import com.lexst.util.*;
import com.lexst.util.lock.*;

public final class DataDelegate {

	private SingleLock lock = new SingleLock();

	// all client
	private int client_size;
	// all row
	private long items;
	// data buffer
	private ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 1024);
	// client set
	private ArrayList<DataTask> array = new ArrayList<DataTask>();

	/**
	 * @param capacity : pre-client size
	 */
	public DataDelegate(int capacity) {
		super();
		if(capacity < 1) capacity = 10;
		array.ensureCapacity(capacity);
		items = 0;
		// pre padding size
		byte[] b = Numeric.toBytes(items);
		buff.write(b, 0, b.length);
	}

	/**
	 * sleep time
	 * @param timeout
	 */
	private synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {
			
		}
	}

	/**
	 * wakeup object
	 */
	private synchronized void wakeup() {
		try {
			this.notify();
		} catch (IllegalMonitorStateException exp) {
			
		}
	}

	/**
	 * @param client
	 * @param object
	 */
	public void add(DataClient client, BasicObject object) {
		array.add(new DataTask(client, object));
	}

	/**
	 * save all objects
	 * @param finder
	 */
	public void add(DataDelegate finder) {
		array.addAll(finder.array);
	}

	public int size() {
		return array.size();
	}
	
	/**
	 * real item count
	 * @return
	 */
	public long getItems() {
		return this.items;
	}

	/**
	 * data client flush to here
	 */
	public void flushTo(long elements, byte[] b, int off, int len) {
		lock.lock();
		try {
			client_size--;
			items += elements;
			if (b != null && len > 0) {
				buff.write(b, off, len);
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			lock.unlock();
		}
		
		if (client_size < 1) {
			wakeup();
		}
	}

	/**
	 * stop all client
	 * 
	 * @param release
	 */
	public void discontinue(boolean release) {
		for (DataTask task : array) {
			if (release) {
				if (task.client.isRunning()) {
					task.client.stop(); // exit thread
				} else {
					task.client.close();
				}
			} else {
				task.client.unlock();
			}
			// clear resource
			task.release();
		}
	}

	/**
	 * launch jobs
	 */
	public void execute() {
		client_size = array.size();
		for (DataTask task : array) {
			switch (task.object.getMethod()) {
			case BasicObject.SELECT_METHOD:
				task.client.select(this, (Select) task.object);
				break;
			case BasicObject.DC_METHOD:
				task.client.dc(this, (DC) task.object);
				break;
			case BasicObject.ADC_METHOD:
				task.client.adc(this, (ADC) task.object);
				break;
			}
			// clear resource
			task.release();
		}
	}

	/**
	 * wait finish
	 * @return
	 */
	public void waiting() {
		while (client_size > 0) {
			this.delay(20);
		}
	}

	/**
	 * @return
	 */
	public byte[] data() {
		byte[] b = Numeric.toBytes(items);
		byte[] data = buff.toByteArray();
		// update head size
		System.arraycopy(b, 0, data, 0, b.length);
		// flush data
		return data;
	}

}