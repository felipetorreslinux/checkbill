package com.checkmybill.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.MyLocationMonitor;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.NetworkWifi;
import com.checkmybill.entity.PlanoFiltroOpts;
import com.checkmybill.entity.SignalScan;
import com.checkmybill.entity.SignalStrengthAverage;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;
import com.checkmybill.entity.Unavailability;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Victor Guerra on 03/12/2015.
 */
public class OrmLiteHelper extends OrmLiteSqliteOpenHelper {

    // Nome da base de dados.
    private static final String DATABASE_NAME = "check-my-bill.db";

    // Versão da base de dados.
    private static final int DATABASE_VERSION = 21;

    // Caso você queria ter apenas uma instancia da base de dados.
    private static OrmLiteHelper mInstance;

    // DataUse
    //private Dao<DataUse, Integer> dataUseDao = null;
    //private RuntimeExceptionDao<DataUse, Integer> dataUseRuntimeDao = null;

    // SIGNAL STRENGTH AVERAGE
    private Dao<SignalStrengthAverage, Integer> signalStrengthAverageDao = null;
    private RuntimeExceptionDao<SignalStrengthAverage, Integer> signalStrengthAverageRuntimeDao = null;

    // Unavailability
    private Dao<Unavailability, Integer> unavailabilityDao = null;
    private RuntimeExceptionDao<Unavailability, Integer> unavailabilityRuntimeDao = null;

    // Filtros para planos
    private Dao<PlanoFiltroOpts, Integer> planoFiltroOptsDao = null;
    private RuntimeExceptionDao<PlanoFiltroOpts, Integer> planoFiltroOptsRuntimeDao = null;

    // NetworkQuality
    private Dao<NetworkQuality, Integer> networkQualityDao = null;
    private RuntimeExceptionDao<NetworkQuality, Integer> networkQualityRuntimeDao = null;

    // NetworkWifi
    private Dao<NetworkWifi, Integer> networkWifiDao = null;
    private RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao = null;

    // SMSMonitor
    private Dao<SmsMonitor, Integer> smsMonitorDao = null;
    private RuntimeExceptionDao<SmsMonitor, Integer> smsMonitorRuntimeExceptionDao = null;

    // CallMonitor
    private Dao<CallMonitor, Integer> callMonitorDao = null;
    private RuntimeExceptionDao<CallMonitor, Integer> callMonitorRuntimeExceptionDao = null;

    // ScanSignal
    private Dao<SignalScan, Integer> signalScanDao = null;
    private RuntimeExceptionDao<SignalScan, Integer> signalScanRuntimeExceptionDao = null;

    // MyLocationMonitor
    private Dao<MyLocationMonitor, Integer> myLocationMonitorDao = null;
    private RuntimeExceptionDao<MyLocationMonitor, Integer> myLocationMonitorRuntimeExceptionDao = null;

    public OrmLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            //TableUtils.createTableIfNotExists(connectionSource, DataUse.class);
            TableUtils.createTableIfNotExists(connectionSource, SignalStrengthAverage.class);
            TableUtils.createTableIfNotExists(connectionSource, Unavailability.class);
            TableUtils.createTableIfNotExists(connectionSource, PlanoFiltroOpts.class);
            TableUtils.createTableIfNotExists(connectionSource, NetworkQuality.class);
            TableUtils.createTableIfNotExists(connectionSource, NetworkWifi.class);
            TableUtils.createTableIfNotExists(connectionSource, SmsMonitor.class);
            TableUtils.createTableIfNotExists(connectionSource, CallMonitor.class);
            TableUtils.createTableIfNotExists(connectionSource, SignalScan.class);
            TableUtils.createTableIfNotExists(connectionSource, MyLocationMonitor.class);
            TableUtils.createTableIfNotExists(connectionSource, TrafficMonitor_Mobile.class);
            TableUtils.createTableIfNotExists(connectionSource, TrafficMonitor_WiFi.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            //TableUtils.dropTable(connectionSource, DataUse.class, true);
            TableUtils.dropTable(connectionSource, SignalStrengthAverage.class, true);
            TableUtils.dropTable(connectionSource, Unavailability.class, true);
            TableUtils.dropTable(connectionSource, PlanoFiltroOpts.class, true);
            TableUtils.dropTable(connectionSource, NetworkQuality.class, true);
            TableUtils.dropTable(connectionSource, NetworkWifi.class, true);
            TableUtils.dropTable(connectionSource, SmsMonitor.class, true);
            TableUtils.dropTable(connectionSource, CallMonitor.class, true);
            TableUtils.dropTable(connectionSource, SignalScan.class, true);
            TableUtils.dropTable(connectionSource, MyLocationMonitor.class, true);
            TableUtils.dropTable(connectionSource, TrafficMonitor_Mobile.class, true);
            TableUtils.dropTable(connectionSource, TrafficMonitor_WiFi.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static OrmLiteHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OrmLiteHelper(context);
        }
        return mInstance;
    }

