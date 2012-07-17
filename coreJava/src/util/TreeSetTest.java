package util;

import java.util.*;

public class TreeSetTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		TreeSet<String> tree = new TreeSet<String>();
		
		tree.add("Beauty");
		tree.add("City,");
		tree.add("Beaury");
		tree.add("Life");
		
		Iterator<String> it = tree.iterator();
		
		while(it.hasNext())
		{
			System.out.println(it.next());
		}

	}

}
