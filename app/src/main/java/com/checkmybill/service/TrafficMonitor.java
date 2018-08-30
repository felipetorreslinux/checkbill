package com.checkmybill.service;

import android.content.Context;
import android.net.TrafficStats;
import android.text.format.Formatter;
import android.util.Log;

import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;

import org.joda.time.LocalDate;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Petrus A. (R@G3), ESPE... On 08/05/2017.
 */

public class TrafficMonitor {
    final private String TAG = getClass().getName();
    private Context mContext;
    private OrmLiteHelper ormLiteHelper;

    public TrafficMonitor(Context context) {
        this.mContext = context;
        this.ormLiteHelper = new OrmLiteHelper(mContext);
    }

    private TrafficMonitor_Mobile newTrafficMobDBEntry() {
        Date currentDate = new Date();
        TrafficMonitor_Mobile entry = new TrafficMonitor_Mobile();
        entry.setDatePeriodo(currentDate);
        entry.setCurrentReceivedBytes_start(TrafficStats.getMobileRxBytes());
        entry.setCurrentReceivedBytes_end(TrafficStats.getMobileRxBytes());
        entry.setCurrentSendedBytes_start(TrafficStats.getMobileTxBytes());
        entry.setCurrentSendedBytes_end(TrafficStats.getMobileTxBytes());
        return entry;
    }

    private TrafficMonitor_WiFi newTrafficWifiDBEntry() {
        Date currentDate = new Date();
        TrafficMonitor_WiFi entry = new TrafficMonitor_WiFi();
        entry.setDatePeriodo(currentDate);
        final long wifiTxBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
        final long wifiRxBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
        entry.setCurrentReceivedBytes_start(wifiRxBytes);
        entry.setCurrentReceivedBytes_end(wifiRxBytes);
        entry.setCurrentSendedBytes_start(wifiTxBytes);
        entry.setCurrentSendedBytes_end(wifiTxBytes);
        return entry;
    }

    private TrafficMonitor_Mobile getLastMobileEntry() throws SQLException {
        List<TrafficMonitor_Mobile> trafficMobList = ormLiteHelper.getTrafficMonitorMobileDao().queryBuilder()
                .orderBy("id_mob_monitor", false)
                .limit(1L)
                .where().eq("data_sync", false)
                .query();
        return (trafficMobList.size() > 0) ? trafficMobList.get(0) : null;
    }

    private TrafficMonitor_WiFi getLastWiFiEntry() throws SQLException {
        List<TrafficMonitor_WiFi> trafficWifiList = ormLiteHelper.getTrafficMonitorWifiDao().queryBuilder()
                .orderBy("id_wifi_monitor", false)
                .limit(1L)
                .where().eq("data_sync", false)
                .query();
        return (trafficWifiList.size() > 0) ? trafficWifiList.get(0) : null;
    }


    public void ExecuteMidnightIntervalDataUpdate() {
        try {
            this.ExecuteTimeIntervalDataUpdate();

            // Criando uma nova entrada para o dia atual
            ormLiteHelper.getTrafficMonitorMobileDao().create(newTrafficMobDBEntry());
            ormLiteHelper.getTrafficMonitorWifiDao().create(newTrafficWifiDBEntry());
        } catch ( SQLException ex ) {
        }
    }

    public void ExecuteTimeIntervalDataUpdate() {
        LocalDate nowDate = LocalDate.now();
        try {
            // Obtendo a ultima entrada do dia anterior...
            TrafficMonitor_Mobile currentMobTrafficValue = getLastMobileEntry();
            if (currentMobTrafficValue != null && currentMobTrafficValue.getCurrentReceivedBytes_end() <= TrafficStats.getMobileRxBytes() && !nowDate.isBefore(LocalDate.fromDateFields(currentMobTrafficValue.getDatePeriodo()))) {
                Log.d(TAG, "Updating entry (Mobile)");
                currentMobTrafficValue.setCurrentReceivedBytes_end(TrafficStats.getMobileRxBytes());
                currentMobTrafficValue.setCurrentSendedBytes_end(TrafficStats.getMobileTxBytes());
                ormLiteHelper.getTrafficMonitorMobileDao().update(currentMobTrafficValue);
            } else {
                Log.d(TAG, "Creating a new entry (Mobile)");
                ormLiteHelper.getTrafficMonitorMobileDao().create(newTrafficMobDBEntry());
            }

            final long wifiRxBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
            final long wifiTxBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
            TrafficMonitor_WiFi currentWifiTrafficValue = getLastWiFiEntry();
            if (currentWifiTrafficValue != null && currentWifiTrafficValue.getCurrentReceivedBytes_end() <= wifiRxBytes && !nowDate.isBefore(LocalDate.fromDateFields(currentWifiTrafficValue.getDatePeriodo()))) {
                Log.d(TAG, "Updating entry (WiFi)");
                currentWifiTrafficValue.setCurrentReceivedBytes_end(wifiRxBytes);
                currentWifiTrafficValue.setCurrentSendedBytes_end(wifiTxBytes);
                ormLiteHelper.getTrafficMonitorWifiDao().update(currentWifiTrafficValue);
            } else {
                Log.d(TAG, "Creating a new entry (WiFi)");
                ormLiteHelper.getTrafficMonitorWifiDao().create(newTrafficWifiDBEntry());
            }
        } catch (SQLException ex ) {
            Log.e(TAG, ex.getMessage());
        }
    }


