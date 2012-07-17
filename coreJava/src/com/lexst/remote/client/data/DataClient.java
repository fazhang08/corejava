/**
 *
 */
package com.lexst.remote.client.data;

import java.io.*;
import java.lang.reflect.*;
import java.util.zip.*;

import com.lexst.db.chunk.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.data.*;

public class DataClient extends ThreadClient implements DataVisit {

	private static Method methodNothing;
	private static Method methodApplyRank;
	private static Method methodFindChunk;
	private static Method methodRevive;
	private static Method methodDistribute;
	private static Method methodUpgrade;
	private static Method methodFindTable;
	private static Method methodCreateSpace;
	private static Method methodDeleteSpace;
	
	private static Method methodOptimize;
	private static Method methodLoadIndex;
	private static Method methodStopIndex;
	private static Method methodLoadChunk;
	private static Method methodStopChunk;
	
	private static Method methodDownloadDCField;

	static {
		try {
			methodNothing = (DataVisit.class).getMethod("nothing", new Class<?>[0]);
			methodApplyRank = (DataVisit.class).getMethod("applyRank", new Class<?>[0]);
			methodFindChunk = (DataVisit.class).getMethod("findChunk", new Class<?>[] { String.class, String.class });
			methodDistribute = (DataVisit.class).getMethod("distribute", new Class<?>[] { SiteHost.class, String.class, String.class, Long.TYPE , Long.TYPE});
			methodUpgrade = (DataVisit.class).getMethod("upgrade", new Class<?>[] { SiteHost.class, String.class, String.class, long[].class, long[].class });
			methodRevive = (DataVisit.class).getMethod("revive", new Class<?>[] { String.class, String.class, SiteHost.class });
			methodFindTable = (DataVisit.class).getMethod("findIndex", new Class<?>[] { String.class, String.class });
			methodCreateSpace = (DataVisit.class).getMethod("createSpace", new Class<?>[] { Table.class });
			methodDeleteSpace = (DataVisit.class).getMethod("deleteSpace", new Class<?>[] { String.class, String.class });
			
			methodOptimize = (DataVisit.class).getMethod("optimize", new Class<?>[] { String.class, String.class });
			methodLoadIndex = (DataVisit.class).getMethod("loadIndex", new Class<?>[] { String.class, String.class });
			methodStopIndex = (DataVisit.class).getMethod("stopIndex", new Class<?>[] { String.class, String.class });
			methodLoadChunk = (DataVisit.class).getMethod("loadChunk", new Class<?>[] { String.class, String.class });
			methodStopChunk = (DataVisit.class).getMethod("stopChunk", new Class<?>[] { String.class, String.class });
			
			methodDownloadDCField = (DataVisit.class).getMethod("downloadDCField", new Class<?>[] { Long.TYPE, Integer.TYPE, Long.TYPE, Long.TYPE });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}
	
	// sql object array
	private LockArray<DataCommand> array = new LockArray<DataCommand>(10);
	
//	private LinkedList<DataCommand> array = new LinkedList<DataCommand>();

	/**
	 *
	 */
	public DataClient(boolean stream) {
		super(stream, DataVisit.class.getName());
		this.setRecvTimeout(180);
	}

	/**
	 * @param host
	 */
	public DataClient(boolean stream, SocketHost host) {
		this(stream);
		super.setRemote(host);
	}

	/**
	 * @param ip
	 * @param port
	 */
	public DataClient(boolean stream, String ip, int port) {
		this(stream, new SocketHost(SocketHost.TCP, ip, port));
	}

	/**
	 * @param remote
	 */
	public DataClient(SocketHost remote) {
		this(remote.getType() == SocketHost.TCP);
		super.setRemote(remote);
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		super.invoke(methodNothing, null);
		super.refreshTime();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#getRank()
	 */
	@Override
	public int applyRank() throws VisitException {
		super.refreshTime();
		Object param = super.invoke(DataClient.methodApplyRank, null);
		return ((Integer)param).intValue();
	}
	
	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public Chunk[] findChunk(Space space) throws VisitException {
		return findChunk(space.getSchema(), space.getTable());
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#findChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public Chunk[] findChunk(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(DataClient.methodFindChunk, params);
		return (Chunk[]) param;
	}
	
	/**
	 * @param from
	 * @param space
	 * @param chunkid
	 * @return
	 * @throws VisitException
	 */
	public boolean distribute(SiteHost from, Space space, long chunkid, long length) throws VisitException {
		return distribute(from, space.getSchema(), space.getTable(), chunkid, length);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#distribute(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, long, long)
	 */
	@Override
	public boolean distribute(SiteHost from, String db, String table,
			long chunkid, long length) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { from, db, table, new Long(chunkid), new Long(length) };
		Object param = super.invoke(methodDistribute, params);
		return ((Boolean) param).booleanValue();
	}
	
	/**
	 * @param from
	 * @param space
	 * @param oldIds
	 * @param newIds
	 * @return
	 * @throws VisitException
	 */
	public boolean upgrade(SiteHost from, Space space, long[] oldIds, long[] newIds) throws VisitException {
		return this.upgrade(from, space.getSchema(), space.getTable(), oldIds, newIds);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#upgrade(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, long[], long[])
	 */
	@Override
	public boolean upgrade(SiteHost from, String db, String table, long[] oldIds, long[] newIds) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { from, db, table, oldIds, newIds };
		Object param = super.invoke(methodUpgrade, params);
		return ((Boolean) param).booleanValue();
	}
	
	/**
	 * @param space
	 * @param from
	 * @return
	 * @throws VisitException
	 */
	public boolean revive(Space space, SiteHost from) throws VisitException {
		return revive(space.getSchema(), space.getTable(), from);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#revive(java.lang.String, java.lang.String, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean revive(String db, String table, SiteHost from) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table, from };
		Object param = super.invoke(methodRevive, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public IndexTable findIndex(Space space) throws VisitException {
		return findIndex(space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#findTable(java.lang.String, java.lang.String)
	 */
	@Override
	public IndexTable findIndex(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table};
		Object param = super.invoke(methodFindTable, params);
		return (IndexTable) param;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#createSpace(com.lexst.db.schema.Table)
	 */
	@Override
	public boolean createSpace(Table table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { table };
		Object param = super.invoke(methodCreateSpace, params);
		return ((Boolean)param).booleanValue();
	}
	
	/**
	 * delete a table space
	 * 
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean deleteSpace(Space space) throws VisitException {
		return deleteSpace(space.getSchema(), space.getTable());
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#removeSpace(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteSpace(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodDeleteSpace, params);
		return ((Boolean)param).booleanValue();
	}
	
	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean optimize(Space space) throws VisitException {
		return optimize(space.getSchema(), space.getTable());
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#optimize(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean optimize(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodOptimize, params);
		return ((Boolean) param).booleanValue();
	}
	
	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean loadIndex(Space space) throws VisitException {
		return loadIndex(space.getSchema(), space.getTable());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#loadIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean loadIndex(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodLoadIndex, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean stopIndex(Space space) throws VisitException {
		return stopIndex(space.getSchema(), space.getTable());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#stopIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean stopIndex(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodStopIndex, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean loadChunk(Space space) throws VisitException {
		return loadChunk(space.getSchema(), space.getTable());
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#loadChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean loadChunk(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodLoadChunk, params);
		return ((Boolean) param).booleanValue();
	}

	/**
	 * @param space
	 * @return
	 * @throws VisitException
	 */
	public boolean stopChunk(Space space) throws VisitException {
		return stopChunk(space.getSchema(), space.getTable());
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#stopChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean stopChunk(String db, String table) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { db, table };
		Object param = super.invoke(methodStopChunk, params);
		return ((Boolean) param).booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#downloadDCField(long, int, long, long)
	 */
	@Override
	public byte[] downloadDCField(long jobid, int mod, long begin, long end) throws VisitException {
		super.refreshTime();
		Object[] params = new Object[] { new Long(jobid), new Integer(mod), new Long(begin), new Long(end) };
		Object param = super.invoke(methodDownloadDCField, params);
		return (byte[])param;
	}

	/**
	 * download a chunk, from primary site
	 * @param space
	 * @param chunkId
	 * @return
	 * @throws IOException
	 */
	public Stream download(Space space, long chunkId, long breakpoint) throws IOException {
		Command cmd = new Command(Request.DATA, Request.DOWNLOAD_CHUNK);
		Stream request = new Stream(cmd);
		request.addMessage(new Message(Key.CHUNK_ID, chunkId));
		if (breakpoint > 0) request.addMessage(new Message(Key.CHUNK_BREAKPOINT, breakpoint));
		request.addMessage(new Message(Key.SCHEMA, space.getSchema()));
		request.addMessage(new Message(Key.TABLE, space.getTable()));
		return super.executeStream(request, false);
	}

	/**
	 * @param finder
	 * @param object
	 */
	public void select(DataDelegate finder, Select object) {
		array.add(new DataCommand(finder, object));
		this.wakeup();
	}

	/**
	 * @param finder
	 * @param object
	 */
	public void delete(DataDelegate finder, Delete object) {
		array.add(new DataCommand(finder, object));
		this.wakeup();
	}
	
	/**
	 * @param finder
	 * @param object
	 */
	public void dc(DataDelegate finder, DC object) {
		array.add(new DataCommand(finder, object));
		this.wakeup();
	}

	/**
	 * @param finder
	 * @param object
	 */
	public void adc(DataDelegate finder, ADC object) {
		array.add(new DataCommand(finder, object));
		this.wakeup();
	}

	/**
	 * insert sql data to db
	 * @param data
	 * @param sync (synchronization or asynchronization)
	 * @return
	 */
	public int insert(byte[] data, boolean sync) {
		super.refreshTime();
		
		CRC32 checksum = new CRC32();
		checksum.update(data, 0, data.length);
		long sum = checksum.getValue();
		
		Logger.debug("DataClient.insert, data len:%d, crc32:%d", data.length, sum);

		Command cmd = new Command(Request.SQL, Request.SQL_INSERT);
		Stream request = new Stream(getRemote(), cmd);
		request.addMessage(Key.CHECKSUM_CRC32, sum);
		request.addMessage(Key.INSERT_MODE, (sync ? Value.INSERT_SYNC : Value.INSERT_ASYNC));

		request.setData(data);
		Stream resp = null;
		try {
			resp = super.executeStream(request, true);
		} catch (IOException exp) {
			super.close();
			Logger.error(exp);
		} catch (Throwable exp) {
			super.close();
			Logger.error(exp);
		}

		int items = -1;
		if (resp != null) {
			// resolve response data
			cmd = resp.getCommand();
			if (cmd.getResponse() == Response.ACCEPTED) {
				byte[] b = resp.getData();
				Logger.debug("DataClient.insert, result size %d", (b == null ? -1 : b.length));
				items = Numeric.toInteger(b);
			}
		}
		// unlock self (data client)
		super.unlock();
		return items;
	}

	/**
	 * execute "SQL select syntax"
	 */
	private void doSelect(DataCommand cmd) {
		this.refreshTime();
		
		DataDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_SELECT);
		Stream request = new Stream(getRemote(), fixpcmd);
		Object[] params = new Object[] { cmd.object }; // select object
		request.setData(params);
		Stream resp = null;
		// query execute
		try {
			resp = super.executeStream(request, false);
		} catch (IOException exp) {
			super.close();
			Logger.error(exp);
		} catch (Throwable exp) {
			super.close();
			Logger.fatal(exp);
		}

		if(resp == null) {
			delegate.flushTo(0, null, 0, 0);
			return;
		}
		// check command
		fixpcmd = resp.getCommand();
		if (fixpcmd.getResponse() != Response.SELECT_FOUND) {
			Logger.warning("DataClient.doSelect, cannot find from %s - %s", getRemote(), ((Select)cmd.object).getSpace());
			delegate.flushTo(0, null, 0, 0);
			return;
		}

		// item size
		long items = 0;
		Message msg = resp.findMessage( Key.CONTENT_ITEMS );
		if(msg != null) {
			items = msg.longValue();
		}
		// data size
		int datalen = resp.getContentLength();
		
		Logger.debug("DataClient.doSelect, Content Items:%d, Content Length:%d", items, datalen);

		byte[] bytes = null;
		try {
			bytes = resp.readContent();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.error(exp);
		}

		// flush to memory
		delegate.flushTo(items, bytes, 0, (bytes == null ? 0 : bytes.length));
	}

	/**
	 * execute "SQL dc syntax"
	 */
	private void doDC(DataCommand cmd) {
		this.refreshTime();
		
		DataDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_DC);
		Stream request = new Stream(getRemote(), fixpcmd);
		Object[] objects = new Object[] { cmd.object }; // dc object
		request.setData(objects);
		
		Stream resp = null;
		// query execute
		try {
			resp = super.executeStream(request, false);
		} catch (IOException exp) {
			super.close();
			Logger.error(exp);
		} catch (Throwable exp) {
			super.close();
			Logger.fatal(exp);
		}
		if(resp == null) {
			delegate.flushTo(0, null, 0, 0);
			return;
		}
		// check command
		fixpcmd = resp.getCommand();
		if (fixpcmd.getResponse() != Response.DC_FOUND) {
			Logger.error("DataClient.doDC, response code:%d, cannot find from %s",
				fixpcmd.getResponse(), getRemote());
			delegate.flushTo(0, null, 0, 0);
			return;
		}

		// item size
		long items = 0;
		Message msg = resp.findMessage( Key.CONTENT_ITEMS );
		if(msg != null) {
			items = msg.longValue();
		}
		// data size
		int datalen = resp.getContentLength();
		
		Logger.debug("DataClient.doDC, Content Items:%d, Content Length:%d", items, datalen);

		byte[] bytes = null;
		try {
			bytes = resp.readContent();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.error(exp);
		}

		// flush to memory
		delegate.flushTo(items, bytes, 0, (bytes == null ? 0 : bytes.length));
	}
	
	/**
	 * execute "SQL dc syntax"
	 */
	private void doADC(DataCommand cmd) {
		this.refreshTime();
		
		DataDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_ADC);
		Stream request = new Stream(getRemote(), fixpcmd);
		Object[] objects = new Object[] { cmd.object }; // adc object
		request.setData(objects);
		Stream resp = null;
		// query execute
		try {
			resp = super.executeStream(request, false);
		} catch (IOException exp) {
			super.close();
			Logger.error(exp);
		} catch (Throwable exp) {
			super.close();
			Logger.fatal(exp);
		}
		if(resp == null) {
			delegate.flushTo(0, null, 0, 0);
			return;
		}
		// check command
		fixpcmd = resp.getCommand();
		if (fixpcmd.getResponse() != Response.DC_FOUND) {
			Logger.warning("DataClient.doADC, cannot find from %s", getRemote());
			delegate.flushTo(0, null, 0, 0);
			return;
		}

		// item size
		long items = 0;
		Message msg = resp.findMessage( Key.CONTENT_ITEMS );
		if(msg != null) {
			items = msg.longValue();
		}
		// data size
		int datalen = resp.getContentLength();
		
		Logger.debug("DataClient.doADC, Content Items:%d, Content Length:%d", items, datalen);

		byte[] bytes = null;
		try {
			bytes = resp.readContent();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.error(exp);
		}

		// flush to memory
		delegate.flushTo(items, bytes, 0, (bytes == null ? 0 : bytes.length));
	}
	
	/**
	 * execute "SQL delete syntax"
	 */
	private void doDelete(DataCommand cmd) {
		this.refreshTime();
		
		DataDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_DELETE);
		Stream request = new Stream(getRemote(), fixpcmd);
		Object[] params = new Object[] { cmd.object }; // delete object
		request.setData(params);
		Stream resp = null;
		// query
		try {
			resp = super.executeStream(request, false);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (resp == null) {
			delegate.flushTo(0, null, 0, 0);
		} else {
			fixpcmd = resp.getCommand();
			if (fixpcmd.getResponse() != Response.ACCEPTED) {
				delegate.flushTo(0, null, 0, 0);
			}
		}
	}

	/**
	 * execute "select" and "delete"
	 */
	private void subprocess() {
		while(!array.isEmpty()) {
			DataCommand cmd = array.poll();
			if(cmd == null) {
				Logger.fatal("DataClient.subprocess, null DataCommand object, size:%d", array.size());
				continue;
			}
			
			switch(cmd.object.getMethod()) {
			case BasicObject.SELECT_METHOD:
				doSelect(cmd);
				break;
			case BasicObject.DELETE_METHOD:
				doDelete(cmd);
				break;
			case BasicObject.DC_METHOD:
				doDC(cmd);
				break;
			case BasicObject.ADC_METHOD:
				doADC(cmd);
				break;
			}
		}
		super.unlock();
	}

	/**
	 * active connect
	 */
	private boolean active() {
		if (!super.lock()) return false;
		try {
			this.nothing();
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			this.unlock();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.remote.client.ThreadClient#execute()
	 */
	@Override
	protected void execute() {
		while (!isInterrupted()) {
			if (array.size() > 0) {
				subprocess();
			} else {
				if (isRefreshTimeout(20000)) {
					if (!active()) { delay(500); continue; }
				}
				this.delay(5000);
			}
		}
	}

}