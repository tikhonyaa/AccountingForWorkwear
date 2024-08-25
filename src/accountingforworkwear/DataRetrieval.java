package accountingforworkwear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataRetrieval {

    private final Connection connection;
    private final State STATE;

    public DataRetrieval(Connection connection, State STATE) {
        this.connection = connection;
        this.STATE = STATE;
    }

    public void fetchData() {
        String query = "SELECT staff.*, positions.Position, genders.Gender, sizesclothing.SizeClothing, heights.Height, sizesshoe.SizeShoe, sizesheaddress.SizeHeaddress FROM staff "
                + "JOIN positions ON staff.Position = positions.ID_Positions "
                + "JOIN genders ON staff.Gender = genders.ID_genders "
                + "JOIN sizesclothing ON staff.SizeClothing = sizesclothing.ID_SizesClothing "
                + "JOIN heights ON staff.Height = heights.ID_Heights "
                + "JOIN sizesshoe ON staff.SizeShoe = sizesshoe.ID_SizesShoe "
                + "JOIN sizesheaddress ON staff.SizeHeaddress = sizesheaddress.ID_SizesHeaddress";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Получение данных из результата запроса
                String serviceNum = resultSet.getString("ServiceNumber");
                String FIO = resultSet.getString("FullName");
                String jobID = resultSet.getString("Position");
                String dateStr = resultSet.getString("DateOfBirth");
                String sexID = resultSet.getString("Gender");
                String sizeClothingID = resultSet.getString("SizeClothing");
                String heightID = resultSet.getString("Height");
                String sizeShoeID = resultSet.getString("SizeShoe");
                String sizeHeaddressID = resultSet.getString("SizeHeaddress");

                // Получение текстовых значений для должности и пола
                String job = getPositionName(jobID);
                String sex = getGender(sexID);
                String sizeClothing = getSizeClothing(sizeClothingID);
                String height = getHeight(heightID);
                String sizeShoe = getSizeShoe(sizeShoeID);
                String sizeHeaddress = getSizeHeaddress(sizeHeaddressID);

                // Изменение формата даты
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                String date;
                try {
                    date = outputFormat.format(inputFormat.parse(dateStr));
                } catch (ParseException e) {
                    date = dateStr;
                }

                //Создание массива объектов для хранения данных строки
                Object[] rowData = {serviceNum, FIO, job, date, sex, sizeClothing, height, sizeShoe, sizeHeaddress};
                //Добавление массива в список
                STATE.addST(rowData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Object[]> retrieveStaffData() {
        List<Object[]> staffData = new ArrayList<>();

        String query = "SELECT * FROM staff";

        try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                // Получение данных из результата запроса
                String serviceNum = resultSet.getString("ServiceNumber");
                String FIO = resultSet.getString("FullName");
                String jobID = resultSet.getString("Position");
                String dateStr = resultSet.getString("DateOfBirth");
                String sexID = resultSet.getString("Gender");
                String sizeClothingID = resultSet.getString("SizeClothing");
                String heightID = resultSet.getString("Height");
                String sizeShoeID = resultSet.getString("SizeShoe");
                String sizeHeaddressID = resultSet.getString("SizeHeaddress");

                // Создание массива объектов для хранения данных строки
                Object[] rowData = {serviceNum, FIO, jobID, dateStr, sexID, sizeClothingID, heightID, sizeShoeID, sizeHeaddressID};
                // Добавление массива в список
                staffData.add(rowData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return staffData;
    }

    public Object[][] loadDataIntoArray() {
        String query = "SELECT * FROM staff";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {
            List<Object[]> data = new ArrayList<>();
            while (resultSet.next()) {
                // Получение текстовых значений для каждого поля
                String serviceNum = resultSet.getString("ServiceNumber");
                String FIO = resultSet.getString("FullName");
                String jobID = resultSet.getString("Position");
                String dateOfBirthStr = resultSet.getString("DateOfBirth");
                String sexID = resultSet.getString("Gender");
                String sizeClothingID = resultSet.getString("SizeClothing");
                String heightID = resultSet.getString("Height");
                String sizeShoeID = resultSet.getString("SizeShoe");
                String sizeHeaddressID = resultSet.getString("SizeHeaddress");

                // Получение текстовых расшифровок для идентификаторов
                String job = getPositionName(jobID);
                String sex = getGender(sexID);
                String sizeClothing = getSizeClothing(sizeClothingID);
                String height = getHeight(heightID);
                String sizeShoe = getSizeShoe(sizeShoeID);
                String sizeHeaddress = getSizeHeaddress(sizeHeaddressID);

                // Форматирование даты из строки в формат dd.MM.yyyy
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                Date dateOfBirth;
                try {
                    dateOfBirth = inputFormat.parse(dateOfBirthStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                    dateOfBirth = null; // Чтобы избежать NullPointerException
                }
                String formattedDateOfBirth = dateOfBirth != null ? outputFormat.format(dateOfBirth) : "";

                // Создание массива объектов для хранения данных строки
                Object[] row = {serviceNum, FIO, job, formattedDateOfBirth, sex, sizeClothing, height, sizeShoe, sizeHeaddress};
                // Добавление массива в список
                data.add(row);
            }
            return data.toArray(new Object[0][]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Object[0][];
        }
    }

    //Методы для выполнения запросов к таблицам и получения текстовых значений по идентификаторам
    private String getPositionName(String positionID) throws SQLException {
        String query = "SELECT Position FROM positions WHERE ID_Positions = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, positionID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Position");
                } else {
                    return positionID;
                }
            }
        }
    }

    private String getGender(String genderID) throws SQLException {
        String query = "SELECT Gender FROM genders WHERE ID_genders = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, genderID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Gender");
                } else {
                    return genderID;
                }
            }
        }
    }

    private String getSizeClothing(String sizeClothingID) throws SQLException {
        String query = "SELECT SizeClothing FROM sizesclothing WHERE ID_SizesClothing = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sizeClothingID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("SizeClothing");
                } else {
                    return sizeClothingID;
                }
            }
        }
    }

    private String getHeight(String heightID) throws SQLException {
        String query = "SELECT Height FROM heights WHERE ID_Heights = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, heightID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("Height");
                } else {
                    return heightID;
                }
            }
        }
    }

    private String getSizeShoe(String sizeShoeID) throws SQLException {
        String query = "SELECT SizeShoe FROM sizesshoe WHERE ID_SizesShoe = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sizeShoeID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("SizeShoe");
                } else {
                    return sizeShoeID;
                }
            }
        }
    }

    private String getSizeHeaddress(String sizeHeaddressID) throws SQLException {
        String query = "SELECT SizeHeaddress FROM sizesheaddress WHERE ID_SizesHeaddress = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, sizeHeaddressID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("SizeHeaddress");
                } else {
                    return sizeHeaddressID;
                }
            }
        }
    }

    public void updateStaffTableFromModel(String serviceNumber, String fullName, String positionText, Date dateOfBirth, String genderText, String sizeClothingText, String heightText, String sizeShoeText, String sizeHeaddressText) throws SQLException {
        int positionID = getPositionID(positionText);
        int genderID = getGenderID(genderText);
        int clothingSizeID = getClothingSizeID(sizeClothingText);
        int heightID = getHeightID(heightText);
        int shoeSizeID = getShoeSizeID(sizeShoeText);
        int headdressSizeID = getHeaddressSizeID(sizeHeaddressText);
        String staffPassword = getStaffPassword(serviceNumber);

        String query = "UPDATE staff SET FullName=?, Position=?, DateOfBirth=?, Gender=?, SizeClothing=?, Height=?, SizeShoe=?, SizeHeaddress=?, StaffPassword=? WHERE ServiceNumber=?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Установка параметров запроса
            statement.setString(1, fullName);
            statement.setInt(2, positionID);
            statement.setDate(3, new java.sql.Date(dateOfBirth.getTime()));
            statement.setInt(4, genderID);
            statement.setInt(5, clothingSizeID);
            statement.setInt(6, heightID);
            statement.setInt(7, shoeSizeID);
            statement.setInt(8, headdressSizeID);
            statement.setString(9, staffPassword);
            statement.setString(10, serviceNumber);

            // Выполнение запроса
            int rowsUpdated = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    private String getStaffPassword(String serviceNumber) throws SQLException {
        String query = "SELECT StaffPassword FROM staff WHERE ServiceNumber = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, serviceNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("StaffPassword");
                } else {
                    return "";
                }
            }
        }
    }

    private int getPositionID(String positionText) {
        String query = "SELECT ID_Positions FROM positions WHERE Position = ?";
        return getIDFromTable(query, positionText);
    }

    private int getGenderID(String genderText) {
        String query = "SELECT ID_genders FROM genders WHERE Gender = ?";
        return getIDFromTable(query, genderText);
    }

    private int getClothingSizeID(String sizeText) {
        String query = "SELECT ID_SizesClothing FROM sizesclothing WHERE SizeClothing = ?";
        return getIDFromTable(query, sizeText);
    }

    private int getHeightID(String heightText) {
        String query = "SELECT ID_Heights FROM heights WHERE Height = ?";
        return getIDFromTable(query, heightText);
    }

    private int getShoeSizeID(String sizeText) {
        String query = "SELECT ID_SizesShoe FROM sizesshoe WHERE SizeShoe = ?";
        return getIDFromTable(query, sizeText);
    }

    private int getHeaddressSizeID(String sizeText) {
        String query = "SELECT ID_SizesHeaddress FROM sizesheaddress WHERE SizeHeaddress = ?";
        return getIDFromTable(query, sizeText);
    }

    private int getIDFromTable(String query, String value) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                } else {
                    return -1; //-1, если значение не найдено
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void addStaffToDatabase(String serviceNumber, String fullName, String positionText, Date dateOfBirth, String genderText, String sizeClothingText, String heightText, String sizeShoeText, String sizeHeaddressText) throws SQLException {
        int positionID = getPositionID(positionText);
        int genderID = getGenderID(genderText);
        int clothingSizeID = getClothingSizeID(sizeClothingText);
        int heightID = getHeightID(heightText);
        int shoeSizeID = getShoeSizeID(sizeShoeText);
        int headdressSizeID = getHeaddressSizeID(sizeHeaddressText);

        String query = "INSERT INTO staff (ServiceNumber, FullName, Position, DateOfBirth, Gender, SizeClothing, Height, SizeShoe, SizeHeaddress, StaffPassword) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // Установка параметров запроса
            statement.setString(1, serviceNumber);
            statement.setString(2, fullName);
            statement.setInt(3, positionID);
            statement.setDate(4, new java.sql.Date(dateOfBirth.getTime()));
            statement.setInt(5, genderID);
            statement.setInt(6, clothingSizeID);
            statement.setInt(7, heightID);
            statement.setInt(8, shoeSizeID);
            statement.setInt(9, headdressSizeID);
            String StaffPassword = "";
            statement.setString(10, StaffPassword);

            // Выполнение запроса
            int rowsInserted = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }
}
