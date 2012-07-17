/**
 * 
 */
package com.lexst.live.pool;

import java.io.*;

import com.lexst.log.client.*;
import com.lexst.remote.client.call.*;
import com.lexst.remote.client.top.*;
import com.lexst.site.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.util.host.SiteHost;
import com.lexst.util.host.SocketHost;
import com.lexst.visit.*;


public class SQLCaller  {

	private static int insertIndex = 0;
	
	/**
	 * 
	 */
	public SQLCaller() {
		super();
	}
	
	/**
	 * apply a top client handle
	 * @return
	 * @throws VisitException
	 */
	private TopClient solicit(SocketHost remote) {
		TopClient client = new TopClient(true);
		try {
			client.connect(remote);
			return client;
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return null;
	}

	/**
	 * apply a top client handle
	 * @return
	 * @throws VisitException
	 */
	private CallClient fetch(SocketHost remote) {
		CallClient client = new CallClient(true);
		try {
			client.connect(remote);
			return client;
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return null;
	}

	/**
	 * release connect and close socket
	 * @param client
	 */
	private void complete(CallClient client) {
		if (client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			Logger.error(exp);
		}
	}
	
	/**
	 * release connect and close socket
	 * @param client
	 */
	private void complete(TopClient client) {
		if (client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {

		}
	}
	
	private SiteHost[] selectCallSite(SiteHost top, Site local, Space space) {
		TopClient client = solicit(top.getTCPHost());
		if (client == null) return null;

		SiteHost[] hosts = null;
		try {
			hosts = client.selectCallSite(space.getSchema(), space.getTable());
		} catch (VisitException exp) {
			Logger.error(exp);
		}
		this.complete(client);

		return hosts;
	}
	
	private SiteHost[] selectCallSite(SiteHost top, Site local, String naming) {
		TopClient client = solicit(top.getTCPHost());
		if(client == null) return null;
		
		SiteHost[] hosts = null;
		try {
			hosts = client.selectCallSite(naming);
		} catch (VisitException exp) {
			Logger.error(exp);
		}
		this.complete(client);
		return hosts;
	}
	
	private SiteHost[] selectCallSite(SiteHost top, Site local, String naming, Space space) {
		TopClient client = solicit(top.getTCPHost());
		if(client == null) return null;
		
		SiteHost[] hosts = null;
		try {
			hosts = client.selectCallSite(naming, space);
		} catch (VisitException exp) {
			Logger.error(exp);
		}
		this.complete(client);
		return hosts;
	}

	/**
	 * @param hosts
	 * @param select
	 * @param dc
	 * @param findWorkIP
	 * @return
	 */
	public byte[] select(SiteHost top, Site local, Select select) {
		Space space = select.getSpace();
		SiteHost[] hosts = selectCallSite(top, local, space);
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.select, cannot find call site");
			return null;
		}
		
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 512);
		
		for(int i = 0; i < hosts.length; i++) {
			CallClient client = this.fetch(hosts[i].getTCPHost());
			if(client == null) {
				Logger.error("SQLCaller.select, cannot connect call site %s", hosts[i]);
				continue;
			}
			try {
				byte[] data = client.select(select);
				if(data != null  && data.length>0) {
					buff.write(data, 0, data.length);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			}
			this.complete(client);
		}
		if(buff.size() == 0) return null;
		return buff.toByteArray();
	}
	
	/**
	 * @param top
	 * @param local
	 * @param adc
	 * @return
	 */
	public byte[] adc(SiteHost top, Site local, ADC adc) {
		// chocie call site
		String naming = adc.getFromNaming();
		SiteHost[] hosts = null;
		if (adc.getFromSelect() != null) {
			Space space = adc.getFromSelect().getSpace();
			hosts = selectCallSite(top, local, naming, space);
		} else {
			hosts = selectCallSite(top, local, naming);
		}
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.adc, cannot find call site");
			return null;
		}
		
		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 1024);
		
		for(int i = 0; i < hosts.length; i++) {
			CallClient client = fetch(hosts[i].getTCPHost());
			if(client == null) {
				Logger.error("SQLCaller.adc, cannot connect call site %s", hosts[i]);
				continue;
			}
			try {
				byte[] data = client.adc(adc);
				if (data != null && data.length > 0) {
					buff.write(data, 0, data.length);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			}
			this.complete(client);
		}
		if(buff.size() == 0) return null;
		return buff.toByteArray();
	}

	/**
	 * @param top
	 * @param local
	 * @param dc
	 * @return
	 */
	public byte[] dc(SiteHost top, Site local, DC dc) {
		String naming = dc.getFromNaming();
		SiteHost[] hosts = null;
		if (dc.getFromSelect() != null) {
			Space space = dc.getFromSelect().getSpace();
			hosts = selectCallSite(top, local, naming, space);
		} else {
			hosts = selectCallSite(top, local, naming);
		}
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.dc, cannot find call site");
			return null;
		}

		ByteArrayOutputStream buff = new ByteArrayOutputStream(1024 * 512);
		
		for(int i = 0; i < hosts.length; i++) {
			CallClient client = this.fetch(hosts[i].getTCPHost());
			if(client == null) {
				Logger.error("SQLCaller.dc, cannot connect call site %s", hosts[i]);
				continue;
			}
			try {
				byte[] data = client.dc(dc);
				if (data != null && data.length > 0) {
					buff.write(data, 0, data.length);
				}
			} catch (VisitException exp) {
				Logger.error(exp);
			}
			this.complete(client);
		}
		if(buff.size() == 0) return null;
		return buff.toByteArray();
	}
		
