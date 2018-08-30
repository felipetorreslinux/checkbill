package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.R;
import com.checkmybill.entity.Operadora;
import com.checkmybill.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Guerra on 12/08/2016.
 */

public class OperadoraRequester {

    private static final String TAG = "AccountRequester";
    private static final String URL_LISTA_OPERADORAS = Util.getSubFoldersURI() + "/obter-lista-operadoras";
    private static final String PARAMETER_TIPO_OPERADORA = "tipo_operadora";
    private static final String PARAMETER_OPERADORAS = "operadoras";
    private static final String PARAMETER_ID_OPERADORA = "id_operadora";
    private static final String PARAMETER_NOME_OPERADORA = "nome_operadora";
    private static final String PARAMETER_COR_OPERADORA = "cor_operadora";

    public static JsonObjectRequest prepareListaOperadorasRequest(Response.Listener responseListener,
                                                                  Response.ErrorListener erroListener,
                                                                  String tipoOperado,
                                                                  Context context) {

        String url = context.getString(R.string.url_base) + URL_LISTA_OPERADORAS;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_TIPO_OPERADORA, tipoOperado);
        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + URL_LISTA_OPERADORAS);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static List<Operadora> parserListaOperadorasRequest(JSONObject resposne) {
        List<Operadora> operadoras = new ArrayList<>();

        try {
            JSONArray jsonArray = resposne.getJSONArray(PARAMETER_OPERADORAS);

            for (int i = 0; i < jsonArray.length(); i++) {
                Operadora operadora = new Operadora();

                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                operadora.setId(jsonObject.getInt(PARAMETER_ID_OPERADORA));
                operadora.setTipoOperadora(jsonObject.getString(PARAMETER_TIPO_OPERADORA));
                operadora.setCorOperadora(jsonObject.getString(PARAMETER_COR_OPERADORA));
                operadora.setNomeOperadora(jsonObject.getString(PARAMETER_NOME_OPERADORA));

                operadoras.add(operadora);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return operadoras;
    }

}
