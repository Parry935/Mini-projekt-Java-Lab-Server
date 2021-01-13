import java.sql.*;

public class DBContext {
    public Connection dbLink;

    public Connection getContection(){
        String dataBaseUser = "root";
        String dataBasePassword = "9926";
        String url = "jdbc:mysql://localhost:3306/mysql";

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbLink = DriverManager.getConnection(url,dataBaseUser,dataBasePassword);
        }catch (Exception e)
        {
            e.printStackTrace();
            e.getCause();
        }

        return dbLink;
    }
}