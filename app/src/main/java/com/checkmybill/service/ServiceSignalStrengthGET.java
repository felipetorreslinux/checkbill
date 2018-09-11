package com.checkmybill.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.SignalStrengthAverage;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.Dao;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by Victor Guerra on 10/12/2015.
 */
public class ServiceSignalStrengthGET extends Service {
    private Context mContext;
    private MyPhoneStateListener myListener;
    private TelephonyManager tel;
    private SignalStrengthAverage signalStrengthAverage;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        signalStrengthAverage = new SignalStrengthAverage();
        mContext = getApplicationContext();

        if ( ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
             ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.i("Start Service", "GPS OFF");
            stopSelf();
        }

        // Requisitando posicao atual
        SmartLocation.with(mContext)
                .location()
                .continuous()
                .config(new LocationParams.Builder().setInterval(0).setDistance(0f).setAccuracy(LocationAccuracy.HIGH).build())
                .oneFix()
                .start(smartLocationUpdateListener);

        return super.onStartCommand(intent, flags, startId);
    }

    private OnLocationUpdatedListener smartLocationUpdateListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            if ( location != null ) {
                signalStrengthAverage.setLat(location.getLatitude());
                signalStrengthAverage.setLng(location.getLongitude());
            }

            // Obtendo o status da rede telefonica
            myListener = new MyPhoneStateListener();
            tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

            // Finalizando servico
            SmartLocation.with(mContext).location().stop();
            //stopSelf();
        }
    };

    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                int networkType = telephonyManager.getNetworkType();
                int valueRssi;
                if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G
                    String[] arraySignalValues = signalStrength.toString().split(" ");
                    valueRssi = Integer.valueOf(arraySignalValues[9]);
                    Log.i("GET SIGNAL", "DBM VALUE: 4G / " + valueRssi);
                } else {
                    //2G & 3G
                    valueRssi = (signalStrength.getGsmSignalStrength() * 2) - 113;
                    Log.i("GET SIGNAL", "DBM VALUE: 2G or 3G / " + valueRssi);
                }

                if (valueRssi != 0) {
                    signalStrengthAverage.setValue(valueRssi);
                    tel.listen(myListener, PhoneStateListener.LISTEN_NONE);
                    insert();
                }

            } catch (RuntimeException e) {
                Log.i("log", "Pau");
            }
        }
    }

    public void insert() {
        Log.i("GPS_PROVIDER Listener", "insert");
        Context context = getApplicationContext();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

        signalStrengthAverage.setDate(new DateTime().toDate());
        signalStrengthAverage.setSaved(false);
        signalStrengthAverage.setFinished(false);
        signalStrengthAverage.setCurrent(true);
        signalStrengthAverage.setCellId(cellLocation.getCid() % 65536 + "|" + cellLocation.getLac() + "|" + telephonyManager.getNetworkOperator().substring(3));
        //signalStrengthAverage.setLat(getLatLng().latitude);
        //signalStrengthAverage.setLng(getLatLng().longitude);
        //sendMedicao("http://54.207.195.4:8282/services/adicionar-dados", signalStrengthAverageNew);

        Log.i("LOG SERVICE NEW", "Create new signal average with value " + signalStrengthAverage.getValue());

        Log.i("LOG ServiceStrengthGET", "Verify network");
        if (Util.isConnectionTrue(getApplicationContext())) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifiConnected = activeWifiInfo != null && activeWifiInfo.isConnectedOrConnecting();
            if (isWifiConnected) {
                Log.i("LOG ServiceStrengthGET", "isWifiConnected TRUE");
                new RetrieveFeedTask().execute();
            } else {
                Log.i("LOG ServiceStrengthGET", "isWifiConnected FALSE");
                Dao<SignalStrengthAverage, Integer> signalStrengthAverageDao;
                try {
                    signalStrengthAverage.setSaved(false);
                    signalStrengthAverageDao = OrmLiteHelper.getInstance(getApplicationContext()).getSignalStrengthAverageDao();
                    signalStrengthAverageDao.create(signalStrengthAverage);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                ServiceSignalStrengthGET.this.stopSelf();
                Log.i("LOG ServiceStrengthGET", "stopSelf");
            }

        }

    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        boolean result = false;

        protected String doInBackground(String... urls) {
            Log.i("LOG ServiceStrengthGET", "executing doInBackground by RetrieveFeedTask");
            result = sendMedicao(Util.getSuperUrlServiceAddData(getApplicationContext()), signalStrengthAverage);
            signalStrengthAverage.setSaved(result);
            Log.i("STATUS SEND MEDICAO", "Status: " + result);

            Dao<SignalStrengthAverage, Integer> signalStrengthAverageDao;
            try {
                signalStrengthAverageDao = OrmLiteHelper.getInstance(getApplicationContext()).getSignalStrengthAverageDao();
                signalStrengthAverageDao.create(signalStrengthAverage);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String feed) {
            Log.i("LOGGGGG", "stopppp");
            if (result) {
                new UploadFalseRegisters().execute("teste");
            } else {
                stopSelf();
            }

        }
    }

    private boolean sendMedicao(String url, SignalStrengthAverage signalStrengthAverage) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

        InputStream inputStream;
        String result;
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json;

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("tipo_pacote", "MA");
            jsonObject.accumulate("datahora_inicio_medicao", format.format(signalStrengthAverage.getDate()));
            jsonObject.accumulate("datahora_fim_medicao", format.format(signalStrengthAverage.getDate()));
            //jsonObject.accumulate("datahora_inicio_medicao", "01-01-2016 13:12:11");
            //jsonObject.accumulate("datahora_fim_medicao", "01-01-2016 13:12:11");
            jsonObject.accumulate("nivel_sinal", signalStrengthAverage.getValue());
            jsonObject.accumulate("celula_gsm", signalStrengthAverage.getCellId());
            jsonObject.accumulate("operadora", "TIM");
            jsonObject.accumulate("latitude", signalStrengthAverage.getLat());
            jsonObject.accumulate("longitude", signalStrengthAverage.getLng());

            jsonObject.accumulate("velocidade_download", 0);
            jsonObject.accumulate("velocidade_upload", 0);
            jsonObject.accumulate("perda_pacotes", 0);
            jsonObject.accumulate("latencia", 0);
            jsonObject.accumulate("jitter", 0);
            jsonObject.accumulate("numero_telefone", new SharedPrefsUtil(getApplicationContext()).getUserPhone());

            //params.put("velocidade_download", "");
            //params.put("velocidade_upload", "");
            //params.put("perda_pacotes:", "");
            //params.put("latencia", "");
            //params.put("jitter", "");
            //jsonObject.accumulate("exibição", "normal");
            //jsonObject.accumulate("numero_telefone", "81996090238");
            //jsonObject.accumulate("gerar_html", "false");
/*
            String msgJson =
                    "{ \"data\": " +
                            "{ \"mensagem\":\""+ msg +"\", \"id\":\""+ 1 +"\", \"user\":\""+ compartilha.getPacienteUsuario().getId() +"\"}, " +
                            //"{ \"mensagem\":\""+ msg +"\"}, " +


                            "\"registration_ids\": [ \""+
                            device.getRegistrationId() +  "\" ] }";*/

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();
            //json = "{\"tipo_pacote\":\"MF\",\"datahora_inicio_medicao\":\"2015-11-24 17:35:53\",\"datahora_fim_medicao\":\"2015-11-24 17:35:53\",\"nivel_sinal\":-87,\"celula_gsm\":55675,\"operadora\":\"TIM\",\"latitude\":-8.0537458,\"longitude\":-34.8792223,\"velocidade_download\":10,\"velocidade_upload\":20,\"perda_pacotes\":1,\"latencia\":1,\"jitter\":2}";


            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
                Log.d("RESULT", result);
            } else {
            }
        } catch (Exception e) {
            Log.d("InputStream", Util.getMessageErrorFromExcepetion(e));
            return false;
        }


        Log.i("GPS_PROVIDER Listener", "send medicao");
        // 11. return result
        return true;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    class UploadFalseRegisters extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            Log.i("LOG ServiceStrengthGET", "executing doInBackground by UploadFalseRegisters");

            Dao<SignalStrengthAverage, Integer> signalStrengthAverageDao = null;
            List<SignalStrengthAverage> signalStrengthAverageListAux = null;
            try {
                signalStrengthAverageDao = OrmLiteHelper.getInstance(getApplicationContext()).getSignalStrengthAverageDao();
                signalStrengthAverageListAux = signalStrengthAverageDao.queryBuilder()
                        .where().eq("SIG_STR_AVE_SAVED", false).query();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (signalStrengthAverageListAux != null && !signalStrengthAverageListAux.isEmpty()) {
                for (SignalStrengthAverage signalStrengthAverageAux : signalStrengthAverageListAux) {
                    boolean result = sendMedicao(Util.getSuperUrlServiceAddData(getApplicationContext()), signalStrengthAverageAux);
                    signalStrengthAverageAux.setSaved(result);
                    if (result) {
                        try {
                            signalStrengthAverageDao.update(signalStrengthAverageAux);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("STATUS SEND MEDICAO", "Status: " + result);
                }
            }

            return null;
        }

        protected void onPostExecute(String feed) {
            Log.i("LOGGGGG", "stopppp");
            stopSelf();
        }
    }

}