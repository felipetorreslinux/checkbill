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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by Petrus A. (R@G3), ESPE... On 12/04/2017.
 */

public class ServiceSaveMyPosition extends Service {
    final private static String TAG = ServiceSaveMyPosition.class.getName();

    final private long EXECUTION_INTERVAL = (1000 * 60) * 30; // Salva a cada 30 minutos
    private MyLocationMonitor myLocation;
    private Context mContext = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting Service...");
        if ( mContext == null ) mContext = getApplicationContext();

        // Iniciando servico de localizacao
        SmartLocation.with(mContext)
                .location()
                .continuous()
                .config( new LocationParams.Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(0).setInterval(EXECUTION_INTERVAL).build())
                .start(smartLocationUpdateListener);

        return super.onStartCommand(intent, flags, startId);
    }

    private OnLocationUpdatedListener smartLocationUpdateListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            // Localizacao recebida?
            if ( location == null ) return;

            // Alimentando a classe de localizacao
            Log.d(TAG, "Location received, Value -> " + location.getLatitude() + " <> " + location.getLongitude());
            if ( myLocation == null ) myLocation = new MyLocationMonitor();
            myLocation.setLat(location.getLatitude());
            myLocation.setLng(location.getLongitude());

            // Salvando localizacao
            SaveMyLocation();
        }
    };

    private void SaveMyLocation() {
        if ( myLocation == null )
            return;

        try {
            Dao<MyLocationMonitor, Integer> myLocationMonitorDao = OrmLiteHelper.getInstance(getApplicationContext()).getMyLocationMonitorDao();
            myLocationMonitorDao.create(myLocation);
            Log.e(TAG, "Saved");
        } catch (SQLException e) {
            Log.e(TAG, "DB -> " + e.getMessage());
        }
    }
}
