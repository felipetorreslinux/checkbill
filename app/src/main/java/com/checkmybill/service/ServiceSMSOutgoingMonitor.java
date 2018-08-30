package com.checkmybill.service;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.Dao;

import java.util.Date;

/**
 * Created by espe on 28/07/2016.
 */
public class ServiceSMSOutgoingMonitor extends Service {
    private Context mContext;
    private ContentResolver contentResolver;
    private long lastMessageId = 0;

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
        this.contentResolver.registerContentObserver(Uri.parse("content://sms"), true, new SMSContentObserver(new Handler()));

        // Continuando a inicializando do servico...
        return super.onStartCommand(intent, flags, startId);
    }


    // Observer
    private class SMSContentObserver extends ContentObserver {
        public SMSContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            // Checando permissao e se os dados iniciais foram lidos...
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(ServiceSMSOutgoingMonitor.class.getName(), "No Permission to SMS Read");
                return;
            }

            // Obtendo os dados mais recente de SMS
            Uri uriSMS = Uri.parse("content://sms/sent");
            Cursor cur = contentResolver.query(uriSMS, null, null, null, null);

            // Checando o tipo e mensagem
            try {
                cur.moveToNext();
                int type = cur.getInt(cur.getColumnIndex("type"));
                if (type != 2) return; // 2 == SMS Sent

                // Checando se a mensagem ja foi processada previamente
                long messageId = cur.getLong(cur.getColumnIndex("_id"));
                if (lastMessageId == messageId) return;
                else lastMessageId = messageId;

                // Salvando os dados no banco
                final long timestamp = cur.getLong(cur.getColumnIndex("date"));
                SmsMonitor monitor = new SmsMonitor();
                monitor.setId(messageId);
                monitor.setToAddress(cur.getString(cur.getColumnIndex("address")));
                monitor.setDateCad(new Date(timestamp));
                cur.close();

                // Adicionando os dados no banco
                Dao<SmsMonitor, Integer> smsMonitor = OrmLiteHelper.getInstance(mContext).getSmsMonitorDao();
                int id = smsMonitor.create(monitor);
                Log.d("SmsMonitor", "AddID:" + id);
                Log.d("SmsMonitor", "Data Saved");
            } catch (Exception ex) {
                Log.e("SmsMonitorError", Util.getMessageErrorFromExcepetion(ex));
            }
        }
    }
}
