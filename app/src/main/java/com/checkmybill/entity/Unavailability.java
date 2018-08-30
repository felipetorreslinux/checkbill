package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Victor Guerra on 21/01/2016.
 */

@DatabaseTable(tableName = "UNAVAILABILITY_TABLE")
public class Unavailability {

    @DatabaseField(id = true, columnName = "UNAVA_ID")
    private Integer id;
    @DatabaseField(columnName = "UNAVA_VALUE_SIGNAL", dataType = DataType.DOUBLE)
    private double valueSignal;
    @DatabaseField(columnName = "UNAVA_DATE_STARTED", dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date dateStarted;
    @DatabaseField(columnName = "UNAVA_DATE_FINISHED", dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date dateFinished;
    @DatabaseField(columnName = "UNAVA_CELL_ID", dataType = DataType.STRING)
    private String cellId;
    @DatabaseField(columnName = "UNAVA_LAT", dataType = DataType.DOUBLE)
    private double lat;
    @DatabaseField(columnName = "UNAVA_LNG", dataType = DataType.DOUBLE)
    private double lng;
    @DatabaseField(columnName = "UNAVA_SAVED", dataType = DataType.BOOLEAN)
    private boolean saved;

    @DatabaseField(columnName = "UNAVA_CURRENT", dataType = DataType.BOOLEAN)
    private boolean current;

    @DatabaseField(columnName = "UNAVA_USED", dataType = DataType.BOOLEAN, defaultValue = "true", canBeNull = false)
    private boolean used;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getValueSignal() {
        return valueSignal;
    }

    public void setValueSignal(double valueSignal) {
        this.valueSignal = valueSignal;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public Date getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(Date dateFinished) {
        this.dateFinished = dateFinished;
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

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isUsed() { return used; }

    public void setUsed(boolean used) { this.used = used; }
}
