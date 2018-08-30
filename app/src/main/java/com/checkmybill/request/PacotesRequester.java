package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.entity.Plano;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Petrus A, ESPE on 06/12/2016.
 */

public class PacotesRequester {
    private static final String TAG = "PacotesRequester";
    private static final String PARAMETER_MINUTAGENS = "minutagens";
    private static final String PARAMETER_ID_OPERADORA = "id_operadora";
    private static final String PARAMETER_MODALIDADE = "modalidade";
    private static final String PARAMETER_ACCESS_KEY = "access_key";
    private static final String PARAMETER_DATE_START = "data_inicio";
    private static final String PARAMETER_DATE_END = "data_fim";
    private static final String PARAMETER_DATE_CAD = "dat_cad";
    private static final String PARAMETER_ID_PACOTE = "id_pacote";
    private static final String PARAMETER_ID_PACOTE_PLANO_USUARIO = "id_pacote_plano_usuario";

    /* ------------------------------------------------------------------------------------------ */
    // -> Principais metodos da classe...
    public static JsonObjectRequest prepareObterPacotesPlanoUsuario(Response.Listener responseListener, Response.ErrorListener erroListener, Date start, Date end, Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final String url = Util.getSuperUrlServiceObterPacotesPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, new SharedPrefsUtil(context).getAccessKey());
            if ( start != null ) jsonObject.put(PARAMETER_DATE_START, sdf.format(start));
            if ( end != null ) jsonObject.put(PARAMETER_DATE_END, sdf.format(end));
        } catch ( JSONException ex ) {
            Log.e(TAG, Util.getMessageErrorFromExcepetion(ex));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareObterListaPacotes(Response.Listener responseListener, Response.ErrorListener erroListener, Plano plano, Context context) {
        String url = Util.getSuperUrlServiceObterListaPacotes(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_ID_OPERADORA, plano.getIdOperadora());
            jsonObject.put(PARAMETER_MODALIDADE, plano.getIdModalidadePlano());
            jsonObject.put(PARAMETER_MINUTAGENS, 1);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareAnexarPacotePlanoUsuario(Response.Listener responseListener, Response.ErrorListener erroListener, int idPacote, Date datCad, Context context) {
        String url = Util.getSuperUrlServiceAnexarPacotePlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, new SharedPrefsUtil(context).getAccessKey());
            jsonObject.put(PARAMETER_ID_PACOTE, idPacote);
            jsonObject.put(PARAMETER_DATE_CAD, sdf.format(datCad));
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareRemoverPacotePlanoUsuario(Response.Listener responseListener, Response.ErrorListener erroListener, int idPacotePlanousuario, Context context){
        String url = Util.getSuperUrlServiceRemoverPacotePlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, new SharedPrefsUtil(context).getAccessKey());
            jsonObject.put(PARAMETER_ID_PACOTE_PLANO_USUARIO, idPacotePlanousuario);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }
}
