/**
 *
 */
package com.lexst.data;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.algorithm.TaskEventListener;
import com.lexst.algorithm.diffuse.*;
import com.lexst.data.effect.*;
import com.lexst.data.pool.*;
import com.lexst.db.*;
import com.lexst.db.chunk.*;
import com.lexst.db.index.range.*;
import com.lexst.db.schema.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.data.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.datetime.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;
import com.lexst.visit.impl.data.*;
import com.lexst.xml.*;

/**
 * data site
 */
public class Launcher extends JobLauncher implements TaskEventListener {

	private static Launcher selfHandle = new Launcher();
	
	// local data site
	private DataSite local = new DataSite();
	// space -> table configure
	private LockMap<Space, Table> mapTable = new LockMap<Space, Table>();
	
	private IdentityPuddle puddle = new IdentityPuddle(); 
	// space catalog
	private SpacePuddle spaces = new SpacePuddle();
	
	// node store directory
	private String nodePath;
	// update space index identity
	private boolean updateModule;
	
	
	/**
	 * default constructor
	 */
	private Launcher() {
		super();
		super.setExitVM(true);
		streamImpl = new DataStreamInvoker();
		packetImpl = new DataPacketInvoker(fixpPacket);
		updateModule = false;
	}

	/**
	 * return a static handle
	 * @return
	 */
	public static Launcher getInstance() {
		return Launcher.selfHandle;
	}
	
	/**
	 * rpc null call
	 */
	public void nothing() {
		// none call
	}

	/**
	 * get site's rank
	 * @return
	 */
	public int getRank() {
		return local.getRank();
	}
	
	public DataSite getLocal() {
		return this.local;
	}
	
	public SiteHost getHome() {
		return this.home;
	}
	
	public Chunk[] findChunk(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();

		// call jni, get all chunkid
		long[] chunkIds = Install.getChunkIds(db, table);
		if (chunkIds == null || chunkIds.length == 0) {
			Logger.warning("Launcher.findChunk, cannot find '%s' chunk", space);
			return null;
		}
		// get chunk information
		ArrayList<Chunk> array = new ArrayList<Chunk>();
		for (long chunkid : chunkIds) {
			// find chunk filename
			byte[] path = Install.findChunkPath(db, table, chunkid);
			if (path == null || path.length == 0) {
				Logger.warning("Launcher.findChunk, cannot find '%s' - %x path", space, chunkid);
				continue;
			}
			String filename = new String(path);
			File file = new File(filename);
			if (file.exists() && file.isFile()) {
				long length = file.length();
				long modified = file.lastModified();
				Chunk info = new Chunk(chunkid, length, modified);
				array.add(info);
			}
		}
		int size = array.size();
		
		Logger.info("Launcher.findChunk, '%s' chunk size %d", space, size);
		
		if (size == 0) return null;
		Chunk[] infos = new Chunk[size];
		return array.toArray(infos);
	}
	
	/**
	 * rebuild chunk data
	 * @param space
	 * @return
	 */
	public boolean optimize(Space space) {
		Logger.debug("Launcher.optimize, rebuild space '%s'", space);
		// when contains
		Table table = mapTable.get(space);
		if (table == null) {
			Logger.error("Launcher.optimize, cannot find '%s'", space);
			return false;
		}
		return CommandPool.getInstance().deflate(table.getSpace());
	}

	/**
	 * load index into memory
	 * @param space
	 * @return
	 */
	public boolean loadIndex(Space space) {
		Logger.debug("Launcher.loadIndex, load index '%s'", space);
		Table table = mapTable.get(space);
		if (table == null) {
			Logger.error("Launcher.loadIndex, cannot find '%s'", space);
			return false;
		}
		return CommandPool.getInstance().loadIndex(table.getSpace());
	}

	/**
	 * clear index from memory
	 * @param space
	 * @return
	 */
	public boolean stopIndex(Space space) {
		Logger.debug("Launcher.stopIndex, unload index '%s'", space);
		Table table = mapTable.get(space);
		if (table == null) {
			Logger.error("Launcher.stopIndex, cannot find '%s'", space);
			return false;
		}
		return CommandPool.getInstance().stopIndex(table.getSpace());
	}

