/**
 * 
 */
package com.lexst.algorithm.marshaleduce;

import com.lexst.site.build.*;
import com.lexst.util.host.*;
import com.lexst.util.naming.Naming;

public interface BuildInbox {

	void setLogin(boolean f);

	SiteHost getHome();

	BuildSite getLocal();

	boolean removeTask(Naming naming);
}