/**
 * 
 */
package com.lexst.remote.client.work;

import java.io.*;
import java.lang.reflect.*;

import com.lexst.db.statement.*;
import com.lexst.fixp.*;
import com.lexst.log.client.*;
import com.lexst.remote.*;
import com.lexst.remote.client.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.visit.naming.work.*;
import com.lexst.util.LockArray;

public class WorkClient extends ThreadClient implements WorkVisit {

	private static Method methodNothing;
	
	private static Method methodGetRefreshTime;
	
	private static Method methodLogin;
	private static Method methodLogout;
	private static Method methodRelogin;
	private static Method methodRefresh;
	
	static {
		try {
			methodNothing = (WorkVisit.class).getMethod("nothing", new Class<?>[0]);
			
			methodGetRefreshTime = (WorkVisit.class).getMethod("getRefreshTime", new Class<?>[0]);
			
			methodLogin = (WorkVisit.class).getMethod("login", new Class<?>[] { SocketHost.class, SiteHost.class });
			methodLogout = (WorkVisit.class).getMethod("logout", new Class<?>[] { SocketHost.class });
			methodRelogin = (WorkVisit.class).getMethod("relogin", new Class<?>[] { SocketHost.class, SiteHost.class });
			methodRefresh = (WorkVisit.class).getMethod("refresh", new Class<?>[] { SocketHost.class });
		} catch (NoSuchMethodException exp) {
			throw new NoSuchMethodError("stub class initialization failed");
		}
	}

	private LockArray<WorkCommand> array = new LockArray<WorkCommand>();

	/**
	 * 
	 */
	public WorkClient(boolean stream) {
		this(stream, WorkVisit.class.getName());
	}
	
	/**
	 * @param interfaceName
	 */
	public WorkClient(boolean stream, String interfaceName) {
		super(stream, interfaceName);
	}
	
	/**
	 * @param remote
	 */
	public WorkClient(SocketHost remote) {
		this(remote.getType() == SocketHost.TCP);
		this.setRemote(remote);
	}
		
