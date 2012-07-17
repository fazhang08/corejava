/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * a column index set
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 12/7/2009
 * 
 * @see com.lexst.db.view
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.view;

import java.util.*;

import com.lexst.db.index.range.*;
import com.lexst.db.statement.*;
import com.lexst.util.host.*;

/**
 *
 * 某一列的所有索引集合
 */
public interface View {

	boolean add(SiteHost host, IndexRange index);

	int remove(SiteHost host, long chunkId);

	int remove(SiteHost host);

	List<Long> delete(SiteHost host);

	Set<Long> find(Condition condi);

	Set<Long> getChunkIds();
	
	boolean isEmpty();

	int size();
}