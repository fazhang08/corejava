package com.lexst.data.pool;

import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.home.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;


public class CommandPool extends LocalPool {
	
	private static CommandPool selfHandle = new CommandPool();
	
	private LockArray<CommandType> array = new LockArray<CommandType>();

	// optimize space queue
	private LockArray<Space> queue = new LockArray<Space>();

	/**
	 * 
	 */
	private CommandPool() {
		super();
	}
	
	/**
	 * get static handle
	 * @return
	 */
	public static CommandPool getInstance() {
		return CommandPool.selfHandle;
	}
	
	/**
	 * @param space
	 * @return
	 */
	public boolean deflate(Space space) {
		if(!Launcher.getInstance().getLocal().isPrime()) {
			Logger.error("CommandPool.deflate, command refuse, this is slave node");
			return false;
		}

		Logger.info("CommandPool.deflate, add space '%s'", space);
		if (queue.contains(space)) {
			Logger.info("CommandPool.deflate, space '%s' is running", space);
			return false;
		}
		for (CommandType task : array.list()) {
			if (task.type == CommandType.OPTIMIZE && task.space.equals(space)) {
				return false;
			}
		}
		boolean success = array.add(new CommandType(CommandType.OPTIMIZE, space));
		if (success) {
			queue.add(space);
		}
		if(success) wakeup();
		return success;
	}
	
	public boolean loadIndex(Space space) {
		for (CommandType task : array.list()) {
			if(task.type == CommandType.LOAD_INDEX && task.space.equals(space)) {
				return false;
			}
		}
		Logger.info("CommandPool.loadIndex, space '%s'", space);
		boolean success = array.add(new CommandType(CommandType.LOAD_INDEX, space));
		if(success) wakeup();
		return success;
	}
	
	public boolean stopIndex(Space space) {
		for (CommandType task : array.list()) {
			if(task.type == CommandType.STOP_INDEX && task.space.equals(space)) {
				return false;
			}
		}
		Logger.info("CommandPool.stopIndex, space '%s'", space);
		boolean success = array.add(new CommandType(CommandType.STOP_INDEX, space));
		if(success) wakeup();
		return success;
	}
	
	public boolean loadChunk(Space space) {
		for (CommandType task : array.list()) {
			if(task.type == CommandType.LOAD_CHUNK && task.space.equals(space)) {
				return false;
			}
		}
		Logger.info("CommandPool.loadChunk, space '%s'", space);
		boolean success = array.add(new CommandType(CommandType.LOAD_CHUNK, space));
		if(success) wakeup();
		return success;
	}
	
	public boolean stopChunk(Space space) {
		for (CommandType task : array.list()) {
			if (task.type == CommandType.STOP_CHUNK && task.space.equals(space)) {
				return false;
			}
		}
		Logger.info("CommandPool.stopChunk, space '%s'", space);
		boolean success = array.add(new CommandType(CommandType.STOP_CHUNK, space));
		if(success) wakeup();
		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("CommandPool.process, into...");
		while (!isInterrupted()) {
			execute();
			delay(10000);
		}
		Logger.info("CommandPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		array.clear();
		queue.clear();
	}

	/**
	 * check and call jni
	 */
	private void execute() {
		while(!array.isEmpty()) {
			CommandType task = array.poll();
			if(task == null) {
				Logger.error("CommandPool.execute, null object, size:%d", array.size());
				continue;
			}
			switch (task.type) {
			case CommandType.OPTIMIZE:
				optimize(task.space);
				queue.remove(task.space);
				break;
			case CommandType.LOAD_INDEX:
				inrush(task.space);
				break;
			case CommandType.STOP_INDEX:
				uninrush(task.space);
				break;
			case CommandType.LOAD_CHUNK:
				afflux(task.space);
				break;
			case CommandType.STOP_CHUNK:
				unafflux(task.space);
				break;
			}
		}
	}

	/**
	 * optimize space
	 * @param space
	 */
	private boolean optimize(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		
		long time = System.currentTimeMillis();
		Logger.info("CommandPool.optimize, optimize '%s'", space);
		// call jni, optimize space
		byte[] bytes = Install.optimize(db, table);
		Logger.info("CommandPool.optimize, optimze '%s' usedtime %d, byte size %d",
				space, System.currentTimeMillis() - time, (bytes == null ? -1 : bytes.length));

		int off = 0;
		int oldsize = Numeric.toInteger(bytes, off, 4);
		off += 4;
		int newsize = Numeric.toInteger(bytes, off, 4);
		off += 4;
		long[] oldids = new long[oldsize];
		long[] newids = new long[newsize];
		for (int i = 0; i < oldsize; i++) {
			long chunkid = Numeric.toLong(bytes, off, 8);
			off += 8;
			oldids[i] = chunkid;
		}
		for (int i = 0; i < newsize; i++) {
			long chunkid = Numeric.toLong(bytes, off, 8);
			off += 8;
			newids[i] = chunkid;
		}
		
		Logger.info("CommandPool.optimize, old chunkid size:%d, new chunkid size:%d",
				oldids.length, newids.length);

		// update index and relogin
		Launcher.getInstance().setUpdateModule(true);
		
		// call home, upgrade chunk
		SiteHost home = Launcher.getInstance().getHome();
		HomeClient client = super.bring(home);
		if(client == null) {
			Logger.error("CommandPool.optimize, cannot connect to home site %s", home);
			return false;
		}
		SiteHost local = Launcher.getInstance().getLocal().getHost();
		boolean success = false;
		try {
			success = client.upgrade(local, space, oldids, newids);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		complete(client);

		Logger.note(success, "CommandPool.optimize, upgrade '%s' to home site %s", space, home);
		return success;
	}
	
	/**
	 * load index
	 * @param space
	 * @return
	 */
	private boolean inrush(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		int ret = Install.inrush(db, table);
		boolean success = (ret >= 0);
		Logger.note(success, "CommandPool.inrush, load '%s' result code %d", space, ret);
		return success;
	}

	/**
	 * clear index
	 * @param space
	 * @return
	 */
	private boolean uninrush(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		int ret = Install.uninrush(db, table);
		boolean success = (ret >= 0);
		Logger.note(success, "CommandPool.uninrush, stop '%s' result code %d", space, ret);
		return success;
	}

	/**
	 * load chunk
	 * @param space
	 * @return
	 */
	private boolean afflux(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		int ret = Install.afflux(db, table);
		boolean success = (ret >= 0);
		Logger.note(success, "CommandPool.afflux, stop '%s' result code %d", space, ret);
		return success;
	}

	/**
	 * clear chunk
	 * @param space
	 * @return
	 */
	private boolean unafflux(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		int ret = Install.unafflux(db, table);
		boolean success = (ret >= 0);
		Logger.note(success, "CommandPool.unafflux, stop '%s' result code %d", space, ret);
		return success;
	}
}