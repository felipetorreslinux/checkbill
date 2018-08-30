package com.checkmybill.entity;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Victor Guerra on 27/04/2016.
 */

@DatabaseTable(tableName = "NETWORK_QUALITY_TABLE")
public class NetworkQuality implements Comparable<NetworkQuality> {

    @DatabaseField(id = true, columnName = "NET_QUALI_ID")
    private Integer id;
    @DatabaseField(columnName = "NET_QUALI_DATE", dataType = DataType.DATE_STRING,
            format = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    @DatabaseField(columnName = "NET_QUALI_LATENCY")
    private int latency;
    @DatabaseField(columnName = "NET_QUALI_DOWNLOAD")
    private double download;
    @DatabaseField(columnName = "NET_QUALI_UPLOAD")
    private double upload;
    @DatabaseField(columnName = "NET_NAME")
    private String name;
    @DatabaseField(columnName = "NET_QUALI_NETWORK_WIFI", foreign = true, foreignAutoRefresh = true)
    private NetworkWifi networkWifi;
    @DatabaseField(columnName = "NET_QUALI_SYNC", dataType = DataType.BOOLEAN, defaultValue = "false")
    private boolean sync;

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

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public double getDownload() {
        return download;
    }

    public void setDownload(double download) {
        this.download = download;
    }

    public double getUpload() {
        return upload;
    }

    public void setUpload(double upload) {
        this.upload = upload;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NetworkWifi getNetworkWifi() {
        return networkWifi;
    }

    public void setNetworkWifi(NetworkWifi networkWifi) {
        this.networkWifi = networkWifi;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public String toString() {
        return id + " | " + latency + " | " + upload + " | " + download + " | " + name + " | [[" + networkWifi.toString() + "]]";
    }

    @Override
    public int compareTo(@NonNull NetworkQuality networkQuality) {
        Double local = this.download;
        return local.compareTo(networkQuality.getDownload());
    }
}
