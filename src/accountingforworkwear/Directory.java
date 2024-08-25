package accountingforworkwear;

import com.mysql.cj.jdbc.DatabaseMetaData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class Directory {

    private List<String> positions;
    private List<String> genders;
    private List<String> sizesClothing;
    private List<String> heights;
    private List<String> sizesShoe;
    private List<String> sizesHeaddress;
    //private static Connection connection;

    public Directory(Connection connection) {
        //this.connection = connection;
        this.positions = new ArrayList<>();
        this.genders = new ArrayList<>();
        this.sizesClothing = new ArrayList<>();
        this.heights = new ArrayList<>();
        this.sizesShoe = new ArrayList<>();
        this.sizesHeaddress = new ArrayList<>();

        fetchData(connection);
    }

    private void fetchData(Connection connection) {
        fetchPositions(connection);
        fetchGenders(connection);
        fetchSizesClothing(connection);
        fetchHeights(connection);
        fetchSizesShoe(connection);
        fetchSizesHeaddress(connection);
    }

    private void fetchPositions(Connection connection) {
        String query = "SELECT Position FROM positions";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                positions.add(resultSet.getString("Position"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchGenders(Connection connection) {
        String query = "SELECT Gender FROM genders";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                genders.add(resultSet.getString("Gender"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchSizesClothing(Connection connection) {
        String query = "SELECT SizeClothing FROM sizesclothing";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                sizesClothing.add(resultSet.getString("SizeClothing"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchHeights(Connection connection) {
        String query = "SELECT Height FROM heights";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                heights.add(resultSet.getString("Height"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchSizesShoe(Connection connection) {
        String query = "SELECT SizeShoe FROM sizesshoe";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                sizesShoe.add(resultSet.getString("SizeShoe"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchSizesHeaddress(Connection connection) {
        String query = "SELECT SizeHeaddress FROM sizesheaddress";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                sizesHeaddress.add(resultSet.getString("SizeHeaddress"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPositions() {
        return positions;
    }

    public List<String> getGenders() {
        return genders;
    }

    public List<String> getSizesClothing() {
        return sizesClothing;
    }

    public List<String> getHeights() {
        return heights;
    }

    public List<String> getSizesShoe() {
        return sizesShoe;
    }

    public List<String> getSizesHeaddress() {
        return sizesHeaddress;
    }

    public static boolean updateData(int tableIndex, String oldValue, String newValue) {
        String[] tableNames = {"positions", "genders", "sizesclothing", "heights", "sizesshoe", "sizesheaddress"};
        if (tableIndex < 0 || tableIndex >= tableNames.length) {
            System.out.println("Недопустимый индекс таблицы.");
            return false;
        }

        Connection connection = DatabaseConnection.getConnection();
        if (connection == null) {
            System.out.println("Не удалось установить соединение с базой данных.");
            return false;
        }

        String tableName = tableNames[tableIndex];

        try {
            DatabaseMetaData metaData = (DatabaseMetaData) connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            String secondColumnName = ""; // Имя второго столбца
            boolean foundSecondColumn = false; // Флаг для нахождения второго столбца
            while (columns.next()) {
                if (!foundSecondColumn) {
                    foundSecondColumn = true; // Первый столбец уже пропущен
                    continue;
                }
                secondColumnName = columns.getString("COLUMN_NAME");
                break;
            }

            // Проверка наличия второго столбца
            if (secondColumnName.isEmpty()) {
                System.out.println("Не удалось найти подходящий второй столбец для обновления.");
                return false;
            }

            // Обновление значения в таблице
            String updateQuery = "UPDATE " + tableName + " SET " + secondColumnName + " = ? WHERE " + secondColumnName + " = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setString(1, newValue);
                updateStatement.setString(2, oldValue);
                int rowsUpdated = updateStatement.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertIntoDatabase(Connection connection, String tableName, String columnName, String value) {
        String query = "INSERT INTO " + tableName + " (" + columnName + ") VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteFromDatabase(Connection connection, String tableName, String columnName, String value) {
        String query = "DELETE FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, value);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
