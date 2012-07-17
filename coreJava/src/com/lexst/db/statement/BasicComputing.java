/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2011 lexst.com. All rights reserved
 * 
 * dc and adc basic class
 * 
 * @author scott.liu lexst@126.com
 * 
 * @version 1.0 6/12/2011
 * 
 * @see com.lexst.db.statement
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.statement;

import java.util.*;

import com.lexst.db.schema.*;

/**
 * distributed computing syntax
 * 
 * DC|ADC 	FROM naming:[diffuse-name] blocks:[column-name%digit] sites:[digit] query:"select syntax" values:"key1=value1,key2=value2..." 
 * 			TO naming:[aggreage-name] sites:[digit] values:"k1=v1,k2=v2..." 
 * 			COLLECT naming:[user-task] show:[schema.table] writeto:[local file] 
 */
public class BasicComputing extends BasicObject {
	
	private static final long serialVersionUID = -2225982041065972370L;
	
	/* distributed computing identity */
	private long identity;

	/* diffuse params set */
	private String from_naming;
	private int from_sites;
	private String from_blocks_name;
	private int from_blocks_size;
	private Select from_select;
	private List<DCValue> from_values = new ArrayList<DCValue>();

	/* aggregate params set */
	private String to_naming;
	private int to_sites;
	private List<DCValue> to_values = new ArrayList<DCValue>();

	/* collect params set */
	private String collect_naming;
	private Space collect_space;
	private String collect_writeto;

	/**
	 * @param method
	 */
	protected BasicComputing(byte method) {
		super(method);
	}

	/**
	 * set or get adc identity
	 * @param id
	 */
	public void setIdentity(long id) {
		this.identity = id;
	}

	public long getIdentity() {
		return this.identity;
	}

	/* diffuse functions */
	public void setFromNaming(String s) {
		this.from_naming = s;
	}

	public String getFromNaming() {
		return this.from_naming;
	}
	
	public void setFromSites(int i) {
		this.from_sites = i;
	}
	public int getFromSites() {
		return this.from_sites;
	}
	
	public void setFromBlocks(String column, int size) {
		this.from_blocks_name = column;
		this.from_blocks_size = size;
	}

	public String getFromBlocksName() {
		return this.from_blocks_name;
	}

	public int getFromBlocksSize() {
		return this.from_blocks_size;
	}

	public void setFromSelect(Select s) {
		this.from_select = s;
	}

	public Select getFromSelect() {
		return this.from_select;
	}

	public void addFromValues(Collection<DCValue> values) {
		this.from_values.addAll(values);
	}

	public void addFromValue(DCValue value) {
		from_values.add(value);
	}
	
	public List<DCValue> listFromValues() {
		return from_values;
	}

	public DCValue findFromValue(String name, int index) {
		if (index < 0) {
			throw new IllegalArgumentException("invalid index");
		}
		int count = 0;
		for (DCValue value : from_values) {
			if (value.getName().equalsIgnoreCase(name)) {
				if (count == index) return value;
				count++;
			}
		}
		return null;
	}
	
	public DCValue findFromValue(String name) {
		return findFromValue(name, 0);
	}
	
	/* aggregate functions */
	public void setToNaming(String s) {
		this.to_naming = s;
	}

	public String getToNaming() {
		return this.to_naming;
	}

	public void setToSites(int size) {
		this.to_sites = size;
	}

	public int getToSites() {
		return this.to_sites;
	}

	public void addToValues(Collection<DCValue> values) {
		this.to_values.addAll(values);
	}

	public void addToValue(DCValue value) {
		to_values.add(value);
	}
	
	public List<DCValue> listToValues() {
		return to_values;
	}
	
	public DCValue findToValue(String name, int index) {
		if (index < 0) {
			throw new IllegalArgumentException("invalid index");
		}
		int count = 0;
		for (DCValue value : to_values) {
			if (value.getName().equalsIgnoreCase(name)) {
				if (count == index) return value;
				count++;
			}
		}
		return null;
	}
	
	public DCValue findToValue(String name) {
		return findToValue(name, 0);
	}

	/* collect functions */
	public void setCollectNaming(String s) {
		this.collect_naming = s;
	}

	public String getCollectNaming() {
		return this.collect_naming;
	}

	public void setCollectSpace(Space s) {
		this.collect_space = new Space(s);
	}

	public Space getCollectSpace() {
		return this.collect_space;
	}

	public void setCollectWriteto(String s) {
		this.collect_writeto = s;
	}

	public String getCollectWriteto() {
		return this.collect_writeto;
	}

	protected void set(BasicComputing bc) {
		bc.identity = this.identity;
		// diffuse set
		bc.from_naming = this.from_naming;
		bc.from_sites = this.from_sites;
		bc.from_blocks_name = this.from_blocks_name;
		bc.from_blocks_size = this.from_blocks_size;
		bc.from_select = this.from_select;
		bc.from_values.addAll(this.from_values);
		// aggregate set
		bc.to_naming = this.to_naming;
		bc.to_sites = this.to_sites;
		bc.to_values.addAll(this.to_values);
		// collect set
		bc.collect_naming = this.collect_naming;
		if (this.collect_space != null) {
			bc.collect_space = new Space(this.collect_space);
		}
		bc.collect_writeto = this.collect_writeto;
	}

}
