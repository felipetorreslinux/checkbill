package com.checkmybill.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.checkmybill.CheckBillApplication;
import com.checkmybill.R;
import com.checkmybill.service.ServiceDataUploader;
import com.checkmybill.service.ServiceNetworkQuality;
import com.checkmybill.service.ServiceSaveMyPosition;
import com.checkmybill.service.ServiceSignalStrengthGET;
import com.checkmybill.service.ServiceWifiMonitor;
import com.checkmybill.service.TrafficMonitor;


/**
 * Created by Victor Guerra on 28/11/2015.
 */
public class ReceiverMain extends BroadcastReceiver {
    final private static String TAG = ReceiverMain.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {
        final TrafficMonitor trafficMonitor = new TrafficMonitor(context);
        final CheckBillApplication application = (CheckBillApplication) context.getApplicationContext();
        final String action = intent.getAction();
        Log.d(TAG, "Action -> " + action);

        if ( action.equals("checkmybill.intent.action.SAVE_MY_POSITION_ALARM") ) {
            // Salva a posicao atual (evita ficar sempre iniciando o servico para obter os dados)
            context.startService(new Intent(context, ServiceSaveMyPosition.class));
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Boot initialized... Starting Services and Alarms
            application.InitializeAlarms();
            application.InitializeServices();
            trafficMonitor.ExecuteTimeIntervalDataUpdate();
        } else if (action.equals(Intent.ACTION_SHUTDOWN)) {
            trafficMonitor.ExecuteTimeIntervalDataUpdate();
        } else if (action.equals(context.getString(R.string.intent_action_traffic_monitor))) {
            trafficMonitor.ExecuteTimeIntervalDataUpdate();
        } else if (action.equals(context.getString(R.string.intent_action_traffic_monitor_midnight_reset))) {
            trafficMonitor.ExecuteMidnightIntervalDataUpdate();
        } else if (action.equals(context.getString(R.string.intent_action_measure_signal_strength_alarm))) {
            context.startService(new Intent(context, ServiceSignalStrengthGET.class));
        } else if (action.equals(context.getString(R.string.intent_action_unavailability_alarm))) {
            context.startService(new Intent(context, ServiceDataUploader.class));
        } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
            context.startService(new Intent(context, ServiceWifiMonitor.class));
        } else if (action.equals(context.getString(R.string.intent_action_wifi_monitor_alarm))) {
            context.startService(new Intent(context, ServiceNetworkQuality.class));
        }
    }
    /*
    private void verifiyDataUseDb(Context context) {
        try {

            DateTime dateTimeCurrent = new DateTime();
            DateTime startDate = dateTimeCurrent.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
            DateTime endDate = dateTimeCurrent.withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
            List<DataUse> dataUses = null;

            //WIFI
            RuntimeExceptionDao<DataUse, Integer> dataUseeExceptionDao = OrmLiteHelper.getInstance(context).getDataUseRuntimeDao();
            try {
                dataUses = dataUseeExceptionDao.queryBuilder()
                        .where().between("DAT_USE_DATA", startDate.toDate(), endDate.toDate())
                        .and()
                        .eq("DAT_USE_MOBILE_DOWN_START", -1)
                        .query();
            } catch (SQLException e) {
                Log.e(getClass().getName(), Util.getMessageErrorFromExcepetion(e));
            }

            if (dataUses != null && !dataUses.isEmpty()) {
                for (DataUse dataUse : dataUses) {
                    if (dataUse.getWifiDownStart() > dataUse.getMobileDownEnd()) {
                        dataUseeExceptionDao.delete(dataUse);
                    }
                }
            }

            //MOB
            try {
                dataUses = dataUseeExceptionDao.queryBuilder()
                        .where().between("DAT_USE_DATA", startDate.toDate(), endDate.toDate())
                        .and()
                        .eq("DAT_USE_WIFI_DOWN_START", -1)
                        .query();
            } catch (SQLException e) {
                Log.e(getClass().getName(), Util.getMessageErrorFromExcepetion(e));
            }

            if (dataUses != null && !dataUses.isEmpty()) {
                for (DataUse dataUse : dataUses) {
                    if (dataUse.getWifiDownStart() > dataUse.getMobileDownEnd()) {
                        dataUseeExceptionDao.delete(dataUse);
                    }
                }
            }
        }catch(Exception e){
            Log.e(getClass().getName(), Util.getMessageErrorFromExcepetion(e));
        }
    }

    private void startServicePhoneListener(Context context) {
        context.startService(new Intent(context, ServicePhoneStateListener.class));
    }*/

}
