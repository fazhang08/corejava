/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;
import java.util.*;

import com.lexst.algorithm.diffuse.*;
import com.lexst.log.client.*;
import com.lexst.db.statement.dc.DCField;

public class DiskPool extends LocalPool implements ADCTrustor {
	
	class Area {
		private long identity;

		private List<DCField> array = new ArrayList<DCField>();

		/**
		 * 
		 */
		public Area() {
			super();
		}

		public Area(long identity) {
			this();
			this.setIdentity(identity);
		}

		/**
		 * set job identity
		 * 
		 * @param i
		 */
		public void setIdentity(long i) {
			this.identity = i;
		}

		/**
		 * get job identity
		 * 
		 * @return
		 */
		public long getIdentity() {
			return this.identity;
		}

		public boolean add(DCField field) {
			if (array.contains(field))
				return true;
			return array.add(field);
		}

		public boolean remove(DCField filed) {
			return array.remove(filed);
		}

		public boolean isEmpty() {
			return array.isEmpty();
		}

		public int size() {
			return array.size();
		}

		@Override
		public boolean equals(Object arg) {
			if (arg == null || !(arg instanceof Area)) {
				return false;
			} else if (arg == this) {
				return true;
			}

			Area a = (Area) arg;
			return identity == a.identity;
		}

		@Override
		public int hashCode() {
			return (int) (identity >>> 32 & identity);
		}
	}
	

	private static DiskPool selfHandle = new DiskPool();

	/* job identity, default is 0 */
	private long identity;

	/* job identity -> dc area */
	private Map<Long, Area> mapArea = new TreeMap<Long, Area>();
	
	/* job identity -> last time */
	private Map<Long, Long> mapTime = new TreeMap<Long, Long>();

	/* adc data directory */
	private String path;

	/* adc service timeout */
	private long timeout;
	/**
	 * 
	 */
	private DiskPool() {
		super();
		this.identity = 0;
		path = "";
		this.setTimeout(30 * 60 * 1000);
	}

	/**
	 * @return
	 */
	public static DiskPool getInstance() {
		return DiskPool.selfHandle;
	}

	/**
	 * create a directory
	 * 
	 * @param s
	 * @return
	 */
	public boolean setPath(String s) {
		char c = File.separatorChar;
		s = s.replace('\\', c);
		s = s.replace('/', c);
		if (s.charAt(s.length() - 1) != c) s += c;

		this.path = s;

		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			return true;
		}
		boolean success = file.mkdirs();
		Logger.note(success, "DiskPool.setPath, create path '%s'", path);
		return success;
	}

	public String getPath() {
		return this.path;
	}
	
	public void setTimeout(long millisecond) {
		this.timeout = millisecond;
	}
	
	public long getTimeout() {
		return this.timeout;
	}
	
	private String format(long jobid) {
		StringBuilder b = new StringBuilder();
		b.append(String.format("%x", jobid));
		while (b.length() < 16) {
			b.insert(0, '0');
		}

		return path + b.toString() + ".adc";
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.algorithm.diffuse.ADCTrustor#nextIdentity()
	 */
	@Override
	public synchronized long nextIdentity() {
		if (identity >= Long.MAX_VALUE) identity = 0;
		return identity++;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.data.dc.ADCListener#write(long, int, byte[], int, int)
	 */
	@Override
	public long[] write(long jobid, int mod, byte[] data, int off, int len) {
		String filename = format(jobid);
		
		super.lockSingle();
		try {
			long begin = 0;
			File file = new File(filename);
			if (file.exists() && file.isFile()) begin = file.length();

			// write to disk
			FileOutputStream out = new FileOutputStream(file);
			out.write(data, off, len);
			out.close();

			file = new File(filename);
			long end = file.length();

			DCField field = new DCField(mod, begin, end);
			Area area = mapArea.get(jobid);
			if (area == null) {
				area = new Area(jobid);
				mapArea.put(jobid, area);
			}
			area.add(field);
			// save time
			mapTime.put(jobid, System.currentTimeMillis() + timeout);
			
			return new long[] { begin, end};
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		return null; // failed
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.data.dc.ADCListener#read(long, int, long, long)
	 */
	@Override
	public byte[] read(long jobid, int mod, long begin, long end) {
		String filename = format(jobid);
		byte[] b = null;

		super.lockSingle();
		try {
			// check file
			File file = new File(filename);
			if (!file.exists() || !file.isFile()) return null;
			if (file.length() < begin || file.length() < end) return null;

			DCField field = new DCField(mod, begin, end);
			Area area = mapArea.get(jobid);
			if (area == null) return null;
			boolean success = area.remove(field);
			if (!success) return null;

			int size = (int) field.length();
			b = new byte[size];

			FileInputStream in = new FileInputStream(file);
			in.read(b, 0, b.length);
			in.close();

			if (area.isEmpty()) {
				mapArea.remove(jobid);
				mapTime.remove(jobid);
				file.delete(); // delete disk file
			}
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}

		return b;
	}

	/**
	 * clear timeout adc file
	 */
	private void check() {
		int size = mapTime.size();
		if (size == 0) return;

		ArrayList<Long> array = new ArrayList<Long>(size);
		long now = System.currentTimeMillis();

		super.lockSingle();
		try {
			for (long jobid : mapTime.keySet()) {
				long time = mapTime.get(jobid);
				if (now >= time) array.add(jobid);
			}

			for (long jobid : array) {
				mapTime.remove(jobid);
				mapArea.remove(jobid);

				String filename = format(jobid);
				File file = new File(filename);
				if (file.exists() && file.isFile()) {
					file.delete();
					Logger.info("DiskPool.check, delete timeout file %s", filename);
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * clear all
	 */
	private void clear() {
		for(long jobid : mapArea.keySet()) {
			String filename = format(jobid);
			File file = new File(filename);
			if(file.exists() && file.isFile()) file.delete();
		}
		
		mapArea.clear();
		mapTime.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("DiskPool.process, into...");
		while (!isInterrupted()) {
			check();
			this.delay(5000);
		}
		Logger.info("DiskPool.process, exit");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.clear();
	}

}