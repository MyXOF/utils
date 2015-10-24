package myxof.git.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//create table student
//	   (ID			varchar(5), 
// 		name			varchar(20) not null, 
// 		dept_name		varchar(20), 
// 		tot_cred		numeric(3,0) check (tot_cred >= 0),
// 		primary key (ID),
// 		foreign key (dept_name) references department
//		on delete set null
//	   );

public class MSSQLServer {

	public static void main(String[] args) {
		Connection conn = null;
		Statement sql = null;
		ResultSet rs = null;
		try {
            String dbURL = "jdbc:sqlserver://xxx.xxx.xxx.xxx:1433;databaseName=DB_NAME";
            String user = "sa";
            String pass = "";
            conn = DriverManager.getConnection(dbURL, user, pass);
            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
            }         
            sql = conn.createStatement();
            rs = sql.executeQuery("select * from student");

            while(rs.next()){
            	String id = rs.getString("ID");
            	String name = rs.getString("name");
            	String deptName = rs.getString("dept_name");
            	int totalCredit = rs.getInt("tot_cred");
            	System.out.println(id + " " + name + " " + deptName + " "  + totalCredit);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
            	if(rs != null && !rs.isClosed()){
            		rs.close();
            	}
            	if(sql != null && !sql.isClosed()){
            		sql.close();
            	}
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
	}

}
