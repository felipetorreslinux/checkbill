package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Petrus A. (R@G3), ESPE... On 11/01/2017.
 */

public class ComparacaoPlanoRequester {
    private static final String TAG = "CompPlanoRequester";
    private static final String PARAMETER_ACCESS_KEY = "access_key";
    private static final String PARAMETER_ID_OPERADORA = "operadora";
    private static final String PARAMETER_ID_MODALIDADE_PLANO = "modalidade_plano";
    private static final String PARAMETER_ID_TIPO_PLANO = "tipo_plano";
    private static final String PARAMETER_REGIAO_COBERTURA = "regiao";

    public static JsonObjectRequest prepareCompararPlanoRequest(Response.Listener responseListener, Response.ErrorListener erroListener, final int id_operadora, final int id_modalidade, final int id_tipo_plano, final String area, Context context) {
        final String url = Util.getSuperUrlServiceCompararPlanoUsuario(context);
        final String accessKey = new SharedPrefsUtil(context).getAccessKey();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(PARAMETER_ACCESS_KEY, accessKey);
            jsonObject.put(PARAMETER_ID_OPERADORA, id_operadora);
            jsonObject.put(PARAMETER_ID_MODALIDADE_PLANO, id_modalidade);
            jsonObject.put(PARAMETER_ID_TIPO_PLANO, id_tipo_plano);
            jsonObject.put(PARAMETER_REGIAO_COBERTURA, area);
        } catch ( JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }
}
