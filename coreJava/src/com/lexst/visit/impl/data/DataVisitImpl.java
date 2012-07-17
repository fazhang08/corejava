/**
 *
 */
package com.lexst.visit.impl.data;

import com.lexst.data.*;
import com.lexst.data.pool.*;
import com.lexst.db.chunk.*;
import com.lexst.db.schema.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.data.*;

public class DataVisitImpl implements DataVisit {

	/**
	 *
	 */
	public DataVisitImpl() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		Launcher.getInstance().nothing();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#findChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public Chunk[] findChunk(String db, String table) throws VisitException {
		return Launcher.getInstance().findChunk(new Space(db, table));
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#revive(java.lang.String, java.lang.String, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean revive(String db, String table, SiteHost from) throws VisitException {
		Space space = new Space(db, table);
		return UpdatePool.getInstance().revive(space, from);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#distribute(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, long, long)
	 */
	@Override
	public boolean distribute(SiteHost from, String db, String table,
			long chunkId, long length) {
		Space space = new Space(db, table);
		return SlavePool.getInstance().distribute(from, space, chunkId, length);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#upgrade(com.lexst.util.host.SiteHost, java.lang.String, java.lang.String, long[], long[])
	 */
	@Override
	public boolean upgrade(SiteHost from, String db, String table,
			long[] oldIds, long[] newIds) throws VisitException {
		Space space = new Space(db, table);
		return SlavePool.getInstance().upgrade(from, space, oldIds, newIds);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#applyRank()
	 */
	public int applyRank() throws VisitException {
		return Launcher.getInstance().getRank();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#findTable(java.lang.String, java.lang.String)
	 */
	@Override
	public IndexTable findIndex(String db, String table) throws VisitException {
		return Launcher.getInstance().findIndex(new Space(db, table));
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#createSpace(com.lexst.db.schema.Table)
	 */
	@Override
	public boolean createSpace(Table table) throws VisitException {
		return Launcher.getInstance().createSpace(table);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#removeSpace(java.lang.String, java.lang.String)
	 */
	public boolean deleteSpace(String db, String table) throws VisitException {
		return Launcher.getInstance().deleteSpace(new Space(db, table));
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#optimize(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean optimize(String db, String table) throws VisitException {
		// TODO Auto-generated method stub
		return Launcher.getInstance().optimize(new Space(db, table));
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#loadIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean loadIndex(String db, String table) throws VisitException {
		return Launcher.getInstance().loadIndex(new Space(db, table));
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#stopIndex(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean stopIndex(String db, String table) throws VisitException {
		return Launcher.getInstance().stopIndex(new Space(db, table));
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#loadChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean loadChunk(String db, String table) throws VisitException {
		// TODO Auto-generated method stub
		return Launcher.getInstance().loadChunk(new Space(db, table));
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#stopChunk(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean stopChunk(String db, String table) throws VisitException {
		// TODO Auto-generated method stub
		return Launcher.getInstance().stopChunk(new Space(db, table));
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.data.DataVisit#downloadDCField(long, int, long, long)
	 */
	@Override
	public byte[] downloadDCField(long jobid, int mod, long begin, long end) throws VisitException {
		return DiskPool.getInstance().read(jobid, mod, begin, end);
	}
	
}