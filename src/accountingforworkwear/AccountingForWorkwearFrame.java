package accountingforworkwear;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.sql.Connection;
import java.util.Enumeration;
import javax.swing.DefaultListModel;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.table.TableModel;

public class AccountingForWorkwearFrame extends javax.swing.JFrame {

    private static State STATE;
    private int accessRightsID;
    private DatabaseConnection DB;
    private DataRetrieval DATARET;
    private Directory DIRECTORY;
    private Nomenclature nomenclature;
    private Standards standards;
    private Logbook logbook;
    Font allFront = new Font("Century Gothic", 0, 16);
    private final CustomTableModel model;
    private String currentId = "";
    private int countID = 0;
    private int directoryNum = 0;
    private List<String> idList = new ArrayList<>();
    private boolean boolDir = true;
    private boolean setEdit = false;
    DefaultListModel<String> positionListModel;
    DefaultListModel<String> genderListModel;
    DefaultListModel<String> sizeClothingListModel;
    DefaultListModel<String> heightListModel;
    DefaultListModel<String> sizeShoeListModel;
    DefaultListModel<String> sizeHeaddressListModel;
    boolean userAccess;
    private String selectedNumber = "";
    private int numenCount = 0;

    public AccountingForWorkwearFrame() {
        STATE = new State();
        initComponents();
        model = new CustomTableModel();
        nomenclature = new Nomenclature();
        standards = new Standards();
        logbook = new Logbook();

        String fullName = "";
        String password = "";

        do {
            fullName = JOptionPane.showInputDialog("Введите ФИО:");
            password = JOptionPane.showInputDialog("Введите пароль:");
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "SELECT Position FROM staff WHERE FullName = ? AND StaffPassword = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, fullName);
                    statement.setString(2, password);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            int position = resultSet.getInt("Position");
                            accessRightsID = getPositionAccessRights(position);
                            userAccess = accessRightsID != -1;
                        } else {
                            JOptionPane.showMessageDialog(null, "Неправильное ФИО или пароль. Попробуйте еще раз.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка при обращении к базе данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

            if (fullName == null || password == null) {
                JOptionPane.showMessageDialog(null, "Авторизация отменена. Программа будет закрыта.", "Внимание", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        } while (!userAccess);

        if (userAccess) {
            try {
                DB = new DatabaseConnection();
                if (DB != null) {
                    Connection connection = DB.getConnection();
                    if (connection != null) {
                        System.out.println("YES");
                        DATARET = new DataRetrieval(connection, STATE);
                        DATARET.fetchData();
                        DIRECTORY = new Directory(connection);
                        updateTable();
                        addDirectoryList();
                    } else {
                        System.out.println("NO");
                        JOptionPane.showMessageDialog(null, "Ошибка подключения к базе данных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    boolDir = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            JScrollPane mainScrollPane = new JScrollPane(jTabbedPane1);
            getContentPane().add(mainScrollPane);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            accessRights(accessRightsID);

            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1); // Минимум, максимум, шаг
            jSpinner1.setModel(spinnerModel);

            loadSIZNamesWithCategory();
            updateTableStandards();
            updateTableLogbook();
            loadSIZNamesToComboBox();
            loadEmployeeNamesToComboBox();
            populateComboBoxBasedOnTab();
            useraccess(accessRightsID);
        } else {
            JOptionPane.showMessageDialog(null, "У вас нет доступа к программе.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void useraccess(int access) {
        if (access == 1) {
            AddLogbookCheckBox.setEnabled(false);
            jComboBox4.setEnabled(false);
            EditButton1.setEnabled(false);
            jButton2.setEnabled(false);
            EditButton.setEnabled(false);
            IDComboBox.setEnabled(false);
            DelButton.setEnabled(false);
            AddNumenCheckBox.setEnabled(false);
            jComboBox3.setEnabled(false);
            NumenEditButton.setEnabled(false);
            AddDirectoryCheckBox.setEnabled(false);
            AddDirectoryCheckBox.setEnabled(false);
            jComboBox5.setEnabled(false);
            DirectoryEditButton.setEnabled(false);
        }
    }

    private void populateComboBoxBasedOnTab() {
        DefaultComboBoxModel<String> comboBoxModel = (DefaultComboBoxModel<String>) jComboBox3.getModel();
        comboBoxModel.removeAllElements(); 
        int selectedIndex = jTabbedPane2.getSelectedIndex();
        if (selectedIndex == 0) {
            for (int i = 0; i < WorkwearList.getModel().getSize(); i++) {
                comboBoxModel.addElement(WorkwearList.getModel().getElementAt(i));
            }
        } else if (selectedIndex == 1) {
            for (int i = 0; i < SafetyShoesList.getModel().getSize(); i++) {
                comboBoxModel.addElement(SafetyShoesList.getModel().getElementAt(i));
            }
        } else if (selectedIndex == 2) {
            for (int i = 0; i < OtherSIZList.getModel().getSize(); i++) {
                comboBoxModel.addElement(OtherSIZList.getModel().getElementAt(i));
            }
        }
        jComboBox3.setModel(comboBoxModel);
        jComboBox3.setSelectedIndex(0);
    }

    private void loadEmployeeNamesToComboBox() {
        FIOComboBox.removeAllItems();
        List<String> employeeNames = logbook.getAllEmployeeNames();
        for (String employeeName : employeeNames) {
            FIOComboBox.addItem(employeeName);
        }
    }

    private void loadSIZNamesToComboBox() {
        SIZComboBox.removeAllItems();
        List<String> sizNames = logbook.getAllSizNames();
        for (String sizName : sizNames) {
            SIZComboBox.addItem(sizName);
        }
    }

    private void updateTableLogbook() {
        DefaultTableModel model = (DefaultTableModel) LogbookTable.getModel();
        model.setRowCount(0);

        List<Logbook> logbooks = Logbook.getAllLogbooks();
        for (Logbook logbook : logbooks) {
            model.addRow(new Object[]{
                logbook.getLogId(),
                logbook.getFIO(),
                logbook.getEmployeeId(),
                logbook.getItemId(),
                logbook.getSizeSIZ(),
                logbook.getEmployeeHeight(),
                logbook.getIssuedDate(),
                logbook.getIssuedQuantity(),
                logbook.getDueDate()
            });
        }

        DefaultComboBoxModel<String> comboBoxModel = (DefaultComboBoxModel<String>) jComboBox4.getModel();
        comboBoxModel.removeAllElements(); 

        for (Logbook logbook : logbooks) {
            comboBoxModel.addElement(Integer.toString(logbook.getLogId()));
        }
    }

    private void updateTableStandards() {
        DefaultTableModel model = (DefaultTableModel) TableStandards.getModel();
        model.setRowCount(0);
        String returnDateString = "";
        for (Standards.TermsOfIssue termOfIssue : standards.getTermsOfIssueList()) {
            if (termOfIssue.getReturnDate() != null) {
                returnDateString = new SimpleDateFormat("dd.MM.yyyy").format(termOfIssue.getReturnDate());
            }
            model.addRow(new Object[]{
                termOfIssue.getPositionName(),
                termOfIssue.getSizName(),
                termOfIssue.getQuantity(),
                termOfIssue.getTerm(),
                termOfIssue.getQuantitySum(),
                returnDateString
            });
            returnDateString = "";
        }
    }

    private void loadSIZNamesWithCategory() {
        Map<Integer, List<String>> sizByCategory = nomenclature.getSIZByCategory();
        List<String> sizNames1 = sizByCategory.get(1);
        List<String> sizNames2 = sizByCategory.get(2);
        List<String> sizNames3 = sizByCategory.get(3);

        DefaultListModel<String> listModel1 = new DefaultListModel<>();
        if (sizNames1 != null) {
            for (String sizName1 : sizNames1) {
                listModel1.addElement(sizName1);
            }
        }

        DefaultListModel<String> listModel2 = new DefaultListModel<>();
        if (sizNames2 != null) {
            for (String sizName2 : sizNames2) {
                listModel2.addElement(sizName2);
            }
        }

        DefaultListModel<String> listModel3 = new DefaultListModel<>();
        if (sizNames3 != null) {
            for (String sizName3 : sizNames3) {
                listModel3.addElement(sizName3);
            }
        }

        WorkwearList.setModel(listModel1);
        SafetyShoesList.setModel(listModel2);
        OtherSIZList.setModel(listModel3);
    }

    private int getPositionAccessRights(int position) {
        int accessRightsID = -1;
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT AccessRightsID FROM positions WHERE ID_Positions = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, position);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        accessRightsID = resultSet.getInt("AccessRightsID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accessRightsID;
    }

    private void accessRights(int accessRightsID) {
        if (accessRightsID == 2) {
            AddEmployeeCheckBox.setEnabled(true);
            IDComboBox.setEnabled(true);
            AddLogbookCheckBox.setEnabled(true);
        }
    }

    private void addDirectoryList() {
        int selectedIndex = jTabbedPane3.getSelectedIndex();
        List<String> positions = DIRECTORY.getPositions();
        positionListModel = new DefaultListModel<>();
        for (String position : positions) {
            positionListModel.addElement(position);
            JobComboBox.addItem(position);
            JobComboBox2.addItem(position);
            JobComboBox3.addItem(position);
        }
        PositionList.setModel(positionListModel);
        if (selectedIndex == 0) {
            DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
            jComboBox5.setModel(comboBoxModel);
            comboBoxModel.removeAllElements();
            for (int i = 0; i < positionListModel.getSize(); i++) {
                comboBoxModel.addElement(positionListModel.getElementAt(i));
            }
        }
        List<String> genders = DIRECTORY.getGenders();
        genderListModel = new DefaultListModel<>();
        for (String gender : genders) {
            genderListModel.addElement(gender);
            SexComboBox.addItem(gender);
        }
        GenderList.setModel(genderListModel);

        List<String> sizesClothing = DIRECTORY.getSizesClothing();
        sizeClothingListModel = new DefaultListModel<>();
        for (String sizeClothing : sizesClothing) {
            sizeClothingListModel.addElement(sizeClothing);
            SizeClothComboBox.addItem(sizeClothing);
        }
        SizeClothingList.setModel(sizeClothingListModel);

        List<String> heights = DIRECTORY.getHeights();
        heightListModel = new DefaultListModel<>();
        for (String height : heights) {
            heightListModel.addElement(height);
            HeightComboBox.addItem(height);
        }
        HeightList.setModel(heightListModel);

        List<String> sizesShoe = DIRECTORY.getSizesShoe();
        sizeShoeListModel = new DefaultListModel<>();
        for (String sizeShoe : sizesShoe) {
            sizeShoeListModel.addElement(sizeShoe);
            SizeShoeComboBox.addItem(sizeShoe);
        }
        SizeShoeList.setModel(sizeShoeListModel);

        List<String> sizesHeaddress = DIRECTORY.getSizesHeaddress();
        sizeHeaddressListModel = new DefaultListModel<>();
        for (String sizeHeaddress : sizesHeaddress) {
            sizeHeaddressListModel.addElement(sizeHeaddress);
            SizeHeaddressComboBox.addItem(sizeHeaddress);
        }
        SizeHeaddressList.setModel(sizeHeaddressListModel);
    }

    private void updateComboBoxData(DefaultListModel<String> listModel) {
        if (listModel != null) {
            DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
            jComboBox5.setModel(comboBoxModel);
            comboBoxModel.removeAllElements();
            for (int i = 0; i < listModel.getSize(); i++) {
                comboBoxModel.addElement(listModel.getElementAt(i));
            }
        }
    }

    private void updateTable() {
        DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
        model.setRowCount(0);
        Object[][] data = DATARET.loadDataIntoArray();
        for (Object[] row : data) {
            model.addRow(row);
            countID++;
            idList.add((String) row[0]);
            updateIDComboBox();
        }
    }

    private String formatDate(Date date) {
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            return sdf.format(date);
        }
        return null;
    }

    private String formatAnDate(Date date) {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        }
        return null;
    }

    private class CustomTableModel extends DefaultTableModel {
        public CustomTableModel() {
            super(new Object[][]{}, new String[]{"ТН", "ФИО", "Должность", "Дата рождения", "Пол", "Размер одежды", "Рост", "Размер обуви", "Размер ГУ"});
        }
        public void addRowWithAutoIncrementId(Object[] rowData) {
            super.addRow(rowData);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column != 0;
        }

        public Object[] getRowDataById(String id) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0).toString().equals(id)) {
                    Object[] rowData = new Object[getColumnCount()];
                    for (int j = 0; j < getColumnCount(); j++) {
                        rowData[j] = getValueAt(i, j);
                    }
                    return rowData;
                }
            }
            return null;
        }

        public void updateRowById(String id, String upId, String upFIO, String upJob, String upDate, String upSex, String upSC, String upHeight, String upSS, String upSH) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0).toString().equals(id)) {
                    setValueAt(upId, i, 0);
                    setValueAt(upFIO, i, 1);
                    setValueAt(upJob, i, 2);
                    setValueAt(upDate, i, 3);
                    setValueAt(upSex, i, 4);
                    setValueAt(upSC, i, 5);
                    setValueAt(upHeight, i, 6);
                    setValueAt(upSS, i, 7);
                    setValueAt(upSH, i, 8);
                    break;
                }
            }

        }
    }

    private void addRowToTable() {
        Object[] newRowData = {
            STATE.getServiceNum(),
            STATE.getFIO(),
            STATE.getJob(),
            STATE.getDate(),
            STATE.getSex(),
            STATE.getSizeCloth(),
            STATE.getHeight(),
            STATE.getSizeShoe(),
            STATE.getSizeHeaddressCloth()
        };

        model.addRowWithAutoIncrementId(newRowData);
        STATE.addST(newRowData);
        idList.add(STATE.getServiceNum());
        clearInputFields();
        updateIDComboBox();
    }

    private void addRowToTableSCV(String serviceNumber, String fullName, String positionText, String dateOfBirth, String genderText, String sizeClothingText, String heightText, String sizeShoeText, String sizeHeaddressText) {
        STATE.setServiceNum(serviceNumber);
        STATE.setFIO(fullName);
        STATE.setJob(positionText);
        STATE.setDate(dateOfBirth);
        STATE.setSex(genderText);
        STATE.setSizeCloth(sizeClothingText);
        STATE.setHeight(heightText);
        STATE.setSizeShoe(sizeShoeText);
        STATE.setSizeHeaddressCloth(sizeHeaddressText);

        Object[] newRowData = {
            STATE.getServiceNum(),
            STATE.getFIO(),
            STATE.getJob(),
            STATE.getDate(),
            STATE.getSex(),
            STATE.getSizeCloth(),
            STATE.getHeight(),
            STATE.getSizeShoe(),
            STATE.getSizeHeaddressCloth()
        };

        model.addRowWithAutoIncrementId(newRowData);
        STATE.addST(newRowData);
        idList.add(STATE.getServiceNum());
        clearInputFields();
        updateIDComboBox();
    }

    private void updateIDComboBox() {
        String[] idArray = idList.toArray(new String[0]);
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>(idArray);
        IDComboBox.setModel(comboBoxModel);
    }

    private void clearInputFields() {
        ServiceNText.setText("");
        FIOText.setText("");
        JobComboBox.setSelectedIndex(0);
        DateChooser.setDate(null);
        SexComboBox.setSelectedIndex(0);
        SizeClothComboBox.setSelectedIndex(0);
        HeightComboBox.setSelectedIndex(0);
        SizeShoeComboBox.setSelectedIndex(0);
        SizeHeaddressComboBox.setSelectedIndex(0);
        AddEmployeeCheckBox.setSelected(false);
    }

    private void enabledStateInputFields(boolean var) {
        if (var == true) {
            ServiceNText.setEnabled(true);
            FIOText.setEnabled(true);
            JobComboBox.setEnabled(true);
            DateChooser.setEnabled(true);
            SexComboBox.setEnabled(true);
            SizeClothComboBox.setEnabled(true);
            HeightComboBox.setEnabled(true);
            SizeShoeComboBox.setEnabled(true);
            SizeHeaddressComboBox.setEnabled(true);
        } else {
            ServiceNText.setEnabled(false);
            ServiceNText.setText("");
            FIOText.setEnabled(false);
            FIOText.setText("");
            JobComboBox.setEnabled(false);
            JobComboBox.setSelectedIndex(0);
            DateChooser.setEnabled(false);
            DateChooser.setDate(null);
            SexComboBox.setEnabled(false);
            SexComboBox.setSelectedIndex(0);
            SizeClothComboBox.setEnabled(false);
            SizeClothComboBox.setSelectedIndex(0);
            HeightComboBox.setEnabled(false);
            HeightComboBox.setSelectedIndex(0);
            SizeShoeComboBox.setEnabled(false);
            SizeHeaddressComboBox.setEnabled(false);
            SizeHeaddressComboBox.setSelectedIndex(0);
        }
    }

    public boolean checkDate(JTable table, Date selectedDate) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM.yyyy");
        String selectedDateStr = sdf1.format(selectedDate);
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yyyy");
        boolean dataExists = false;

        int rowCount = table.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String lastColumnValue = table.getValueAt(i, table.getColumnCount() - 1).toString();
            if (!lastColumnValue.equals("")) {
                try {
                    Date lastColumnDate = sdf2.parse(lastColumnValue);
                    String lastColumnDateStr = sdf2.format(lastColumnDate);
                    lastColumnDateStr = lastColumnDateStr.substring(3);
                    if (lastColumnDateStr.equals(selectedDateStr)) {
                        transfeValue();
                        dataExists = true;
                        break; 
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return dataExists;
    }

    public void checkPPEStatus() {
        LocalDate today = LocalDate.now();
        TableModel logbookTableModel = LogbookTable.getModel();
        TableModel tableStandardsModel = TableStandards.getModel();

        List<PPERecord> ppeToDecommission = new ArrayList<>();

        int logbookPositionIndex = getColumnIndex(logbookTableModel, "Должность");
        int logbookPpeNameIndex = getColumnIndex(logbookTableModel, "Наименование СИЗ");
        int logbookDateOfIssueIndex = getColumnIndex(logbookTableModel, "Дата выдачи");
        int logbookDateOfReturnIndex = logbookTableModel.getColumnCount() - 1;

        int standardsPositionIndex = getColumnIndex(tableStandardsModel, "Должность");
        int standardsPpeNameIndex = getColumnIndex(tableStandardsModel, "Название СИЗ");
        int standardsWearPeriodIndex = getColumnIndex(tableStandardsModel, "Срок носки (года)");

        if (logbookPositionIndex == -1 || logbookPpeNameIndex == -1 || logbookDateOfIssueIndex == -1 || standardsPositionIndex == -1 || standardsPpeNameIndex == -1 || standardsWearPeriodIndex == -1) {
            JOptionPane.showMessageDialog(this, "Ошибка: один или несколько столбцов не найдены.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // Шаг 2: Пройтись по всем записям в LogbookTable
        for (int i = 0; i < logbookTableModel.getRowCount(); i++) {
            String dateOfReturn = (String) logbookTableModel.getValueAt(i, logbookDateOfReturnIndex);

            // Шаг 3: Найти записи с пустым столбцом "Дата сдачи"
            if (dateOfReturn == null || dateOfReturn.isEmpty()) {
                String position = (String) logbookTableModel.getValueAt(i, logbookPositionIndex);
                String ppeName = (String) logbookTableModel.getValueAt(i, logbookPpeNameIndex);
                String dateOfIssueStr = (String) logbookTableModel.getValueAt(i, logbookDateOfIssueIndex);

                LocalDate dateOfIssue;
                try {
                    dateOfIssue = LocalDate.parse(dateOfIssueStr, formatter);
                } catch (Exception e) {
                    // Если формат даты неверный, пропустить запись
                    continue;
                }

                // Шаг 4: Найти соответствующие записи в TableStandards
                for (int j = 0; j < tableStandardsModel.getRowCount(); j++) {
                    String standardPosition = (String) tableStandardsModel.getValueAt(j, standardsPositionIndex);
                    String standardPpeName = (String) tableStandardsModel.getValueAt(j, standardsPpeNameIndex);

                    if (position.equals(standardPosition) && ppeName.equals(standardPpeName)) {
                        int wearPeriodYears = (int) tableStandardsModel.getValueAt(j, standardsWearPeriodIndex);

                        // Шаг 5: Добавить к дате выдачи количество лет ("Срок носки (года)")
                        LocalDate decommissionDate = dateOfIssue.plusYears(wearPeriodYears);

                        // Шаг 6: Проверить, нужно ли списать СИЗ в этом месяце
                        if (today.getMonth() == decommissionDate.getMonth() && today.getYear() == decommissionDate.getYear()) {
                            ppeToDecommission.add(new PPERecord(i, position, ppeName));
                        }
                    }
                }
            }
        }

        if (!ppeToDecommission.isEmpty()) {
            StringBuilder message = new StringBuilder("<html>Следующие СИЗ нужно списать в этом месяце:<br>");
            for (PPERecord record : ppeToDecommission) {
                message.append("ID: ").append(record.id)
                        .append(", Должность: ").append(record.position)
                        .append(", Наименование СИЗ: ").append(record.ppeName).append("<br>");
            }
            message.append("</html>");
            showRedMessageDialog(message.toString());
        }
    }

    private void showRedMessageDialog(String message) {
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);

        JOptionPane.showMessageDialog(this, label, "Уведомление о списании СИЗ", JOptionPane.INFORMATION_MESSAGE);
    }

    private int getColumnIndex(TableModel model, String columnName) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnName(i).equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static class PPERecord {

        int id;
        String position;
        String ppeName;

        PPERecord(int id, String position, String ppeName) {
            this.id = id;
            this.position = position;
            this.ppeName = ppeName;
        }
    }

    public void transfeValue() {
        Map<String, Map<String, Map<String, Integer>>> summaryMap = new HashMap<>();

        // Проход по таблице сотрудников (LogbookTable)
        for (int i = 0; i < LogbookTable.getRowCount(); i++) {
            String itemName = LogbookTable.getValueAt(i, 3).toString(); // Название СИЗ
            String size = LogbookTable.getValueAt(i, 4).toString(); // Размер
            String height = LogbookTable.getValueAt(i, 5).toString(); // Рост
            int issuedQuantity = Integer.parseInt(LogbookTable.getValueAt(i, 7).toString()); // Количество

            // Проверка наличия записи с данными о СИЗ в summaryMap
            if (summaryMap.containsKey(itemName)) {
                Map<String, Map<String, Integer>> sizeMap = summaryMap.get(itemName);

                // Проверка наличия записи с данным размером для данного СИЗ
                if (sizeMap.containsKey(size)) {
                    Map<String, Integer> heightMap = sizeMap.get(size);

                    // Проверка наличия записи с данным ростом для данного размера
                    if (heightMap.containsKey(height)) {
                        // Увеличение количества для существующего СИЗ, размера и роста
                        int currentQuantity = heightMap.get(height);
                        heightMap.put(height, currentQuantity + issuedQuantity);
                    } else {
                        // Добавление новой записи с указанием роста для существующего СИЗ и размера
                        heightMap.put(height, issuedQuantity);
                    }
                } else {
                    // Добавление новой записи с указанием размера и роста для существующего СИЗ
                    Map<String, Integer> heightMap = new HashMap<>();
                    heightMap.put(height, issuedQuantity);
                    sizeMap.put(size, heightMap);
                }
            } else {
                // Добавление новой записи с указанием размера и роста для нового СИЗ
                Map<String, Map<String, Integer>> sizeMap = new HashMap<>();
                Map<String, Integer> heightMap = new HashMap<>();
                heightMap.put(height, issuedQuantity);
                sizeMap.put(size, heightMap);
                summaryMap.put(itemName, sizeMap);
            }
        }

        // Заполнение jTable2 суммарными данными
        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0); 

        // Проход по структуре данных с суммарным итогом и добавление данных в jTable2
        for (Map.Entry<String, Map<String, Map<String, Integer>>> entry : summaryMap.entrySet()) {
            String itemName = entry.getKey();
            Map<String, Map<String, Integer>> sizeMap = entry.getValue();

            for (Map.Entry<String, Map<String, Integer>> sizeEntry : sizeMap.entrySet()) {
                String size = sizeEntry.getKey();
                Map<String, Integer> heightMap = sizeEntry.getValue();

                for (Map.Entry<String, Integer> heightEntry : heightMap.entrySet()) {
                    String height = heightEntry.getKey();
                    int totalQuantity = heightEntry.getValue();
                    
                    model.addRow(new Object[]{itemName, size, height, totalQuantity});
                    model.fireTableDataChanged();
                }
            }
        }
    }

    private void exportToPDF(String filePath) {
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath + ".pdf"));

            // Установка шрифта
            BaseFont bf = BaseFont.createFont("E:\\ДИПЛОМ\\лаба\\AccountingForWorkwear\\fonts\\TimesNewRomanRegular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font font = new com.itextpdf.text.Font(bf, 14, com.itextpdf.text.Font.NORMAL);

            document.open();

            PdfPTable pdfTable = new PdfPTable(jTable2.getColumnCount());

            // Заголовки столбцов в PDF
            TableModel model = jTable2.getModel();
            for (int i = 0; i < model.getColumnCount(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(i), font));
                pdfTable.addCell(cell);
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(i, j);
                    String cellValue = value != null ? value.toString() : "";
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, font));
                    pdfTable.addCell(cell);
                }
            }

            document.add(pdfTable);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportToCSV(String filePath) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + ".csv"), StandardCharsets.UTF_8));
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();

            bw.write("\ufeff");

            for (int i = 0; i < model.getColumnCount(); i++) {
                bw.write(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) {
                    bw.write(",");
                }
            }
            bw.newLine();
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    bw.write(model.getValueAt(i, j).toString());
                    if (j < model.getColumnCount() - 1) {
                        bw.write(",");
                    }
                }
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileDialogForCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            exportToCSV(filePath);
        }
    }

    private void saveFileDialogForPDF() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            exportToPDF(filePath);
        }
    }

    private Object[] getTableRowDataById(String selectedId) {
        DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();
        Object[] rowData = null;

        for (int i = 0; i < rowCount; i++) {
            String idValue = model.getValueAt(i, 0).toString();
            if (idValue.equals(selectedId)) {
                rowData = new Object[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    rowData[j] = model.getValueAt(i, j);
                }
                break; 
            }
        }

        return rowData;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        LogbookSplitPane = new javax.swing.JSplitPane();
        jScrollPane12 = new javax.swing.JScrollPane();
        LogbookTable = new javax.swing.JTable();
        EditLogbookPanel = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        AddLogbookCheckBox = new javax.swing.JCheckBox();
        LogbookAddButton = new javax.swing.JButton();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        DirectorySaveButton2 = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JSeparator();
        DirectoryLabel2 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<>();
        DirectoryLabel3 = new javax.swing.JLabel();
        DirectoryLabel4 = new javax.swing.JLabel();
        DateChooser1 = new com.toedter.calendar.JDateChooser();
        DirectoryLabel5 = new javax.swing.JLabel();
        SIZComboBox = new javax.swing.JComboBox<>();
        FIOComboBox = new javax.swing.JComboBox<>();
        DirectoryLabel6 = new javax.swing.JLabel();
        SizeSIZText = new javax.swing.JTextField();
        DirectoryLabel7 = new javax.swing.JLabel();
        JobComboBox3 = new javax.swing.JComboBox<>();
        jSpinner1 = new javax.swing.JSpinner();
        jButton2 = new javax.swing.JButton();
        EditButton1 = new javax.swing.JButton();
        HeightSIZText = new javax.swing.JTextField();
        DirectoryLabel9 = new javax.swing.JLabel();
        StateSplitPane = new javax.swing.JSplitPane();
        StatePanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        AddEmployeeCheckBox = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        FIOText = new javax.swing.JTextField();
        SexComboBox = new javax.swing.JComboBox<>();
        DelButton = new javax.swing.JButton();
        AddButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        SizeClothComboBox = new javax.swing.JComboBox<>();
        JobComboBox = new javax.swing.JComboBox<>();
        HeightComboBox = new javax.swing.JComboBox<>();
        SizeShoeComboBox = new javax.swing.JComboBox<>();
        SizeHeaddressComboBox = new javax.swing.JComboBox<>();
        DateChooser = new com.toedter.calendar.JDateChooser();
        jSeparator1 = new javax.swing.JSeparator();
        EditButton = new javax.swing.JButton();
        SaveButton = new javax.swing.JButton();
        IDComboBox = new javax.swing.JComboBox<>();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel19 = new javax.swing.JLabel();
        ServiceNText = new javax.swing.JTextField();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        NomenclaturePanel = new javax.swing.JPanel();
        jSplitPane4 = new javax.swing.JSplitPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        WorkwearList = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        SafetyShoesList = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        OtherSIZList = new javax.swing.JList<>();
        EditNomenclaturePanel = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        AddNumenCheckBox = new javax.swing.JCheckBox();
        NumenAddButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        NumenSaveButton = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JSeparator();
        DirectoryLabel1 = new javax.swing.JLabel();
        NumenText = new javax.swing.JTextField();
        NumenDeleteButton = new javax.swing.JButton();
        jComboBox3 = new javax.swing.JComboBox<>();
        NumenEditButton = new javax.swing.JButton();
        DirectorySplitPane = new javax.swing.JSplitPane();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        PositionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        PositionList = new javax.swing.JList<>();
        GenderPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        GenderList = new javax.swing.JList<>();
        SizeClothingPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        SizeClothingList = new javax.swing.JList<>();
        HeightPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        HeightList = new javax.swing.JList<>();
        SizeShoePanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        SizeShoeList = new javax.swing.JList<>();
        SizeHeaddressPanel = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        SizeHeaddressList = new javax.swing.JList<>();
        EditDirectoryPanel = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        AddDirectoryCheckBox = new javax.swing.JCheckBox();
        DirectoryAddButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        DirectorySaveButton = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JSeparator();
        DirectoryLabel = new javax.swing.JLabel();
        DirectoryText = new javax.swing.JTextField();
        DirectoryDeleteButton = new javax.swing.JButton();
        jComboBox5 = new javax.swing.JComboBox<>();
        DirectoryEditButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        TableStandards = new javax.swing.JTable();
        JobComboBox2 = new javax.swing.JComboBox<>();
        SortCheckBox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        DirectoryLabel8 = new javax.swing.JLabel();
        DateChooser2 = new com.toedter.calendar.JDateChooser();
        DirectoryLabel10 = new javax.swing.JLabel();
        CSVRadioButton = new javax.swing.JRadioButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        PDFRadioButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jTabbedPane1.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N

        LogbookSplitPane.setDividerLocation(1500);
        LogbookSplitPane.setResizeWeight(0.7);
        LogbookSplitPane.setToolTipText("");
        LogbookSplitPane.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        LogbookSplitPane.setPreferredSize(new java.awt.Dimension(800, 1117));

        LogbookTable.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        LogbookTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "№", "ФИО", "Должность", "Наименование СИЗ", "Размер", "Рост", "Дата выдачи", "Количество", "Дата сдачи"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        LogbookTable.setEnabled(false);
        LogbookTable.setMaximumSize(new java.awt.Dimension(2147483647, 60));
        LogbookTable.setMinimumSize(new java.awt.Dimension(675, 120));
        LogbookTable.setRowHeight(30);
        LogbookTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        LogbookTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        LogbookTable.setShowGrid(true);
        jScrollPane12.setViewportView(LogbookTable);
        if (LogbookTable.getColumnModel().getColumnCount() > 0) {
            LogbookTable.getColumnModel().getColumn(0).setMinWidth(30);
            LogbookTable.getColumnModel().getColumn(0).setPreferredWidth(30);
            LogbookTable.getColumnModel().getColumn(0).setMaxWidth(30);
            LogbookTable.getColumnModel().getColumn(4).setMinWidth(100);
            LogbookTable.getColumnModel().getColumn(4).setPreferredWidth(100);
            LogbookTable.getColumnModel().getColumn(4).setMaxWidth(100);
            LogbookTable.getColumnModel().getColumn(5).setMinWidth(50);
            LogbookTable.getColumnModel().getColumn(5).setPreferredWidth(50);
            LogbookTable.getColumnModel().getColumn(5).setMaxWidth(50);
            LogbookTable.getColumnModel().getColumn(6).setMinWidth(120);
            LogbookTable.getColumnModel().getColumn(6).setPreferredWidth(120);
            LogbookTable.getColumnModel().getColumn(6).setMaxWidth(120);
            LogbookTable.getColumnModel().getColumn(7).setMinWidth(100);
            LogbookTable.getColumnModel().getColumn(7).setPreferredWidth(100);
            LogbookTable.getColumnModel().getColumn(7).setMaxWidth(100);
            LogbookTable.getColumnModel().getColumn(8).setMinWidth(120);
            LogbookTable.getColumnModel().getColumn(8).setPreferredWidth(120);
            LogbookTable.getColumnModel().getColumn(8).setMaxWidth(120);
        }

        LogbookSplitPane.setLeftComponent(jScrollPane12);

        EditLogbookPanel.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        EditLogbookPanel.setMinimumSize(new java.awt.Dimension(300, 1117));
        EditLogbookPanel.setName(""); // NOI18N
        EditLogbookPanel.setPreferredSize(new java.awt.Dimension(300, 1117));

        jLabel22.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("НАСТРОЙКИ");

        AddLogbookCheckBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        AddLogbookCheckBox.setText("Добавить запись");
        AddLogbookCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AddLogbookCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AddLogbookCheckBoxItemStateChanged(evt);
            }
        });

        LogbookAddButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        LogbookAddButton.setText("Добавить");
        LogbookAddButton.setEnabled(false);
        LogbookAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogbookAddButtonActionPerformed(evt);
            }
        });

        DirectorySaveButton2.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectorySaveButton2.setText("Сохранить");
        DirectorySaveButton2.setToolTipText("");
        DirectorySaveButton2.setEnabled(false);
        DirectorySaveButton2.setMaximumSize(new java.awt.Dimension(105, 28));
        DirectorySaveButton2.setMinimumSize(new java.awt.Dimension(105, 28));
        DirectorySaveButton2.setPreferredSize(new java.awt.Dimension(105, 28));
        DirectorySaveButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DirectorySaveButton2ActionPerformed(evt);
            }
        });

        DirectoryLabel2.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel2.setText("ФИО");

        jComboBox4.setMinimumSize(new java.awt.Dimension(64, 27));
        jComboBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox4ItemStateChanged(evt);
            }
        });

        DirectoryLabel3.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel3.setText("Наименование СИЗ");

        DirectoryLabel4.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel4.setText("Дата выдачи");

        DateChooser1.setDateFormatString("dd.MM.yyyy");
        DateChooser1.setEnabled(false);
        DateChooser1.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N

        DirectoryLabel5.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel5.setText("Количество");

        SIZComboBox.setEnabled(false);

        FIOComboBox.setEnabled(false);
        FIOComboBox.setPreferredSize(new java.awt.Dimension(64, 27));

        DirectoryLabel6.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel6.setText("Размер");

        SizeSIZText.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SizeSIZText.setToolTipText("");
        SizeSIZText.setEnabled(false);

        DirectoryLabel7.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel7.setText("Должность");

        JobComboBox3.setEnabled(false);

        jSpinner1.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 5, 1));
        jSpinner1.setEnabled(false);

        jButton2.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jButton2.setText("Списать");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        EditButton1.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        EditButton1.setText("Изменить");
        EditButton1.setEnabled(false);
        EditButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditButton1ActionPerformed(evt);
            }
        });

        HeightSIZText.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        HeightSIZText.setToolTipText("");
        HeightSIZText.setEnabled(false);

        DirectoryLabel9.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel9.setText("Рост");

        javax.swing.GroupLayout EditLogbookPanelLayout = new javax.swing.GroupLayout(EditLogbookPanel);
        EditLogbookPanel.setLayout(EditLogbookPanelLayout);
        EditLogbookPanelLayout.setHorizontalGroup(
            EditLogbookPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditLogbookPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EditLogbookPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator11)
                    .addComponent(AddLogbookCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(JobComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SIZComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(LogbookAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSpinner1)
                    .addComponent(DateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SizeSIZText)
                    .addComponent(DirectoryLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator12)
                    .addComponent(jSeparator10)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(EditLogbookPanelLayout.createSequentialGroup()
                        .addComponent(EditButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addComponent(DirectorySaveButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FIOComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(HeightSIZText)
                    .addComponent(DirectoryLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        EditLogbookPanelLayout.setVerticalGroup(
            EditLogbookPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditLogbookPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator11, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AddLogbookCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FIOComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JobComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SIZComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SizeSIZText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(HeightSIZText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(LogbookAddButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(EditLogbookPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(EditButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DirectorySaveButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator10, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(488, Short.MAX_VALUE))
        );

        LogbookSplitPane.setRightComponent(EditLogbookPanel);

        jTabbedPane1.addTab("Журнал", LogbookSplitPane);

        StateSplitPane.setDividerLocation(1205);
        StateSplitPane.setResizeWeight(0.48);
        StateSplitPane.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N

        StatePanel.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        StatePanel.setMinimumSize(new java.awt.Dimension(300, 1117));
        StatePanel.setPreferredSize(new java.awt.Dimension(300, 1117));

        jLabel10.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("НАСТРОЙКИ");

        AddEmployeeCheckBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        AddEmployeeCheckBox.setText("Добавить сотрудника");
        AddEmployeeCheckBox.setEnabled(false);
        AddEmployeeCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AddEmployeeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AddEmployeeCheckBoxItemStateChanged(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel11.setText("ФИО");

        jLabel12.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("Должность");

        jLabel13.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("Дата рождения");

        jLabel14.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("Пол");

        jLabel15.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel15.setText("Размер одежды");

        jLabel16.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText("Рост");

        jLabel17.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("Размер обуви");

        jLabel18.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel18.setText("Размер головного убора");

        FIOText.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        FIOText.setToolTipText("");
        FIOText.setEnabled(false);

        SexComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SexComboBox.setEnabled(false);
        SexComboBox.setMinimumSize(new java.awt.Dimension(38, 29));
        SexComboBox.setPreferredSize(new java.awt.Dimension(38, 29));

        DelButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DelButton.setText("Загрузить штат");
        DelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelButtonActionPerformed(evt);
            }
        });

        AddButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        AddButton.setText("Добавить");
        AddButton.setEnabled(false);
        AddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });

        SizeClothComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SizeClothComboBox.setEnabled(false);

        JobComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        JobComboBox.setEnabled(false);

        HeightComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        HeightComboBox.setEnabled(false);

        SizeShoeComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SizeShoeComboBox.setEnabled(false);

        SizeHeaddressComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SizeHeaddressComboBox.setEnabled(false);
        SizeHeaddressComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SizeHeaddressComboBoxItemStateChanged(evt);
            }
        });

        DateChooser.setDateFormatString("dd.MM.y");
        DateChooser.setEnabled(false);
        DateChooser.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N

        EditButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        EditButton.setText("Изменить");
        EditButton.setEnabled(false);
        EditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditButtonActionPerformed(evt);
            }
        });

        SaveButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SaveButton.setText("Сохранить");
        SaveButton.setToolTipText("");
        SaveButton.setEnabled(false);
        SaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveButtonActionPerformed(evt);
            }
        });

        IDComboBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        IDComboBox.setEnabled(false);

        jLabel19.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel19.setText("Табельный номер");

        ServiceNText.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        ServiceNText.setToolTipText("");
        ServiceNText.setEnabled(false);

        javax.swing.GroupLayout StatePanelLayout = new javax.swing.GroupLayout(StatePanel);
        StatePanel.setLayout(StatePanelLayout);
        StatePanelLayout.setHorizontalGroup(
            StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, StatePanelLayout.createSequentialGroup()
                .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, StatePanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(AddEmployeeCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(StatePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                            .addComponent(SexComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AddButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SizeClothComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(JobComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(HeightComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SizeShoeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SizeHeaddressComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(StatePanelLayout.createSequentialGroup()
                                .addComponent(EditButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(IDComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(FIOText, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ServiceNText)
                            .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, StatePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(StatePanelLayout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jSeparator1))
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        StatePanelLayout.setVerticalGroup(
            StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AddEmployeeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ServiceNText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FIOText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(JobComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SizeClothComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(HeightComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SizeShoeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(SizeHeaddressComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(AddButton)
                .addGap(14, 14, 14)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(StatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(EditButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(IDComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SaveButton)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DelButton)
                .addContainerGap(340, Short.MAX_VALUE))
        );

        StateSplitPane.setRightComponent(StatePanel);
        StatePanel.getAccessibleContext().setAccessibleName("");

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ТН", "ФИО", "Должность", "Дата рождения", "Пол", "Размер одежды", "Рост", "Размер обуви", "Размер ГУ"
            }
        ));
        jTable3.setRowHeight(30);
        jScrollPane14.setViewportView(jTable3);
        if (jTable3.getColumnModel().getColumnCount() > 0) {
            jTable3.getColumnModel().getColumn(0).setMinWidth(100);
            jTable3.getColumnModel().getColumn(0).setPreferredWidth(100);
            jTable3.getColumnModel().getColumn(0).setMaxWidth(100);
            jTable3.getColumnModel().getColumn(3).setMinWidth(150);
            jTable3.getColumnModel().getColumn(3).setPreferredWidth(150);
            jTable3.getColumnModel().getColumn(3).setMaxWidth(150);
            jTable3.getColumnModel().getColumn(4).setMinWidth(50);
            jTable3.getColumnModel().getColumn(4).setPreferredWidth(50);
            jTable3.getColumnModel().getColumn(4).setMaxWidth(50);
            jTable3.getColumnModel().getColumn(5).setMinWidth(150);
            jTable3.getColumnModel().getColumn(5).setPreferredWidth(150);
            jTable3.getColumnModel().getColumn(5).setMaxWidth(150);
            jTable3.getColumnModel().getColumn(6).setMinWidth(50);
            jTable3.getColumnModel().getColumn(6).setPreferredWidth(50);
            jTable3.getColumnModel().getColumn(6).setMaxWidth(50);
            jTable3.getColumnModel().getColumn(7).setMinWidth(150);
            jTable3.getColumnModel().getColumn(7).setPreferredWidth(150);
            jTable3.getColumnModel().getColumn(7).setMaxWidth(150);
            jTable3.getColumnModel().getColumn(8).setMinWidth(100);
            jTable3.getColumnModel().getColumn(8).setPreferredWidth(100);
            jTable3.getColumnModel().getColumn(8).setMaxWidth(100);
        }

        StateSplitPane.setLeftComponent(jScrollPane14);

        jTabbedPane1.addTab("Штат", StateSplitPane);

        NomenclaturePanel.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N

        jSplitPane4.setDividerLocation(1200);
        jSplitPane4.setResizeWeight(0.48);

        jTabbedPane2.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jTabbedPane2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane2StateChanged(evt);
            }
        });

        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel1ComponentShown(evt);
            }
        });

        WorkwearList.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        WorkwearList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane9.setViewportView(WorkwearList);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 1236, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Спецодежда", jPanel1);

        jPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel2ComponentShown(evt);
            }
        });

        SafetyShoesList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane10.setViewportView(SafetyShoesList);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 1236, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Спецобувь", jPanel2);

        jPanel3.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel3ComponentShown(evt);
            }
        });

        OtherSIZList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane8.setViewportView(OtherSIZList);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 1242, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Другое", jPanel3);

        jSplitPane4.setLeftComponent(jTabbedPane2);

        EditNomenclaturePanel.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N

        jLabel21.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("НАСТРОЙКИ");

        AddNumenCheckBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        AddNumenCheckBox.setText("Добавить значение");
        AddNumenCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AddNumenCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AddNumenCheckBoxItemStateChanged(evt);
            }
        });

        NumenAddButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        NumenAddButton.setText("Добавить");
        NumenAddButton.setEnabled(false);
        NumenAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumenAddButtonActionPerformed(evt);
            }
        });

        NumenSaveButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        NumenSaveButton.setText("Сохранить");
        NumenSaveButton.setToolTipText("");
        NumenSaveButton.setEnabled(false);
        NumenSaveButton.setMaximumSize(new java.awt.Dimension(105, 28));
        NumenSaveButton.setMinimumSize(new java.awt.Dimension(105, 28));
        NumenSaveButton.setPreferredSize(new java.awt.Dimension(105, 28));
        NumenSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumenSaveButtonActionPerformed(evt);
            }
        });

        DirectoryLabel1.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel1.setText("Спецодежда");

        NumenText.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        NumenText.setToolTipText("");
        NumenText.setEnabled(false);

        NumenDeleteButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        NumenDeleteButton.setText("Удалить");
        NumenDeleteButton.setToolTipText("");
        NumenDeleteButton.setEnabled(false);
        NumenDeleteButton.setMaximumSize(new java.awt.Dimension(105, 28));
        NumenDeleteButton.setMinimumSize(new java.awt.Dimension(105, 28));
        NumenDeleteButton.setPreferredSize(new java.awt.Dimension(105, 28));
        NumenDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumenDeleteButtonActionPerformed(evt);
            }
        });

        jComboBox3.setMinimumSize(new java.awt.Dimension(64, 27));

        NumenEditButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        NumenEditButton.setText("Изменить");
        NumenEditButton.setToolTipText("");
        NumenEditButton.setMaximumSize(new java.awt.Dimension(105, 28));
        NumenEditButton.setMinimumSize(new java.awt.Dimension(105, 28));
        NumenEditButton.setPreferredSize(new java.awt.Dimension(105, 28));
        NumenEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NumenEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EditNomenclaturePanelLayout = new javax.swing.GroupLayout(EditNomenclaturePanel);
        EditNomenclaturePanel.setLayout(EditNomenclaturePanelLayout);
        EditNomenclaturePanelLayout.setHorizontalGroup(
            EditNomenclaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditNomenclaturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EditNomenclaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator5)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator8)
                    .addComponent(AddNumenCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(NumenText)
                    .addComponent(NumenAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator9)
                    .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditNomenclaturePanelLayout.createSequentialGroup()
                        .addComponent(NumenSaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(NumenDeleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                    .addComponent(NumenEditButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        EditNomenclaturePanelLayout.setVerticalGroup(
            EditNomenclaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditNomenclaturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AddNumenCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NumenText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(NumenAddButton)
                .addGap(14, 14, 14)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(NumenEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(EditNomenclaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NumenSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NumenDeleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(886, 886, 886))
        );

        jSplitPane4.setRightComponent(EditNomenclaturePanel);

        javax.swing.GroupLayout NomenclaturePanelLayout = new javax.swing.GroupLayout(NomenclaturePanel);
        NomenclaturePanel.setLayout(NomenclaturePanelLayout);
        NomenclaturePanelLayout.setHorizontalGroup(
            NomenclaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
        );
        NomenclaturePanelLayout.setVerticalGroup(
            NomenclaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane4)
        );

        jTabbedPane1.addTab("Номенклатура", NomenclaturePanel);

        DirectorySplitPane.setDividerLocation(1000);
        DirectorySplitPane.setResizeWeight(0.48);
        DirectorySplitPane.setMinimumSize(new java.awt.Dimension(21, 16));
        DirectorySplitPane.setName(""); // NOI18N
        DirectorySplitPane.setPreferredSize(new java.awt.Dimension(714, 943));

        jTabbedPane3.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jTabbedPane3.setMinimumSize(new java.awt.Dimension(16, 16));
        jTabbedPane3.setPreferredSize(new java.awt.Dimension(452, 402));
        jTabbedPane3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane3StateChanged(evt);
            }
        });

        PositionPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                PositionPanelComponentShown(evt);
            }
        });

        PositionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                PositionListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(PositionList);

        javax.swing.GroupLayout PositionPanelLayout = new javax.swing.GroupLayout(PositionPanel);
        PositionPanel.setLayout(PositionPanelLayout);
        PositionPanelLayout.setHorizontalGroup(
            PositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        PositionPanelLayout.setVerticalGroup(
            PositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PositionPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1206, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane3.addTab("Должности", PositionPanel);

        GenderPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                GenderPanelComponentShown(evt);
            }
        });

        GenderList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                GenderListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(GenderList);

        javax.swing.GroupLayout GenderPanelLayout = new javax.swing.GroupLayout(GenderPanel);
        GenderPanel.setLayout(GenderPanelLayout);
        GenderPanelLayout.setHorizontalGroup(
            GenderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        GenderPanelLayout.setVerticalGroup(
            GenderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Пол", GenderPanel);

        SizeClothingPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                SizeClothingPanelComponentShown(evt);
            }
        });

        SizeClothingList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                SizeClothingListValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(SizeClothingList);

        javax.swing.GroupLayout SizeClothingPanelLayout = new javax.swing.GroupLayout(SizeClothingPanel);
        SizeClothingPanel.setLayout(SizeClothingPanelLayout);
        SizeClothingPanelLayout.setHorizontalGroup(
            SizeClothingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        SizeClothingPanelLayout.setVerticalGroup(
            SizeClothingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Размеры одежды", SizeClothingPanel);

        HeightPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                HeightPanelComponentShown(evt);
            }
        });

        HeightList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                HeightListValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(HeightList);

        javax.swing.GroupLayout HeightPanelLayout = new javax.swing.GroupLayout(HeightPanel);
        HeightPanel.setLayout(HeightPanelLayout);
        HeightPanelLayout.setHorizontalGroup(
            HeightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        HeightPanelLayout.setVerticalGroup(
            HeightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Росты", HeightPanel);

        SizeShoePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                SizeShoePanelComponentShown(evt);
            }
        });

        SizeShoeList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                SizeShoeListValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(SizeShoeList);

        javax.swing.GroupLayout SizeShoePanelLayout = new javax.swing.GroupLayout(SizeShoePanel);
        SizeShoePanel.setLayout(SizeShoePanelLayout);
        SizeShoePanelLayout.setHorizontalGroup(
            SizeShoePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SizeShoePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                .addContainerGap())
        );
        SizeShoePanelLayout.setVerticalGroup(
            SizeShoePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Размеры обуви", SizeShoePanel);

        SizeHeaddressPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                SizeHeaddressPanelComponentShown(evt);
            }
        });

        SizeHeaddressList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                SizeHeaddressListValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(SizeHeaddressList);

        javax.swing.GroupLayout SizeHeaddressPanelLayout = new javax.swing.GroupLayout(SizeHeaddressPanel);
        SizeHeaddressPanel.setLayout(SizeHeaddressPanelLayout);
        SizeHeaddressPanelLayout.setHorizontalGroup(
            SizeHeaddressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        SizeHeaddressPanelLayout.setVerticalGroup(
            SizeHeaddressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 1212, Short.MAX_VALUE)
        );

        jTabbedPane3.addTab("Размеры головного убора", SizeHeaddressPanel);

        DirectorySplitPane.setLeftComponent(jTabbedPane3);

        EditDirectoryPanel.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        EditDirectoryPanel.setMinimumSize(new java.awt.Dimension(300, 1117));
        EditDirectoryPanel.setName(""); // NOI18N
        EditDirectoryPanel.setPreferredSize(new java.awt.Dimension(300, 1117));

        jLabel20.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("НАСТРОЙКИ");

        AddDirectoryCheckBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        AddDirectoryCheckBox.setText("Добавить значение");
        AddDirectoryCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        AddDirectoryCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                AddDirectoryCheckBoxItemStateChanged(evt);
            }
        });

        DirectoryAddButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryAddButton.setText("Добавить");
        DirectoryAddButton.setEnabled(false);
        DirectoryAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DirectoryAddButtonActionPerformed(evt);
            }
        });

        DirectorySaveButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectorySaveButton.setText("Сохранить");
        DirectorySaveButton.setToolTipText("");
        DirectorySaveButton.setEnabled(false);
        DirectorySaveButton.setMaximumSize(new java.awt.Dimension(105, 28));
        DirectorySaveButton.setMinimumSize(new java.awt.Dimension(105, 28));
        DirectorySaveButton.setPreferredSize(new java.awt.Dimension(105, 28));
        DirectorySaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DirectorySaveButtonActionPerformed(evt);
            }
        });

        DirectoryLabel.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel.setText("Должность");

        DirectoryText.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryText.setToolTipText("");
        DirectoryText.setEnabled(false);

        DirectoryDeleteButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryDeleteButton.setText("Удалить");
        DirectoryDeleteButton.setToolTipText("");
        DirectoryDeleteButton.setEnabled(false);
        DirectoryDeleteButton.setMaximumSize(new java.awt.Dimension(105, 28));
        DirectoryDeleteButton.setMinimumSize(new java.awt.Dimension(105, 28));
        DirectoryDeleteButton.setPreferredSize(new java.awt.Dimension(105, 28));
        DirectoryDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DirectoryDeleteButtonActionPerformed(evt);
            }
        });

        jComboBox5.setMinimumSize(new java.awt.Dimension(64, 27));

        DirectoryEditButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryEditButton.setText("Изменить");
        DirectoryEditButton.setToolTipText("");
        DirectoryEditButton.setMaximumSize(new java.awt.Dimension(105, 28));
        DirectoryEditButton.setMinimumSize(new java.awt.Dimension(105, 28));
        DirectoryEditButton.setPreferredSize(new java.awt.Dimension(105, 28));
        DirectoryEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DirectoryEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EditDirectoryPanelLayout = new javax.swing.GroupLayout(EditDirectoryPanel);
        EditDirectoryPanel.setLayout(EditDirectoryPanelLayout);
        EditDirectoryPanelLayout.setHorizontalGroup(
            EditDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditDirectoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EditDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator6)
                    .addComponent(AddDirectoryCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryText)
                    .addComponent(DirectoryAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator7)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(EditDirectoryPanelLayout.createSequentialGroup()
                        .addComponent(DirectorySaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DirectoryDeleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                    .addComponent(jComboBox5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DirectoryEditButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        EditDirectoryPanelLayout.setVerticalGroup(
            EditDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditDirectoryPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AddDirectoryCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DirectoryText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(DirectoryAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DirectoryEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(EditDirectoryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DirectoryDeleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DirectorySaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(902, 902, 902))
        );

        DirectorySplitPane.setRightComponent(EditDirectoryPanel);

        jTabbedPane1.addTab("Справочники", DirectorySplitPane);

        TableStandards.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        TableStandards.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Должность", "Название СИЗ", "Количество", "Срок носки (года)", "Количество выданного СИЗ", "Дата сдачи"
            }
        ));
        TableStandards.setEnabled(false);
        TableStandards.setRowHeight(30);
        TableStandards.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TableStandards.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane11.setViewportView(TableStandards);

        JobComboBox2.setEnabled(false);
        JobComboBox2.setMinimumSize(new java.awt.Dimension(64, 27));
        JobComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                JobComboBox2ItemStateChanged(evt);
            }
        });

        SortCheckBox.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        SortCheckBox.setText("Сортировать по должности:");
        SortCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SortCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(SortCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JobComboBox2, 0, 459, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(JobComboBox2, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(SortCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 1225, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Нормативы", jPanel5);

        jPanel4.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel4ComponentShown(evt);
            }
        });

        DirectoryLabel8.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel8.setText("Дата формирования заявки:");

        DateChooser2.setDateFormatString("dd.MM.y");
        DateChooser2.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N

        DirectoryLabel10.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        DirectoryLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DirectoryLabel10.setText("Формат выгружаемого файла:");

        buttonGroup1.add(CSVRadioButton);
        CSVRadioButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        CSVRadioButton.setText("CSV");
        CSVRadioButton.setEnabled(false);
        CSVRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CSVRadioButtonActionPerformed(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Наименование СИЗ", "Размер", "Рост", "Количество"
            }
        ));
        jTable2.setEnabled(false);
        jTable2.setRowHeight(30);
        jScrollPane13.setViewportView(jTable2);

        jButton1.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jButton1.setText("Выбрать путь и выгрузить");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        jButton3.setText("OK");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        buttonGroup1.add(PDFRadioButton);
        PDFRadioButton.setFont(new java.awt.Font("Century Gothic", 0, 16)); // NOI18N
        PDFRadioButton.setText("PDF");
        PDFRadioButton.setEnabled(false);
        PDFRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PDFRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(DirectoryLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(CSVRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(PDFRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(DirectoryLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(DateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(DateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(DirectoryLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(DirectoryLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(CSVRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PDFRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 1121, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Выгрузка", new javax.swing.ImageIcon(getClass().getResource("/accountingforworkwear/1.png")), jPanel4); // NOI18N

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void DirectoryDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DirectoryDeleteButtonActionPerformed
        Connection connection = DatabaseConnection.getConnection();
        String directoryText = DirectoryText.getText();
        boolean positionExists = false;
        boolean success = false;

        switch (directoryNum) {
            case 0:
                positionExists = DIRECTORY.getPositions().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Вы уверены, что хотите удалить эту позицию?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, удалить", "Нет, отменить"},
                            "Нет, отменить"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        success = DIRECTORY.deleteFromDatabase(connection, "positions", "Position", directoryText);
                        if (success) {
                            positionListModel.removeElement(directoryText);
                            DIRECTORY.getPositions().remove(directoryText);
                            updateComboBoxData(positionListModel);
                        }
                    }
                }
                break;

            case 1:
                positionExists = DIRECTORY.getGenders().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Вы уверены, что хотите удалить этот пол?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, удалить", "Нет, отменить"},
                            "Нет, отменить"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        success = DIRECTORY.deleteFromDatabase(connection, "genders", "Gender", directoryText);
                        if (success) {
                            DIRECTORY.getGenders().remove(directoryText);
                            genderListModel.removeElement(directoryText);
                            updateComboBoxData(genderListModel);
                        }
                    }
                }
                break;

            case 2:
                positionExists = DIRECTORY.getSizesClothing().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Вы уверены, что хотите удалить этот размер одежды?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, удалить", "Нет, отменить"},
                            "Нет, отменить"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        success = DIRECTORY.deleteFromDatabase(connection, "sizesclothing", "SizeClothing", directoryText);
                        if (success) {
                            DIRECTORY.getSizesClothing().remove(directoryText);
                            sizeClothingListModel.removeElement(directoryText);
                            updateComboBoxData(sizeClothingListModel);
                        }
                    }
                }
                break;

            case 3:
                positionExists = DIRECTORY.getHeights().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Вы уверены, что хотите удалить этот рост?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, удалить", "Нет, отменить"},
                            "Нет, отменить"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        success = DIRECTORY.deleteFromDatabase(connection, "heights", "Height", directoryText);
                        if (success) {
                            DIRECTORY.getHeights().remove(directoryText);
                            heightListModel.removeElement(directoryText);
                            updateComboBoxData(heightListModel);
                        }
                    }
                }
                break;

            case 4:
                positionExists = DIRECTORY.getSizesShoe().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Вы уверены, что хотите удалить этот размер обуви?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, удалить", "Нет, отменить"},
                            "Нет, отменить"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        success = DIRECTORY.deleteFromDatabase(connection, "sizesshoe", "SizeShoe", directoryText);
                        if (success) {
                            DIRECTORY.getSizesShoe().remove(directoryText);
                            sizeShoeListModel.removeElement(directoryText);
                            updateComboBoxData(sizeShoeListModel);
                        }
                    }
                }
                break;

            case 5:
                positionExists = DIRECTORY.getSizesHeaddress().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Вы уверены, что хотите удалить этот размер головного убора?",
                            "Подтверждение удаления",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, удалить", "Нет, отменить"},
                            "Нет, отменить"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        success = DIRECTORY.deleteFromDatabase(connection, "sizesheaddress", "SizeHeaddress", directoryText);
                        if (success) {
                            DIRECTORY.getSizesHeaddress().remove(directoryText);
                            sizeHeaddressListModel.removeElement(directoryText);
                            updateComboBoxData(sizeHeaddressListModel);
                        }
                    }
                }
                break;

            default:
                break;
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Запись успешно удалена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Не удалось удалить запись.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

        DirectoryText.setText("");
        DirectoryDeleteButton.setEnabled(false);
        DirectorySaveButton.setEnabled(true);
    }//GEN-LAST:event_DirectoryDeleteButtonActionPerformed

    private void DirectorySaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DirectorySaveButtonActionPerformed
        String oldStr = "";
        String newStr = "";

        switch (directoryNum) {
            case 0:
                oldStr = PositionList.getSelectedValue();
                newStr = DirectoryText.getText();
                positionListModel.set(PositionList.getSelectedIndex(), DirectoryText.getText());
                updateComboBoxData(positionListModel);
                break;
            case 1:
                oldStr = GenderList.getSelectedValue();
                newStr = DirectoryText.getText();
                genderListModel.set(GenderList.getSelectedIndex(), DirectoryText.getText());
                updateComboBoxData(genderListModel);
                break;
            case 2:
                oldStr = SizeClothingList.getSelectedValue();
                newStr = DirectoryText.getText();
                sizeClothingListModel.set(SizeClothingList.getSelectedIndex(), DirectoryText.getText());
                updateComboBoxData(sizeClothingListModel);
                break;
            case 3:
                oldStr = HeightList.getSelectedValue();
                newStr = DirectoryText.getText();
                heightListModel.set(HeightList.getSelectedIndex(), DirectoryText.getText());
                updateComboBoxData(heightListModel);
            case 4:
                oldStr = SizeShoeList.getSelectedValue();
                newStr = DirectoryText.getText();
                sizeShoeListModel.set(SizeShoeList.getSelectedIndex(), DirectoryText.getText());
                updateComboBoxData(sizeShoeListModel);
            case 5:
                oldStr = SizeHeaddressList.getSelectedValue();
                newStr = DirectoryText.getText();
                sizeHeaddressListModel.set(SizeHeaddressList.getSelectedIndex(), DirectoryText.getText());
                updateComboBoxData(sizeHeaddressListModel);
            default:
                break;
        }

        if (DIRECTORY.updateData(directoryNum, oldStr, newStr) == true) {
            DirectoryText.setText("");
            DirectoryText.setEnabled(false);
            JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Запись успешно обновлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при обновлении записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_DirectorySaveButtonActionPerformed

    private void DirectoryAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DirectoryAddButtonActionPerformed
        String directoryText = DirectoryText.getText();
        boolean positionExists = false;
        Connection connection = DatabaseConnection.getConnection(); // Получение соединения с базой данных

        switch (directoryNum) {
            case 0:
                positionExists = DIRECTORY.getPositions().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Такая позиция уже существует. Хотите добавить ещё одну?",
                            "Предупреждение",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, добавить", "Нет, изменить"},
                            "Нет, изменить"
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        DirectoryText.setText(directoryText);
                        return;
                    }
                }
                DIRECTORY.getPositions().add(DIRECTORY.getPositions().size(), directoryText);
                positionListModel.add(positionListModel.size(), directoryText);
                DIRECTORY.insertIntoDatabase(connection, "positions", "Position", directoryText);
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxData(positionListModel);
                break;

            case 1:
                positionExists = DIRECTORY.getGenders().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Такой пол уже существует. Хотите добавить ещё один?",
                            "Предупреждение",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, добавить", "Нет, изменить"},
                            "Нет, изменить"
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        DirectoryText.setText(directoryText);
                        return;
                    }
                }
                DIRECTORY.getGenders().add(DIRECTORY.getGenders().size(), directoryText);
                genderListModel.add(genderListModel.size(), directoryText);
                DIRECTORY.insertIntoDatabase(connection, "genders", "Gender", directoryText);
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxData(genderListModel);
                break;

            case 2:
                positionExists = DIRECTORY.getSizesClothing().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Такой размер одежды уже существует. Хотите добавить ещё один?",
                            "Предупреждение",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, добавить", "Нет, изменить"},
                            "Нет, изменить"
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        DirectoryText.setText(directoryText);
                        return;
                    }
                }
                DIRECTORY.getSizesClothing().add(DIRECTORY.getSizesClothing().size(), directoryText);
                sizeClothingListModel.add(sizeClothingListModel.size(), directoryText);
                DIRECTORY.insertIntoDatabase(connection, "sizesclothing", "SizeClothing", directoryText);
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxData(sizeClothingListModel);
                break;

            case 3:
                positionExists = DIRECTORY.getHeights().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Такой рост уже существует. Хотите добавить ещё один?",
                            "Предупреждение",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, добавить", "Нет, изменить"},
                            "Нет, изменить"
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        DirectoryText.setText(directoryText);
                        return;
                    }
                }
                DIRECTORY.getHeights().add(DIRECTORY.getHeights().size(), directoryText);
                heightListModel.add(heightListModel.size(), directoryText);
                DIRECTORY.insertIntoDatabase(connection, "heights", "Height", directoryText);
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxData(heightListModel);
                break;

            case 4:
                positionExists = DIRECTORY.getSizesShoe().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Такой размер обуви уже существует. Хотите добавить ещё один?",
                            "Предупреждение",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, добавить", "Нет, изменить"},
                            "Нет, изменить"
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        DirectoryText.setText(directoryText);
                        return;
                    }
                }
                DIRECTORY.getSizesShoe().add(DIRECTORY.getSizesShoe().size(), directoryText);
                sizeShoeListModel.add(sizeShoeListModel.size(), directoryText);
                DIRECTORY.insertIntoDatabase(connection, "sizesshoe", "SizeShoe", directoryText);
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxData(sizeShoeListModel);
                break;

            case 5:
                positionExists = DIRECTORY.getSizesHeaddress().contains(directoryText);
                if (positionExists) {
                    int choice = JOptionPane.showOptionDialog(
                            this,
                            "Такой размер головного убора уже существует. Хотите добавить ещё один?",
                            "Предупреждение",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            new String[]{"Да, добавить", "Нет, изменить"},
                            "Нет, изменить"
                    );

                    if (choice == JOptionPane.NO_OPTION) {
                        DirectoryText.setText(directoryText);
                        return;
                    }
                }
                DIRECTORY.getSizesHeaddress().add(DIRECTORY.getSizesHeaddress().size(), directoryText);
                sizeHeaddressListModel.add(sizeHeaddressListModel.size(), directoryText);
                DIRECTORY.insertIntoDatabase(connection, "sizesheaddress", "SizeHeaddress", directoryText);
                JOptionPane.showMessageDialog(this, "Запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxData(sizeHeaddressListModel);
                break;

            default:
                break;
        }

        DirectoryText.setText("");
        DirectoryAddButton.setEnabled(false);
        AddDirectoryCheckBox.setSelected(false);
        DirectorySaveButton.setEnabled(true);
    }//GEN-LAST:event_DirectoryAddButtonActionPerformed

    private void AddDirectoryCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AddDirectoryCheckBoxItemStateChanged
        if (AddDirectoryCheckBox.isSelected()) {
            DirectoryText.setEnabled(true);
            DirectoryText.setText("");
            DirectoryAddButton.setEnabled(true);
        } else {
            DirectoryText.setEnabled(false);
            DirectoryText.setText("");
            DirectoryAddButton.setEnabled(false);
        }
    }//GEN-LAST:event_AddDirectoryCheckBoxItemStateChanged

    private void SizeHeaddressPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_SizeHeaddressPanelComponentShown
        if (boolDir == true) {
            DirectoryLabel.setText("Размер головного убора");
            directoryNum = 5;
            //sizeHsetmodel();
        }
        DirectoryText.setText("");
        DirectoryText.setEnabled(false);
        DirectorySaveButton.setEnabled(false);
    }//GEN-LAST:event_SizeHeaddressPanelComponentShown

    private void SizeHeaddressListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_SizeHeaddressListValueChanged
        if (!evt.getValueIsAdjusting()) { // Проверка, не происходит ли регулировка значения
            if (accessRightsID == 2) {
                int selectedIndex = SizeHeaddressList.getSelectedIndex();
                if (selectedIndex != -1) { // Проверка, выбран ли элемент
                    DirectoryText.setText(DIRECTORY.getSizesHeaddress().get(SizeHeaddressList.getSelectedIndex()));
                    DirectoryText.setEnabled(true);
                    DirectorySaveButton.setEnabled(true);
                    DirectoryDeleteButton.setEnabled(true);
                }
            }
        }
    }//GEN-LAST:event_SizeHeaddressListValueChanged

    private void SizeShoePanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_SizeShoePanelComponentShown
        if (boolDir == true) {
            DirectoryLabel.setText("Размер обуви");
            directoryNum = 4;
            //sizeSHsetmodel();
        }
        DirectoryText.setText("");
        DirectoryText.setEnabled(false);
        DirectorySaveButton.setEnabled(false);
    }//GEN-LAST:event_SizeShoePanelComponentShown

    private void SizeShoeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_SizeShoeListValueChanged
        if (!evt.getValueIsAdjusting()) { // Проверка, не происходит ли регулировка значения
            if (accessRightsID == 2) {
                int selectedIndex = SizeShoeList.getSelectedIndex();
                if (selectedIndex != -1) { // Проверка, выбран ли элемент
                    DirectoryText.setText(DIRECTORY.getSizesShoe().get(SizeShoeList.getSelectedIndex()));
                    DirectoryText.setEnabled(true);
                    DirectorySaveButton.setEnabled(true);
                    DirectoryDeleteButton.setEnabled(true);
                }
            }
        }
    }//GEN-LAST:event_SizeShoeListValueChanged

    private void HeightPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_HeightPanelComponentShown
        if (boolDir == true) {
            DirectoryLabel.setText("Рост");
            directoryNum = 3;
            //heightsetmodel();
        }
        DirectoryText.setText("");
        DirectoryText.setEnabled(false);
        DirectorySaveButton.setEnabled(false);
    }//GEN-LAST:event_HeightPanelComponentShown

    private void HeightListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_HeightListValueChanged
        if (!evt.getValueIsAdjusting()) { // Проверка, не происходит ли регулировка значения
            if (accessRightsID == 2) {
                int selectedIndex = HeightList.getSelectedIndex();
                if (selectedIndex != -1) { // Проверка, выбран ли элемент
                    DirectoryText.setText(DIRECTORY.getHeights().get(HeightList.getSelectedIndex()));
                    DirectoryText.setEnabled(true);
                    DirectorySaveButton.setEnabled(true);
                    DirectoryDeleteButton.setEnabled(true);
                }
            }
        }
    }//GEN-LAST:event_HeightListValueChanged

    private void SizeClothingPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_SizeClothingPanelComponentShown
        if (boolDir == true) {
            DirectoryLabel.setText("Размер одежды");
            directoryNum = 2;
            //sizeCsetmodel();
        }
        DirectoryText.setText("");
        DirectoryText.setEnabled(false);
        DirectorySaveButton.setEnabled(false);
    }//GEN-LAST:event_SizeClothingPanelComponentShown

    private void SizeClothingListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_SizeClothingListValueChanged
        if (!evt.getValueIsAdjusting()) { // Проверка, не происходит ли регулировка значения
            if (accessRightsID == 2) {
                int selectedIndex = SizeClothingList.getSelectedIndex();
                if (selectedIndex != -1) { // Проверка, выбран ли элемент
                    DirectoryText.setText(DIRECTORY.getSizesClothing().get(SizeClothingList.getSelectedIndex()));
                    DirectoryText.setEnabled(true);
                    DirectorySaveButton.setEnabled(true);
                    DirectoryDeleteButton.setEnabled(true);
                }
            }
        }
    }//GEN-LAST:event_SizeClothingListValueChanged

    private void GenderPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_GenderPanelComponentShown
        if (boolDir == true) {
            DirectoryLabel.setText("Пол");
            directoryNum = 1;
            //gendersetmodel();
        }
        DirectoryText.setText("");
        DirectoryText.setEnabled(false);
        DirectorySaveButton.setEnabled(false);
    }//GEN-LAST:event_GenderPanelComponentShown

    private void GenderListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_GenderListValueChanged
        if (!evt.getValueIsAdjusting()) { // Проверка, не происходит ли регулировка значения
            if (accessRightsID == 2) {
                int selectedIndex = GenderList.getSelectedIndex();
                if (selectedIndex != -1) { // Проверка, выбран ли элемент
                    DirectoryText.setText(DIRECTORY.getGenders().get(GenderList.getSelectedIndex()));
                    DirectoryText.setEnabled(true);
                    DirectorySaveButton.setEnabled(true);
                    DirectoryDeleteButton.setEnabled(true);
                }
            }
        }
    }//GEN-LAST:event_GenderListValueChanged

    private void PositionPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_PositionPanelComponentShown
        if (boolDir == true) {
            DirectoryLabel.setText("Должность");
            directoryNum = 0;
            //directorysetmodel();
        }
        DirectoryText.setText("");
        DirectoryText.setEnabled(false);
        DirectorySaveButton.setEnabled(false);
    }//GEN-LAST:event_PositionPanelComponentShown

    private void PositionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_PositionListValueChanged
        if (!evt.getValueIsAdjusting()) { // Проверка, не происходит ли регулировка значения
            if (accessRightsID == 2) {
                int selectedIndex = PositionList.getSelectedIndex();
                if (selectedIndex != -1) { // Проверка, выбран ли элемент
                    DirectoryText.setText(DIRECTORY.getPositions().get(selectedIndex));
                    DirectoryText.setEnabled(true);
                    DirectorySaveButton.setEnabled(true);
                    DirectoryDeleteButton.setEnabled(true);
                }
            }
        }
    }//GEN-LAST:event_PositionListValueChanged

    private void NumenDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumenDeleteButtonActionPerformed
        String selectedCategoryName = jComboBox3.getSelectedItem().toString();
        DefaultListModel<String> model = null;

        if (numenCount == 1) {
            model = (DefaultListModel<String>) WorkwearList.getModel();
        } else if (numenCount == 2) {
            model = (DefaultListModel<String>) SafetyShoesList.getModel();
        } else if (numenCount == 3) {
            model = (DefaultListModel<String>) OtherSIZList.getModel();
        }

        if (model != null) {
            boolean isDeleted = nomenclature.deleteCategoryFromDatabase(selectedCategoryName, numenCount);
            if (isDeleted) {
                model.removeElement(selectedCategoryName);
                jComboBox3.removeItem(selectedCategoryName);
                NumenSaveButton.setEnabled(false);
                NumenDeleteButton.setEnabled(false);
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Запись успешно удалена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при удалении записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_NumenDeleteButtonActionPerformed

    private void NumenSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumenSaveButtonActionPerformed
        String selectedCategoryName = jComboBox3.getSelectedItem().toString();
        String newCategoryName = NumenText.getText();
        DefaultListModel<String> model = null;

        if (numenCount == 1) {
            model = (DefaultListModel<String>) WorkwearList.getModel();
        } else if (numenCount == 2) {
            model = (DefaultListModel<String>) SafetyShoesList.getModel();
        } else if (numenCount == 3) {
            model = (DefaultListModel<String>) OtherSIZList.getModel();
        }

        if (model != null) {
            boolean isUpdated = nomenclature.updateCategoryInDatabase(selectedCategoryName, newCategoryName, numenCount);
            if (isUpdated) {
                int index = model.indexOf(selectedCategoryName);
                if (index != -1) {
                    model.set(index, newCategoryName);
                }
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Запись успешно обновлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                populateComboBoxBasedOnTab();
                NumenText.setText("");
                NumenText.setEnabled(false);
                NumenSaveButton.setEnabled(false);
                NumenDeleteButton.setEnabled(false);
                jComboBox3.setSelectedIndex(-1);
            } else {
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при обновлении записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_NumenSaveButtonActionPerformed

    private void NumenAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumenAddButtonActionPerformed
        String categoryName = NumenText.getText();
        DefaultListModel<String> model = null;
        
        if (numenCount == 1) {
            model = (DefaultListModel<String>) WorkwearList.getModel();
        } else if (numenCount == 2) {
            model = (DefaultListModel<String>) SafetyShoesList.getModel();
        } else if (numenCount == 3) {
            model = (DefaultListModel<String>) OtherSIZList.getModel();
        }

        if (model != null) {
            model.addElement(categoryName);

            boolean success = nomenclature.addCategoryToDatabase(categoryName, numenCount);
            if (success == true) {
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Новая запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при добавлении новой записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        populateComboBoxBasedOnTab();
        AddNumenCheckBox.setSelected(false);
        NumenText.setText("");
        NumenText.setEnabled(false);
        NumenAddButton.setEnabled(false);
    }//GEN-LAST:event_NumenAddButtonActionPerformed

    private void AddNumenCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AddNumenCheckBoxItemStateChanged
        NumenAddButton.setEnabled(true);
        NumenText.setEnabled(true);
    }//GEN-LAST:event_AddNumenCheckBoxItemStateChanged

    private void SaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonActionPerformed
        if (setEdit == true) {
            String selectedId = IDComboBox.getSelectedItem().toString();

            int rowCount = jTable3.getRowCount();
            int selectedRow = -1; 

            for (int i = 0; i < rowCount; i++) {
                String idInTable = jTable3.getValueAt(i, 0).toString(); 
                if (selectedId.equals(idInTable)) {
                    selectedRow = i;
                    break;
                }
            }

            if (selectedRow != -1) {
                String upID = ServiceNText.getText();
                String upFIO = FIOText.getText();
                String upJob = JobComboBox.getSelectedItem().toString();
                String upDate = formatDate(DateChooser.getDate());
                String upSex = SexComboBox.getSelectedItem().toString();
                String upSC = SizeClothComboBox.getSelectedItem().toString();
                String upHeight = HeightComboBox.getSelectedItem().toString();
                String upSS = SizeShoeComboBox.getSelectedItem().toString();
                String upSH = SizeHeaddressComboBox.getSelectedItem().toString();

                jTable3.setValueAt(upID, selectedRow, 0);
                jTable3.setValueAt(upFIO, selectedRow, 1);
                jTable3.setValueAt(upJob, selectedRow, 2);
                jTable3.setValueAt(upDate, selectedRow, 3);
                jTable3.setValueAt(upSex, selectedRow, 4);
                jTable3.setValueAt(upSC, selectedRow, 5);
                jTable3.setValueAt(upHeight, selectedRow, 6);
                jTable3.setValueAt(upSS, selectedRow, 7);
                jTable3.setValueAt(upSH, selectedRow, 8);

                try {
                    DATARET.updateStaffTableFromModel(selectedId, upFIO, upJob, DateChooser.getDate(), upSex, upSC, upHeight, upSS, upSH);
                    JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Запись успешно обновлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    enabledStateInputFields(false);
                    SaveButton.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при обновлении записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Выберите строку для редактирования.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_SaveButtonActionPerformed

    private void EditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditButtonActionPerformed
        setEdit = true;
        String selectedId = IDComboBox.getSelectedItem().toString();
        Object[] rowData = getTableRowDataById(selectedId);
        if (rowData != null) {
            ServiceNText.setText(rowData[0].toString());
            FIOText.setText(rowData[1].toString());
            JobComboBox.setSelectedItem(rowData[2].toString());
            String dateAsString = rowData[3].toString();
            try {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                Date date = outputFormat.parse(dateAsString);
                String formattedDate = outputFormat.format(date);
                DateChooser.setDate(date);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            SexComboBox.setSelectedItem(rowData[4].toString());
            SizeClothComboBox.setSelectedItem(rowData[5].toString());
            HeightComboBox.setSelectedItem(rowData[6].toString());
            SizeShoeComboBox.setSelectedItem(rowData[7].toString());
            SizeHeaddressComboBox.setSelectedItem(rowData[8].toString());
        }
        enabledStateInputFields(true);
        SaveButton.setEnabled(true);
    }//GEN-LAST:event_EditButtonActionPerformed

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        if (!ServiceNText.getText().equals("") && !FIOText.getText().equals("") && DateChooser.getDate() != null) {
            STATE.setServiceNum(ServiceNText.getText());
            STATE.setFIO(FIOText.getText());
            STATE.setJob(JobComboBox.getSelectedItem().toString());
            STATE.setDate((formatDate(DateChooser.getDate())).toString());
            STATE.setSex(SexComboBox.getSelectedItem().toString());
            STATE.setSizeCloth(SizeClothComboBox.getSelectedItem().toString());
            STATE.setHeight(HeightComboBox.getSelectedItem().toString());
            STATE.setSizeShoe(SizeShoeComboBox.getSelectedItem().toString());
            STATE.setSizeHeaddressCloth(SizeHeaddressComboBox.getSelectedItem().toString());

            try {
                DATARET.addStaffToDatabase(ServiceNText.getText(), FIOText.getText(), JobComboBox.getSelectedItem().toString(), DateChooser.getDate(), SexComboBox.getSelectedItem().toString(), SizeClothComboBox.getSelectedItem().toString(), HeightComboBox.getSelectedItem().toString(), SizeShoeComboBox.getSelectedItem().toString(), SizeHeaddressComboBox.getSelectedItem().toString());
                DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
                Object[] rowData = {
                    STATE.getServiceNum(),
                    STATE.getFIO(),
                    STATE.getJob(),
                    STATE.getDate(),
                    STATE.getSex(),
                    STATE.getSizeCloth(),
                    STATE.getHeight(),
                    STATE.getSizeShoe(),
                    STATE.getSizeHeaddressCloth()
                };
                model.addRow(rowData);

                countID++;
                if (countID > 0) {
                    EditButton.setEnabled(true);
                    SaveButton.setEnabled(true);
                }
                updateIDComboBox();
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Новая запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при добавлении новой записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Заполните пустые поля", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_AddButtonActionPerformed

    private void DelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (BufferedReader br = Files.newBufferedReader(selectedFile.toPath(), Charset.forName("windows-1251"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ";");
                    Object[] rowData = new Object[9];
                    for (int i = 0; i < rowData.length && tokenizer.hasMoreTokens(); i++) {
                        rowData[i] = tokenizer.nextToken();
                    }
                    idList.add(rowData[0].toString());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    Date dateOfBirth = null;
                    try {
                        dateOfBirth = dateFormat.parse(rowData[3].toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        DATARET.addStaffToDatabase(rowData[0].toString(), rowData[1].toString(), rowData[2].toString(), dateOfBirth, rowData[4].toString(),
                                rowData[5].toString(), rowData[6].toString(), rowData[7].toString(), rowData[8].toString());
                        updateTable();
                    } catch (SQLException ex) {
                        Logger.getLogger(AccountingForWorkwearFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                updateIDComboBox();
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Данные успешно обновлены.", "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка при чтении файла", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_DelButtonActionPerformed

    private void AddEmployeeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AddEmployeeCheckBoxItemStateChanged
        if (AddEmployeeCheckBox.isSelected()) {
            enabledStateInputFields(true);
            AddButton.setEnabled(true);
        } else {
            enabledStateInputFields(false);
            AddButton.setEnabled(false);
        }
    }//GEN-LAST:event_AddEmployeeCheckBoxItemStateChanged

    private void SortCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SortCheckBoxActionPerformed
        if (SortCheckBox.isSelected()) {
            JobComboBox2.setEnabled(true);
        } else {
            JobComboBox2.setEnabled(false);
        }
    }//GEN-LAST:event_SortCheckBoxActionPerformed

    private void JobComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_JobComboBox2ItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            String selectedPosition = (String) JobComboBox2.getSelectedItem();
            Standards standards = new Standards();
            standards.updateTableStandards((DefaultTableModel) TableStandards.getModel(), selectedPosition);
        }
    }//GEN-LAST:event_JobComboBox2ItemStateChanged

    private void LogbookAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogbookAddButtonActionPerformed
        String FIO = (String) FIOComboBox.getSelectedItem();
        String employeeId = (String) JobComboBox3.getSelectedItem();
        String itemId = (String) SIZComboBox.getSelectedItem();
        int sizeSIZ = Integer.parseInt(SizeSIZText.getText());
        int heightSIZ = Integer.parseInt(HeightSIZText.getText());
        String issuedDate = formatAnDate(DateChooser1.getDate());
        Number value = (Number) jSpinner1.getValue();
        int issuedQuantity = value.intValue();
        
        boolean success = logbook.addLogbookEntry(FIO, employeeId, itemId, sizeSIZ, heightSIZ, issuedDate, issuedQuantity);

        if (success) {
            updateTableLogbook();
            JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Новая запись успешно добавлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при добавлении новой записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        AddLogbookCheckBox.setSelected(false);
        FIOComboBox.setSelectedIndex(0);
        FIOComboBox.setEnabled(false);
        JobComboBox3.setSelectedIndex(0);
        JobComboBox3.setEnabled(false);
        SIZComboBox.setSelectedIndex(0);
        SIZComboBox.setEnabled(false);
        SizeSIZText.setText("");
        SizeSIZText.setEnabled(false);
        DateChooser1.setDate(null);
        DateChooser1.setEnabled(false);
        jSpinner1.setValue(1);
        jSpinner1.setEnabled(false);
        LogbookAddButton.setEnabled(false);
        HeightSIZText.setText("");
        HeightSIZText.setEnabled(false);
    }//GEN-LAST:event_LogbookAddButtonActionPerformed

    private void AddLogbookCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_AddLogbookCheckBoxItemStateChanged
        if (AddLogbookCheckBox.isSelected()) {
            FIOComboBox.setEnabled(true);
            JobComboBox3.setEnabled(true);
            SIZComboBox.setEnabled(true);
            SizeSIZText.setEnabled(true);
            DateChooser1.setEnabled(true);
            jSpinner1.setEnabled(true);
            LogbookAddButton.setEnabled(true);
            HeightSIZText.setEnabled(true);
        } else {
            FIOComboBox.setEnabled(false);
            JobComboBox3.setEnabled(false);
            SIZComboBox.setEnabled(false);
            SizeSIZText.setEnabled(false);
            DateChooser1.setEnabled(false);
            jSpinner1.setEnabled(false);
            LogbookAddButton.setEnabled(false);
            HeightSIZText.setEnabled(false);
        }
    }//GEN-LAST:event_AddLogbookCheckBoxItemStateChanged

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (DateChooser2.getDate() != null) {
            boolean dataExists = checkDate(TableStandards, DateChooser2.getDate());

            if (dataExists) {
                CSVRadioButton.setEnabled(true);
                PDFRadioButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Данные для выгрузки отсутствуют.", "Информация", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (jTable2.getRowCount() == 0) {
            JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Нет данных для выгрузки. Выберите другую дату.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
        } else {
            if (CSVRadioButton.isSelected()) {
                saveFileDialogForCSV();
            } else if (PDFRadioButton.isSelected()) {
                saveFileDialogForPDF();
            } else {
                JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Не выбран формат файла для выгрузки. Укажите расширение.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void EditButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditButton1ActionPerformed
        selectedNumber = jComboBox4.getSelectedItem().toString();
        FIOComboBox.setEnabled(true);
        JobComboBox3.setEnabled(true);
        SIZComboBox.setEnabled(true);
        SizeSIZText.setEnabled(true);
        DateChooser1.setEnabled(true);
        jSpinner1.setEnabled(true);
        LogbookAddButton.setEnabled(true);
        HeightSIZText.setEnabled(true);
        DirectorySaveButton2.setEnabled(true);

        for (int i = 0; i < LogbookTable.getRowCount(); i++) {
            Object numberValue = LogbookTable.getValueAt(i, 0); 
            if (numberValue != null && numberValue.toString().equals(selectedNumber)) {
                String FIO = LogbookTable.getValueAt(i, 1).toString();
                FIOComboBox.setSelectedItem(FIO);
                String position = LogbookTable.getValueAt(i, 2).toString();
                JobComboBox3.setSelectedItem(position);
                String itemName = LogbookTable.getValueAt(i, 3).toString();
                SIZComboBox.setSelectedItem(itemName);
                SizeSIZText.setText(LogbookTable.getValueAt(i, 4).toString());
                HeightSIZText.setText(LogbookTable.getValueAt(i, 5).toString());
                String dateIssuedString = LogbookTable.getValueAt(i, 6).toString();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    Date dateIssued = dateFormat.parse(dateIssuedString);
                    DateChooser1.setDate(dateIssued);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                int count = (int) LogbookTable.getValueAt(i, 7);
                jSpinner1.setValue(count);
                //Date dateDue = (Date) LogbookTable.getValueAt(i, 8);
                break;
            }
        }
    }//GEN-LAST:event_EditButton1ActionPerformed

    private void jPanel1ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel1ComponentShown
        DirectoryLabel1.setText("Спецодежда");
        populateComboBoxBasedOnTab();
        numenCount = 1;
        AddNumenCheckBox.setSelected(false);
        NumenText.setText("");
        NumenText.setEnabled(false);
        NumenAddButton.setEnabled(false);
        jComboBox3.setSelectedIndex(-1);
    }//GEN-LAST:event_jPanel1ComponentShown

    private void jPanel3ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel3ComponentShown
        DirectoryLabel1.setText("Другое");
        populateComboBoxBasedOnTab();
        numenCount = 3;
        AddNumenCheckBox.setSelected(false);
        NumenText.setText("");
        NumenText.setEnabled(false);
        NumenAddButton.setEnabled(false);
        jComboBox3.setSelectedIndex(-1);
    }//GEN-LAST:event_jPanel3ComponentShown

    private void jPanel2ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel2ComponentShown
        DirectoryLabel1.setText("Спецобувь");
        populateComboBoxBasedOnTab();
        numenCount = 2;
        AddNumenCheckBox.setSelected(false);
        NumenText.setText("");
        NumenText.setEnabled(false);
        NumenAddButton.setEnabled(false);
        jComboBox3.setSelectedIndex(-1);
    }//GEN-LAST:event_jPanel2ComponentShown

    private void jComboBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox4ItemStateChanged
        EditButton1.setEnabled(true);
    }//GEN-LAST:event_jComboBox4ItemStateChanged

    private void DirectorySaveButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DirectorySaveButton2ActionPerformed
        for (int i = 0; i < LogbookTable.getRowCount(); i++) {
            Object numberValue = LogbookTable.getValueAt(i, 0);
            if (numberValue != null && numberValue.toString().equals(selectedNumber)) {
                String FIO = FIOComboBox.getSelectedItem().toString();
                String position = JobComboBox3.getSelectedItem().toString();
                String itemName = SIZComboBox.getSelectedItem().toString();
                int sizeSIZ = Integer.parseInt(SizeSIZText.getText());
                int heightSIZ = Integer.parseInt(HeightSIZText.getText());
                String dateIssued2 = formatAnDate(DateChooser1.getDate());
                String dateIssued1 = formatDate(DateChooser1.getDate());

                LogbookTable.getModel().setValueAt(FIO, i, 1);
                LogbookTable.getModel().setValueAt(position, i, 2);
                LogbookTable.getModel().setValueAt(itemName, i, 3);
                LogbookTable.getModel().setValueAt(sizeSIZ, i, 4);
                LogbookTable.getModel().setValueAt(heightSIZ, i, 5);
                LogbookTable.getModel().setValueAt(dateIssued1, i, 6);

                boolean success = logbook.updateLogbookEntry(selectedNumber, FIO, position, itemName, sizeSIZ, heightSIZ, dateIssued2);
                if (success) {
                    FIOComboBox.setSelectedIndex(0);
                    FIOComboBox.setEnabled(false);
                    JobComboBox3.setSelectedIndex(0);
                    JobComboBox3.setEnabled(false);
                    SIZComboBox.setSelectedIndex(0);
                    SIZComboBox.setEnabled(false);
                    SizeSIZText.setText("");
                    SizeSIZText.setEnabled(false);
                    DateChooser1.setDate(null);
                    DateChooser1.setEnabled(false);
                    jSpinner1.setValue(1);
                    jSpinner1.setEnabled(false);
                    LogbookAddButton.setEnabled(false);
                    HeightSIZText.setText("");
                    HeightSIZText.setEnabled(false);
                    EditButton1.setEnabled(false);
                    DirectorySaveButton2.setEnabled(false);
                    jComboBox4.setSelectedIndex(-1);
                    JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Запись успешно обновлена.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Ошибка при обновлении записи.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                break;
            }
        }
    }//GEN-LAST:event_DirectorySaveButton2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String selectedNumber2 = jComboBox4.getSelectedItem().toString();

        int rowIndex = -1;
        for (int i = 0; i < LogbookTable.getRowCount(); i++) {
            Object numberValue = LogbookTable.getValueAt(i, 0);
            if (numberValue != null && numberValue.toString().equals(selectedNumber2)) {
                rowIndex = i;
                break;
            }
        }

        if (rowIndex != -1) {
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String formattedDate = dateFormat.format(currentDate);
            LogbookTable.setValueAt(currentDate, rowIndex, 8);
            try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE logbook SET DueDate = ? WHERE ID_Log = ?")) {
                preparedStatement.setDate(1, new java.sql.Date(currentDate.getTime()));
                preparedStatement.setInt(2, rowIndex + 1);
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(AccountingForWorkwearFrame.this, "Строка с номером " + selectedNumber2 + " не найдена в таблице.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void NumenEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NumenEditButtonActionPerformed
        NumenDeleteButton.setEnabled(true);
        NumenSaveButton.setEnabled(true);
        NumenText.setEnabled(true);
        if (jComboBox3.getSelectedIndex() != -1) {
            NumenText.setText(jComboBox3.getSelectedItem().toString());
        }
    }//GEN-LAST:event_NumenEditButtonActionPerformed

    private void jTabbedPane2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane2StateChanged
        populateComboBoxBasedOnTab();
        jComboBox3.setSelectedIndex(1);
    }//GEN-LAST:event_jTabbedPane2StateChanged

    private void SizeHeaddressComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SizeHeaddressComboBoxItemStateChanged
        EditButton.setEnabled(true);
    }//GEN-LAST:event_SizeHeaddressComboBoxItemStateChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int choice = JOptionPane.showOptionDialog(
                AccountingForWorkwearFrame.this,
                "Вы уверены, что хотите завершить работу?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new String[]{"Да, выйти", "Нет, остаться"},
                "Нет, остаться"
        );

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else {
            setDefaultCloseOperation(AccountingForWorkwearFrame.DO_NOTHING_ON_CLOSE);
        }
    }//GEN-LAST:event_formWindowClosing

    private void CSVRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CSVRadioButtonActionPerformed
        if (CSVRadioButton.isSelected()) {
            jButton1.setEnabled(true);
        } else {
            jButton1.setEnabled(false);
        }
    }//GEN-LAST:event_CSVRadioButtonActionPerformed

    private void PDFRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PDFRadioButtonActionPerformed
        if (CSVRadioButton.isSelected()) {
            jButton1.setEnabled(true);
        } else {
            jButton1.setEnabled(false);
        }
    }//GEN-LAST:event_PDFRadioButtonActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        Timer timer = new Timer(5000, event -> checkPPEStatus());
        timer.setRepeats(false); // Однократный запуск
        timer.start();
    }//GEN-LAST:event_formWindowOpened

    private void jPanel4ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel4ComponentShown
        Timer timer = new Timer(5000, event -> checkPPEStatus());
        timer.setRepeats(false); // Однократный запуск
        timer.start();
    }//GEN-LAST:event_jPanel4ComponentShown

    private void DirectoryEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DirectoryEditButtonActionPerformed
        DirectoryDeleteButton.setEnabled(true);
        DirectorySaveButton.setEnabled(true);
        DirectoryText.setEnabled(true);
        if (jComboBox5.getSelectedIndex() != -1) {
            DirectoryText.setText(jComboBox5.getSelectedItem().toString());
        }
    }//GEN-LAST:event_DirectoryEditButtonActionPerformed

    private void jTabbedPane3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane3StateChanged
        int selectedIndex = jTabbedPane3.getSelectedIndex();
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        jComboBox5.setModel(comboBoxModel);
        switch (selectedIndex) {
            case 0: // Должности
                updateComboBoxData(positionListModel);
                break;
            case 1: // Пол
                updateComboBoxData(genderListModel);
                break;
            case 2: // Размеры одежды
                updateComboBoxData(sizeClothingListModel);
                break;
            case 3: // Росты
                updateComboBoxData(heightListModel);
                break;
            case 4: // Размеры обуви
                updateComboBoxData(sizeShoeListModel);
                break;
            case 5: // Размеры головного убора
                updateComboBoxData(sizeHeaddressListModel);
                break;
            default:
                comboBoxModel.removeAllElements();
                break;
        }
    }//GEN-LAST:event_jTabbedPane3StateChanged

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AccountingForWorkwearFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AccountingForWorkwearFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AccountingForWorkwearFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AccountingForWorkwearFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            // Установка глобальных стилей
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setUIFont(new javax.swing.plaf.FontUIResource(new Font("Century Gothic", Font.PLAIN, 16)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                State STATE = new State();
                AccountingForWorkwearFrame frame = new AccountingForWorkwearFrame(/*STATE*/);
                frame.setVisible(true);
            }
        });
    }

    // Метод для установки шрифта для всех элементов
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddButton;
    private javax.swing.JCheckBox AddDirectoryCheckBox;
    private javax.swing.JCheckBox AddEmployeeCheckBox;
    private javax.swing.JCheckBox AddLogbookCheckBox;
    private javax.swing.JCheckBox AddNumenCheckBox;
    private javax.swing.JRadioButton CSVRadioButton;
    private com.toedter.calendar.JDateChooser DateChooser;
    private com.toedter.calendar.JDateChooser DateChooser1;
    private com.toedter.calendar.JDateChooser DateChooser2;
    private javax.swing.JButton DelButton;
    private javax.swing.JButton DirectoryAddButton;
    private javax.swing.JButton DirectoryDeleteButton;
    private javax.swing.JButton DirectoryEditButton;
    private javax.swing.JLabel DirectoryLabel;
    private javax.swing.JLabel DirectoryLabel1;
    private javax.swing.JLabel DirectoryLabel10;
    private javax.swing.JLabel DirectoryLabel2;
    private javax.swing.JLabel DirectoryLabel3;
    private javax.swing.JLabel DirectoryLabel4;
    private javax.swing.JLabel DirectoryLabel5;
    private javax.swing.JLabel DirectoryLabel6;
    private javax.swing.JLabel DirectoryLabel7;
    private javax.swing.JLabel DirectoryLabel8;
    private javax.swing.JLabel DirectoryLabel9;
    private javax.swing.JButton DirectorySaveButton;
    private javax.swing.JButton DirectorySaveButton2;
    private javax.swing.JSplitPane DirectorySplitPane;
    private javax.swing.JTextField DirectoryText;
    private javax.swing.JButton EditButton;
    private javax.swing.JButton EditButton1;
    private javax.swing.JPanel EditDirectoryPanel;
    private javax.swing.JPanel EditLogbookPanel;
    private javax.swing.JPanel EditNomenclaturePanel;
    private javax.swing.JComboBox<String> FIOComboBox;
    private javax.swing.JTextField FIOText;
    private javax.swing.JList<String> GenderList;
    private javax.swing.JPanel GenderPanel;
    private javax.swing.JComboBox<String> HeightComboBox;
    private javax.swing.JList<String> HeightList;
    private javax.swing.JPanel HeightPanel;
    private javax.swing.JTextField HeightSIZText;
    private javax.swing.JComboBox<String> IDComboBox;
    private javax.swing.JComboBox<String> JobComboBox;
    private javax.swing.JComboBox<String> JobComboBox2;
    private javax.swing.JComboBox<String> JobComboBox3;
    private javax.swing.JButton LogbookAddButton;
    private javax.swing.JSplitPane LogbookSplitPane;
    private javax.swing.JTable LogbookTable;
    private javax.swing.JPanel NomenclaturePanel;
    private javax.swing.JButton NumenAddButton;
    private javax.swing.JButton NumenDeleteButton;
    private javax.swing.JButton NumenEditButton;
    private javax.swing.JButton NumenSaveButton;
    private javax.swing.JTextField NumenText;
    private javax.swing.JList<String> OtherSIZList;
    private javax.swing.JRadioButton PDFRadioButton;
    private javax.swing.JList<String> PositionList;
    private javax.swing.JPanel PositionPanel;
    private javax.swing.JComboBox<String> SIZComboBox;
    private javax.swing.JList<String> SafetyShoesList;
    private javax.swing.JButton SaveButton;
    private javax.swing.JTextField ServiceNText;
    private javax.swing.JComboBox<String> SexComboBox;
    private javax.swing.JComboBox<String> SizeClothComboBox;
    private javax.swing.JList<String> SizeClothingList;
    private javax.swing.JPanel SizeClothingPanel;
    private javax.swing.JComboBox<String> SizeHeaddressComboBox;
    private javax.swing.JList<String> SizeHeaddressList;
    private javax.swing.JPanel SizeHeaddressPanel;
    private javax.swing.JTextField SizeSIZText;
    private javax.swing.JComboBox<String> SizeShoeComboBox;
    private javax.swing.JList<String> SizeShoeList;
    private javax.swing.JPanel SizeShoePanel;
    private javax.swing.JCheckBox SortCheckBox;
    private javax.swing.JPanel StatePanel;
    private javax.swing.JSplitPane StateSplitPane;
    private javax.swing.JTable TableStandards;
    private javax.swing.JList<String> WorkwearList;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    // End of variables declaration//GEN-END:variables
}
