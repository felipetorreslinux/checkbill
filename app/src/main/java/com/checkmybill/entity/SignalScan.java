package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Victor Guerra on 16/11/2016.
 */

@DatabaseTable(tableName = "SIGNAL_SCAN_TABLE")
public class SignalScan {

    @DatabaseField(id = true, columnName = "SIG_SCA_ID")
    private Integer id;
    @DatabaseField(columnName = "SIG_SCA_DATE", dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    @DatabaseField(columnName = "SIG_SCA_DBM", dataType = DataType.DOUBLE)
    private double dbm;
    @DatabaseField(columnName = "SIG_SCA_ASU", dataType = DataType.INTEGER)
    private int asu;
    @DatabaseField(columnName = "SIG_SCA_TYPE", dataType = DataType.STRING)
    private String type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getDbm() {
        return dbm;
    }

    public void setDbm(double dbm) {
        this.dbm = dbm;
    }

    public Integer getAsu() {
        return asu;
    }

    public void setAsu(Integer asu) {
        this.asu = asu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