	/**
	 * load chunk data to memory
	 * @param space
	 * @return
	 */
	public boolean loadChunk(Space space) {
		Logger.debug("Launcher.loadChunk, space '%s'", space);
		Table table = mapTable.get(space);
		if (table == null) {
			Logger.error("Launcher.loadChunk, cannot find '%s'", space);
			return false;
		}
		return CommandPool.getInstance().loadChunk(table.getSpace());
	}

	/**
	 * release chunk data from memory
	 * @param space
	 * @return
	 */
	public boolean stopChunk(Space space) {
		Logger.debug("Launcher.stopChunk, space '%s'", space);
		Table table = mapTable.get(space);
		if (table == null) {
			Logger.error("Launcher.stopChunk, cannot find '%s'", space);
			return false;
		}
		return CommandPool.getInstance().stopChunk(table.getSpace());
	}

	/**
	 * find space table
	 * 
	 * @param space
	 * @return
	 */
	public Table findTable(Space space) {
		return mapTable.get(space);
	}

	/**
	 * load table
	 * @param client
	 * @return
	 */
	private boolean loadTable(HomeClient client) {
		boolean success = false;
		boolean nullable = (client == null);
		try {
			if (nullable) client = bring(home);
			for (Space space : spaces.list()) {
				Table table = client.findTable(space);
				if (table == null) {
					Logger.error("Launcher.loadTable, not found table '%s'", space);
					return false;
				}
				mapTable.put(space, table);
				Logger.info("Launcher.loadTable, load table '%s'", space);
			}

			for (Naming naming : DiffuseTaskPool.getInstance().listNaming()) {
				Project project = DiffuseTaskPool.getInstance().findProject(naming);
				for(Space space : project.getSpaces()) {
					Table table = mapTable.get(space);
					if(table == null) {
						table = client.findTable(space);
					}
					if(table == null) {
						Logger.error("Launcher.loadTable, cannot find table '%s'", space);
						return false;
					}
					project.setTable(space, table);
					Logger.info("Launcher.loadTable, load project table '%s'", space);
				}
			}
			
			success = true;
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			if (nullable) complete(client);
		}
		return success;
	}

	/**
	 * load jni service
	 * @param client
	 * @return
	 */
	private boolean loadJNI(HomeClient client) {
		//1. load table space
		for (Space space : mapTable.keySet()) {
			Table table = mapTable.get(space);
			boolean prime = local.isPrime(); //check site rank
			if (prime) {
				prime = table.isCaching(); //using cache? only master site
			}
			// build to byte
			byte[] data = table.build();
			int ret = Install.initSpace(data, prime);
			boolean success = (ret == 0);
			Logger.note(success, "Launcher.loadJNI, init '%s' result code %d", space, ret);
			if (!success) return false;
		}
		//2. check chunk id, when missing, add it
		int frees = Install.countFreeChunkIds();
		int size = mapTable.size();
		if (local.isPrime() && frees < size) {
			size = size - size % 10 + 10;
			long[] chunkIds = null;
			try {
				chunkIds = client.pullSingle(size);
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			if (chunkIds == null) {
				Logger.error("Launcher.loadJNI, cannot request chunkid!");
				return false;
			}
			for (int i = 0; i < chunkIds.length; i++) {
				Install.addChunkId(chunkIds[i]);
			}
		}
		//3. goto service
		long time = System.currentTimeMillis();
		int ret = Install.launch();
		Logger.info("Launcher.loadJNI, launch used time %s, result code %d", System.currentTimeMillis() - time, ret);
		return ret == 0;
	}

	/**
	 * stop jni service
	 */
	private void stopJNI() {
		Install.stop();
	}

	/**
	 * @param client
	 */
	private boolean loadTime(HomeClient client) {
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) return false;

		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if(client.isClosed()) client.reconnect();
				long time = client.currentTime();
				Logger.info("Launcher.loadTime, set time %d", time);
				if (time != 0L) {
					int ret = SystemTime.set(time);
					success = (ret == 0);
					break;
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(500);
		}
		if(nullable) complete(client);
		Logger.note("Launcher.loadTime", success);
		return success;
	}


	/**
	 * login to home site
	 * @param client
	 * @return
	 */
	private boolean login(HomeClient client) {
		Logger.info("Launcher.login, %s login to %s", local.getHost(), home);
		
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) {
			Logger.error("Launcher.login, cannot connect %s", home);
			return false;
		}

