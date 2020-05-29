package dataprocess;




import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Created by xiangchao on 2020/2/29.
 */
public class DatabaseUtil {
    static Properties pro = null;

    static {
        pro = new Properties();
        try {
            pro.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection getMyconnection() {
        try {
            Class.forName(pro.getProperty("mysqlDriver"));
            return DriverManager.getConnection(pro.getProperty("mysqlUrl"), pro.getProperty("user"), pro.getProperty("pwd"));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void close(ResultSet rs,ResultSet rs1, PreparedStatement preparedStatement,Statement st, Connection conn){
        try {
            if(rs!=null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(rs1!=null)
                rs1.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (preparedStatement != null)
                preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (st != null)
                st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
