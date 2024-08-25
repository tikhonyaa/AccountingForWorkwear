package accountingforworkwear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Nomenclature {

    // Метод для получения категорий SIZ
    public Map<Integer, String> getCategoriesSIZ() {
        Map<Integer, String> categoriesSIZ = new HashMap<>();
        String query = "SELECT * FROM categoriessiz";
        try (
                Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery();) {
            while (resultSet.next()) {
                int id = resultSet.getInt("ID_categoriesSIZ");
                String name = resultSet.getString("Name_categoriesSIZ");
                categoriesSIZ.put(id, name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoriesSIZ;
    }

    // Метод для получения SIZ по категории
    public Map<Integer, List<String>> getSIZByCategory() {
        Map<Integer, List<String>> sizByCategory = new HashMap<>();
        String query = "SELECT * FROM siz";
        try (
                Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery();) {
            while (resultSet.next()) {
                int categoryID = resultSet.getInt("Categories_siz");
                String sizName = resultSet.getString("Name_siz");

                // Добавление SIZ в список для данной категории
                if (!sizByCategory.containsKey(categoryID)) {
                    sizByCategory.put(categoryID, new ArrayList<>());
                }
                sizByCategory.get(categoryID).add(sizName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sizByCategory;
    }

    public boolean addCategoryToDatabase(String categoryName, int categoryNumber) {
        String sql = "INSERT INTO siz (Name_siz, Categories_siz) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, categoryName);
            preparedStatement.setInt(2, categoryNumber);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategoryFromDatabase(String categoryName, int categoryNumber) {
        String sql = "DELETE FROM siz WHERE Name_siz = ? AND Categories_siz = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, categoryName);
            preparedStatement.setInt(2, categoryNumber);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategoryInDatabase(String oldCategoryName, String newCategoryName, int categoryNumber) {
        String sql = "UPDATE siz SET Name_siz = ? WHERE Name_siz = ? AND Categories_siz = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, newCategoryName);
            preparedStatement.setString(2, oldCategoryName);
            preparedStatement.setInt(3, categoryNumber);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
