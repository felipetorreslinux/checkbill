package com.checkmybill.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.SignalScan;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.util.SharedPrefsUtil;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.joda.time.DateTime;

/**
 * Created by Victor Guerra on 10/12/2015.
 */
public class ServicePhoneStateListener extends Service {

    private MyPhoneStateListener myListener;
    private TelephonyManager tel;
    private Unavailability unavailability;
    private int countAverageSignal = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        unavailability = new Unavailability();
        unavailability.setCurrent(false);
        unavailability.setUsed(true);

        myListener = new MyPhoneStateListener();
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        return super.onStartCommand(intent, flags, startId);
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
                int valueRssi;
                int valueASU;
                int valueCompare;

                int networkType = telephonyManager.getNetworkType();
                if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G
                    String[] arraySignalValues = signalStrength.toString().split(" ");
                    valueRssi = Integer.valueOf(arraySignalValues[9]);
                    valueASU = valueRssi + 140;
                    valueCompare = new SharedPrefsUtil(getApplicationContext()).getConfMob4g();//ideal converter -105 dbm
                } else {
                    //2G & 3G
                    valueRssi = (signalStrength.getGsmSignalStrength() * 2) - 113;
                    valueASU = signalStrength.getGsmSignalStrength();
                    valueCompare = new SharedPrefsUtil(getApplicationContext()).getConfMob2g();//ideal -105
                }

                SignalScan signalScan = new SignalScan();
                signalScan.setType(getTypeNetString(networkType));
                signalScan.setDbm(valueRssi);
                signalScan.setAsu(valueASU);
                signalScan.setDate(new DateTime().toDate());
                salvarSignalScan(signalScan);

                //Log.i("valueRssi", "DBM: " + valueRssi + " / ASU: " + valueASU);

                if (!isAirplaneModeOn(getApplicationContext())) {
                    //Log.i("ServicePhoneState", "Airplane mode OFF");
                    //Log.i("ServicePhoneState", "Check unavailability signal");
                    if (valueRssi <= valueCompare) {
                        if (unavailability.isCurrent()) {
                            //Increment
                            countAverageSignal++;
                            unavailability.setValueSignal(valueRssi + unavailability.getValueSignal());
                        } else {
                            //New
                            unavailability.setValueSignal(valueRssi);
                            unavailability.setSaved(false);
                            unavailability.setCurrent(true);
                            //unavailability.setCellId(cellLocation.getCid() % 65536);
                            unavailability.setCellId(cellLocation.getCid() + "|" + cellLocation.getLac() + "|" + telephonyManager.getNetworkOperator().substring(3));
                            unavailability.setDateStarted(new DateTime().toDate());
                            unavailability.setUsed(true);
                        }
                    } else {
                        if (unavailability.isCurrent()) {
                            //Finalize
                            //unavailability.setLng(0);
                            // unavailability.setLat(0);
                            unavailability.setCurrent(false);
                            unavailability.setDateFinished(new DateTime().toDate());

                            int averageSignalValue = (int) (unavailability.getValueSignal() / countAverageSignal);
                            unavailability.setValueSignal(averageSignalValue);

                            //Validar se é uma medição de indisponilidade válida
                            if (unavailability.getDateStarted() != null && unavailability.getDateFinished() != null) {
                                if (!(new DateTime(unavailability.getDateStarted()).getMillis() == new DateTime(unavailability.getDateFinished()).getMillis())) {
                                    long timeUnavailability = new DateTime(unavailability.getDateFinished()).getMillis() - new DateTime(unavailability.getDateStarted()).getMillis();
                                    if (timeUnavailability >= new SharedPrefsUtil(getApplicationContext()).getConfMobTimeUnavailability()) {
                                        RuntimeExceptionDao<Unavailability, Integer> unavailabilityDao = OrmLiteHelper.getInstance(getApplicationContext()).getUnavailabilityRuntimeDao();
                                        unavailabilityDao.create(unavailability);
                                    }
                                }
                            }

                            //Create new instance
                            unavailability = new Unavailability();
                            unavailability.setCurrent(false);
                            countAverageSignal = 1;
                        }
                    }
                } else {
                    Log.i("ServicePhoneState", "Airplane mode ON");
                    if (unavailability.isCurrent()) {
                        //Finalize
                        //unavailability.setLng(0);
                        // unavailability.setLat(0);
                        unavailability.setCurrent(false);
                        unavailability.setDateFinished(new DateTime().toDate());

                        int averageSignalValue = (int) (unavailability.getValueSignal() / countAverageSignal);
                        unavailability.setValueSignal(averageSignalValue);

                        //Validar se é uma medição de indisponilidade válida
                        if (unavailability.getDateStarted() != null && unavailability.getDateFinished() != null) {
                            long timeUnavailability = new DateTime(unavailability.getDateFinished()).getMillis() - new DateTime(unavailability.getDateStarted()).getMillis();
                            if (timeUnavailability >= new SharedPrefsUtil(getApplicationContext()).getConfMobTimeUnavailability()) {
                                RuntimeExceptionDao<Unavailability, Integer> unavailabilityDao = OrmLiteHelper.getInstance(getApplicationContext()).getUnavailabilityRuntimeDao();
                                unavailabilityDao.create(unavailability);
                            }

                        }

                        //Create new instance
                        unavailability = new Unavailability();
                        unavailability.setCurrent(false);
                        countAverageSignal = 1;
                    }
                }
                /*
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        Log.i("getNetworkType", "2G / GPRS");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        Log.i("getNetworkType", "2G / EDGE");
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        Log.i("getNetworkType", "2G / CDMA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        Log.i("getNetworkType", "2G / 1xRTT");
                        break;
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        Log.i("getNetworkType", "2G / IDEN");
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        Log.i("getNetworkType", "3G / UMTS");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        Log.i("getNetworkType", "3G / EVDO_0");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        Log.i("getNetworkType", "3G / EVDO_A");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        Log.i("getNetworkType", "3G / HSDPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        Log.i("getNetworkType", "3G / HSUPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        Log.i("getNetworkType", "3G / HSPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        Log.i("getNetworkType", "3G / EVDO_B");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        Log.i("getNetworkType", "3G / EHRPD");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        Log.i("getNetworkType", "3G / HSPAP");
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        Log.i("getNetworkType", "4G / LTE");
                        break;
                    default:
                        Log.i("getNetworkType", "unknow");
                        break;
                }*/

            } catch (RuntimeException e) {
                Log.i("ServicePhoneState", "onSignalStrengthsChanged exception");
            }


        }

    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    private void salvarSignalScan(SignalScan signalScan) {
        RuntimeExceptionDao<SignalScan, Integer> signalScanRuntimeExceptionDao = OrmLiteHelper.getInstance(getApplicationContext()).getSignalScanRuntimeExceptionDao();
        signalScanRuntimeExceptionDao.create(signalScan);
    }

    private String getTypeNetString(int networkType){
        String sType = "";
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                sType = "2G / GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                sType = "2G / EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                sType = "2G / CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                sType = "2G / 1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                sType = "2G / IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                sType = "3G / UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                sType = "3G / EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                sType = "3G / EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                sType = "3G / HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                sType = "3G / HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                sType = "3G / HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                sType = "3G / EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                sType = "3G / EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                sType = "3G / HSPAP";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                sType = "4G / LTE";
                break;
            default:
                sType = "unknow";
                break;
        }

        return sType;
    }
}