    /**
     * Get total WiFi bytes transfered...
     * @param period -> Periodo
     * @return -> Transfered Bytes
     */
    public long getTotalWifiDataTransfer(Date period) {
        LocalDate today = LocalDate.now();
        long totalByteTrans = 0;
        boolean currentDate = (!today.isBefore(LocalDate.fromDateFields(period)));
        try {
            List<TrafficMonitor_WiFi> trafficList = ormLiteHelper.getTrafficMonitorWifiDao().queryBuilder()
                    .orderBy("id_wifi_monitor", true)
                    .where().eq("date_period", period)
                    .query();
            final int size = trafficList.size() - (currentDate ? 1 : 0);
            if ( size < 0 ) return 0; // Empty

            for ( int i = 0; i < size; i++ ) {
                TrafficMonitor_WiFi dt = trafficList.get(i);
                totalByteTrans += (dt.getCurrentReceivedBytes_end() - dt.getCurrentReceivedBytes_start());
                totalByteTrans += (dt.getCurrentSendedBytes_end() - dt.getCurrentSendedBytes_start());
            }

            if ( currentDate ) {
                TrafficMonitor_WiFi dt = trafficList.get(size);
                long wifiTx = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                long wifiRx = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                totalByteTrans += (wifiTx - dt.getCurrentSendedBytes_start());
                totalByteTrans += (wifiRx - dt.getCurrentReceivedBytes_start());
            }

            return (totalByteTrans < 0 ) ? 0 : totalByteTrans;
        } catch ( SQLException ex ) {
            Log.e(TAG, ex.getMessage());
            return -1;
        }
    }

    /**
     * Get total WiFi bytes transfered (Current Date)
     * @return -> Transfered Bytes
     */
    public long getTotalWifiDataTransfer() {
        return this.getTotalWifiDataTransfer(new Date());
    }

    /**
     * Get total Mobile bytes transfered
     * @param period -> Periodo
     * @return -. Transfered Bytes
     */
    public long getTotalMobileDataTransfer(Date period) {
        LocalDate today = LocalDate.now();
        long totalByteTrans = 0;
        boolean currentDate = (!today.isBefore(LocalDate.fromDateFields(period)));
        try {
            List<TrafficMonitor_Mobile> trafficList = ormLiteHelper.getTrafficMonitorMobileDao().queryBuilder()
                    .orderBy("id_mob_monitor", true)
                    .where().eq("date_period", period).query();
            final int size = trafficList.size() - (currentDate ? 1 : 0);
            if ( size < 0 ) return 0; // Empty

            for ( int i = 0; i < size; i++ ) {
                TrafficMonitor_Mobile dt = trafficList.get(i);
                totalByteTrans += (dt.getCurrentReceivedBytes_end() - dt.getCurrentReceivedBytes_start());
                totalByteTrans += (dt.getCurrentSendedBytes_end() - dt.getCurrentSendedBytes_start());
            }

            if ( currentDate ) {
                TrafficMonitor_Mobile dt = trafficList.get(size);
                totalByteTrans += (TrafficStats.getMobileTxBytes() - dt.getCurrentSendedBytes_start());
                totalByteTrans += (TrafficStats.getMobileRxBytes() - dt.getCurrentReceivedBytes_start());
            }

            return (totalByteTrans < 0 ) ? 0 : totalByteTrans;
        } catch ( SQLException ex ) {
            Log.e(TAG, ex.getMessage());
            return -1;
        }
    }

    /**
     * Get total Mobile bytes transfered
     * @return -> Transfered Bytes
     */
    public long getTotalMobileDataTransfer() {
        return this.getTotalMobileDataTransfer(new Date());
    }


