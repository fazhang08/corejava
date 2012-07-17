/**
 *
 */
package com.lexst.site.call;

import java.util.*;

import com.lexst.db.schema.*;
import com.lexst.site.*;
import com.lexst.util.naming.Naming;

public class CallSite extends Site {

	private static final long serialVersionUID = 1L;

	private List<Space> spaces = new ArrayList<Space>();
	
//	private List<Naming> names = new ArrayList<Naming>();
	
	private Set<Naming> datas = new TreeSet<Naming>();

	private Set<Naming> works = new TreeSet<Naming>();
	
	/**
	 *
	 */
	public CallSite() {
		super(Site.CALL_SITE);
	}
	
	public boolean addDiffuseNaming(String naming) {
		return datas.add(new Naming(naming));
	}
	
	public boolean removeDiffuseNaming(String naming) {
		return datas.remove(new Naming(naming));
	}
	
	public boolean updateDiffuseNaming(String naming) {
		removeDiffuseNaming(naming);
		return addDiffuseNaming(naming);
	}
	
	public int updateDiffuseNaming(String[] all) {
		int count = 0;
		for (int i = 0; all != null && i < all.length; i++) {
			this.removeDiffuseNaming(all[i]);
			if (this.addDiffuseNaming(all[i])) count++;
		}
		return count;
	}
	
	public Set<Naming> listDiffuseNaming() {
		return datas;
	}
	
	public void clearDiffuseNaming() {
		datas.clear();
	}
	
	/**
	 * below is aggreage task naming
	 * @param naming
	 * @return
	 */
	public boolean addAggregateNaming(String naming) {
		return works.add(new Naming(naming));
	}
	
	public boolean removeAggregateNaming(String naming) {
		return works.remove(new Naming(naming));
	}
	
	public boolean updateAggregateNaming(String naming) {
		removeAggregateNaming(naming);
		return addAggregateNaming(naming);
	}
	
	public int updateAggregateNaming(String[] all) {
		int count = 0;
		for (int i = 0; all != null && i < all.length; i++) {
			this.removeAggregateNaming(all[i]);
			if (this.addAggregateNaming(all[i])) count++;
		}
		return count;
	}
	
	public Set<Naming> listAggregateNaming() {
		return works;
	}
	
	public void clearAggregateNaming() {
		works.clear();
	}
	
	public List<Naming> listAllNaming() {
		List<Naming> array = new ArrayList<Naming>();
		array.addAll(datas);
		array.addAll(works);
		return array;
	}
	
//	public boolean addNaming(String naming) {
//		Naming s = new Naming(naming);
//		if(names.contains(s)) return false;
//		return names.add(s);
//	}
//	
//	public boolean removeNaming(String naming) {
//		return names.remove(new Naming(naming));
//	}
//	
//	public int updateNaming(String[] s) {
//		this.names.clear();
//		int count = 0;
//		for (int i = 0; s != null && i < s.length; i++) {
//			if (addNaming(s[i])) count++;
//		}
//		return count;
//	}
//	
//	public List<Naming> listNaming() {
//		return names;
//	}

	public boolean add(Space space) {
		if (!spaces.contains(space)) {
			return spaces.add(space);
		}
		return false;
	}

	public boolean remove(Space space) {
		return spaces.remove(space);
	}

	public boolean contains(Space space) {
		return spaces.contains(space);
	}

	public Collection<Space> list() {
		return spaces;
	}

	public boolean isEmpty() {
		return spaces.isEmpty();
	}

	public int size() {
		return spaces.size();
	}

}