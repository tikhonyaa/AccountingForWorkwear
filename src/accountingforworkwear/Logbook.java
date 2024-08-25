package accountingforworkwear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logbook {

    private int logId;
    private String FIO;
    private String employeeId;
    private String itemId;
    private int sizeSIZ;
    private int employeeHeight;
    private String issuedDate;
    private int issuedQuantity;
    private String dueDate;

    public Logbook() {
        this.logId = 0;
        this.employeeId = null;
        this.itemId = null;
        this.sizeSIZ = 0;
        this.employeeHeight = 0;
        this.issuedDate = null;
        this.issuedQuantity = 0;
        this.dueDate = null;
    }

    public Logbook(int logId, String FIO, String employeeName, String itemName, int sizeSIZ, int employeeHeight, String issuedDate, int issuedQuantity, String dueDate) {
        this.logId = logId;
        this.FIO = FIO;
        this.employeeId = employeeName;
        this.itemId = itemName;
        this.sizeSIZ = sizeSIZ;
        this.employeeHeight = employeeHeight;
        this.issuedDate = issuedDate;
        this.issuedQuantity = issuedQuantity;
        this.dueDate = dueDate;
    }

    public String getFIO() {
        return FIO;
    }

    public void setFIO(String FIO) {
        this.FIO = FIO;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getSizeSIZ() {
        return sizeSIZ;
    }

    public void setSizeSIZ(int sizeSIZ) {
        this.sizeSIZ = sizeSIZ;
    }

    public int getEmployeeHeight() {
        return employeeHeight;
    }

    public void setEmployeeHeight(int employeeHeight) {
        this.employeeHeight = employeeHeight;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(int issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public static String getEmployeeNameById(int FIO_Employee) {
        String name = null;
        String sql = "SELECT FullName FROM staff WHERE ID_STAFF = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, FIO_Employee);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    name = resultSet.getString("FullName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static String getPositionNameById(int positionId) {
        String name = null;
        String sql = "SELECT Position FROM positions WHERE ID_Positions = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, positionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    name = resultSet.getString("Position");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static String getItemNameById(int itemId) {
        String name = null;
        String sql = "SELECT Name_siz FROM siz WHERE ID_siz = ?";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, itemId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    name = resultSet.getString("Name_siz");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return name;
    }

    // Метод для получения данных из таблицы logbook
    public static List<Logbook> getAllLogbooks() {
        List<Logbook> logbooks = new ArrayList<>();
        String sql = "SELECT * FROM logbook";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int logId = resultSet.getInt("ID_Log");
                int FIO_Employee = resultSet.getInt("FIO_Employee");
                int positionId = resultSet.getInt("Name_Position");
                int itemId = resultSet.getInt("ID_Item");
                int sizeSIZ = resultSet.getInt("SizeSIZ");
                int employeeHeight = resultSet.getInt("EmployeeHeight");
                String issuedDateString = resultSet.getString("IssuedDate");
                int issuedQuantity = resultSet.getInt("IssuedQuantity");
                String dueDateString = resultSet.getString("DueDate");

                String FIOName = getEmployeeNameById(FIO_Employee);
                String positionName = getPositionNameById(positionId);
                String itemName = getItemNameById(itemId);

                String issuedDate = formatDate(issuedDateString);
                String dueDate = dueDateString != null ? formatDate(dueDateString) : null;

                Logbook logbook = new Logbook(logId, FIOName, positionName, itemName, sizeSIZ, employeeHeight, issuedDate, issuedQuantity, dueDate);
                logbooks.add(logbook);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logbooks;
    }

    private static String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.y");
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Метод для получения списка всех сиз из базы данных
    public List<String> getAllSizNames() {
        List<String> sizNames = new ArrayList<>();
        String sql = "SELECT Name_siz FROM siz";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String sizName = resultSet.getString("Name_siz");
                sizNames.add(sizName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sizNames;
    }

// Метод для добавления новой записи в таблицу SQL
    public boolean addLogbookEntry(String FIO_Employee, String positionName, String itemName, int sizeSIZ, int heightSIZ, String issuedDate, int issuedQuantity) {
        String sql = "INSERT INTO logbook (FIO_Employee, Name_Position, ID_Item, SizeSIZ, IssuedDate, IssuedQuantity, EmployeeHeight) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int employeeId = getEmployeeIdByName(FIO_Employee); // ID сотрудника
            int positionId = getPositionIdByName(positionName); // ID должности
            int itemId = getItemIdByName(itemName); // ID предмета
            preparedStatement.setInt(1, employeeId); // ID сотрудника
            preparedStatement.setInt(2, positionId); // ID должности
            preparedStatement.setInt(3, itemId);
            preparedStatement.setInt(4, sizeSIZ);
            preparedStatement.setString(5, issuedDate);
            preparedStatement.setInt(6, issuedQuantity);
            preparedStatement.setInt(7, heightSIZ);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private int getEmployeeIdByName(String FIO_Employee) {
        int employeeId = 0;
        String sql = "SELECT ID_STAFF FROM staff WHERE FullName = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, FIO_Employee);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    employeeId = resultSet.getInt("ID_STAFF");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeeId;
    }

    private int getPositionIdByName(String positionName) {
        int positionId = 0;
        String sql = "SELECT ID_Positions FROM positions WHERE Position = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, positionName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    positionId = resultSet.getInt("ID_Positions");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return positionId;
    }

    private int getItemIdByName(String itemName) {
        int itemId = 0;
        String sql = "SELECT ID_siz FROM siz WHERE Name_siz = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, itemName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    itemId = resultSet.getInt("ID_siz");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemId;
    }

    public List<String> getAllEmployeeNames() {
        List<String> employeeNames = new ArrayList<>();
        String sql = "SELECT FullName FROM staff";

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String fullName = resultSet.getString("FullName");
                employeeNames.add(fullName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employeeNames;
    }

    public boolean updateLogbookEntry(String selectedNumber, String FIO, String position, String itemName, int sizeSIZ, int heightSIZ, String dateIssued) {
        String sql = "UPDATE logbook SET FIO_Employee = ?, Name_Position = ?, ID_Item = ?, SizeSIZ = ?, EmployeeHeight = ?, IssuedDate = ? WHERE ID_Log = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int employeeId = getEmployeeIdByName(FIO); // ID сотрудника
            int positionId = getPositionIdByName(position); // ID должности
            int itemId = getItemIdByName(itemName); // ID предмета
            preparedStatement.setInt(1, employeeId); // ID сотрудника
            preparedStatement.setInt(2, positionId); // ID должности
            preparedStatement.setInt(3, itemId);
            preparedStatement.setInt(4, sizeSIZ);
            preparedStatement.setInt(5, heightSIZ);
            preparedStatement.setString(6, dateIssued);
            preparedStatement.setString(7, selectedNumber); 
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
