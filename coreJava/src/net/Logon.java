package net;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
public class Logon {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception 
	{
		Logon lg = new Logon("52310195","529155815");
		lg.logon();
    }

	public Logon()
	{
	}
    public Logon(String userName, String passWord)
    {
    	this.sUserName = userName;
    	this.sPassWord = passWord;
    }
	public void logon()
	{
		 String locationURL = "";
		 
		 Header header = null;
		 
		 GetMethod getMethod = null;
		 
		 String rcs = "";
		 
		 byte[] responseBody = null;
		 
		 String sHtml = "";
		 
		try
		{
			  MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			  
			  //创建HTTP客户端 实例
			  HttpClient client = new HttpClient(connectionManager);
			  
			  //创建POST请求，传入请求的Action
			  PostMethod postMethod = new PostMethod("http://u.uc.cn/?uc_param_str=sspfligiwinieisi&r=&s1=2&302=1&ss=1440x870&pf=31");
			  
			  //设置表单域
			  NameValuePair[] postData = new NameValuePair[2];
			  postData[0] = new NameValuePair("username",this.sUserName);
			  postData[1] = new NameValuePair("password",this.sPassWord);
			  
			  //将表单的值放入postMethod中
			  postMethod.addParameters(postData);
			  
			  //发送请求 服务端返回状态码
			  int statusCode = client.executeMethod(postMethod);
			 // System.out.println(statusCode);
			  postMethod.releaseConnection();//关闭POST请求
			
			  
			  //读取转向地址
			 
			  if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
			    statusCode == HttpStatus.SC_MOVED_TEMPORARILY) 
			  {
			   header = postMethod.getResponseHeader("location");
			   
			   if(header != null)
			   {
			    locationURL = header.getValue();
			    System.out.println(locationURL);
			   }else
			   {
			    return;
			   }
			   
			   getMethod = new GetMethod(locationURL);
			   statusCode = client.executeMethod(getMethod);
			  // System.out.println("statusCode: " + statusCode);
			   
			   rcs = getMethod.getResponseCharSet();
			   
			   System.out.println(rcs);
			   
			   //读取内容
			   responseBody = getMethod.getResponseBody();
			   //处理内容
			   sHtml = new String(responseBody,"utf-8");
			   System.out.println(sHtml);
			   
			   locationURL = HtmlParser.getURL(sHtml, "a");
			   System.out.println(locationURL);
			   getMethod.releaseConnection();
			   
			  

			   
			   getMethod = new GetMethod("http://ssfight.u.uc.cn/FightGame/h.jsp");
			   statusCode = client.executeMethod(getMethod);
			  // System.out.println("statusCode: " + statusCode);
			   
			   rcs = getMethod.getResponseCharSet();
			   
			   System.out.println(rcs);
			   
			   //读取内容
			   responseBody = getMethod.getResponseBody();
			   //处理内容
			   
			   sHtml = new String(responseBody,"utf-8");
			   System.out.println(sHtml);
			   
			   HtmlParser.getURL(sHtml, "");
			   
			   
			   getMethod.releaseConnection();
			   
			   
			  
			  }
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
}
	private String sUserName = "";
	private String sPassWord = "";
	
}