	public long delete(SiteHost top, Site local, Delete delete) {
		Space space = delete.getSpace();
		SiteHost[] hosts = selectCallSite(top, local, space);
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.delete, cannot find call site");
			return -1;
		}
		
		long count = 0;
		for(int i = 0; i < hosts.length; i++) {
			CallClient client = this.fetch(hosts[i].getTCPHost());
			if(client == null) {
				Logger.error("SQLCaller.delete, cannot connect call site %s", hosts[i]);
				continue;
			}
			
			try {
				long num = client.delete(delete);
				if(num >0) count += num;
			} catch (VisitException exp) {
				Logger.error(exp);
			}
			this.complete(client);	
		}
		return count;
	}
		
	private static synchronized int index(int num) {
		if( insertIndex >= num) insertIndex = 0;
		return insertIndex = 0;
	}
	
	public int insert(SiteHost top, Site local, Insert insert) {
		Space space = insert.getSpace();
		SiteHost[] hosts = selectCallSite(top, local, space);
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.insert, cannot find call site");
			return -1;
		}

		int index = SQLCaller.index(hosts.length);

		CallClient client = this.fetch(hosts[index].getTCPHost());
		if(client == null) {
			Logger.error("SQLCaller.insert, cannot connect call site %s", hosts[index]);
			return -1;
		}

		int count = 0;
		try {
			count = client.insert(insert, true);
		} catch (VisitException exp) {
			Logger.error(exp);
		}
		this.complete(client);	

		return count;
	}
	
	public int inject(SiteHost top, Site local, Inject inject) {
		Space space = inject.getSpace();
		SiteHost[] hosts = selectCallSite(top, local, space);
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.inject, cannot find call site");
			return -1;
		}

		int index = SQLCaller.index(hosts.length);

		CallClient client = this.fetch(hosts[index].getTCPHost());
		if(client == null) {
			Logger.error("SQLCaller.inject, cannot connect call site %s", hosts[index]);
			return -1;
		}

		int count = 0;
		try {
			count = client.inject(inject, true);
		} catch (VisitException exp) {
			Logger.error(exp);
		}
		this.complete(client);	

		return count;
	}
	
	public long update(SiteHost top, Site local, Update update) {
		Space space = update.getSpace();
		SiteHost[] hosts = selectCallSite(top, local, space);
		if (hosts == null || hosts.length == 0) {
			Logger.error("SQLCaller.update, cannot find call site");
			return -1;
		}
		
		long count = 0;
		for(int i = 0; i < hosts.length; i++) {
			CallClient client = this.fetch(hosts[i].getTCPHost());
			if(client == null) {
				Logger.error("SQLCaller.update, cannot connect call site %s", hosts[i]);
				continue;
			}
			
			try {
				long num = client.update(update);
				if (num > 0) count += num;
			} catch (VisitException exp) {
				Logger.error(exp);
			}
			this.complete(client);	
		}
		return count;
	}
}