    /**
     * Get total WiFi bytes transfered between date range
     * @param start -> Start date range
     * @param end -> End date range
     * @return -> Transfered Bytes between this date range
     */
    public long getTotalWifiDataTransfer(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date periodStart, periodoEnd;
        try {
            periodStart = sdf2.parse(sdf.format(start) + " 00:00:00");
            periodoEnd = sdf2.parse(sdf.format(end) + " 23:59:59");
        } catch (ParseException e) {
            periodStart = start;
            periodoEnd = end;
        }

        LocalDate today = LocalDate.now();
        long totalByteTrans = 0;
        boolean currentDate = (!today.isBefore(LocalDate.fromDateFields(end)));
        try {
            List<TrafficMonitor_WiFi> trafficList = ormLiteHelper.getTrafficMonitorWifiDao().queryBuilder()
                    .orderBy("id_wifi_monitor", true)
                    .where().between("date_period", periodStart, periodoEnd).query();
            final int size = trafficList.size() - (currentDate ? 1 : 0);
            if ( size < 0 ) return 0; // Empty

            for ( int i = 0; i < size; i++ ) {
                TrafficMonitor_WiFi dt = trafficList.get(i);
                totalByteTrans += (dt.getCurrentReceivedBytes_end() - dt.getCurrentReceivedBytes_start());
                totalByteTrans += (dt.getCurrentSendedBytes_end() - dt.getCurrentSendedBytes_start());
            }

            Log.d(TAG, "Value0 -> " + totalByteTrans );
            if ( currentDate ) {
                TrafficMonitor_WiFi dt = trafficList.get(size);
                long tx = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                long rx = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                totalByteTrans += (tx - dt.getCurrentSendedBytes_start());
                totalByteTrans += (rx - dt.getCurrentReceivedBytes_start());
            }

            Log.d(TAG, "Value1 -> " + totalByteTrans );
            return (totalByteTrans < 0 ) ? 0 : totalByteTrans;
        } catch ( SQLException ex ) {
            Log.e(TAG, ex.getMessage());
            return -1;
        }
    }

    /**
     * Get total Mobile bytes transfered between date range
     * @param start -> Start date range
     * @param end -> End date range
     * @return -> Transfered Bytes between this date range
     */
    public long getTotalMobileDataTransfer(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date periodStart, periodoEnd;
        try {
            periodStart = sdf2.parse(sdf.format(start) + " 00:00:00");
            periodoEnd = sdf2.parse(sdf.format(end) + " 23:59:59");
        } catch (ParseException e) {
            periodStart = start;
            periodoEnd = end;
        }

        LocalDate today = LocalDate.now();
        long totalByteTrans = 0;
        boolean currentDate = (!today.isBefore(LocalDate.fromDateFields(end)));
        try {
            List<TrafficMonitor_Mobile> trafficList = ormLiteHelper.getTrafficMonitorMobileDao().queryBuilder()
                    .orderBy("id_mob_monitor", true)
                    .where().between("date_period", periodStart, periodoEnd).query();
            Log.d(TAG, "Count -> " + trafficList.size());
            final int size = trafficList.size() - (currentDate ? 1 : 0);
            if ( size < 0 ) return 0; // Empty

            for ( int i = 0; i < size; i++ ) {
                TrafficMonitor_Mobile dt = trafficList.get(i);
                totalByteTrans += (dt.getCurrentReceivedBytes_end() - dt.getCurrentReceivedBytes_start());
                totalByteTrans += (dt.getCurrentSendedBytes_end() - dt.getCurrentSendedBytes_start());
            }

            if ( currentDate ) {
                TrafficMonitor_Mobile dt = trafficList.get(size);
                totalByteTrans += (TrafficStats.getMobileTxBytes() - dt.getCurrentSendedBytes_start());
                totalByteTrans += (TrafficStats.getMobileRxBytes() - dt.getCurrentReceivedBytes_start());
            }

            Log.d(TAG, "Value -> " + totalByteTrans );
            return (totalByteTrans < 0 ) ? 0 : totalByteTrans;
        } catch ( SQLException ex ) {
            Log.e(TAG, ex.getMessage());
            return -1;
        }
    }

    /**
     * #Static -> Get bytes in a formatted String
     * @param mContext -> Context
     * @param bytes -> Bytes (long)
     * @return -> Formatted String
     */
    public static String FormatBytes(Context mContext, long bytes) {
        return Formatter.formatShortFileSize(mContext, bytes);
    }

    /**
     * Static -> get bytes in formattedString in SplittedMode (0 == Value, 1 == Unit)
     * @param mContext -> Context
     * @param bytes ->Bytes(long)
     * @return -> Formatted String Array (in 2 position, 0 == Value, 1 == Unit)
     */
    public static String[] GetSeparatedFormattedValues(Context mContext, long bytes) {
        String formatedBytesValue = TrafficMonitor.FormatBytes(mContext, bytes);
        String[] splittedValue = formatedBytesValue.split(" ");
        return new String[] { splittedValue[0].trim(), splittedValue[1].trim()};
    }
}
