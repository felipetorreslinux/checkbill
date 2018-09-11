package com.checkmybill.request;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.R;
import com.checkmybill.entity.Operadora;
import com.checkmybill.entity.Plano;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Victor Guerra on 12/08/2016.
 */

public class PlanoRequester {

    private static final String TAG = "PlanoRequester";
    private static final String PARAMETER_REMOVER_RECARGAS_PACOTES_ANTIGOS = "remover_recargas_pacotes";
    private static final String PARAMETER_OPERADORA = "operadora";
    private static final String PARAMETER_MODALIDADE = "modalidade";
    private static final String PARAMETER_MINUTAGENS = "minutagens";
    private static final String PARAMETER_VENCIMENTO = "dt_vencimento";
    private static final String PARAMETER_ID_OPERADORA = "id_operadora";
    private static final String PARAMETER_ID_MODALIDADE_PLANO = "id_modalidade_plano";
    private static final String PARAMETER_ID_TIPO_PLANO = "id_tipo_plano";
    private static final String PARAMETER_NOME_PLANO = "nome_plano";
    private static final String PARAMETER_ACCESS_KEY = "access_key";
    private static final String PARAMETER_ID_PLANO_REFERENCIA = "id_plano_operadora_ref";
    private static final String PARAMETER_LIMITE_SMS = "limite_sms";
    private static final String PARAMETER_LIMITE_NET = "limite_net";
    private static final String PARAMETER_LIMITE_CALL_OO = "limite_call_oo";
    private static final String PARAMETER_LIMITE_CALL_MO = "limite_call_mo";
    private static final String PARAMETER_LIMITE_CALL_IU = "limite_call_iu";
    private static final String PARAMETER_LIMITE_CALL_FIXO = "limite_call_fixo";
    private static final String PARAMETER_VALOR = "valor_plano";
    private static final String PARAMETER_DATE_START = "data_inicio";
    private static final String PARAMETER_DATE_END = "data_fim";
    private static final String PARAMETER_RECARGA_DATE = "recarga_data";
    private static final String PARAMETER_RECARGA_VALOR = "recarga_valor";
    private static final String PARAMETER_RECARGA_ID = "id_recarga";


    public static JsonObjectRequest prepareSalvarPlanoUsuarioRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Plano plano, boolean removerPacotesRecargasAntigos, Context context) {
        final String url = Util.getSuperUrlServiceSalvarPlanoUsuario(context);
        final String accessKey = new SharedPrefsUtil(context).getAccessKey();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_ID_MODALIDADE_PLANO, plano.getIdModalidadePlano());
            jsonObject.put(PARAMETER_ID_TIPO_PLANO, plano.getIdTipoPlano());
            jsonObject.put(PARAMETER_LIMITE_SMS, plano.getSmsInclusos());
            jsonObject.put(PARAMETER_LIMITE_NET, plano.getLimiteDadosWeb());
            jsonObject.put(PARAMETER_LIMITE_CALL_MO, plano.getMinMO());
            jsonObject.put(PARAMETER_LIMITE_CALL_IU, plano.getMinIU());
            jsonObject.put(PARAMETER_LIMITE_CALL_FIXO, plano.getMinFixo());
            jsonObject.put(PARAMETER_LIMITE_CALL_OO, plano.getMinOO());
            jsonObject.put(PARAMETER_NOME_PLANO, plano.getNomePlano());
            jsonObject.put(PARAMETER_ID_OPERADORA, plano.getIdOperadora());
            jsonObject.put(PARAMETER_REMOVER_RECARGAS_PACOTES_ANTIGOS, removerPacotesRecargasAntigos);
            jsonObject.put(PARAMETER_VALOR, String.format("%.2f", plano.getValorPlano()).replace(",","."));
            jsonObject.put(PARAMETER_VENCIMENTO, plano.getDtVencimento());
            jsonObject.put(PARAMETER_ID_PLANO_REFERENCIA, plano.getIdPlanoReferencia());
        } catch ( JSONException ex ) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareObterPlanoUsuarioRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Context context) {
        String url = Util.getSuperUrlServiceObterPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            String accessKey = new SharedPrefsUtil(context).getAccessKey();
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareObterPlanoUsuarioRequest(boolean anexarPacote, Response.Listener responseListener, Response.ErrorListener erroListener, Context context) {
        String url = Util.getSuperUrlServiceObterPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            String accessKey = new SharedPrefsUtil(context).getAccessKey();
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put("anexar_pacote", (anexarPacote) ? 1 : 0);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareConsultaPlanosOperadoraRequest(Response.Listener responseListener, Response.ErrorListener erroListener, int id_modalidade, int id_operadora, int minutagens, Context context) {
        String url = Util.getSuperUrlServiceObterListaPlanosSimples(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_OPERADORA, id_operadora);
            jsonObject.put(PARAMETER_MODALIDADE, id_modalidade);
            jsonObject.put(PARAMETER_MINUTAGENS, minutagens);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareConsultaRecargasPlanosUsuarioRequest(Response.Listener responseListener, Response.ErrorListener erroListener, String startDate, String endDate, Context context) {
        String url = Util.getSuperUrlServiceObterRecargasPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            String accessKey = new SharedPrefsUtil(context).getAccessKey();
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_DATE_START, startDate);
            jsonObject.put(PARAMETER_DATE_END, endDate);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareAddRecargaPlanoUsuarioRequest(Response.Listener responseListener, Response.ErrorListener erroListener, String date, float valor, Context context) {
        String url = Util.getSuperUrlServiceAdicionarRecargaPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            String accessKey = new SharedPrefsUtil(context).getAccessKey();
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_RECARGA_DATE, date);
            jsonObject.put(PARAMETER_RECARGA_VALOR, String.format("%.2f", valor));
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareRemoverRecargaPlanoUsuarioRequest(Response.Listener responseListener, Response.ErrorListener erroListener, int id_recarga, Context context) {
        String url = Util.getSuperUrlServiceRemoverRecargaPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            String accessKey = new SharedPrefsUtil(context).getAccessKey();
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_RECARGA_ID, id_recarga);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static JsonObjectRequest prepareAvaliaPlanoUsuarioRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Context context) {
        String url = Util.getSuperUrlServiceAvaliaPlanoUsuario(context);
        JSONObject jsonObject = new JSONObject();
        try {
            String accessKey = new SharedPrefsUtil(context).getAccessKey();
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }
}
