package accountingforworkwear;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;


public class State {

    String serviceNum = "";
    String FIO = "";
    String job = "";
    String date = "";
    String sex = "";
    String sizeCloth = "";
    String height = "";
    String sizeShoe = "";
    String sizeHeaddressCloth = "";
    List<Object[]> ST = new ArrayList<>();

    public String getServiceNum() {
        return serviceNum;
    }

    public void setServiceNum(String serviceNum) {
        this.serviceNum = serviceNum;
    }

    public String getFIO() {
        return FIO;
    }

    public void setFIO(String FIO) {
        this.FIO = FIO;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSizeCloth() {
        return sizeCloth;
    }

    public void setSizeCloth(String sizeCloth) {
        this.sizeCloth = sizeCloth;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getSizeShoe() {
        return sizeShoe;
    }

    public void setSizeShoe(String sizeShoe) {
        this.sizeShoe = sizeShoe;
    }

    public String getSizeHeaddressCloth() {
        return sizeHeaddressCloth;
    }

    public void setSizeHeaddressCloth(String sizeHeaddressCloth) {
        this.sizeHeaddressCloth = sizeHeaddressCloth;
    }

    public List<Object[]> getST() {
        return ST;
    }

    public void addST(Object[] rowST) {
        ST.add(rowST);
    }
}