		local.clearAllNaming();
		local.addAllNaming(DiffuseTaskPool.getInstance().listNaming());
		
		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.login(local);
				break;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			// when error, close
			client.close();
			delay(1000);
		}
		if(nullable) complete(client);
		return success;
	}

	/**
	 * relogn to home site
	 * @param site
	 * @return
	 */
	private boolean relogin() {
		Logger.info("Launcher.relogin, to home site %s", home);
		boolean success = false;
		HomeClient client = bring(home);
		if (client == null) {
			Logger.error("Launcher.relogin, cannot connect %s", home);
			return false;
		}
		
		local.clearAllNaming();
		local.addAllNaming(DiffuseTaskPool.getInstance().listNaming());

		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.relogin(local);
				break;
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} 
			client.close();
			this.delay(1000);
		}
		complete(client);
		return success;
	}
	
	/**
	 * logout from home site
	 * @param client
	 * @return
	 */
	private boolean logout(HomeClient client) {
		Logger.info("Launcher.logout, %s from %s", local.getHost(), home);
		
		boolean nullable = (client == null);
		if (nullable) client = bring(home);
		if (client == null) {
			Logger.error("Launcher.logout, cannot connect %s", home);
			return false;
		}

		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if (client.isClosed()) client.reconnect();
				success = client.logout(local.getType(), local.getHost());
				break;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			delay(1000);
		}
		if(nullable) complete(client);
		return success;
	}

	private boolean loadPool1() {
		CachePool.getInstance().start();
		ChunkPool.getInstance().start();
		DiskPool.getInstance().start();
		
		FlushPool.getInstance().start();
		DiffuseTaskPool.getInstance().setTaskEventListener(this);
		DiffuseTaskPool.getInstance().start();
		
		SQLPool.getInstance().setListener(fixpPacket);
		boolean success = SQLPool.getInstance().start();
		Logger.note("Launcher.loadPool1, load sql pool", success);
		if (success) {
			success = CommandPool.getInstance().start();
			Logger.note("Launcher.loadPool1, load command pool", success);
		}
		return success;
	}

	private boolean loadPool2() {
		boolean success = false;
		if (local.isPrime()) {
			success = PrimePool.getInstance().start();
			Logger.note("Launcher.loadPool2, load prime pool", success);
			if (success) {
				success = UpdatePool.getInstance().start();
				Logger.note("Launcher.loadPool2, load update pool", success);
				if(!success) {
					PrimePool.getInstance().stop();
				}
			}
		} else {
			success = SlavePool.getInstance().start();
			Logger.note("Launcher.loadPool2, load slave pool", success);
		}
		return success;
	}

	private void stopPool() {
		FlushPool.getInstance().stop();
		while (FlushPool.getInstance().isRunning()) {
			this.delay(200);
		}

		UpdatePool.getInstance().stop();
		PrimePool.getInstance().stop();
		SlavePool.getInstance().stop();
		SQLPool.getInstance().stop();
		DiffuseTaskPool.getInstance().stop();
		CommandPool.getInstance().stop();
		ChunkPool.getInstance().stop();
		CachePool.getInstance().stop();
		DiskPool.getInstance().stop();

		while (SQLPool.getInstance().isRunning()) {
			this.delay(200);
		}
		while (DiffuseTaskPool.getInstance().isRunning()) {
			this.delay(200);
		}
		while (PrimePool.getInstance().isRunning()) {
			this.delay(200);
		}
		while (SlavePool.getInstance().isRunning()) {
			this.delay(200);
		}
		while (UpdatePool.getInstance().isRunning()) {
			this.delay(500);
		}
		while(CommandPool.getInstance().isRunning()) {
			this.delay(500);
		}
		while(CachePool.getInstance().isRunning()) {
			this.delay(500);
		}
		while(ChunkPool.getInstance().isRunning()) {
			this.delay(500);
		}
		while(DiskPool.getInstance().isRunning()) {
			this.delay(500);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		//1. logout site
		this.logout(null);
		//2. stop pool
		this.stopPool();
		//3. stop listen
		super.stopListen();
		//4. flush space configure
		this.flushSpace();
		//5. flush chunk num configure
		this.flushIdentity();
		// 6. flush cache entity
		this.flushEntity();
		//6. close jni service
		this.stopJNI();
		//7. stop log service
		super.stopLog();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		HomeClient client = bring(home);
		if (client == null) {
			Logger.error("Launcher.init, cannot find home site %s", home);
			return false;
		}
		//1. get log site and start log service
		boolean	success = loadLog(local.getType(), client);
		Logger.note("Launcher.init, load log", success);
		//2. get site timeout
		if (success) {
			success = loadTimeout(local.getType(), client);
			Logger.note(success, "Launcher.init, site timeout %d", getSiteTimeout());
			if (!success) stopLog();
		}
		//3. set system time
		if (success) {
			loadTime(client);
		}
		//4. start fixp monitor
		if (success) {
			Class<?>[] cls = { DataVisitImpl.class };
			success = loadListen(cls, local.getHost());
			Logger.note("Launcher.init, load listen", success);
			if (!success) stopLog();
		}
		//5. load table 
		if (success) {
			success = loadTable(client);
			Logger.note("Launcher.init, load table", success);
			if (!success) {
				stopListen();
				stopLog();
			}
		}
		//6. load native(c,c++) service
		if (success) {
			success = loadJNI(client);
			Logger.note("Launcher.init, load JNI service", success);
			if (!success) {
				stopListen();
				stopLog();
			}
		}
		//7. load index
		if(success) {
			success = loadIndex();
			Logger.note("Launcher.init, load index", success);
			if (!success) {
				stopJNI();
				stopListen();
				stopLog();
			}
		}
		//8. load pool
		if(success) {
			success = loadPool1();
			Logger.note("Launcher.init, load pool1", success);
			if(!success) {
				stopJNI();
				stopListen();
				stopLog();
			}
		}
		//9. login to home site
		if (success) {
			success = login(client);
			Logger.note("Launcher.init, login", success);
			if (!success) {
				stopPool();
				stopJNI();
				stopListen();
				stopLog();
			}
		}
		// close client
		complete(client);
		//10. choose load rank pool
		if(success) {
			success = loadPool2();
			Logger.note("Launcher.init, load pool2", success);
			if(!success) {
				this.finish();
			}
		}
		
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("Launcher.process, site into ...");
		// update last active time
		refreshEndTime();
		while (!isInterrupted()) {
			long end = System.currentTimeMillis() + 1000;
			
			if(super.isLoginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.login(null);
			} else if(super.isReloginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.relogin();
			} else if (isMaxSiteTimeout()) {
				this.refreshEndTime();
				this.relogin();
			} else if (this.isSiteTimeout()) {
				this.hello(local.getType(), home); // active to home
			}

			//1. check chunkid(only prime site)
			this.checkMissing();
			// when update space, update index and relogin to home site
			if (updateModule) {
				Logger.info("Launcher.process, update data space");
				setUpdateModule(false);
				boolean success = loadIndex();
				if(success) this.relogin();
			}

			long timeout = end - System.currentTimeMillis();
			if (timeout > 0) delay(timeout);
		}
		Logger.info("Launcher.process, site exit");
	}

	/**
	 * check chunkid, when missing, add it
	 */
	private void checkMissing() {
		if (!local.isPrime()) return;
		// call jni
		int count = Install.countFreeChunkIds();
		if (count > 3) return;

		// apply chunkid from top site
		long[] chunkIds = this.applyChunkId(10);
		int size = (chunkIds == null ? 0 : chunkIds.length);
		// add chunkid to jni
		Logger.info("Launcher.checkMissing, new chunkid count %d", size);
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				Install.addChunkId(chunkIds[i]);
			}
		}
	}

	/**
	 * load space module (index map)
	 * @return
	 */
	private boolean loadIndex() {
		// clear all old data
		local.clear();
		// check disk space size
		long[] sizes = Install.getDiskSpace();
		if (sizes == null || sizes.length != 2) {
			Logger.error("Launcher.loadIndex, cannot get disk size");
			return false;
		}

		local.setFree(sizes[0]);
		local.setUsable(sizes[1]);
		Logger.info("Launcher.loadIndex, disk free size [%d - %dM], used size [%d - %dM]",
				local.getFree(), local.getFree() / 1024 / 1024, local.getUsable(), local.getUsable() / 1024 / 1024);

		// table space is none, exit
		if (spaces.isEmpty()) return true;
		// load chunk index
		byte[] bytes = Install.pullChunkIndex();
		Logger.info("Launcher.loadIndex, index module size %d", (bytes == null ? -1 : bytes.length));
		if (bytes != null && bytes.length > 0) {
			boolean success = splitIndex(bytes);
			Logger.note("Launcher.loadIndex, split index", success);
			if (!success) {
				return false;
			}
		}
		// padding missing space
		IndexSchema base = local.getIndexSchema();
		for (Space space : spaces.list()) {
			if (!base.contains(space)) {
				base.add(space);
			}
		}
		return true;
	}

	/**
	 * split c++ data to java
	 * @param bytes
	 * @return
	 */
	private boolean splitIndex(byte[] bytes) {
		// bytes less 12, this is error
		if (bytes.length < 12) return false;
		
		IndexSchema base = local.getIndexSchema();
		int off = 0;
		// all byte size
		int datalen = Numeric.toInteger(bytes, off, 4); off += 4;
		// chunk count
		int allchunk = Numeric.toInteger(bytes, off, 4); off += 4;
		// version
		int version = Numeric.toInteger(bytes, off, 4); off += 4;
		// check error
		if (bytes.length - 12 != datalen || version != 1) {
			return false;
		}

		int chunks = 0;
		while(off < bytes.length) {
			if (off + 2 > bytes.length) return false;
			// table space
			int dbsz = bytes[off++] & 0xff;
			int tbsz = bytes[off++] & 0xff;
			if (off + dbsz + tbsz > bytes.length) return false;
			String db = new String(bytes, off, dbsz); off += dbsz;
			String table = new String(bytes, off, tbsz); off += tbsz;
			Space space = new Space(db, table);

			if (off + 8 > bytes.length) return false;
			// all chunk byte size by space
			int chunklen = Numeric.toInteger(bytes, off, 4); off += 4;
			// chunk count by space
			int chunksum = Numeric.toInteger(bytes, off, 4); off += 4;

			Logger.info("Launcher.splitIndex, space '%s', chunk len: %d, chunk count: %d", space, chunklen, chunksum);

			int elements = chunks;
			for (int pos = off; off - pos < chunklen;) {
				if (off + 16 > bytes.length) return false;
				// chunk id
				long chunkId = Numeric.toLong(bytes, off, 8); off += 8;
				// chunk rank(primary or slave)
				byte rank = bytes[off++];
				// chunk status(incomplete or complete)
				byte status = bytes[off++];
				// byte size of chunk size and index count
				int idxsize = Numeric.toInteger(bytes, off, 4); off += 4;
				short idxcount = Numeric.toShort(bytes, off, 2); off += 2;
				
				Logger.info("Launcher.splitIndex, chunk %x, rank %d, status %d, index size %d, index count %d",
						chunkId, rank, status, idxsize, idxcount);

				ChunkSheet sheet = new ChunkSheet(chunkId, rank, status);
				int chunkoff = off;
				for(short i = 0; i < idxcount; i++) {
					IndexRange index = null;
					if (off + 3 > bytes.length) return false;
					byte type = bytes[off++];
					short columnId = Numeric.toShort(bytes, off, 2); off += 2;
					
					Logger.info("Launcher.splitIndex, columnId:%d - type:%d", columnId, type);
					
					// check type and build index
					if (type == Type.SHORT_INDEX) {
						if(off + 4 > bytes.length) return false;
						short begin = Numeric.toShort(bytes, off, 2); off += 2;
						short end = Numeric.toShort(bytes, off, 2); off += 2;
						index = new ShortIndexRange(chunkId, columnId, begin, end);
					} else if (type == Type.INTEGER_INDEX) {
						if (off + 8 > bytes.length) return false;
						int begin = Numeric.toInteger(bytes, off, 4); off += 4;
						int end = Numeric.toInteger(bytes, off, 4); off += 4;
						index = new IntegerIndexRange(chunkId, columnId, begin, end);
					} else if (type == Type.LONG_INDEX) {
						if(off + 16 > bytes.length) return false;
						long begin = Numeric.toLong(bytes, off, 8); off += 8;
						long end = Numeric.toLong(bytes, off, 8); off += 8;
						index = new LongIndexRange(chunkId, columnId, begin, end);
					} else if (type == Type.REAL_INDEX) {
						if (off + 8 > bytes.length) return false;
						int begin = Numeric.toInteger(bytes, off, 4); off += 4;
						int end = Numeric.toInteger(bytes, off, 4); off += 4;
						index = new RealIndexRange(chunkId, columnId,
								Float.intBitsToFloat(begin), Float.intBitsToFloat(end));
					} else if (type == Type.DOUBLE_INDEX) {
						if(off + 16 > bytes.length) return false;
						long begin = Numeric.toLong(bytes, off, 8); off += 8;
						long end = Numeric.toLong(bytes, off, 8); off += 8;
						index = new DoubleIndexRange(chunkId, columnId,
								Double.longBitsToDouble(begin), Double.longBitsToDouble(end));
					}
					if (index == null) {
						Logger.error("Launcher.splitIndex, invalid index '%s'", space);
						return false;
					}
					sheet.add(index);
				}
				if (off - chunkoff != idxsize) {
					Logger.error("Launcher.splitIndex, invalid chunk size!");
					return false;
				}
				// save chunk
				base.add(space, sheet);
				chunks++;
			}
			if(chunks - elements != chunksum) {
				Logger.error("Launcher.splitIndex, invalid chunk num '%s'", space);
				return false;
			}
		}
		if(chunks != allchunk) {
			Logger.error("Launcher.splitIndex, invalid chunk num!");
			return false;
		}
		return true;
	}

	/**
	 * find index table
	 * @param space
	 * @return
	 */
	public IndexTable findIndex(Space space) {
		IndexSchema schema = local.getIndexSchema();
		return schema.find(space);
	}

	/**
	 * 
	 * @param b
	 */
	public synchronized void setUpdateModule(boolean b) {
		this.updateModule = b;
	}

	/**
	 * @param table
	 * @return
	 */
	public boolean createSpace(Table table) {
		Space space = table.getSpace();
		Logger.info("Launcher.createSpace, create table space '%s'", space);
		
		// when prime site, check chunkid num
		boolean prime = local.isPrime();
		if(prime) {
			int cid = Install.countFreeChunkIds();
			Logger.info("Launcher.createSpace, count chunk id is:%d", cid);
			while (cid == 0) {
				if (puddle.isEmpty()) {
					long[] chunkIds = applyChunkId(10);
					puddle.add(chunkIds);
					Logger.info("Launcher.createSpace, count apply chunk identity %d", (chunkIds == null ? 0 : chunkIds.length));
				} else {
					while (true) {
						long chunkId = puddle.poll();
						if (chunkId == 0L) break;
						Install.addChunkId(chunkId);
					}
					break;
				}
			}
		}
		// when prime site, chack cache mode
		if (prime) {
			prime = table.isCaching();
		}
		// call jni, create table space
		byte[] data = table.build();
		int ret = Install.createSpace(data, prime);
		boolean success = (ret == 0);
		Logger.note(success, "Launcher.createSpace, create table space '%s' result code %d", space, ret);
		if (success) {
			// write xml configure file
			spaces.add(space);
			mapTable.put(space, table);
			flushSpace();
			// set refresh space status to true
			setUpdateModule(true);
		}
		return success;
	}

	/**
	 * delete a data space (move to temp directory)
	 * @param db
	 * @param table
	 * @return
	 */
	public boolean deleteSpace(Space space) {
		Logger.info("Launcher.deleteSpace, delete table space '%s'", space);
		// jni call
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		int ret = Install.deleteSpace(db, table);
		// error!
		if (ret != 0) {
			Logger.info("Launcher.deleteSpace, cannot delete table space '%s'", space);
			return false;
		}
		Logger.info("Launcher.deleteSpace, delete table space '%s' success", space);
		// delete success, re-write configure
		spaces.remove(space);
		mapTable.remove(space);
		flushSpace();
		setUpdateModule(true);
		return true;
	}

	public boolean existSpace(Space space) {
		return spaces.exists(space);
	}
	
	public List<Space> listSpace() {
		return spaces.list();
	}

	/* (non-Javadoc)
	 * @see com.lexst.algorithm.TaskEventListener#updateTask()
	 */
	@Override
	public void updateTask() {
		setOperate(BasicLauncher.RELOGIN);
	}

	/**
	 * request chunk id, from home site
	 * @return
	 */
	public long[] applyChunkId(int num) {
		if(num < 10) num = 10;
		HomeClient client = bring(home);
		long[] chunkIds = null;
		try {
			chunkIds = client.pullSingle(num);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.error(exp);
		}
		complete(client);
		return chunkIds;
	}

	private boolean createPath(String s) {
		char c = File.separatorChar;
		s = s.replace('\\', c);
		s = s.replace('/', c);
		if (s.charAt(s.length() - 1) != c) s += c;
		File dir = new File(nodePath = s);
		// create directory
		if (dir.exists() && dir.isDirectory()) {
			return true;
		}
		return dir.mkdirs();
	}
	
	private File buildFile(String filename) {
		String path = String.format("%s%s%s", nodePath, File.separator, filename );
		return new File(path);
	}

