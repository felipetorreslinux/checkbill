package com.checkmybill.service;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.Dao;

import java.util.Date;

/**
 * Created by espe on 28/07/2016.
 */
public class ServiceCallMonitor extends Service {
    private Context mContext;
    private ContentResolver contentResolver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        this.contentResolver = this.getContentResolver();

        // Registrando o servico e observação
        this.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, new CallContentObserver(new Handler()));
        return super.onStartCommand(intent, flags, startId);
    }

    private class CallContentObserver extends ContentObserver {
        private long lastCallId = 0;

        public CallContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // tem que ter a permissao 'READ_CALL_LOG'
            if (ActivityCompat.checkSelfPermission(ServiceCallMonitor.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                // Não ha permissão
                Log.e(ServiceCallMonitor.class.getName(), "No Permission to CALL Read");
                return;
            }

            Cursor cur = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            if ( cur.moveToFirst() == false ) return; // Movendo para a primeira entrada...
            long idCall = cur.getLong(cur.getColumnIndex("_id"));

            // Vericacando se ja foi tratado este ID
            if (lastCallId == idCall) return;
            else lastCallId = idCall;

            //Log.d("CallMonitor DATE", cur.getString(cur.getColumnIndex("date")));
            final int type = cur.getColumnIndex(CallLog.Calls.TYPE);
            String callType = cur.getString(type);
            final int dirCode = Integer.parseInt(callType);

            if (dirCode == CallLog.Calls.OUTGOING_TYPE)
                callType = "OUTGOING";
            else if (dirCode == CallLog.Calls.INCOMING_TYPE)
                callType = "INCOMING";
            else
                return; // Unknown

            // Salvando os dados dentro do SGDB
            String calldate = cur.getString(cur.getColumnIndex(CallLog.Calls.DATE));
            long seconds = Long.parseLong(calldate);
            Date cdate = new Date(seconds);
            final long timestamp = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE));
            CallMonitor monitor = new CallMonitor();
            //monitor.setId(cur.getLong(cur.getColumnIndex("_id")));
            monitor.setCallType(callType);
            monitor.setTelNumber(cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)));
            monitor.setElapsedTime(cur.getLong(cur.getColumnIndex(CallLog.Calls.DURATION)));
            monitor.setDateCad(new Date(timestamp));
            cur.close();

            // Salvando os dados
            try {
                // Adicionando os dados no banco
                Dao<CallMonitor, Integer> callMonitor = OrmLiteHelper.getInstance(mContext).getCallMonitorDao();
                int id = callMonitor.create(monitor);
                Log.d("CallMonitor", "Data Saved -> " + monitor.getDateCad().toString() + " | " + cdate.toString());
            } catch (Exception ex) {
                Log.e("CallMonitorError", Util.getMessageErrorFromExcepetion(ex));
            }
        }
    }
}

