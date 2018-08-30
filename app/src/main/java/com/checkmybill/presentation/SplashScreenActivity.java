package com.checkmybill.presentation;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.CheckBillApplication;
import com.checkmybill.R;
import com.checkmybill.request.AccountRequester;
import com.checkmybill.service.ServiceAutoStarter;
import com.checkmybill.service.ServiceDataUploader;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

import org.androidannotations.annotations.EActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Victor Guerra on 09/03/2016.
 */
@EActivity(R.layout.activity_splash_screen)
public class SplashScreenActivity extends AppCompatActivity {
    private Context mContext;
    private String LOG_TAG;
    private static int TIME_SPLASH = 2000;
    private RequestQueue requestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_TAG = getClass().getName();
        mContext = this;
        requestQueue = Volley.newRequestQueue(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        final SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(this);

        // Checando se o aplicativo ja foi inicializado/logado...
        if ( sharedPrefsUtil.getSignFinished() ) {
            Intent it = new Intent(IntentMap.HOME);
            startActivity(it);
            finish();
        } else if ( sharedPrefsUtil.getJumpLogin() ) {
            LoginManager.getInstance().logOut(); // Garantindo o logout do facebook
            Intent it = new Intent(IntentMap.HOME);
            startActivity(it);
            finish();
        } else {
            // Requisitando os dados do usuario anonimo (obtem um ID de usuario que sera usada)
            // ao longo do uso, e, ao ser feito o cadastro, o usuario obtera esta ID...
            LoginManager.getInstance().logOut(); // Garantindo o logout do facebook

            // Requisitando as permissões necessárias
            if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
                String[] permissionList = ((CheckBillApplication) getApplication()).GetUnGrantedNecessaryPermissions();
                if ( permissionList.length > 0 ) ActivityCompat.requestPermissions(this, permissionList, 101);
            } else {
                // Requisitando os dados atraves do Volley...
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                final String imei = telephonyManager.getDeviceId();
                JsonObjectRequest request = AccountRequester.prepareAnonymousAccountRequest(this.successListener, this.errorListener, imei, this);
                request.setTag(getClass().getName());
                requestQueue.add(request);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissionList[], @NonNull int[] grantResults) {
        // Verificando a lista de permission (nesse ponto, aom enos a permissao para 'READ_PHONE_STATE' é obrigatoria
        for ( int i = 0; i < permissionList.length; i++ ) {
            final String permName = permissionList[i];
            boolean isGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
            if ( permName.equals(Manifest.permission.READ_PHONE_STATE) && !isGranted ) {
                // Permissao principal nesse momento foi recusada, finalizando o aplicativo...
                new NotifyWindow(mContext).showErrorMessage(getString(R.string.app_name), "Erro[112] inicializando aplicativo... Você rejeitou a permissão requisitada!", false, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                return;
            }
        }

        // ... Permissao inicial (e possivelmente, as outras) esta OK, onbtendo o IMEI e
        // preparando o uso para o modo 'Anonymous'.
        // - Requisitando os dados atraves do Volley...
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        final String imei = telephonyManager.getDeviceId();
        JsonObjectRequest request = AccountRequester.prepareAnonymousAccountRequest(this.successListener, this.errorListener, imei, this);
        request.setTag(getClass().getName());
        requestQueue.add(request);
    }

    private void initializeIntroActivityScreen() {
        Intent intent = new Intent(IntentMap.INTRO);
        startActivity(intent);
        finish();
    }

    // ---------------------------------------------------------------------------------------------
    // Listener relacionados ao volley/requests...
    private Response.Listener<JSONObject> successListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if ( response == null ) return;
            else Log.d(LOG_TAG, "JSON-RCV:" + response.toString());

            // Checando a resposta do servidor...
            try {
                // Checando o status
                if (response.getString("status").equalsIgnoreCase("success") == false) {
                    new NotifyWindow(mContext).showErrorMessage(getString(R.string.app_name), "Erro[111] inicializando aplicativo... Se o problema persistir, contate o desenvolvedor!", false);
                    finish();
                }

                // Salvando os dados e enviando para a interface inicial
                final int idIMEI = response.getInt("id_imei");
                new SharedPrefsUtil(mContext).setIDImei(idIMEI);
                initializeIntroActivityScreen();
            } catch ( JSONException ex ) {
                new NotifyWindow(mContext).showErrorMessage("FALTAL ERROR", Util.getMessageErrorFromExcepetion(ex), false);
            }
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            new NotifyWindow(mContext).showErrorMessage(getString(R.string.app_name), "Erro[112] inicializando aplicativo... Se o problema persistir, contate o desenvolvedor!", false);
        }
    };
}