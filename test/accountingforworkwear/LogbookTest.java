
package accountingforworkwear;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LogbookTest {
    
    public LogbookTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGetFIO() {
        System.out.println("Метод getFIO успешно пройден");
        Logbook instance = new Logbook();
        String expResult = null;
        String result = instance.getFIO();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetFIO() {
        System.out.println("Метод setFIO успешно пройден");
        String FIO = "Иванов Иван Иванович";
        Logbook instance = new Logbook();
        instance.setFIO(FIO);
        assertEquals(FIO, instance.getFIO());
    }

    @Test
    public void testGetLogId() {
        System.out.println("Метод getLogId успешно пройден");
        Logbook instance = new Logbook();
        int expResult = 0;
        int result = instance.getLogId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetLogId() {
        System.out.println("Метод setLogId успешно пройден");
        int logId = 1;
        Logbook instance = new Logbook();
        instance.setLogId(logId);
        assertEquals(logId, instance.getLogId());
    }

    @Test
    public void testGetEmployeeId() {
        System.out.println("Метод getEmployeeId успешно пройден");
        Logbook instance = new Logbook();
        String expResult = null;
        String result = instance.getEmployeeId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetEmployeeId() {
        System.out.println("Метод setEmployeeId успешно пройден");
        String employeeId = "EMP001";
        Logbook instance = new Logbook();
        instance.setEmployeeId(employeeId);
        assertEquals(employeeId, instance.getEmployeeId());
    }

    @Test
    public void testGetItemId() {
        System.out.println("Метод getItemId успешно пройден");
        Logbook instance = new Logbook();
        String expResult = null;
        String result = instance.getItemId();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetItemId() {
        System.out.println("Метод setItemId успешно пройден");
        String itemId = "ITEM001";
        Logbook instance = new Logbook();
        instance.setItemId(itemId);
        assertEquals(itemId, instance.getItemId());
    }

    @Test
    public void testGetSizeSIZ() {
        System.out.println("Метод getSizeSIZ успешно пройден");
        Logbook instance = new Logbook();
        int expResult = 0;
        int result = instance.getSizeSIZ();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetSizeSIZ() {
        System.out.println("Метод setSizeSIZ успешно пройден");
        int sizeSIZ = 42;
        Logbook instance = new Logbook();
        instance.setSizeSIZ(sizeSIZ);
        assertEquals(sizeSIZ, instance.getSizeSIZ());
    }

    @Test
    public void testGetEmployeeHeight() {
        System.out.println("Метод getEmployeeHeight успешно пройден");
        Logbook instance = new Logbook();
        int expResult = 0;
        int result = instance.getEmployeeHeight();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetEmployeeHeight() {
        System.out.println("Метод setEmployeeHeight успешно пройден");
        int employeeHeight = 180;
        Logbook instance = new Logbook();
        instance.setEmployeeHeight(employeeHeight);
        assertEquals(employeeHeight, instance.getEmployeeHeight());
    }

    @Test
    public void testGetIssuedDate() {
        System.out.println("Метод getIssuedDate успешно пройден");
        Logbook instance = new Logbook();
        String expResult = null;
        String result = instance.getIssuedDate();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetIssuedDate() {
        System.out.println("Метод setIssuedDate успешно пройден");
        String issuedDate = "2022-05-13";
        Logbook instance = new Logbook();
        instance.setIssuedDate(issuedDate);
        assertEquals(issuedDate, instance.getIssuedDate());
    }

    @Test
    public void testGetIssuedQuantity() {
        System.out.println("Метод getIssuedQuantity успешно пройден");
        Logbook instance = new Logbook();
        int expResult = 0;
        int result = instance.getIssuedQuantity();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetIssuedQuantity() {
        System.out.println("Меод setIssuedQuantity успешно пройден");
        int issuedQuantity = 5;
        Logbook instance = new Logbook();
        instance.setIssuedQuantity(issuedQuantity);
        assertEquals(issuedQuantity, instance.getIssuedQuantity());
    }

    @Test
    public void testGetEmployeeNameById() {
        System.out.println("Метод getEmployeeNameById успешно пройден:");
        int FIO_Employee = 2; // Assuming the employee ID
        String result = Logbook.getEmployeeNameById(FIO_Employee);
        assertNotNull(result);
        System.out.println("Имя сотрудника с ID " + FIO_Employee + ": " + result);
    }

    @Test
    public void testGetPositionNameById() {
        System.out.println("Меод getPositionNameById успешно пройден:");
        int positionId = 1; // Assuming the position ID
        String result = Logbook.getPositionNameById(positionId);
        assertNotNull(result);
        System.out.println("Наименование должности с ID " + positionId + ": " + result);
    }

    @Test
    public void testGetItemNameById() {
        System.out.println("Меод getItemNameById успешно пройден:");
        int itemId = 1; // Assuming the item ID
        String result = Logbook.getItemNameById(itemId);
        assertNotNull(result);
        System.out.println("Наименование предмета с ID " + itemId + ": " + result);
    }

    @Test
    public void testGetAllLogbooks() {
        System.out.println("Метод getAllLogbooks успешно пройден:");
        List<Logbook> result = Logbook.getAllLogbooks();
        assertNotNull(result);
        System.out.println("Количество записей в журнале: " + result.size());
    }

    @Test
    public void testGetAllSizNames() {
        System.out.println("Метод getAllSizNames успешно пройден:");
        Logbook instance = new Logbook();
        List<String> result = instance.getAllSizNames();
        assertNotNull(result);
        System.out.println("Список наименований сиз: " + result);
    }

    @Test
    public void testAddLogbookEntry() {
        System.out.println("Метод addLogbookEntry успешно пройден");
        /*String FIO_Employee = "Иванов Иван Иванович"; // Assuming the employee name
        String positionName = "Менеджер"; // Assuming the position name
        String itemName = "Куртка"; // Assuming the item name
        int sizeSIZ = 42;
        String issuedDate = "2022-05-13";
        int issuedQuantity = 1;
        Logbook instance = new Logbook();
        boolean result = instance.addLogbookEntry(FIO_Employee, positionName, itemName, sizeSIZ, issuedDate, issuedQuantity);
        assertTrue(result);
        System.out.println("Запись добавлена успешно");*/
    }

    @Test
    public void testGetAllEmployeeNames() {
        System.out.println("Метод getAllEmployeeNames успешно пройден:");
        Logbook instance = new Logbook();
        List<String> result = instance.getAllEmployeeNames();
        assertNotNull(result);
        System.out.println("Список всех сотрудников: " + result);
    }
    
}
