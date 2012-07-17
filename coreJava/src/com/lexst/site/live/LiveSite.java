/**
 * 
 */
package com.lexst.site.live;

import java.util.*;

import com.lexst.db.account.*;
import com.lexst.db.schema.*;
import com.lexst.site.*;

public class LiveSite extends Site {
	private static final long serialVersionUID = 1L;
	
	// user account
	private User user;
	// user table space set
	private List<Space> array = new ArrayList<Space>();
	
	private String algo;
	
	/**
	 * 
	 */
	public LiveSite() {
		super(Site.LIVE_SITE);
	}
	
	/**
	 * @param site
	 */
	public LiveSite(LiveSite site) {
		super(site);
		array = new ArrayList<Space>(site.array);
	}

	public void setUser(String username, String password) {
		user = new User(username);
		user.setTextPassword(password);
	}

	public void setUser(User account) {
		user = new User(account);
	}

	public User getUser() {
		return user;
	}
	
	public void setAlgorithm(String s) {
		this.algo = s;
	}
	public String getAlgorithm() {
		return this.algo;
	}

	public boolean add(Space space) {
		if(array.contains(space)) {
			return false;
		}
		return array.add(space);
	}

	public boolean remove(Space space) {
		return array.remove(space);
	}

	public boolean contains(Space space) {
		return array.contains(space);
	}

	public Collection<Space> list() {
		return array;
	}
	
	public void clear() {
		array.clear();
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public int size() {
		return array.size();
	}
}
