package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ESPENOTE-06 on 14/11/2016.
 */

public class ObterFiltrosRequester {
    private static final String TAG = "ObterFiltrosRequester";
    private static final String PARAMETER_FILTROS = "filtros";

    public static JsonObjectRequest prepareObterFiltrosBasePlanos(Response.Listener responseListener, Response.ErrorListener erroListener, String filtros, Context context) {
        String url = Util.getSuperUrlServiceObterFiltrosBasePlanos(context);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PARAMETER_FILTROS, filtros);
        } catch (JSONException e) {
            Log.e(TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
        }

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }
}
