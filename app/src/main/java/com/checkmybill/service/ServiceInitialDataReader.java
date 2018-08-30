package com.checkmybill.service;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.Dao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ESPENOTE-06 on 17/11/2016.
 */

public class ServiceInitialDataReader extends Service {
    private Context mContext;
    private String LOG_TAG;
    private ContentResolver contentResolver;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG_TAG = "ServiceInitialDataReader";
        mContext = getApplicationContext();
        contentResolver = getContentResolver();

        final SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(mContext);
        final CallReader callReader = new CallReader();
        final SMSReader smsReader = new SMSReader();

        // Argumento de inicialização
        boolean loadSMS = intent.getBooleanExtra("SMS", false);
        boolean loadCALL = intent.getBooleanExtra("CALL", false);

        // Obtendo apenas os dados locais do aparelho...
        // Checando se os dados iniciais de ligacao ja foram obtidos
        if (loadCALL && sharedPrefsUtil.getInitialUserCallData())
            new Thread(callReader).start();
        if (loadSMS && sharedPrefsUtil.getInitialUserSMSData())
            new Thread(smsReader).start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /* ------------------------------------------------------------------------------------------ */
    // -> Metodo para obter os dados de sincronização do servidor...
    public static void ObterSincronizarDadosUsuarioServidor(final Context mContext) {
        final String accessKey = new SharedPrefsUtil(mContext).getAccessKey();
        if ( new SharedPrefsUtil(mContext).getInitialServerUserData() == false )
            return;
        else if ( accessKey == null || accessKey.length() <= 0 )
            return;

        final RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        final String url = Util.getSuperUrlServiceObterDadosSincronizacao(mContext);
        final String LOG_TAG = "DataSyncFromServer";

        Log.d(LOG_TAG, "Initializind Service...");

        // Montando JSON da requisicao
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("access_key", accessKey);
        } catch ( Exception ex ) {
            Log.e(LOG_TAG, "Fatal error, no request");
            return;
        }


        // Montando request do Volley
        final JsonObjectRequest request = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(),
                new Response.Listener<JSONObject>() { // Success Response Listener
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "JSON-RCV > " + response.toString());
                        try {
                            final OrmLiteHelper ormLiteHelper = OrmLiteHelper.getInstance(mContext);
                            final Dao<CallMonitor, Integer> callMonitorDao = ormLiteHelper.getCallMonitorDao();
                            final Dao<SmsMonitor, Integer> smsMonitorDao = ormLiteHelper.getSmsMonitorDao();
                            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                                Log.e(LOG_TAG, response.getString("message"));
                                return;
                            }

                            // Salvando os dados e obtendo os dados locais...
                            // -> SMS
                            JSONArray sms = response.getJSONArray("sms_data");
                            for ( int i = 0; i < sms.length(); i++ ) {
                                JSONObject smsInfo = sms.getJSONObject(i);
                                SmsMonitor smsMonitor = new SmsMonitor();
                                //smsMonitor.setId(smsInfo.getLong("id_mon_sms"));
                                smsMonitor.setSmsSended(true);
                                smsMonitor.setDateCad( sdf.parse(smsInfo.getString("sms_date")) );
                                smsMonitor.setToAddress("00000000");

                                List<SmsMonitor> list = smsMonitorDao.queryBuilder().where().eq("SMS_DAT_CAD", smsMonitor.getDateCad()).query();
                                if ( list.size() > 0 ) {
                                    Log.d(LOG_TAG, "A SMS Entry was kicked xD, -> " + smsMonitor.getDateCad().toString());
                                    continue;
                                }

                                final int id = smsMonitorDao.create(smsMonitor);
                                Log.d(LOG_TAG, "SMS Entry Created ID");
                            }

                            // -> CALL
                            JSONArray call = response.getJSONArray("call_data");
                            for ( int i = 0; i < call.length(); i++ ) {
                                JSONObject callInfo = call.getJSONObject(i);
                                CallMonitor callMonitor = new CallMonitor();
                                //callMonitor.setId(callInfo.getLong("id_mon_call"));
                                callMonitor.setDateCad(sdf.parse(callInfo.getString("call_date_time")));
                                callMonitor.setElapsedTime(callInfo.getLong("elapsed_call_time"));
                                callMonitor.setCallType("OUTGOING");
                                callMonitor.setTelNumber("00000000");
                                callMonitor.setCallSended(true);

                                // Checando se os dados existem
                                List<CallMonitor> list = callMonitorDao.queryBuilder().where().eq("CALL_DAT_CAD", callMonitor.getDateCad()).query();
                                if ( list.size() > 0 ) {
                                    Log.d(LOG_TAG, "A Call Entry was kicked xD -> " + callMonitor.getDateCad().toString());
                                    continue;
                                }

                                final int id = callMonitorDao.create(callMonitor);
                                Log.d(LOG_TAG, "Call Entry Created");
                            }