//	private boolean splitDiffuseTask(String filename) {
//		File file = new File(filename);
//		if(!file.exists()) return true;
//		
//		XMLocal xml = new XMLocal();
//		Document doc = xml.loadXMLSource(filename);
//		if (doc == null) {
//			return false;
//		}
//
//		NodeList list =	doc.getElementsByTagName("task");
//		int size = list.getLength();
//		for (int i = 0; i < size; i++) {
//			Element element = (Element) list.item(i);
//			
//			String naming = xml.getValue(element, "naming");
//			String project_class = xml.getValue(element, "project-class");
//			String task_class = xml.getValue(element, "task-class");
//			String resource = xml.getValue(element, "resource");
//			
//			boolean success = SQLPool.getInstance().addProject(project_class, naming, task_class, resource);
//			Logger.note(success, "Launcher.splitDiffuseTask, load '%s' - '%s' - '%s' - '%s'",
//					naming, project_class, task_class, resource);			
//			if(success) {
//				// save diffuse naming
//				local.addNaming(naming);
//			}
//		}
//		return true;
//	}

	/**
	 * load configure file
	 * @param filename
	 * @return
	 */
	private boolean loadLocal(String filename) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(filename);
		if (doc == null) return false;

		Logger.info("Launcher.loadLocal, load xml resource");

		SiteHost host = splitHome(doc);
		if(host == null) {
			Logger.error("Launcher.loadLocal, split home failed");
			return false;
		}
		home.set(host);

		host = super.splitLocal(doc);
		if(host == null) {
			Logger.error("Launcher.loadLocal, split local failed!");
			return false;
		}
		local.setHost(host);
		if (!loadShutdown(doc)) {
			Logger.error("Launcher.loadLocal, split shutdown failed!");
			return false;
		}

		Logger.info("Launcher.loadLocal, load local resource");

		// create configure path
		String s = xml.getXMLValue(doc.getElementsByTagName("data-path"));
		if (!createPath(s)) {
			Logger.error("Launcher.loadLocal, cannot create path %s", s);
			return false;
		}
		
		// resovle security configure file
		if(!super.loadSecurity(doc)) {
			Logger.error("Launcher.loadLocal, cannot parse security file");
			return false;
		}
		
		// create async path
		s = xml.getXMLValue(doc.getElementsByTagName("async-path"));
		if (s != null && s.length() > 0) {
			FlushPool.getInstance().setTempPath(s);
		}
		
		// creata adc path
		s = xml.getXMLValue(doc.getElementsByTagName("adc-path"));
		if (s != null && s.length() > 0) {
			DiskPool.getInstance().setPath(s);
		}

