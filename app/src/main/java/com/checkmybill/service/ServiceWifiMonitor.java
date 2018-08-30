package com.checkmybill.service;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.NetworkWifi;
import com.checkmybill.receiver.ReceiverMain;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Victor Guerra on 10/12/2015.
 */
public class ServiceWifiMonitor extends Service {

    private MyLocationListener myLocationListener;
    private LocationManager locationManager;
    private NetworkWifi networkWifi;
    private Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        boolean valueSfMonitorWifi = sharedPrefsUtil.getSfMonitorWifi();

        if (wifiManager.isWifiEnabled()) {
            Log.i(BroadcastReceiver.class.getSimpleName(), "WIFI ENABLE");

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connManager.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i(BroadcastReceiver.class.getSimpleName(), "WIFI CONNECT");
                Log.i(BroadcastReceiver.class.getSimpleName(), "NETWORK NAME: " + netInfo.getExtraInfo());
                Log.i(BroadcastReceiver.class.getSimpleName(), "NETWORK NAME: " + netInfo.getReason());
                Log.i(BroadcastReceiver.class.getSimpleName(), "NETWORK NAME: " + netInfo.getSubtypeName());
                Log.i(BroadcastReceiver.class.getSimpleName(), "NETWORK NAME: " + netInfo.getTypeName());

                if (!valueSfMonitorWifi) {
                    Log.i(BroadcastReceiver.class.getSimpleName(), "SERVICE WIFI MONITOR DISABLE");
                    //iniciar service

                    RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao = OrmLiteHelper.getInstance(context).getNetworkWifiRuntimeExceptionDao();
                    try {
                        List<NetworkWifi> networkWifis = networkWifiRuntimeExceptionDao.queryBuilder()
                                .where().eq("NET_WIF_SSID", wifiManager.getConnectionInfo().getSSID()).query();

                        if (networkWifis.isEmpty()) {
                            Log.i(BroadcastReceiver.class.getSimpleName(), "NEW WIFI");
                            //nova wifi
                            networkWifi = new NetworkWifi();
                            networkWifi.setNetworkId(wifiManager.getConnectionInfo().getNetworkId());
                            networkWifi.setSsID(wifiManager.getConnectionInfo().getSSID());

                            networkWifiRuntimeExceptionDao.create(networkWifi);

                            List<NetworkWifi> networkWifisAux = networkWifiRuntimeExceptionDao.queryBuilder()
                                    .where().eq("NET_WIF_SSID", wifiManager.getConnectionInfo().getSSID()).query();
                            networkWifi = networkWifisAux.get(0);
                            int idResult = networkWifi.getId();

                            Log.i(BroadcastReceiver.class.getSimpleName(), "   ID NEW:  " + idResult);
                            sharedPrefsUtil.setSfCurrentWifi(idResult);
                        } else {
                            Log.i(BroadcastReceiver.class.getSimpleName(), "WIFI EXIST");
                            sharedPrefsUtil.setSfCurrentWifi(networkWifis.get(0).getId());
                            //wifi existente
                            networkWifi = networkWifis.get(0);
                        }

                        startAlarmService();
                        getOperatorName();

                        Log.i(BroadcastReceiver.class.getSimpleName(), "START -> SERVICE WIFI MONITOR");
                        sharedPrefsUtil.setSfMonitorWifi(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        stopSelf();
                    }
                } else {
                    stopSelf();
                }


            } else {
                Log.i(BroadcastReceiver.class.getSimpleName(), "WIFI DISCONNECT");
                if (valueSfMonitorWifi) {
                    Log.i(BroadcastReceiver.class.getSimpleName(), "SERVICE WIFI MONITOR RUNNING");
                    Log.i(BroadcastReceiver.class.getSimpleName(), "STOP -> SERVICE WIFI MONITOR");

                    //cancelar service
                    stopAlarmService();
                    sharedPrefsUtil.setSfMonitorWifi(false);

                    stopSelf();
                }
            }
        } else {
            Log.i(BroadcastReceiver.class.getSimpleName(), "WIFI DISABLE");
            if (valueSfMonitorWifi) {
                Log.i(BroadcastReceiver.class.getSimpleName(), "SERVICE WIFI MONITOR RUNNING");
                Log.i(BroadcastReceiver.class.getSimpleName(), "STOP -> SERVICE WIFI MONITOR");

                //cancelar service
                stopAlarmService();
                sharedPrefsUtil.setSfMonitorWifi(false);

                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void getLocationNetwork() {

        if (networkWifi.getLat() == 0 || networkWifi.getLng() == 0) {
            myLocationListener = new MyLocationListener();

            if (!(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.i("Start Service", "GPS ON");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                            0, myLocationListener);
                } else {
                    Log.i("Start Service", "GPS OFF");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("ServiceWifiMonitor", "PERMISSIONS PROBLEM");
                    }
                    locationManager.removeUpdates(myLocationListener);
                    locationManager = null;
                    myLocationListener = null;

                    stopSelf();
                }
            }
        } else {
            stopSelf();
        }


    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i("GPS_PROVIDER Listener", "onLocationChanged: " + location.toString());
            if (locationManager != null && location != null) {
                networkWifi.setLat(location.getLatitude());
                networkWifi.setLng(location.getLongitude());

                RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao = OrmLiteHelper.getInstance(context).getNetworkWifiRuntimeExceptionDao();
                networkWifiRuntimeExceptionDao.update(networkWifi);

                stopLocationLsitener();
            }
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

    private void stopLocationLsitener() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("ServiceWifiMonitor", "PERMISSIONS PROBLEM");
        } else {
            locationManager.removeUpdates(myLocationListener);
        }
        locationManager = null;
        myLocationListener = null;

        stopSelf();
    }

    private void startAlarmService() {
        Util.defineWifiMonitorAlarm(context);
    }

    private void stopAlarmService() {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReceiverMain.class);
        intent.setAction(context.getString(R.string.intent_action_wifi_monitor_alarm));
        alarmIntent = PendingIntent.getBroadcast(context, Util.WIFI_MONITOR_ALARM_ID, intent, 0);

        alarmMgr.cancel(alarmIntent);
    }

    private void getOperatorName() {

        if (networkWifi.getIsp() == null || networkWifi.getIsp().length() == 0) {
            final JsonObjectRequest jsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(JsonObjectRequest.Method.POST, Util.getSuperUrlServiceObterNomePorvedor(getApplicationContext()), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            String isp = response.getString("isp");
                            networkWifi.setIsp(isp);

                            RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao = OrmLiteHelper.getInstance(context).getNetworkWifiRuntimeExceptionDao();
                            networkWifiRuntimeExceptionDao.update(networkWifi);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    getLocationNetwork();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i(getClass().getName(), error.toString() + error.getNetworkTimeMs() + Util.getMessageErrorFromExcepetion(error));

//                    String json = "";
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {
                        /*switch (response.statusCode) {
                            case 400:
                                json = new String(response.data);
                                break;
                        }*/
                        Log.i(getClass().getName(), "response MyRequest: " + response.toString() + " / code: " + response.statusCode + " / data: " + new String(response.data).toString());
                    }

                    getLocationNetwork();
                }
            });

            jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 5000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 5000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            Volley.newRequestQueue(context).add(jsonObjectRequest);
        } else {
            getLocationNetwork();
        }


    }

}