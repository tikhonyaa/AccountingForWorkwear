package accountingforworkwear;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DatabaseConnectionTest {

    private static Connection connection;

    public DatabaseConnectionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        connection = DatabaseConnection.getConnection();
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getConnection method, of class DatabaseConnection.
     */
    @Test
    public void testGetConnection() {
        System.out.println("Получение соединения");
        assertNotNull("Соединение не должно быть равно null", connection);
        try {
            assertFalse("Соединение должно быть открыто", connection.isClosed());
            System.out.println("Соединение установлено успешно");
        } catch (SQLException ex) {
            fail("Произошла ошибка SQLException: " + ex.getMessage());
        }
    }

}
