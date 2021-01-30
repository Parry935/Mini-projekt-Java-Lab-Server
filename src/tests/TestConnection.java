package tests;

import database.DBContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class TestConnection {

    public Connection dbLink;
    public DBContext dbContext;

    @Before
    public void setUpConnection() {
        dbContext = new DBContext();

        String dataBaseUser = "root";
        String dataBasePassword = "9926";
        String url = "jdbc:mysql://localhost:3306/mysql";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbLink = DriverManager.getConnection(url, dataBaseUser, dataBasePassword);
        } catch (Exception e) {
            e.printStackTrace();
            e.getCause();
        }
    }

    @Test
    public void testDatabaseCatalog() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getCatalog(), testDbLink.getCatalog());
    }

    @Test
    public void testDatabaseSchema() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getSchema(), testDbLink.getSchema());
    }

    @Test
    public void testDatabaseClient() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getClientInfo(), testDbLink.getClientInfo());
    }

    @Test
    public void testDatabaseHoldability() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getHoldability(), testDbLink.getHoldability());
    }

    @Test
    public void testDatabaseNetwork() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getNetworkTimeout(), testDbLink.getNetworkTimeout());
    }

    @Test
    public void testDatabaseAutoCommit() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getAutoCommit(), testDbLink.getAutoCommit());
    }

    @Test
    public void testDatabaseWarnings() throws SQLException {

        Connection testDbLink = dbContext.getContection();
        assertEquals(dbLink.getWarnings(), testDbLink.getWarnings());
    }
}

