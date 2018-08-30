package com.checkmybill.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

/**
 * Created by Victor Guerra on 28/04/2016.
 */
@DatabaseTable(tableName = "NETWORK_WIFI_TABLE")
public class NetworkWifi {

    @DatabaseField(id = true, columnName = "NET_WIF_ID")
    private Integer id;
    @DatabaseField(columnName = "NET_WIF_ISP")
    private String isp;
    @DatabaseField(columnName = "NET_WIF_SSID")
    private String ssID;
    @DatabaseField(columnName = "NET_WIF_NET_ID")
    private Integer networkId;
    @ForeignCollectionField(eager = true)
    private Collection<NetworkQuality> networkQualiites;
    @DatabaseField(columnName = "NET_WIF_LAT")
    private double lat;
    @DatabaseField(columnName = "NET_WIF_LNG")
    private double lng;
    @DatabaseField(columnName = "NET_WIF_URL_LOGO")
    private String urlLogo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getSsID() {
        return ssID;
    }

    public void setSsID(String ssID) {
        this.ssID = ssID;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }

    public Collection<NetworkQuality> getNetworkQualiites() {
        return networkQualiites;
    }

    public void setNetworkQualiites(Collection<NetworkQuality> networkQualiites) {
        this.networkQualiites = networkQualiites;
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

    public String getUrlLogo() {
        return urlLogo;
    }

    public void setUrlLogo(String urlLogo) {
        this.urlLogo = urlLogo;
    }

    @Override
    public String toString() {
        if (networkQualiites != null && !networkQualiites.isEmpty()) {
            return id + " | " + ssID + " | " + networkId + " | [" + networkQualiites.size() + "]";
        }
        return id + " | " + ssID + " | " + networkId;
    }
}
