package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.R;
import com.checkmybill.entity.Account;
import com.checkmybill.entity.Colabore;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Victor Guerra on 25/10/2016.
 */

public class ColaboreRequester {
    private static final String TAG = "ColaboreRequester";
    private static final String ACCESS_KEY = "access_key";
    private static final String SUBJECT = "subject";
    private static final String MESSAGE = "message";

    public static JsonObjectRequest prepareEnviarColaboreRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Colabore colabore, Context context) {
        final String url = Util.getSuperUrlServiceEnviarColabore(context);
        String accessKey = getAccessKey(context);
        if ( accessKey == null || accessKey.length() <= 0 ) accessKey = "";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ACCESS_KEY, getAccessKey(context));
            jsonObject.put(SUBJECT, colabore.getSubject());
            jsonObject.put(MESSAGE, colabore.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "prepareLoginRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    private static String getAccessKey(Context context){
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        String accessKey = sharedPrefsUtil.getAccessKey();
        return accessKey;
    }

}
