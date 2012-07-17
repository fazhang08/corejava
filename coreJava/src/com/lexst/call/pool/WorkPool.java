/**
 * 
 */
package com.lexst.call.pool;

import java.io.*;
import java.util.*;

import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.*;
import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.remote.client.home.*;
import com.lexst.remote.client.work.*;
import com.lexst.site.call.*;
import com.lexst.site.work.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.*;
import com.lexst.visit.*;

public class WorkPool extends LocalPool {
	
	private static WorkPool selfHandle = new WorkPool();
	
	/* naming object -> work site set */	
	private Map<Naming, SiteSet> mapNaming = new TreeMap<Naming, SiteSet>();
	
	/* site address -> connect set */
	private Map<SiteHost, ClientSet> mapClient = new TreeMap<SiteHost, ClientSet>();
	
	/* homesite notify, update all worksite */
	private boolean refresh;

	/**
	 * 
	 */
	private WorkPool() {
		super();
	}
	
	/**
	 * @return
	 */
	public static WorkPool getInstance() {
		return WorkPool.selfHandle;
	}
	
	public void refresh() {
		this.refresh = true;
		this.wakeup();
	}
	
	/**
	 * find a data client 
	 * @param host
	 * @return
	 */
	private WorkClient findClient(SiteHost host, boolean stream) {
		ClientSet set = null;
		super.lockMulti();
		try {
			set = mapClient.get(host);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}

		if (set != null && set.size() > 0) {
			WorkClient client = (WorkClient) set.lockNext();
			if (client != null) return client;
			if (set.size() >= ClientSet.LIMIT) {
				client = (WorkClient) set.next();
				client.locking();
				return client;
			}
		}
		
		boolean success = false;
		// connect to host
		SocketHost address = (stream ? host.getTCPHost() : host.getUDPHost());
		WorkClient client = new WorkClient(address);
		try {
			client.reconnect();
			success = true;
		} catch (IOException exp) {
			Logger.error(exp);
		}
		if (!success) {
			client.close();
			return null;
		}

		client.locking();	// locked client
		client.start(); 	// start client thread

		super.lockSingle();
		try {
			if(set == null) {
				set = new ClientSet();
				mapClient.put(host, set);
			}
			set.add(client);
		} catch(Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
		return client;
	}
	
	private WorkClient findClient(SiteHost host) {
		return findClient(host, true);
	}
	
	public byte[] adc(ADC adc, List<DCArea> files) {
		Naming naming = new Naming(adc.getToNaming());
		SiteSet set = null;
		super.lockMulti();
		try {
			set = mapNaming.get(naming);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if(set == null || set.isEmpty()) {
			Logger.error("WorkPool.adc, cannot find naming '%s'", naming);
			return null;
		}
		
		int sites = adc.getToSites();
		if (sites < 1 || sites > set.size()) {
			sites = set.size();
		}
		
		// 处理数据平衡
		DCModule module = new DCModule(files);
		DCTable[] tables = module.split(sites);

		WorkDelegate finder = new WorkDelegate(tables.length);

		for (int index = 0; index < tables.length; index++) {
			SiteHost host = set.next();
			WorkClient client = this.findClient(host);
			if (client == null) {
				Logger.error("WorkPool.adc, cannot find site %s", host);
				break;
			}
			byte[] data = tables[index].build();
			ADC clone_adc = adc.clone();
			finder.add(client, clone_adc, data);
		}
		
		if(finder.size() != tables.length) {
			Logger.error("WorkPool.adc, client not match!");
			finder.discontinue(true);
			return null;
		}
		
		// start job
		finder.execute();
		// wait job...
		finder.waiting();
		// get data
		byte[] data = finder.data();
		return data;
	}
	
	/**
	 * @param naming
	 * @return
	 */
	public List<SiteHost> find(String naming) {
		Naming s = new Naming(naming);
		List<SiteHost> array = new ArrayList<SiteHost>();
		super.lockMulti();
		try {
			SiteSet set = mapNaming.get(s);
			if (set != null && set.size() > 0) {
				array.addAll(set.list());
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		if(!array.isEmpty()) return array;
		
		// scan home site
		SiteHost[] hosts = null;
		HomeClient client = super.bring(false);
		if(client == null) return null;
		try {
			hosts = client.findWorkSite(naming);
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
		if(hosts == null) return null;

		super.lockSingle();
		try {
			mapNaming.put(s, new SiteSet(array));
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return array;
	}
	
	/**
	 * 
	 */
	private void refreshSite() {
		HomeClient client = super.bring(false);
		if (client == null) {
			Logger.error("WorkPool.refreshSite, cannot connect home-site:%s", home);
			return;
		}

		boolean error = true;
		WorkSite[] sites = null;
		try {
			sites = (WorkSite[]) client.batchWorkSite();
			error = false;
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		this.complete(client);
		
		if (error) {
			Logger.warning("WorkPool.refreshSite, visit error!");
			return;
		}
		
		Logger.debug("WorkPool.refreshSite, work site size:%d", (sites == null ? -1 : sites.length));

		List<SiteHost> allsite = new ArrayList<SiteHost>();
//		String[] namings = null;
		super.lockSingle();
		try {
			mapNaming.clear();
			for (int i = 0; sites != null && i < sites.length; i++) {
				SiteHost host = sites[i].getHost();
				allsite.add(host);
				for (Naming naming : sites[i].list()) {
					SiteSet set = mapNaming.get(naming);
					if (set == null) {
						set = new SiteSet();
						mapNaming.put(naming, set);
					}
					set.add(host);
				}
			}
			
//			List<Naming> a = new ArrayList<Naming>(mapNaming.keySet());
//			if (a.size() > 0) {
//				namings = new String[a.size()];
//				for (int i = 0; i < namings.length; i++) {
//					namings[i] = a.get(i).get();
//				}
//			}
			
			// release exclude client
			List<SiteHost> excludes = new ArrayList<SiteHost>();
			for (SiteHost host : mapClient.keySet()) {
				if (!allsite.contains(host)) excludes.add(host);
			}
			for (SiteHost host : excludes) {
				ClientSet set = mapClient.get(host);
				int size = set.size();
				for (int i = 0; i < size; i++) {
					WorkClient ws = (WorkClient) set.get(i);
					ws.stop();
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}

		CallSite site = super.callInstance.getLocal();
		// update all naming
		site.clearAggregateNaming();
		for(Naming naming: mapNaming.keySet()) {
			Logger.debug("WorkPool.refreshSite, add naming:%s", naming);
			site.addAggregateNaming(naming.toString());
		}
		// relogin
		super.callInstance.setOperate(BasicLauncher.RELOGIN);
	}
	
	private void check() {
		if (refresh) {
			this.refresh = false;
			this.refreshSite();
		}
	}
	
	/**
	 * stop all client
	 */
	private void stopClients() {
		if(mapClient.isEmpty()) return;
		
		super.lockSingle();
		try {
			ArrayList<SiteHost> array = new ArrayList<SiteHost>(mapClient.keySet());
			for (SiteHost host : array) {
				ClientSet set = mapClient.remove(host);
				int size = set.size();
				for (int i = 0; i < size; i++) {
					WorkClient client = (WorkClient) set.get(i);
					client.stop();
				}
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
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
		Logger.info("WorkPool.process, into...");

		this.delay(10000);
		refresh = true;
		
		while (!super.isInterrupted()) {
			this.check();
			this.delay(5000);
		}
		Logger.info("WorkPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.stopClients();
	}

}