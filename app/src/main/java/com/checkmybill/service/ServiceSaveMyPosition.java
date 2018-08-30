package com.checkmybill.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.MyLocationMonitor;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;

/**
 * Created by Petrus A. (R@G3), ESPE... On 12/04/2017.
 */

public class ServiceSaveMyPosition extends Service {
    final private static String TAG = ServiceSaveMyPosition.class.getName();

    private MyLocationMonitor myLocation;
    private MyLocationListener myLocationListener;
    private LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting Service...");
        this.myLocationListener = new MyLocationListener();

        if (!(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "GPS ON");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            } else {
                Log.i(TAG, "GPS OFF");
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("ServiceSignalStrength", "PERMISSIONS PROBLEM");
                }

                locationManager.removeUpdates(myLocationListener);
                locationManager = null;
                myLocationListener = null;
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void SaveMyLocation() {
        if ( myLocation == null ) return;
        try {
            Dao<MyLocationMonitor, Integer> myLocationMonitorDao = OrmLiteHelper.getInstance(getApplicationContext()).getMyLocationMonitorDao();
            myLocationMonitorDao.create(myLocation);
            Log.e(TAG, "Saved");
        } catch (SQLException e) {
            Log.e(TAG, "DB -> " + e.getMessage());
        }
    }

    private void stopLocationListener() {
        if ( ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "PERMISSIONS PROBLEM");
        } else if ( locationManager != null ) {
            locationManager.removeUpdates(myLocationListener);
        }
        locationManager = null;
        myLocationListener = null;
        SaveMyLocation();
    }


    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location.toString());
            if ( myLocation == null ) myLocation = new MyLocationMonitor();
            myLocation.setLat(location.getLatitude());
            myLocation.setLng(location.getLongitude());

            // Finalizando listener e salvandoos dados...
            stopLocationListener();

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }
}
