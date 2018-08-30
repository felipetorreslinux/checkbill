package com.checkmybill.presentation.HomeFragments;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Petrus A. (R@G3), ESPE... On 15/12/2016.
 */

public class CoverageMapAreaClass {
    private final static String TAG = "CoverageMapAreaClass";
    private final static int LATITUDE_INCREMENT = 0;
    private final static int LONGITUDE_INCREMENT = 0;
    private class NortheastCoordinates {
        public double latitude = 0;
        public double longitude = 0;

        @Override
        public String toString() {return "lat/lng: (" + latitude + "," + longitude + ")";}
    }

    private class SouthwestCoordinates {
        public double latitude = 0;
        public double longitude = 0;
        @Override
        public String toString() {return "lat/lng: (" + latitude + "," + longitude + ")";}
    }

    // Metodos privados...
    public NortheastCoordinates northeast;
    public SouthwestCoordinates southwest;

    public CoverageMapAreaClass() {
        this.northeast = new NortheastCoordinates();
        this.southwest = new SouthwestCoordinates();
    }

    public boolean RegiaoDentroAreaCoberturaAtual(GoogleMap gmap) {
        // Checando se a area atual é maior que a anterior...
        final LatLngBounds currentArea = gmap.getProjection().getVisibleRegion().latLngBounds;
        Log.d(TAG, "Current Area: SouthWest ->" + currentArea.southwest.toString());
        Log.d(TAG, "Current Area: NorthEast ->" + currentArea.northeast.toString());

        Log.d(TAG, "Old Area: SouthWest ->" + southwest.toString());
        Log.d(TAG, "Old Area: NorthEast ->" + northeast.toString());

        if ( this.northeast.latitude < currentArea.northeast.latitude ) return false;
        if ( this.southwest.latitude > currentArea.southwest.latitude ) return false;

        if ( currentArea.northeast.longitude > northeast.longitude ) return false;
        if ( currentArea.southwest.longitude < southwest.longitude ) return false;

        return true;
    }

    public void AtualizarAreaCoberturaMapa(GoogleMap gmap) {
        final LatLngBounds curScreen = gmap.getProjection().getVisibleRegion().latLngBounds;
        this.northeast.latitude = curScreen.northeast.latitude + LATITUDE_INCREMENT;
        this.northeast.longitude = curScreen.northeast.longitude + LONGITUDE_INCREMENT;
        this.southwest.latitude = curScreen.southwest.latitude + LATITUDE_INCREMENT;
        this.southwest.longitude = curScreen.southwest.longitude + LONGITUDE_INCREMENT;
    }

    public String ObterAreaCobertura() {
        StringBuilder areaMapa = new StringBuilder();
        areaMapa.append(this.northeast.latitude);
        areaMapa.append("|");
        areaMapa.append(this.northeast.longitude);
        areaMapa.append(",");
        areaMapa.append(this.southwest.latitude);
        areaMapa.append("|");
        areaMapa.append(this.southwest.longitude);
        return areaMapa.toString();
    }
}
