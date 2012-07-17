/**
 * 
 */
package com.lexst.remote.client.work;

import java.io.*;
import java.util.*;

import com.lexst.log.client.*;
import com.lexst.util.lock.*;
import com.lexst.db.statement.*;

public class WorkDelegate {
	
	private SingleLock lock = new SingleLock();

	private int client_size;
	private ByteArrayOutputStream buff = new ByteArrayOutputStream(1048576);
	private ArrayList<WorkTask> array = new ArrayList<WorkTask>();
	
	/**
	 * 
	 */
	public WorkDelegate(int capacity) {
		super();
		if(capacity < 1) capacity = 10;
		client_size = 0;
		array.ensureCapacity(capacity);
	}
	
	public WorkDelegate() {
		this(10);
	}
	
	public void add(WorkClient client, DC dc, byte[] data) {
		array.add(new WorkTask(client, dc, data));
	}

	public void add(WorkClient client, ADC adc, byte[] data) {
		array.add(new WorkTask(client, adc, data));
	}
	
	public int size() {
		return array.size();
	}
	
	private synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {
			exp.printStackTrace();
		}
	}

	private synchronized void wakeup() {
		try {
			this.notify();
		} catch (IllegalMonitorStateException exp) {
			exp.printStackTrace();
		}
	}

	public void flushTo(byte[] b, int off, int len) {
		lock.lock();
		try {
			client_size--;
			if (b != null && len > 0) {
				buff.write(b, off, len);
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			lock.unlock();
		}
		if (client_size < 1) {
			this.wakeup();
		}
	}
	
	public byte[] data() {
		if (buff.size() == 0) return null;
		return buff.toByteArray();
	}
	
	/**
	 * stop all client
	 * 
	 * @param release
	 */
	public void discontinue(boolean release) {
		for (WorkTask task : array) {
			if (release) {
				if (task.client.isRunning()) {
					task.client.stop(); // exit thread
				} else {
					task.client.close();
				}
			} else {
				task.client.unlock();
			}
		}
	}
	
	public void execute() {
		client_size = array.size();
		for (WorkTask task : array) {
			switch (task.object.getMethod()) {
			case BasicObject.DC_METHOD:
				task.client.dc(this, (DC) task.object, task.data);
				break;
			case BasicObject.ADC_METHOD:
				task.client.adc(this, (ADC) task.object, task.data);
				break;
			}
		}
	}

	/**
	 * wait job
	 */
	public void waiting() {
		while(client_size > 0) {
			this.delay(10);
		}
	}
}