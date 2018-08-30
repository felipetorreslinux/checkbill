package com.checkmybill.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.entity.Colabore;
import com.checkmybill.request.ColaboreRequester;

import org.json.JSONObject;

/**
 * Created by Victor Guerra on 25/10/2016.
 */

public class ServiceEnviarColabore extends Service{

    private static final String TAG = "ServiceEnviarColabore";

    public static final String EXTRA_COLABORE = "EXTRA_COLABORE";

    private Colabore colabore;
    private RequestQueue requestQueue;
    private Response.Listener responListener;
    private Response.ErrorListener errorListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try{

            Toast.makeText(getApplicationContext(), "Enviando mensagem...", Toast.LENGTH_SHORT).show();

            requestQueue = Volley.newRequestQueue(getApplicationContext());

            if(intent.getExtras() != null){
                colabore = (Colabore) intent.getSerializableExtra(EXTRA_COLABORE);

                sendEnviarColaboreRequest();
            }else{
                stopSelf();
            }

        }catch(Exception e){
            Log.e(TAG, "Error: " + e.getMessage());
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendEnviarColaboreRequest() {
        try{
            initVolleyListeners();

            JsonObjectRequest jsonObjectRequest = ColaboreRequester.prepareEnviarColaboreRequest(responListener, errorListener, colabore, getApplicationContext());
            jsonObjectRequest.setTag(getClass().getName());
            requestQueue.add(jsonObjectRequest);

        }catch(Exception e){
            Log.e(TAG, "Error: " + e.getMessage());
            stopSelf();
        }
    }

    private void initVolleyListeners() {
        responListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "Respoonse: " + response.toString());

                stopSelf();
            }
        };

        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.toString());

                stopSelf();
            }
        };
    }

}
