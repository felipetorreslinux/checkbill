package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Petrus A. (R@G3), ESPE... On 12/04/2017.
 */

@DatabaseTable(tableName = "MY_LOCATION_MONITOR_TABLE")
public class MyLocationMonitor {
    @DatabaseField(generatedId = true, columnName = "LOCATION_ID")
    private Long id;

    @DatabaseField(columnName = "SIG_STR_AVE_DATE", dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    @DatabaseField(columnName = "LATITUDE", dataType = DataType.DOUBLE)
    private double lat;

    @DatabaseField(columnName = "LONGITUDE", dataType = DataType.DOUBLE)
    private double lng;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
}
