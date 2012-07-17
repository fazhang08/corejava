/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;
import java.util.*;

import com.lexst.data.*;
import com.lexst.db.chunk.*;
import com.lexst.db.schema.*;
import com.lexst.log.client.*;
import com.lexst.remote.client.build.*;
import com.lexst.remote.client.home.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

/**
 *
 * 接收来自HOME的通知,更新BUILD节点上的CHUNK数据.更新完成,通过HOME通知对应的"从节点"
 * 只限主节点操作
 * 
 */
public class UpdatePool extends LocalPool {
	
	private static UpdatePool selfHandle = new UpdatePool();

	private LockMap<Space, Update> mapUpdate = new LockMap<Space, Update>();
	
	/**
	 * 
	 */
	private UpdatePool() {
		super();
	}

	/**
	 * @return
	 */
	public static UpdatePool getInstance() {
		return UpdatePool.selfHandle;
	}

	/**
	 * return a build client handle
	 * 
	 * @return
	 */
	private BuildClient petition(SiteHost host) {
		SocketHost address = host.getTCPHost();
		BuildClient client = new BuildClient(true, address);
		for (int i = 0; i < 3; i++) {
			try {
				client.reconnect();
				return client;
			} catch (IOException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			client.close();
			this.delay(1000);
		}
		return null;
	}

	/**
	 * @param client
	 */
	private void complete(BuildClient client) {
		if (client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
	}

	/**
	 * save object
	 * @param space
	 * @param host
	 */
	public boolean revive(Space space, SiteHost host) {
		boolean prime = Launcher.getInstance().getLocal().isPrime();
		if(!prime) {
			Logger.error("UpdatePool.revive, cannot accpeted '%s', local is slave site", space);
			return false;
		}
		Table table = Launcher.getInstance().findTable(space);
		if (table == null) {
			Logger.error("UpdatePool.revive, cannot find '%s'", space);
			return false;
		}

		Logger.info("UpdatePool.revive, accepted '%s'", space);

		Update object = mapUpdate.get(space);
		if (object == null) {
			object = new Update(space);
			mapUpdate.put(space, object);
		}
		object.add(host);
		this.wakeup();
		return true;
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
		Logger.info("UpdatePool.process, into...");
		while (!isInterrupted()) {
			this.update();
			this.delay(20000);
		}
		Logger.info("UpdatePool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}

	private void update() {
		if(mapUpdate.isEmpty()) {
			return;
		}
		// find home site
		SiteHost home = Launcher.getInstance().getHome();
		HomeClient homeClient = bring(home);
		if (homeClient == null) {
			Logger.error("UpdatePool.update, cannot connect home site %s", home);
			return;
		}
		// find all build site
		SiteHost[] hosts = null;
		try {
			hosts = homeClient.findBuildSite();
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		if (hosts == null || hosts.length == 0) {
			Logger.error("UpdatePool.update, cannot find build site");
			complete(homeClient);
			return;
		}
		
		ArrayList<Space> spaces = new ArrayList<Space>(mapUpdate.keySet());
		
		// check finished
		for (Space space : spaces) {
			Update object = mapUpdate.get(space);
			object.reset();
		}
		
		// check build status
		for(SiteHost host: hosts) {
			BuildClient buildClient = this.petition(host);
			if(buildClient == null) {
				complete(homeClient);
				Logger.error("UpdatePool.update, cannot connect build site %s", host);
				return; // exit, retry
			}
			for (Space space : spaces) {
				Update object = mapUpdate.get(space);
				try {
					boolean found = buildClient.isBuilding(space);
					if (found) object.add(1);
					continue;
				} catch (VisitException exp) {
					Logger.error(exp);
				} catch (Throwable exp) {
					Logger.fatal(exp);
				}
				// when error, exit
				complete(buildClient);
				complete(homeClient);
				Logger.error("UpdatePool.update, occur error, exit!");
				return;
			}
			// close it
			complete(buildClient);
		}
		
		// this is finished status, download it
		for (Space space : spaces) {
			Update object = mapUpdate.get(space);
			if(object.count() == 0) {
				boolean success = execute(homeClient, object);
				// when success, saven and delete it
				if (success) {
					mapUpdate.remove(space);
				}
			}
		}
		// close it
		complete(homeClient);
		// update index and relogin
		Launcher.getInstance().setUpdateModule(true);
	}
	
	/**
	 * 	操作流程
	 *  1. 向HOME节点请求全部BUILD节点
	 *  2. 询问每一个BUILD节点，是否正在进行某个SPACE的重构操作。如有重构，统计加1
	 *  3. 如重构统计为0，表示集群对这个SPACE的重构已经完成，可以对这个空间进行下载分发操作
	 *  
	 *  本地下载操作
	 *  1. 取得集群所有BUILD节点重构后的CHUNK集合
	 *  2. 清除本地所有的CHUNK
	 *  3. 每下载一个CHUNK，询问HOME是否可以下载
	 * 	
	 * @param object
	 */
	private boolean execute(HomeClient homeClient, Update object) {
		Space space = object.getSpace();
		Logger.info("UpdatePool.execute, space is '%s'", space);
		
		// get old chunkid
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		long[] oldids = Install.getChunkIds(db, table);
		
		// delete all chunk
		this.clear(space);
		
		// download chunk info, and choose download
		Map<SiteHost, List<Chunk>> mapInfo = new HashMap<SiteHost, List<Chunk>>();
		for (SiteHost host : object.list()) {
			BuildClient buildClient = petition(host);
			if (buildClient == null) {
				Logger.error("UpdatePool.execute, cannot connect to build site %s", host);
				return false;
			}
			// add
			boolean success = false;
			try {
				Chunk[] all = buildClient.findChunkInfo(space);
				if (all != null) {
					ArrayList<Chunk> array = new ArrayList<Chunk>();
					for (Chunk info : all) {
						array.add(info);
					}
					mapInfo.put(host, array);
					success = true;
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			// close client
			complete(buildClient);
			
			if(!success) {
				Logger.info("UpdatePool.execute, cannot get chunk info!");
				return false;
			}
		}
		
		// 询问HOME，某个CHUNK是否可以下载
		for (SiteHost host : mapInfo.keySet()) {
			List<Chunk> array = mapInfo.get(host);
			
			BuildClient client = petition(host);
			if (client == null) {
				Logger.error("UpdatePool.execute, cannot connect to build site %s", host);
				return false;
			}
			
			for (Chunk info : array) {
				long chunkid = info.getId();
				long length = info.getLength();
				long modified = info.getLastModified();
				boolean allow = false;
				try {
					// query home, allow download?
					allow = homeClient.accede(space, chunkid, length, modified);
				} catch (VisitException exp) {
					Logger.error(exp);
				} catch (Throwable exp) {
					Logger.fatal(exp);
				}
				// if allow, download chunk
				if (allow) {
					boolean success = download(5, client, space, info);
					Logger.note(success, "UpdatePool.execute, download '%s' - %s - %x", space, host, chunkid);
				}
			}
			// close client
			this.complete(client);
		}
		// get new chunkid 
		long[] newids = Install.getChunkIds(db, table);
		
		// upgrade notify
		boolean success = notify(homeClient, space, oldids, newids);
		Logger.note(success, "UpdatePool.execute, send '%s' message", space);
		return success;
	}
	
	private void clear(Space space) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		long[] localIds = Install.getChunkIds(db, table);
		
		if (localIds == null || localIds.length == 0) {
			return;
		}
		// delete all chunk
		for (long chunkid : localIds) {
			int ret = Install.deleteChunk(db, table, chunkid);
			boolean success = (ret == 0);
			Logger.note(success, "UpdatePool.clear, delete chunk '%s' - %x", space, chunkid);
		}
	}
	
	private boolean deleteFile(String filename) {
		File file = new File(filename);
		if(file.exists() || file.isFile()) {
			return file.delete();
		}
		return false;
	}
	
	private boolean download(int num, BuildClient client, Space space, Chunk info) {
		boolean success = false;
		for (int i = 0; i < num; i++) {
			if (i > 0) {
				this.complete(client);
				this.delay(2000);
			}
			success = download(client, space, info);
			if (success) break;
		}
		return success;
	}
	
	private boolean download(BuildClient client, Space space, Chunk info) {
		byte[] db = space.getSchema().getBytes();
		byte[] table = space.getTable().getBytes();
		
		// check disk space
		byte[] path = null;
		for (int index = 0; true; index++) {
			path = Install.getChunkPath(db, table, index);
			if (path == null || path.length == 0) {
				Logger.error("UpdatePool.download, cannot get '%s' directory", space);
				return false;
			}
			File dir = new File(new String(path));
			if (dir.isDirectory() && dir.getFreeSpace() > info.getLength()) {
				break;
			}
		}
		// build chunk name
		long chunkid = info.getId();
		String filename = buildFilename(new String(path), chunkid);
		// download chunk
		BuildDownloader downloader = new BuildDownloader();
		boolean success = downloader.execute(client, space, chunkid, filename);
		Logger.note(success, "UpdatePool.download, download '%s' - %x to %s", space, chunkid, filename);
		// when success, load chunk
		if (success) {
			byte[] b = filename.getBytes();
			int ret = Install.loadChunk(db, table, b);
			success = (ret >= 0);
			Logger.note(success, "UpdatePool.download, load chunk '%s' - %x - %s", space, chunkid, filename);
		} else {
			// occur error, delete file
			boolean b = deleteFile(filename);
			Logger.error("UpdatePool.donwload, occur error, delete file %s %s", filename, (b ? "success" : "failed"));
		}
		if(success) {
			int ret = Install.toPrime(db, table, chunkid);
			success = (ret == 0);
			Logger.note(success, "UpdatePool.download, to prime '%s' - %x", space, chunkid);
		}
		return success;
	}

	/**
	 * call home site, upgrade chunk (only slave site)
	 * @param client
	 * @param space
	 * @param oldids
	 * @param newids
	 * @return
	 */
	private boolean notify(HomeClient client, Space space, long[] oldids, long[] newids) {
		SiteHost local = Launcher.getInstance().getLocal().getHost();
		boolean success = false;
		try {
			success = client.upgrade(local, space, oldids, newids);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return success;
	}

}