                            // -> NET
                            JSONArray net = response.getJSONArray("net_data");
                            for ( int i = 0; i < net.length(); i++ ) {
                                JSONObject netInfo = net.getJSONObject(i);
                                boolean isWifi = (netInfo.getInt("network_type") == 0 ? false : true);
                                if ( isWifi ) {
                                    TrafficMonitor_WiFi monData = new TrafficMonitor_WiFi();
                                    monData.setDatePeriodo(sdf.parse(netInfo.getString("net_date_time")));
                                    monData.setCurrentSendedBytes_start(0);
                                    monData.setCurrentSendedBytes_end(netInfo.getLong("bytes_transferidos"));
                                    monData.setCurrentReceivedBytes_start(0);
                                    monData.setCurrentReceivedBytes_end(0);
                                    monData.setDataSyncronized(true);
                                    ormLiteHelper.getTrafficMonitorWifiDao().create(monData);
                                } else {
                                    TrafficMonitor_Mobile monData = new TrafficMonitor_Mobile();
                                    monData.setDatePeriodo(sdf.parse(netInfo.getString("net_date_time")));
                                    monData.setCurrentSendedBytes_start(0);
                                    monData.setCurrentSendedBytes_end(netInfo.getLong("bytes_transferidos"));
                                    monData.setCurrentReceivedBytes_start(0);
                                    monData.setCurrentReceivedBytes_end(0);
                                    monData.setDataSyncronized(true);
                                    ormLiteHelper.getTrafficMonitorMobileDao().create(monData);
                                }

                                Log.d(LOG_TAG, "Net Entry Created");
                            }

                            // Modificando o status para 'lido'
                            new SharedPrefsUtil(mContext).setInitialServerUserData(false);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                            return;
                        }
                    }
                },
                new Response.ErrorListener() { // Error Response Listener
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, "Fatal Error:" + error.getMessage());
                    }
                });

        // Enviando a requisicao...
        Log.i(LOG_TAG, "requesting to url:" + url);
        Log.i(LOG_TAG, "requesting with jsin data:" + jsonObject.toString());
        request.setTag(ServiceInitialDataReader.class.getName());
        requestQueue.add(request);
    }
    /* ------------------------------------------------------------------------------------------ */
    // -> Metodos de obtenção dos dados locais do aparelho
    private class CallReader implements Runnable {
        @Override
        public void run() {
            // Checandfo permissao
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
                return;

            // Checando a permissao
            try {
                Dao<CallMonitor, Integer> callMonitor = OrmLiteHelper.getInstance(mContext).getCallMonitorDao();
                Log.d(LOG_TAG, "Starting Service -> CallReader");
                Cursor cur = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
                while ( cur.moveToNext() ) {
                    final int type = cur.getColumnIndex(CallLog.Calls.TYPE);
                    String callType = cur.getString(type);
                    final int dirCode = Integer.parseInt(callType);

                    if (dirCode == CallLog.Calls.OUTGOING_TYPE) callType = "OUTGOING";
                    else if (dirCode == CallLog.Calls.INCOMING_TYPE) callType = "INCOMING";
                    else continue;

                    // Salvando os dados dentro do SGDB
                    String calldate = cur.getString(cur.getColumnIndex(CallLog.Calls.DATE));
                    long seconds = Long.parseLong(calldate);
                    Date cdate = new Date(seconds);
                    final long timestamp = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE));
                    CallMonitor monitor = new CallMonitor();
                    monitor.setCallType(callType);
                    monitor.setTelNumber(cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)));
                    monitor.setElapsedTime(cur.getLong(cur.getColumnIndex(CallLog.Calls.DURATION)));
                    monitor.setDateCad(new Date(timestamp));

                    // Adicionando os dados no banco
                    int id = callMonitor.create(monitor);
                    Log.d(LOG_TAG, "CALL Reader -> Created");
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }
            // Definindo o status como Lido
            new SharedPrefsUtil(mContext).setInitialUserCallData(false);
        }
    }
    private class SMSReader implements Runnable {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
                return;

            Log.d(LOG_TAG, "Starting Service -> SmsReader");
            Uri uriSMS = Uri.parse("content://sms/sent");
            try {
                Dao<SmsMonitor, Integer> smsMonitor = OrmLiteHelper.getInstance(mContext).getSmsMonitorDao();
                Cursor cur = contentResolver.query(uriSMS, null, null, null, null);
                while ( cur.moveToNext() ) {
                    int type = cur.getInt(cur.getColumnIndex("type"));
                    if (type != 2) continue;

                    // Checando se a mensagem ja foi processada previamente
                    long messageId = cur.getLong(cur.getColumnIndex("_id"));

                    // Salvando os dados no banco
                    final long timestamp = cur.getLong(cur.getColumnIndex("date"));
                    SmsMonitor monitor = new SmsMonitor();
                    monitor.setId(messageId);
                    monitor.setToAddress(cur.getString(cur.getColumnIndex("address")));
                    monitor.setDateCad(new Date(timestamp));

                    // Adicionando os dados no banco
                    int id = smsMonitor.create(monitor);
                    Log.d(LOG_TAG, "SMS Reader -> Created");
                }
            } catch (Exception ex) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }

            // Definindo o status como Lido
            new SharedPrefsUtil(mContext).setInitialUserSMSData(false);
        }
    }
}