    public void clearAllTables() {
        // Limpa os dados de toda a tabela...
        try {
            //TableUtils.clearTable(connectionSource, DataUse.class);
            TableUtils.clearTable(connectionSource, SignalStrengthAverage.class);
            TableUtils.clearTable(connectionSource, Unavailability.class);
            TableUtils.clearTable(connectionSource, PlanoFiltroOpts.class);
            TableUtils.clearTable(connectionSource, NetworkQuality.class);
            TableUtils.clearTable(connectionSource, NetworkWifi.class);
            TableUtils.clearTable(connectionSource, SmsMonitor.class);
            TableUtils.clearTable(connectionSource, CallMonitor.class);
            TableUtils.clearTable(connectionSource, SignalScan.class);
            TableUtils.clearTable(connectionSource, MyLocationMonitor.class);
            TableUtils.clearTable(connectionSource, TrafficMonitor_Mobile.class);
            TableUtils.clearTable(connectionSource, TrafficMonitor_WiFi.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //DATA USE ******************************
    /*public Dao<DataUse, Integer> getDataUseDao() throws SQLException {
        if (dataUseDao == null) {
            dataUseDao = getDao(DataUse.class);
        }
        return dataUseDao;
    }

    public RuntimeExceptionDao<DataUse, Integer> getDataUseRuntimeDao() {
        if (dataUseRuntimeDao == null) {
            dataUseRuntimeDao = getRuntimeExceptionDao(DataUse.class);
        }
        return dataUseRuntimeDao;
    }*/

    // My Location Monitor **********************************
    public Dao<MyLocationMonitor, Integer> getMyLocationMonitorDao() throws SQLException {
        if (myLocationMonitorDao == null) {
            myLocationMonitorDao = getDao(MyLocationMonitor.class);
        }
        return myLocationMonitorDao;
    }

    public RuntimeExceptionDao<MyLocationMonitor, Integer> getMyLocationMonitorRuntimeExceptionDao() {
        if (myLocationMonitorRuntimeExceptionDao == null) {
            myLocationMonitorRuntimeExceptionDao = getRuntimeExceptionDao(MyLocationMonitor.class);
        }
        return myLocationMonitorRuntimeExceptionDao;
    }

    // SIGNAL STRENGTH AVERAGE ******************************
    public Dao<SignalStrengthAverage, Integer> getSignalStrengthAverageDao() throws SQLException {
        if (signalStrengthAverageDao == null) {
            signalStrengthAverageDao = getDao(SignalStrengthAverage.class);
        }
        return signalStrengthAverageDao;
    }

    public RuntimeExceptionDao<SignalStrengthAverage, Integer> getSignalStrengthAverageRuntimeDao() {
        if (signalStrengthAverageRuntimeDao == null) {
            signalStrengthAverageRuntimeDao = getRuntimeExceptionDao(SignalStrengthAverage.class);
        }
        return signalStrengthAverageRuntimeDao;
    }

    // Unavailability ******************************
    public Dao<Unavailability, Integer> getUnavailabilityDao() throws SQLException {
        if (unavailabilityDao == null) {
            unavailabilityDao = getDao(Unavailability.class);
        }
        return unavailabilityDao;
    }

    public RuntimeExceptionDao<Unavailability, Integer> getUnavailabilityRuntimeDao() {
        if (unavailabilityRuntimeDao == null) {
            unavailabilityRuntimeDao = getRuntimeExceptionDao(Unavailability.class);
        }
        return unavailabilityRuntimeDao;
    }

    // Filtro de Planos **************************
    public Dao<PlanoFiltroOpts, Integer> getPlanoFiltroOptsDao() throws SQLException {
        if (unavailabilityDao == null) {
            planoFiltroOptsDao = getDao(PlanoFiltroOpts.class);
        }
        return planoFiltroOptsDao;
    }

    public RuntimeExceptionDao<PlanoFiltroOpts, Integer> getPlanoFiltroOptsRuntimeDao() {
        if (unavailabilityRuntimeDao == null) {
            planoFiltroOptsRuntimeDao = getRuntimeExceptionDao(PlanoFiltroOpts.class);
        }
        return planoFiltroOptsRuntimeDao;
    }

    // NetworkQuality ******************************
    public Dao<NetworkQuality, Integer> getNetworkQualityDao() throws SQLException {
        if (networkQualityDao == null) {
            networkQualityDao = getDao(NetworkQuality.class);
        }
        return networkQualityDao;
    }

    public RuntimeExceptionDao<NetworkQuality, Integer> getNetworkQualityRuntimeDao() {
        if (networkQualityRuntimeDao == null) {
            networkQualityRuntimeDao = getRuntimeExceptionDao(NetworkQuality.class);
        }
        return networkQualityRuntimeDao;
    }

    // NetworkWifi ******************************
    public Dao<NetworkWifi, Integer> getNetworkWifiDao() throws SQLException {
        if (networkWifiDao == null) {
            networkWifiDao = getDao(NetworkWifi.class);
        }
        return networkWifiDao;
    }

    public RuntimeExceptionDao<NetworkWifi, Integer> getNetworkWifiRuntimeExceptionDao() {
        if (networkWifiRuntimeExceptionDao == null) {
            networkWifiRuntimeExceptionDao = getRuntimeExceptionDao(NetworkWifi.class);
        }
        return networkWifiRuntimeExceptionDao;
    }

    // SmsMonitor ******************************
    public Dao<SmsMonitor, Integer> getSmsMonitorDao() throws SQLException {
        if (smsMonitorDao == null) {
            smsMonitorDao = getDao(SmsMonitor.class);
        }
        return smsMonitorDao;
    }

    public RuntimeExceptionDao<SmsMonitor, Integer> getSmsMonitorRuntimeExceptionDao() {
        if (smsMonitorRuntimeExceptionDao == null) {
            smsMonitorRuntimeExceptionDao = getRuntimeExceptionDao(SmsMonitor.class);
        }
        return smsMonitorRuntimeExceptionDao;
    }

    // CallMonitor ******************************
    public Dao<CallMonitor, Integer> getCallMonitorDao() throws SQLException {
        if (callMonitorDao == null) {
            callMonitorDao = getDao(CallMonitor.class);
        }
        return callMonitorDao;
    }

    public RuntimeExceptionDao<CallMonitor, Integer> getCallMonitorRuntimeExceptionDao() {
        if (callMonitorRuntimeExceptionDao == null) {
            callMonitorRuntimeExceptionDao = getRuntimeExceptionDao(CallMonitor.class);
        }
        return callMonitorRuntimeExceptionDao;
    }

    // SignalScan ******************************
    public Dao<SignalScan, Integer> getSignalScanDao() throws SQLException {
        if (signalScanDao == null) {
            signalScanDao = getDao(SignalScan.class);
        }
        return signalScanDao;
    }

    public RuntimeExceptionDao<SignalScan, Integer> getSignalScanRuntimeExceptionDao() {
        if (signalScanRuntimeExceptionDao == null) {
            signalScanRuntimeExceptionDao = getRuntimeExceptionDao(SignalScan.class);
        }
        return signalScanRuntimeExceptionDao;
    }

    // ---------------------------------------------------------------------------------------------
    // TrafficMonitor_Mobile
    // ---------------------------------------------------------------------------------------------
    private Dao<TrafficMonitor_Mobile, Integer> trafficMonitorMobileDao = null;
    public Dao<TrafficMonitor_Mobile, Integer> getTrafficMonitorMobileDao() throws SQLException {
        if ( trafficMonitorMobileDao == null ) trafficMonitorMobileDao = getDao(TrafficMonitor_Mobile.class);
        return trafficMonitorMobileDao;
    }

    private RuntimeExceptionDao<TrafficMonitor_Mobile, Integer> trafficMonitorMobilesRuntimeDao = null;
    public RuntimeExceptionDao<TrafficMonitor_Mobile, Integer> getTrafficMonitorMobileRuntimeDao() {
        if ( trafficMonitorMobilesRuntimeDao == null ) trafficMonitorMobilesRuntimeDao = getRuntimeExceptionDao(TrafficMonitor_Mobile.class);
        return trafficMonitorMobilesRuntimeDao;
    }

    // ---------------------------------------------------------------------------------------------
    // TrafficMonitor_Wifi
    // ---------------------------------------------------------------------------------------------
    private Dao<TrafficMonitor_WiFi, Integer> trafficMonitorWifiDao = null;
    public Dao<TrafficMonitor_WiFi, Integer> getTrafficMonitorWifiDao() throws SQLException {
        if ( trafficMonitorWifiDao == null ) trafficMonitorWifiDao = getDao(TrafficMonitor_WiFi.class);
        return trafficMonitorWifiDao;
    }

    private RuntimeExceptionDao<TrafficMonitor_WiFi, Integer> trafficMonitorWifiRuntimeDao = null;
    public RuntimeExceptionDao<TrafficMonitor_WiFi, Integer> getTrafficMonitorWifiRuntimeDao() {
        if ( trafficMonitorWifiRuntimeDao == null ) trafficMonitorWifiRuntimeDao = getRuntimeExceptionDao(TrafficMonitor_WiFi.class);
        return trafficMonitorWifiRuntimeDao;
    }
}
