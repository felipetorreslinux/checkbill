package com.checkmybill.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Victor Guerra on 21/01/2016.
 */
public class ServiceConfMob extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("ServiceConfMob", "onStartCommand");

        final JsonObjectRequest jsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(JsonObjectRequest.Method.POST, Util.getSuperUrlServiceGetConfMob(getApplicationContext()), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Response: {"status":"success","indisponibilidade":10000,"sinal_2g":105,"sinal_4g":125}
                try {
                    String status = response.getString("status");
                    if (status.equals("success")) {
                        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
                        sharedPrefsUtil.setConfMob2g(response.getInt("sinal_2g") * -1);
                        sharedPrefsUtil.setConfMob4g(response.getInt("sinal_4g") * -1);
                        sharedPrefsUtil.setConfMobTimeUnavailability(response.getInt("indisponibilidade"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                stopSelf();
            }
        });

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);

        return super.onStartCommand(intent, flags, startId);
    }
}
