package net;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;

public class HtmlParser 
{

	public static String getURL(String sSource, String strTagName)
	{
		String sURL = "";
		
		Parser parser = null;
		
		NodeList innerNodes = null;
		
		try
		{
			
			parser = Parser.createParser(sSource, "UTF-8");
			
			NodeFilter innerFilter = new TagNameFilter (strTagName);
			
			NodeFilter filter = new HasChildFilter(innerFilter);
			
			NodeList nodes = parser.extractAllNodesThatMatch(filter); 
			
			for(int i = 0 ; i < nodes.size() ; i++)
			{
				Node textnode = (Node)  nodes.elementAt(i);
				NodeList iNodes = textnode.getChildren();
				
				for(int j = 0 ; j<iNodes.size(); j++)
				{
				  	Node node = iNodes.elementAt(j);
				  	
					if(node.getText().indexOf("href")>=0)
					{
						sURL = node.getText();
						
						sURL = sURL.substring(7);
						
						return sURL;
					}
				}
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return sURL;
		
	}
}
