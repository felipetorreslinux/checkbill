package com.checkmybill.entity;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Victor Guerra on 27/04/2016.
 */

public class NetworkQualityAverageApi {

    private String isp;
    private int latency;
    private double download;
    private double upload;
    private LatLng latLng;
    private String idRequest;
    private List<NetworkQuality> networkQualities;
    private int numEntradas;
    private int unavailability;
    private int signal;
    private String urlLogo;

    public NetworkQualityAverageApi() {
    }

    public NetworkQualityAverageApi(int latency, double download, double upload, LatLng latLng, List<NetworkQuality> networkQualities, int numEntradas, int unavailability, int signal) {
        this.latency = latency;
        this.download = download;
        this.upload = upload;
        this.latLng = latLng;
        this.networkQualities = networkQualities;
        this.numEntradas = numEntradas;
        this.unavailability = unavailability;
        this.signal = signal;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
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

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(String idRequest) {
        this.idRequest = idRequest;
    }

    public List<NetworkQuality> getNetworkQualities() {
        return networkQualities;
    }

    public void setNetworkQualities(List<NetworkQuality> networkQualities) {
        this.networkQualities = networkQualities;
    }

    public int getNumEntradas() {
        return numEntradas;
    }

    public void setNumEntradas(int numEntradas) {
        this.numEntradas = numEntradas;
    }

    public int getUnavailability() {
        return unavailability;
    }

    public void setUnavailability(int unavailability) {
        this.unavailability = unavailability;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public String getUrlLogo() {
        return urlLogo;
    }

    public void setUrlLogo(String urlLogo) {
        this.urlLogo = urlLogo;
    }
}