	/* (non-Javadoc)
	 * @see com.lexst.visit.Visit#nothing()
	 */
	@Override
	public void nothing() throws VisitException {
		super.invoke(methodNothing, null);
		this.refreshTime();
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.work.WorkVisit#getRefreshTime()
	 */
	@Override
	public int getRefreshTime() throws VisitException {
		this.refreshTime();
		Object param = super.invoke(methodGetRefreshTime, null);
		return ((Integer) param).intValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.work.WorkVisit#login(com.lexst.util.host.SocketHost, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean login(SocketHost local, SiteHost monitor) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, monitor };
		Object param = super.invoke(methodLogin, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.work.WorkVisit#logout(com.lexst.util.host.SocketHost)
	 */
	@Override
	public boolean logout(SocketHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodLogout, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.work.WorkVisit#relogin(com.lexst.util.host.SocketHost, com.lexst.util.host.SiteHost)
	 */
	@Override
	public boolean relogin(SocketHost local, SiteHost monitor) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local, monitor };
		Object param = super.invoke(methodRelogin, params);
		return ((Boolean) param).booleanValue();
	}

	/* (non-Javadoc)
	 * @see com.lexst.visit.naming.work.WorkVisit#refresh(com.lexst.util.host.SocketHost)
	 */
	@Override
	public boolean refresh(SocketHost local) throws VisitException {
		this.refreshTime();
		Object[] params = new Object[] { local };
		Object param = super.invoke(methodRefresh, params);
		return ((Boolean) param).booleanValue();
	}
		
	public void dc(WorkDelegate delegate, DC dc, byte[] data) {
		array.add(new WorkCommand(delegate, dc, data));
		this.wakeup();
	}

	public void adc(WorkDelegate delegate, ADC adc, byte[] data) {
		array.add(new WorkCommand(delegate, adc, data));
		this.wakeup();
	}

	private byte[] readContent(Stream resp) {
		byte[] b = null;
		try {
			b = resp.readContent();
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
		return b;
	}
	
	private void addDCMessage(DC dc, Entity entity) {
		byte[] s = Apply.build(dc);
		int limit = 1024;
		for(int off = 0; off < s.length; ) {
			int len = (off + limit > s.length ? s.length - off : limit);
			byte[] b = new byte[len];
			System.arraycopy(s, off, b, 0, b.length);
			entity.addMessage(Key.DC_OBJECT, b);
			off += len;
		}
	}
	
	private void addADCMessage(ADC adc, Entity entity) {
		byte[] s = Apply.build(adc);
		int limit = 1024;
		for(int off = 0; off < s.length; ) {
			int len = (off + limit > s.length ? s.length - off : limit);
			byte[] b = new byte[len];
			System.arraycopy(s, off, b, 0, b.length);
			entity.addMessage(Key.ADC_OBJECT, b);
			off += len;
		}
	}

	/**
	 * packet mode
	 */
	private void dc_packet(WorkCommand cmd) {
		DC dc = (DC) cmd.object;
		WorkDelegate delegate = cmd.delegate;
		
		Command fixpcmd = new Command(Request.SQL, Request.SQL_DC);
		Packet request = new Packet(fixpcmd);
		this.addDCMessage(dc, request);
		request.setData(cmd.data);

		Packet resp = null;
		try {
			resp = super.executePacket(request);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}

		if (resp == null) {
			delegate.flushTo(null, 0, 0);
		} else {
			fixpcmd = resp.getCommand();
			if(fixpcmd.getResponse() != Response.ACCEPTED) {
				delegate.flushTo(null, 0 , 0);
			} else {
				// read all data
				byte[] b = resp.getData();
				if (b == null) {
					delegate.flushTo(null, 0, 0);
				} else {
					delegate.flushTo(b, 0, b.length);
				}
			}
		}
	}
	
	/**
	 * distribute computing
	 */
	private void dc_stream(WorkCommand cmd) {
		DC dc = (DC) cmd.object;
		WorkDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_DC);
		Stream request = new Stream(fixpcmd);
		this.addDCMessage(dc, request);
		request.setData(cmd.data);

		Stream resp = null;
		try {
			resp = super.executeStream(request, false);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}

		if (resp == null) {
			delegate.flushTo(null, 0, 0);
		} else {
			fixpcmd = resp.getCommand();
			if(fixpcmd.getResponse() != Response.ACCEPTED) {
				delegate.flushTo(null, 0 , 0);
			} else {
				// read all data
				byte[] b = readContent(resp);
				if (b == null) {
					delegate.flushTo(null, 0, 0);
				} else {
					delegate.flushTo(b, 0, b.length);
				}
			}
		}
	}

	private void adc_stream(WorkCommand cmd) {
		ADC adc = (ADC) cmd.object;
		WorkDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_ADC);
		Stream request = new Stream(fixpcmd);
		this.addADCMessage(adc, request);
		request.setData( cmd.data );

		Stream resp = null;
		try {
			resp = super.executeStream(request, false);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}

		if (resp == null) {
			delegate.flushTo(null, 0, 0);
		} else {
			fixpcmd = resp.getCommand();
			if(fixpcmd.getResponse() != Response.ACCEPTED) {
				delegate.flushTo(null, 0 , 0);
			} else {
				// read all data
				byte[] b = readContent(resp);
				if (b == null) {
					delegate.flushTo(null, 0, 0);
				} else {
					delegate.flushTo(b, 0, b.length);
				}
			}
		}
	}
	
	private void adc_packet(WorkCommand cmd) {
		ADC adc = (ADC) cmd.object;
		WorkDelegate delegate = cmd.delegate;

		Command fixpcmd = new Command(Request.SQL, Request.SQL_ADC);
		Packet request = new Packet(fixpcmd);
		this.addADCMessage(adc, request);
		request.setData(cmd.data);

		Packet resp = null;
		try {
			resp = super.executePacket(request);
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}

		if (resp == null) {
			delegate.flushTo(null, 0, 0);
		} else {
			fixpcmd = resp.getCommand();
			if (fixpcmd.getResponse() != Response.ACCEPTED) {
				delegate.flushTo(null, 0, 0);
			} else {
				// read all data
				byte[] b = resp.getData();
				if (b == null) {
					delegate.flushTo(null, 0, 0);
				} else {
					delegate.flushTo(b, 0, b.length);
				}
			}
		}
	}
	
	/**
	 * distribute computing
	 */
	private void do_dc(WorkCommand cmd) {
		if(isStream()) {
			dc_stream(cmd);
		} else {
			dc_packet(cmd);
		}
		this.refreshTime();
	}
	
	/**
	 * distribute computing
	 * @param cmd
	 */
	private void do_adc(WorkCommand cmd) {
		if(isStream()) {
			adc_stream(cmd);
		} else {
			adc_packet(cmd);
		}
		this.refreshTime();
	}
	
	private void subprocess() {
		while (array.size() > 0) {
			WorkCommand cmd = array.poll();
			if(cmd == null) {
				Logger.fatal("WorkClient.subprocess, null WorkCommand, size:%d", array.size());
				continue;
			}
			
			switch(cmd.object.getMethod()) {
			case BasicObject.DC_METHOD:
				do_dc(cmd);
				break;
			case BasicObject.ADC_METHOD:
				do_adc(cmd);
				break;
			}
		}
		super.unlock();
	}

	/**
	 * active connect
	 */
	private boolean active() {
		if(!isLocked()) return false;
		try {
			this.nothing();
		} catch (VisitException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlock();
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lexst.remote.client.ThreadClient#execute()
	 */
	@Override
	protected void execute() {
		while (!super.isInterrupted()) {
			if (array.size() > 0) {
				this.subprocess();
			} else {
				if (isRefreshTimeout(20000)) {
					if (!active()) { delay(500); continue;}
				}
				this.delay(2000);
			}
		}
	}

}