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
import javax.swing.table.DefaultTableModel;

public class Standards {

    private List<TermsOfIssue> termsOfIssueList;

    public Standards() {
        termsOfIssueList = new ArrayList<>();
        loadTermsOfIssue();
    }

    private void loadTermsOfIssue() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            if (connection != null) {
                String query = "SELECT t.ID_Positions, p.Position, t.Name_siz, s.Name_siz, t.Quantity_siz, t.Term_siz, t.IssuedQuantity, t.ReturnDate FROM termsofissue t "
                        + "JOIN positions p ON t.ID_Positions = p.ID_Positions "
                        + "JOIN siz s ON t.Name_siz = s.ID_siz";
                try (PreparedStatement statement = connection.prepareStatement(query); ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int positionID = resultSet.getInt(1);
                        String positionName = resultSet.getString(2);
                        int sizID = resultSet.getInt(3);
                        String sizName = resultSet.getString(4);
                        int quantity = resultSet.getInt(5);
                        int term = resultSet.getInt(6);
                        int quantitySum = resultSet.getInt(7);
                        String returnDate = resultSet.getString(8); 
                        termsOfIssueList.add(new TermsOfIssue(positionID, positionName, sizID, sizName, quantity, term, quantitySum, returnDate));
                    }
                }
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<TermsOfIssue> getTermsOfIssueList() {
        return termsOfIssueList;
    }

    public void updateTableStandards(DefaultTableModel model, String selectedPosition) {
        model.setRowCount(0);

        for (TermsOfIssue term : termsOfIssueList) {
            if (term.getPositionName().equals(selectedPosition)) {
                model.addRow(new Object[]{term.getPositionName(), term.getSizName(), term.getQuantity(), term.getTerm(), term.getQuantitySum(), term.getReturnDate()});
            }
        }
    }

    public class TermsOfIssue {

        private int positionID;
        private String positionName;
        private int sizID;
        private String sizName;
        private int quantity;
        private int term;
        private int quantitySum;
        private String returnDate;

        public TermsOfIssue(int positionID, String positionName, int sizID, String sizName, int quantity, int term, int quantitySum, String returnDate) {
            this.positionID = positionID;
            this.positionName = positionName;
            this.sizID = sizID;
            this.sizName = sizName;
            this.quantity = quantity;
            this.term = term;
            this.quantitySum = quantitySum;
            this.returnDate = returnDate;
        }

        public int getPositionID() {
            return positionID;
        }

        public String getPositionName() {
            return positionName;
        }

        public int getSizID() {
            return sizID;
        }

        public String getSizName() {
            return sizName;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getTerm() {
            return term;
        }

        public int getQuantitySum() {
            return quantitySum;
        }

        public Date getReturnDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date returnDate1 = null;
            if (returnDate != null) {
                try {
                    returnDate1 = sdf.parse(returnDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return returnDate1;
            } else {
                return returnDate1;
            }
        }
    }
}
