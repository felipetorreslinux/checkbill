package com.checkmybill.request;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Petrus A. (R@G3), ESPE... On 09/12/2016.
 */

public class MedicoesRequester {
    private static final String TAG = "MedicoesRequester";
    private static final String PARAMETER_ACCESS_KEY = "access_key";
    private static final String PARAMETER_OPCOES = "opcoes";
    private static final String PARAMETER_PERIODO = "periodo";
    private static final String PARAMETER_INC_SMS = "inc_sms";
    private static final String PARAMETER_INC_CALL = "inc_call";
    private static final String PARAMETER_INC_WEB = "inc_web";
    private static final String PARAMETER_INC_UNAVAIABLE = "inc_unav";
    private static final String PARAMETER_LATITUDE = "latitude";
    private static final String PARAMETER_LONGITUDE = "longitude";

    public static JsonObjectRequest prepareInfoIndispRegiaoRequest(Date[] dateRange, String nomeOperadoraGSM, String nomeOperadoraBlarga, Response.Listener responseListener, Response.ErrorListener erroListener, Context context) {
        final String url = Util.getInformacaoIndisponibilidadeRegiao(context);
        JSONObject jsonObject = new JSONObject();
        JSONObject gsmJsonObject = new JSONObject();
        JSONObject blargaJsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        String networkOperator = telephonyManager.getNetworkOperator();
        String mnc;
        try{
            mnc= networkOperator.substring(3);
        }catch(RuntimeException e){
            mnc = "";
        }
        final int cid = cellLocation.getCid();
        final int lac = cellLocation.getLac();

        try {
            gsmJsonObject.put("mnc", mnc);
            gsmJsonObject.put("cid", cid);
            gsmJsonObject.put("lac", lac);
            gsmJsonObject.put("operadora", nomeOperadoraGSM);
            blargaJsonObject.put("operadora", nomeOperadoraBlarga);
            jsonObject.put("inicio_data", sdf.format(dateRange[0]));
            jsonObject.put("fim_data", sdf.format(dateRange[1]));
            jsonObject.put("gsm", gsmJsonObject);

            if ( nomeOperadoraBlarga != null && nomeOperadoraBlarga.length() > 0 )
                jsonObject.put("blarga", blargaJsonObject);

        } catch ( JSONException ex ) {
            Log.e(TAG, "prepareInfoIndispRegiaoRequest | error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        Log.d(TAG, "prepared request to: " + url);
        Log.d(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareInfoSinalRegiaoRequest(Date[] dateRange, String nomeOperadoraGSM, String nomeOperadoraBlarga, Response.Listener responseListener, Response.ErrorListener erroListener, Context context) {
        final String url = Util.getInformacaoSinalRegiao(context);
        JSONObject jsonObject = new JSONObject();
        JSONObject gsmJsonObject = new JSONObject();
        JSONObject blargaJsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation)telephonyManager.getCellLocation();
        String networkOperator = telephonyManager.getNetworkOperator();
        final String mnc = networkOperator.substring(3);
        final int cid = cellLocation.getCid();
        final int lac = cellLocation.getLac();

        try {
            gsmJsonObject.put("mnc", mnc);
            gsmJsonObject.put("cid", cid);
            gsmJsonObject.put("lac", lac);
            gsmJsonObject.put("operadora", nomeOperadoraGSM);
            blargaJsonObject.put("operadora", nomeOperadoraBlarga);
            jsonObject.put("inicio_data", sdf.format(dateRange[0]));
            jsonObject.put("fim_data", sdf.format(dateRange[1]));
            jsonObject.put("gsm", gsmJsonObject);

            if ( nomeOperadoraBlarga != null && nomeOperadoraBlarga.length() > 0 )
                jsonObject.put("blarga", blargaJsonObject);

        } catch ( JSONException ex ) {
            Log.e(TAG, "prepareInfoSinalRegiaoRequest | error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        Log.d(TAG, "prepared request to: " + url);
        Log.d(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareGerarRelatorioConsumo(Response.Listener responseListener, Response.ErrorListener erroListener, Date periodo, int incSMS, long incCall, double incOffline, long incBytes, double latitude, double lontigude, Context context) {
        final String url = Util.getSuperUrlServiceGerarRelatorioConsumo(context);
        JSONObject jsonObject = new JSONObject();
        final String accessKey = new SharedPrefsUtil(context).getAccessKey();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_OPCOES, "sms,call");
            jsonObject.put(PARAMETER_PERIODO, sdf.format(periodo));
            jsonObject.put(PARAMETER_INC_SMS, incSMS);
            jsonObject.put(PARAMETER_INC_CALL, incCall);
            jsonObject.put(PARAMETER_INC_WEB, incBytes);
            jsonObject.put(PARAMETER_INC_UNAVAIABLE, incOffline);
            jsonObject.put(PARAMETER_LATITUDE, latitude);
            jsonObject.put(PARAMETER_LONGITUDE, lontigude);
        } catch ( JSONException ex ) {
            Log.e(TAG, "prepareObterDadosMonitoramento | error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        Log.d(TAG, "prepared request to: " + url);
        Log.d(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareIndispDetailRequest(Date[] dateRange, Response.Listener responseListener, Response.ErrorListener erroListener, Context context) {
        final String url = Util.getInformacaoIndisponibilidadeDetalhe(context);
        JSONObject jsonObject = new JSONObject();
        final String accessKey = new SharedPrefsUtil(context).getAccessKey();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put("data_inicio", sdf.format(dateRange[0]));
            jsonObject.put("data_fim", sdf.format(dateRange[1]));

        } catch ( JSONException ex ) {
            Log.e(TAG, "prepareIndispDetailRequest | error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        Log.d(TAG, "prepared request to: " + url);
        Log.d(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

}
