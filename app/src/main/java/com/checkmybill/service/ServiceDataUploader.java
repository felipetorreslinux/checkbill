package com.checkmybill.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.SignalStrengthAverage;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petrus A & Victor G. on 25/11/2016.
 * Modificado por Petrus: 09/05/2017
 */

public class ServiceDataUploader extends Service {
    /* ------------------------------------------------------------------------------------------ */
    // Elementos de uso interno da classe
    private class DataToSend {
        private JSONArray jsonArray;
        private List<?> sourceObject;
    }

    final private String LOG_TAG = ServiceDataUploader.class.getName();
    final private OrmLiteHelper orm = OrmLiteHelper.getInstance(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Initializing...");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonToSend = new JSONObject();

        // Checando permissão de intenert/rede...
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Log.e(ServiceSMSOutgoingMonitor.class.getName(), "No Permission to use INTERNET");
            return super.onStartCommand(intent, flags, startId);
        }

        // Obtendo acesskey (retorna NULL se não existir)
        String accessKey = new SharedPrefsUtil(getBaseContext()).getAccessKey();
        final int id_imei = new SharedPrefsUtil(getBaseContext()).getIDImei();

        try {
            final DataToSend callData;
            final DataToSend smsData;
            final DataToSend bytesConsumed;
            final DataToSend gsmData = generateGSMDataToUpload();
            final DataToSend blargaData = generateBLargaDataToUpload();
            final DataToSend unavailabilityData = generateUnavailabilityDateToUpload();

            // Montando JSON a ser enviado...
            if (accessKey != null && accessKey.length() > 0) {
                callData = generateCallDataToUpload();
                smsData = generateSMSDataToUpload();
                bytesConsumed = generateBytesConsumidoDataToUpload();

                jsonToSend.put("access_key", accessKey);
                jsonToSend.put("call_data", callData.jsonArray);
                jsonToSend.put("sms_data", smsData.jsonArray);
                jsonToSend.put("bytes_consumo_data", bytesConsumed.jsonArray);
            } else {
                // Checando se deve anexar o imei...
                callData = null;
                smsData = null;
                bytesConsumed = null;

                if (id_imei > 0) jsonToSend.put("id_imei", id_imei);
            }

            jsonToSend.put("gsm_data", gsmData.jsonArray);
            jsonToSend.put("blarga_data", blargaData.jsonArray);
            jsonToSend.put("unavailability_data", unavailabilityData.jsonArray);

            // Preparando Requester para enviar a requisição...
            final String url = Util.getSuperUrlServiceAdicionarDadosMonitoramento(getBaseContext());
            JsonObjectRequest requester = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonToSend.toString(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(LOG_TAG, "RCV-JSON -> " + response.toString());
                        // Checando resposta
                        if (!response.getString("status").equalsIgnoreCase("success"))
                            throw new JSONException(response.getString("message"));

                        // Atualizando os status dos dados enviados para 'true'
                        final boolean gsmSaved = response.getBoolean("gsm_saved");
                        final boolean blargaSaved = response.getBoolean("blarga_saved");
                        final boolean unavailabilitySaved = response.getBoolean("unavailability_saved");
                        final boolean callSaved = response.getBoolean("call_saved");
                        final boolean smsSaved = response.getBoolean("sms_saved");
                        final boolean bytesSaved = response.getBoolean("bytes_saved");

                        if (unavailabilitySaved)
                            updateUnavailabilitySendStatus((List<Unavailability>) unavailabilityData.sourceObject);
                        if (gsmSaved)
                            updateGSMDataSendStatus((List<SignalStrengthAverage>) gsmData.sourceObject);
                        if (blargaSaved)
                            updateBLargaSendStatus((List<NetworkQuality>) blargaData.sourceObject);
                        if (callData != null && callSaved)
                            updateCallSendStatus((List<CallMonitor>) callData.sourceObject);
                        if (smsData != null && smsSaved)
                            updateSMSSendStatus((List<SmsMonitor>) smsData.sourceObject);
                        if ( bytesConsumed != null && bytesSaved)
                            updateBytesConsumedSendStatus((List<BytesSendedReferenceObject>) bytesConsumed.sourceObject);

                        // All done
                        Log.i(LOG_TAG, "All Data was sended...");
                    } catch (SQLException | JSONException e) {
                        Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                    }
                }
            }, this.errorListener);
            requester.setTag(ServiceDataUploader.class.getName());

            // Enviando os dados (realizando a requisção)
            Log.i(LOG_TAG, "Sanding data to server -> " + url);
            Log.d(LOG_TAG, jsonToSend.toString());
            requestQueue.add(requester);
        } catch (Exception e) {
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }

        // Obtendo os dados a serem enviados
        return super.onStartCommand(intent, flags, startId);
    }


    /* ------------------------------------------------------------------------------------------ */
    // Listeners do Volley
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };

    /* ------------------------------------------------------------------------------------------ */
    private DataToSend generateBytesConsumidoDataToUpload() throws SQLException, JSONException {
        // Obtendo os bytes não sincronizados ate o momento (nota: Ignorando o ultimo/atual)
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        final List<TrafficMonitor_Mobile> listMobData = orm.getTrafficMonitorMobileDao().queryBuilder()
                .orderBy("id_mob_monitor", true)
                .where().eq("data_sync", false).query();
        listMobData.remove(listMobData.size() - 1); // Removendo o ultimo (atual)
        final List<TrafficMonitor_WiFi> listWifiData = orm.getTrafficMonitorWifiDao().queryBuilder()
                .orderBy("id_wifi_monitor", true)
                .where().eq("data_sync", false).query();
        listWifiData.remove(listWifiData.size() - 1); // removendo o ultimo (atual)
        final List<BytesSendedReferenceObject> bytesSendedReferenceObjectList = new ArrayList<>();
        final DataToSend returnObject = new DataToSend();

        returnObject.jsonArray = new JSONArray();
        // 1-> Mobile
        for ( int i = 0; i < listMobData.size(); i++ ) {
            TrafficMonitor_Mobile dt = listMobData.get(i);
            long consumedBytes = 0;
            consumedBytes += dt.getCurrentReceivedBytes_end() - dt.getCurrentReceivedBytes_start();
            consumedBytes += dt.getCurrentSendedBytes_end() - dt.getCurrentSendedBytes_start();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bytes_consumidos", consumedBytes);
            jsonObject.put("network_type", "mob");
            jsonObject.put("datetime_numeric", sdf.format(dt.getDatePeriodo()));

            returnObject.jsonArray.put(jsonObject);
            bytesSendedReferenceObjectList.add(new BytesSendedReferenceObject(dt, null));
        }
        // 2-> GSM/Blarga
        for ( int i = 0; i < listWifiData.size(); i++ ) {
            TrafficMonitor_WiFi dt = listWifiData.get(i);
            long consumedBytes = 0;
            consumedBytes += dt.getCurrentReceivedBytes_end() - dt.getCurrentReceivedBytes_start();
            consumedBytes += dt.getCurrentSendedBytes_end() - dt.getCurrentSendedBytes_start();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bytes_consumidos", consumedBytes);
            jsonObject.put("network_type", "blarga");
            jsonObject.put("datetime_numeric", sdf.format(dt.getDatePeriodo()));

            returnObject.jsonArray.put(jsonObject);
            bytesSendedReferenceObjectList.add(new BytesSendedReferenceObject(null, dt));
        }

        returnObject.sourceObject = bytesSendedReferenceObjectList;
        return returnObject;
    }

    private DataToSend generateUnavailabilityDateToUpload() throws SQLException, JSONException {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String nomeOperadora = telephonyManager.getNetworkOperatorName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Unavailability> listUnavailabilityDataToSend = orm.getUnavailabilityDao().queryBuilder().
                where().eq("UNAVA_SAVED", false).and().eq("UNAVA_CURRENT", false).query();

        // Montando JSON
        JSONArray jsonArrData = new JSONArray();
        for (Unavailability unavailability : listUnavailabilityDataToSend) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("tipo_pacote", "MI");
            jsonObject.accumulate("datahora_inicio_medicao", format.format(unavailability.getDateStarted()));
            jsonObject.accumulate("datahora_fim_medicao", format.format(unavailability.getDateFinished()));
            jsonObject.accumulate("nivel_sinal", unavailability.getValueSignal());
            jsonObject.accumulate("celula_gsm", unavailability.getCellId());
            jsonObject.accumulate("operadora", nomeOperadora);
            jsonObject.accumulate("latitude", unavailability.getLat());
            jsonObject.accumulate("longitude", unavailability.getLng());
            jsonObject.accumulate("numero_telefone", new SharedPrefsUtil(getApplicationContext()).getUserPhone());

            jsonArrData.put(jsonObject);
        }

        DataToSend dataToSend = new DataToSend();
        dataToSend.jsonArray = jsonArrData;
        dataToSend.sourceObject = listUnavailabilityDataToSend;
        return dataToSend;
    }

    private DataToSend generateBLargaDataToUpload() throws SQLException, JSONException {
        List<NetworkQuality> listBlargaDataToSend = orm.getNetworkQualityDao().queryBuilder().
                limit(30L).
                where().eq("NET_QUALI_SYNC", false).query();

        // Montando JSON
        JSONArray jsonArrData = new JSONArray();
        for (NetworkQuality networkQuality : listBlargaDataToSend) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("latitude", networkQuality.getNetworkWifi().getLat());
                jsonObject.accumulate("longitude", networkQuality.getNetworkWifi().getLng());
                jsonObject.accumulate("quali_upload", networkQuality.getUpload());
                jsonObject.accumulate("quali_download", networkQuality.getDownload());
                jsonObject.accumulate("quali_latencia", networkQuality.getLatency());
                jsonObject.accumulate("quali_sync", 1);
                jsonObject.accumulate("is_wifi", 1);
                jsonObject.accumulate("operadora", networkQuality.getNetworkWifi().getIsp());

                jsonArrData.put(jsonObject);
            } catch (Exception e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        DataToSend dataToSend = new DataToSend();
        dataToSend.jsonArray = jsonArrData;
        dataToSend.sourceObject = listBlargaDataToSend;
        return dataToSend;
    }

    private DataToSend generateGSMDataToUpload() throws SQLException, JSONException {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String nomeOperadora = telephonyManager.getNetworkOperatorName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SignalStrengthAverage> listSignalDataToSend = orm.getSignalStrengthAverageDao().queryBuilder().
                where().eq("SIG_STR_AVE_SAVED", false).query();

        // Montando JSON
        JSONArray jsonArrData = new JSONArray();
        for (SignalStrengthAverage signalStrengthAverage : listSignalDataToSend) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("tipo_pacote", "MA");
            jsonObject.accumulate("datahora_inicio_medicao", format.format(signalStrengthAverage.getDate()));
            jsonObject.accumulate("datahora_fim_medicao", format.format(signalStrengthAverage.getDate()));
            jsonObject.accumulate("nivel_sinal", signalStrengthAverage.getValue());
            jsonObject.accumulate("celula_gsm", signalStrengthAverage.getCellId());
            jsonObject.accumulate("operadora", nomeOperadora);
            jsonObject.accumulate("latitude", signalStrengthAverage.getLat());
            jsonObject.accumulate("longitude", signalStrengthAverage.getLng());
            jsonObject.accumulate("numero_telefone", new SharedPrefsUtil(getApplicationContext()).getUserPhone());

            jsonObject.accumulate("velocidade_download", 0);
            jsonObject.accumulate("velocidade_upload", 0);
            jsonObject.accumulate("perda_pacotes", 0);
            jsonObject.accumulate("latencia", 0);
            jsonObject.accumulate("jitter", 0);

            jsonArrData.put(jsonObject);
        }

        DataToSend dataToSend = new DataToSend();
        dataToSend.jsonArray = jsonArrData;
        dataToSend.sourceObject = listSignalDataToSend;
        return dataToSend;
    }

    private DataToSend generateCallDataToUpload() throws SQLException, JSONException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<CallMonitor> listCallDataToSend = orm.getCallMonitorDao().queryBuilder().
                where().eq("CALL_SENDED", false).query();

        // Montando JSON
        JSONArray jsonArrData = new JSONArray();
        for (CallMonitor callInfo : listCallDataToSend) {
            JSONObject callJsonData = new JSONObject();
            callJsonData.put("call_duration_time", callInfo.getElapsedTime());
            callJsonData.put("call_date", sdf.format(callInfo.getDateCad()));

            jsonArrData.put(callJsonData);
        }

        DataToSend dataToSend = new DataToSend();
        dataToSend.jsonArray = jsonArrData;
        dataToSend.sourceObject = listCallDataToSend;
        return dataToSend;
    }

    private DataToSend generateSMSDataToUpload() throws SQLException, JSONException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<SmsMonitor> listSmsDataToSend = orm.getSmsMonitorDao().queryBuilder().
                where().eq("SMS_SENDED", false)
                .query();

        // Montando JSON
        JSONArray jsonArrData = new JSONArray();
        for (SmsMonitor smsInfo : listSmsDataToSend) {
            JSONObject smsJsonData = new JSONObject();
            smsJsonData.put("sms_date", sdf.format(smsInfo.getDateCad()));

            jsonArrData.put(smsJsonData);
        }

        DataToSend dataToSend = new DataToSend();
        dataToSend.jsonArray = jsonArrData;
        dataToSend.sourceObject = listSmsDataToSend;
        return dataToSend;
    }

    // -> Lista de funcoes que atualizao o status de envio para 'verdadeiro/true/enviado'
    private void updateBytesConsumedSendStatus(List<BytesSendedReferenceObject> list) throws SQLException {
        for ( BytesSendedReferenceObject dt :  list) {
            if ( dt.mobileTrafficData != null ) {
                dt.mobileTrafficData.setDataSyncronized(true);
                orm.getTrafficMonitorMobileDao().update(dt.mobileTrafficData);
            } else if ( dt.wifiTrafficData != null ) {
                dt.wifiTrafficData.setDataSyncronized(true);
                orm.getTrafficMonitorWifiDao().update(dt.wifiTrafficData);
            }
        }
    }

    private void updateUnavailabilitySendStatus(List<Unavailability> list) throws SQLException {
        for (Unavailability unavailability : list) {
            unavailability.setSaved(true);
            orm.getUnavailabilityDao().update(unavailability);
        }
    }

    private void updateGSMDataSendStatus(List<SignalStrengthAverage> list) throws SQLException {
        for (SignalStrengthAverage signal : list) {
            signal.setSaved(true);
            orm.getSignalStrengthAverageDao().update(signal);
        }
    }

    private void updateBLargaSendStatus(List<NetworkQuality> list) throws SQLException {
        for (NetworkQuality network : list) {
            network.setSync(true);
            orm.getNetworkQualityDao().update(network);
        }
    }

    private void updateCallSendStatus(List<CallMonitor> list) throws SQLException {
        for (CallMonitor call : list) {
            call.setCallSended(true);
            orm.getCallMonitorDao().update(call);
        }
    }

    private void updateSMSSendStatus(List<SmsMonitor> list) throws SQLException {
        for (SmsMonitor sms : list) {
            sms.setSmsSended(true);
            orm.getSmsMonitorDao().update(sms);
        }
    }

    /**
     * Classe usada como referencia no campo de envio dos bytes ao servidor
     * Nota: Necessário pois, como ambas agora são separados, preciso de uma unificada para tratar
     *       cada uma delas separadamente mas sem criar dois objetos DataToSend.
     */
    private class BytesSendedReferenceObject {
        TrafficMonitor_Mobile mobileTrafficData;
        TrafficMonitor_WiFi wifiTrafficData;

        public BytesSendedReferenceObject(TrafficMonitor_Mobile mobileTrafficData, TrafficMonitor_WiFi wifiTrafficData) {
            this.mobileTrafficData = mobileTrafficData;
            this.wifiTrafficData = wifiTrafficData;
        }
    }
}
