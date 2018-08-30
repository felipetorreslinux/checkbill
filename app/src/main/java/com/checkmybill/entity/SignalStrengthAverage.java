package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Victor Guerra on 11/12/2015.
 */

@DatabaseTable(tableName = "SIGNAL_STRENGTH_AVERAGE_TABLE")
public class SignalStrengthAverage {

    @DatabaseField(id = true, columnName = "SIG_STR_AVE_ID")
    private Integer id;
    @DatabaseField(columnName = "SIG_STR_AVE_VALUE", dataType = DataType.DOUBLE)
    private double value;
    @DatabaseField(columnName = "SIG_STR_AVE_SAVED", dataType = DataType.BOOLEAN)
    private boolean saved;
    @DatabaseField(columnName = "SIG_STR_AVE_FINISHED", dataType = DataType.BOOLEAN)
    private boolean finished;
    @DatabaseField(columnName = "SIG_STR_AVE_DATE", dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    @DatabaseField(columnName = "SIG_STR_AVE_CELL_ID", dataType = DataType.STRING)
    //CID|LAC|MNC
    private String cellId;
    @DatabaseField(columnName = "SIG_STR_AVE_LAT", dataType = DataType.DOUBLE)
    private double lat;
    @DatabaseField(columnName = "SIG_STR_AVE_LNG", dataType = DataType.DOUBLE)
    private double lng;
    @DatabaseField(columnName = "SIG_STR_AVE_CUR", dataType = DataType.BOOLEAN)
    private boolean current;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
