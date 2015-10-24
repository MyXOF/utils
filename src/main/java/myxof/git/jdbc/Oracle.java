package myxof.git.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//create table student
//(ID			varchar(5), 
//	name			varchar(20) not null, 
//	dept_name		varchar(20), 
//	tot_cred		numeric(3,0) check (tot_cred >= 0),
//	primary key (ID),
//	foreign key (dept_name) references department
//	on delete set null
//);

public class Oracle {

	public static void main(String[] args) {
		Connection con = null;
		ResultSet result = null;
		Statement sql = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url = "jdbc:oracle:thin:@xxx.xxx.xxx.xxx:1521:DB_NAME";
			String user = "sa";
			String password = "";
			
			con = DriverManager.getConnection(url,user,password);
			sql = con.createStatement();
			result = sql.executeQuery("select * from student");
			
	        while(result.next()){
	        	String id = result.getString("ID");
	        	String name = result.getString("name");
	        	String deptName = result.getString("dept_name");
	        	int totalCredit = result.getInt("tot_cred");
	        	System.out.println(id + " " + name + " " + deptName + " "  + totalCredit);
	        }		
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
            try {
            	if(result != null && !result.isClosed()){
            		result.close();
            	}
            	if(sql != null && !sql.isClosed()){
            		sql.close();
            	}
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }


	

	}

}