//		// load task file
//		s = xml.getXMLValue(doc.getElementsByTagName("task-file"));
//		if (!splitDiffuseTask(s)) {
//			Logger.error("Launcher.loadLocal, cannot resolve task %s", s);
//			return false;
//		}
		
		// set task root
		s = xml.getXMLValue(doc.getElementsByTagName("task-root"));
		if (s != null && s.length() > 0) {
			DiffuseTaskPool.getInstance().setRoot(s);
		}

		// data site rank
		s = xml.getXMLValue(doc.getElementsByTagName("rank"));
		byte rank = 0;
		if ("master".equalsIgnoreCase(s) || "prime".equalsIgnoreCase(s)) {
			rank = DataSite.PRIME_SITE;
		} else if("slave".equalsIgnoreCase(s)) {
			rank = DataSite.SLAVE_SITE;
		}
		Logger.info("Launcher.loadLocal, site rank '%s - %d'", s, rank);
		if (rank == 0) {
			Logger.error("Launcher.loadLocal, unknown site rank");
			return false;
		}
		// set site level
		local.setRank(rank);
		Install.setRank(rank);

		// JNI job threads
		s = xml.getXMLValue(doc.getElementsByTagName("job-threads"));
		int threads = Integer.parseInt(s);
		Install.setWorker(threads);

		Logger.info("Launcher.loadLocal, set worker thread %d", threads);

		// database path
		Element elem = (Element) doc.getElementsByTagName("chunk-path").item(0);
		String build = xml.getValue(elem, "build");
		String cache = xml.getValue(elem, "cache");
		String[] paths = xml.getXMLValues(elem.getElementsByTagName("store"));
		int ret = Install.setBuildRoot(build.getBytes());
		Logger.note(ret == 0, "Launcher.loadLocal, load build path %s", build);
		if (ret != 0) return false;
		ret = Install.setCacheRoot(cache.getBytes());
		Logger.note(ret == 0, "Launcher.loadLocal, load cache path %s", cache);
		if (ret != 0) return false;
		for (String path : paths) {
			ret = Install.setChunkRoot(path.getBytes());
			Logger.note(ret == 0, "Launcher.loadLocal, load store path %s", path);
			if (ret != 0) return false;
		}
		
		// load log configure
		return Logger.loadXML(filename);
	}

	/**
	 * load space data from disk
	 * @return
	 */
	private boolean loadSpace() {
		File file = buildFile(SpacePuddle.filename);
		// not found file, return true
		if (!file.exists()) return true;
		byte[] data = readFile(file);
		if(data == null) return false;
		return spaces.parseXML(data);
	}

	/**
	 * flush space data to disk
	 * @return
	 */
	private boolean flushSpace() {
		byte[] bytes = spaces.buildXML();
		// flush to disk
		File file = buildFile(SpacePuddle.filename);
		return flushFile(file, bytes);
	}

	private boolean loadIdentity() {
		File file = buildFile(IdentityPuddle.filename);
		if(!file.exists()) return true;
		byte[] data = readFile(file);
		if(data == null) return false;
		puddle.parseXML(data);
		// flush chunk id to jni
		int count = 0;
		while (true) {
			long chunkId = puddle.poll();
			if (chunkId == 0L) break;
			int ret = Install.addChunkId(chunkId);
			if(ret == 0) count++;
		}
		Logger.info("Launcher.loadIdentity, load count %d", count);
		return true;
	}

	private boolean flushIdentity() {
		long[] ids = Install.getFreeChunkIds();
		
		Logger.info("Launcher.flushIdentity, chunk id count %d", (ids == null ? -1 : ids.length));
		
		for (int i = 0; ids != null && i < ids.length; i++) {
			puddle.add(ids[i]);
		}
		byte[] data = puddle.buildXML();
		// flush to disk
		File file = buildFile(IdentityPuddle.filename);
		return flushFile(file, data);
	}
	
	private boolean loadEntity() {
		File file = buildFile("cache_entity.xml");
		if(!file.exists()) return true;
		
		byte[] data = readFile(file);
		
		return CachePool.getInstance().resolve(data);
	}
	
	private boolean flushEntity() {
		byte[] data = CachePool.getInstance().build();
		File file = buildFile("cache_entity.xml");
		return flushFile(file, data);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1) {
			Logger.error("parameters missing!");
			Logger.gushing();
			return;
		}
		// init database
		int ret = Install.initialize();
		if(ret != 0) {
			Logger.error("initialize failed, program will exit!");
			Logger.gushing();
			return;
		}
		// load resource
		String filename = args[0];
		boolean success = Launcher.getInstance().loadLocal(filename);
		Logger.note("Launcher.main, load local", success);
		if (success) {
			success = Launcher.getInstance().loadSpace();
			Logger.note("Launcher.main, load space", success);
		}
		// load chunk identity
		if (success) {
			success = Launcher.getInstance().loadIdentity();
			Logger.note("Launcher.main, load chunk identity", success);
		}
		// load cache entity
		if(success) {
			success = Launcher.getInstance().loadEntity();
			Logger.note("Launcher.main, load cache entity", success);
		}
		// start service
		if (success) {
			success = Launcher.getInstance().start();
			Logger.note("Launcher.main, start service", success);
		}
		if(!success) {
			Logger.gushing();
		}
	}
		
}