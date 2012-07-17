package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OperaData {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException 
	{
		//Query();
		insert();

	}

	public static void insert() throws SQLException
	{
		Connection con = null;
		Statement st = null;
		String sSql = "";
		String[] sData = new String[1000];
		
		try
		{
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			
			String url="jdbc:db2://localhost:50000/maxdb75";
			con = DriverManager.getConnection(url,"db2admin","db2admin");
			
			st = con.createStatement();
			
			int ciid = 1000;
			String sCinum = "";
			
			for(int i = 10 ; i < sData.length ; i++)
			{
				ciid = 1000 + i ;
				
				sCinum = "CI" + ciid;
				
				
				sData[i] = "insert into maximo.ci (ciid,cinum,langcode,status,hasld,statusdate,rowstamp)"
						 + " values(" + "int(" + ciid +")" + ",'"+ sCinum + "','" +"EN" + "','" + "OPERATING" + "'," 
						 +"int(0)" + "," + " DATE('2012-06-20')" + ","+ "int(" + ciid +")" + ")";
					

				System.out.println(sData[i]);
				
				st.execute(sData[i]);
			}
			
			
			
		}catch(Exception e)
		{
			con.rollback();
			
			e.printStackTrace();
			
		}finally
		{
			con.commit();
		
			st.close();
			
		    con.close();
		}
	}
	public static void Query()
	{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sSql = "";
		String sTemp = "";
		
		try
		{
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			
			String url="jdbc:db2://localhost:50000/maxdb75";
			con = DriverManager.getConnection(url,"db2admin","db2admin");
			
			sSql = "select personid,status,displayname,statusdate,langcode,rowstamp from maximo.person";
			
			ps = con.prepareStatement(sSql);
			
			rs = ps.executeQuery();
			
			sTemp = "personid,status,displayname,statusdate,langcode,rowstamp";
			System.out.println(sTemp);
			while(rs.next())
			{
				sTemp = rs.getString("personid") + "," + rs.getString("status") + "," + rs.getString("displayname") + ","
					  + rs.getString("statusdate") + "," + rs.getString("langcode") + "," + rs.getString("rowstamp");
				
				System.out.println(sTemp);
				
			}
					
			
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
