package com.checkmybill;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.checkmybill.service.ServiceAutoStarter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Petrus A. (R@G3), ESPE... On 08/05/2017.
 */

public class CheckBillApplication extends Application {
    private final int PERMISSION_REQUEST_CODE = 101;
    final String TAG = getClass().getName();
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;

        // Inializando Alarms (Serviços de Alarme)
        this.InitializeAlarms();
        this.InitializeServices();
    }

    public void InitializeServices() {
        new ServiceAutoStarter(this).initializeCheckbillServices();
    }

    public void InitializeAlarms() {
        new SchedAlarmControl().InitTrafficMonitor();
        new ServiceAutoStarter(this).initializeCheckbillAlarms();
    }

    public String[] GetUnGrantedNecessaryPermissions() {
        ArrayList<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED )
            permissionList.add(Manifest.permission.READ_SMS);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED )
            permissionList.add(Manifest.permission.READ_CALL_LOG);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED )
            permissionList.add(Manifest.permission.INTERNET);
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED )
            permissionList.add(Manifest.permission.READ_PHONE_STATE);

        String[] permissionArrList = permissionList.toArray(new String[permissionList.size()]);
        return permissionArrList;
    }

    class SchedAlarmControl {
        private static final int TRAFFIC_MONITOR_ALARM_ID = 1;

        public void InitTrafficMonitor() {
            Log.d(TAG, "Initializing Traffic Alarm - Seconds Interval");
            String intentAction = getString(R.string.intent_action_traffic_monitor);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, TRAFFIC_MONITOR_ALARM_ID, new Intent(intentAction), PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 200, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

            // Definindo o alarm a ser disparada as 00 horas
            Log.d(TAG, "Initializing Traffic Alarm - Midnight");
            intentAction = getString(R.string.intent_action_traffic_monitor_midnight_reset);
            Calendar curCal = Calendar.getInstance();
            curCal.set(Calendar.HOUR, 0);
            curCal.set(Calendar.MINUTE, 0);
            curCal.set(Calendar.SECOND, 0);
            curCal.add(Calendar.DATE, 1);
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(intentAction), PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, curCal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }
}
