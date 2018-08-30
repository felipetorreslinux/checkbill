package com.checkmybill.service;

import android.app.Service;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.NetworkWifi;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Victor Guerra on 10/12/2015.
 */
public class ServiceNetworkQuality extends Service {

    private final static int UPDATE_THRESHOLD = 300;
    private static final double EDGE_THRESHOLD = 176.0;
    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private SharedPrefsUtil sharedPrefsUtil;
    private RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao;
    private NetworkWifi networkWifi;
    private NetworkQuality networkQuality;
    private LatencySpeedTest latencySpeedTest;

    private long timeStart;
    private long txStart;
    private long start;
    private RequestQueue requestQueue;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPrefsUtil = new SharedPrefsUtil(getApplicationContext());
        networkQuality = new NetworkQuality();

        if (sharedPrefsUtil.getSfCurrentWifi() != -1) {
            networkWifiRuntimeExceptionDao = OrmLiteHelper.getInstance(getApplicationContext()).getNetworkWifiRuntimeExceptionDao();

            try {

                List<NetworkWifi> networkWifis = networkWifiRuntimeExceptionDao.queryBuilder()
                        .where().eq("NET_WIF_ID", sharedPrefsUtil.getSfCurrentWifi()).query();

                Log.i("ServiWifiMonitor.class", "    ID WIFI:  " + sharedPrefsUtil.getSfCurrentWifi());

                networkWifi = networkWifis.get(0);

                if (networkWifis == null) {
                    stopSelf();
                } else {
                    latencySpeedTest = new LatencySpeedTest();
                    latencySpeedTest.execute();
                }

            } catch (SQLException e) {
                Log.i("ServiWifiMonitor.class", "Error: " + Util.getMessageErrorFromExcepetion(e));
                stopSelf();
            } catch (RuntimeException e) {
                Log.i("ServiWifiMonitor.class", "Error: " + Util.getMessageErrorFromExcepetion(e));
                stopSelf();
            }

        } else {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class LatencySpeedTest extends AsyncTask<Void, Long, Void> {

        private String latencyValue;
        private boolean error = false;
        private Process process;

        @Override
        protected synchronized Void doInBackground(Void... params) {
            Log.i("    LOKO", "executeCommand");
            monitorLatencyTest();
            try {
                double resultPreview = 0;
                for (int i = 0; i < 4; i++) {
                    publishProgress();
                    process = Runtime.getRuntime().exec("ping -c 1 google.com");
                    int mExitValue = process.waitFor();
                    Log.i("    LOKO", " mExitValue " + mExitValue);

                    String result = getStringFromInputStream(process.getInputStream());
                    Log.i("    LOKO", " RESULTTTTTTT: " + result);

                    if (!result.contains("100% packet loss")) {
                        latencyValue = result.substring(result.indexOf("time=") + 5, result.indexOf("ms")).trim();
                        Log.i("    LOKO", " RESULTTTTTTT: " + latencyValue);
                    } else {
                        latencyValue = "0";
                    }

                    resultPreview += Double.parseDouble(latencyValue);

                    process.destroy();
                }

                DecimalFormat decimalFormat = new DecimalFormat("#");
                latencyValue = decimalFormat.format(resultPreview / 5);
                Log.i("    LOKO", " RESULTTTTTTT: " + latencyValue);

            } catch (InterruptedException ignore) {
                error = true;
                ignore.printStackTrace();
                Log.i("    LOKO", " Exception:" + ignore);
            } catch (IOException e) {
                error = true;
                e.printStackTrace();
                Log.i("    LOKO", " Exception:" + e);
            } catch (RuntimeException e) {
                error = true;
                e.printStackTrace();
                Log.i("    LOKO", " Exception:" + e);
            }

            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            Log.i("       UP:", "Progresso update");
        }

        @Override
        protected synchronized void onPostExecute(Void params) {

            if (error) {
                networkQuality.setLatency(0);
                stopSelf();
            } else {
                networkQuality.setLatency(Integer.parseInt(latencyValue));
                DownloadSpeedTest downloadSpeedTest = new DownloadSpeedTest();
                downloadSpeedTest.execute();
            }
        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private File inputToFile(InputStream inputStream) {
        OutputStream outputStream = null;

        String outFileName = getApplicationContext().getFilesDir().getPath() + "/fileTestUpload.rar";
        File file = new File(outFileName);

        try {
            outputStream =
                    new FileOutputStream(file);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            System.out.println("Done!");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return file;
    }

    private void monitorLatencyTest() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                try {

                    try {
                        synchronized (this) {

                            if (latencySpeedTest != null && latencySpeedTest.getStatus().equals(AsyncTask.Status.RUNNING)) {
                                //latencySpeedTest.cancel(true);
                                latencySpeedTest.process.destroy();
                                Log.i("    MONITOR", "Cancelled");
                            }

                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }


                } catch (RuntimeException e) {

                }
            }
        }, 7000);
    }

    private class DownloadSpeedTest extends AsyncTask<Void, Long, Void> {

        private int bytesIn = 0;
        private int bytesInThreshold = 0;
        private long updateDelta = 0;
        private long downloadTime = 0;
        private boolean error = false;


        @Override
        protected synchronized Void doInBackground(Void... params) {
            InputStream stream = null;
            try {

                String downloadFileUrl = Util.getSuperUrlSpeedTestDownload(getApplicationContext());
                long startCon = System.currentTimeMillis();
                URL url = new URL(downloadFileUrl);
                URLConnection con = url.openConnection();
                con.setUseCaches(false);
//                long connectionLatency = System.currentTimeMillis() - startCon;
                stream = con.getInputStream();

                long start = System.currentTimeMillis();

                long updateStart = System.currentTimeMillis();
                updateDelta = 0;

                int currentByte = 0;

                while ((currentByte = stream.read()) != -1) {
                    bytesIn++;
                    bytesInThreshold++;
                    if (updateDelta >= UPDATE_THRESHOLD) {

                        long bytespersecond = (bytesInThreshold / updateDelta) * 1000;
                        publishProgress(bytespersecond);

                        //Reset
                        updateStart = System.currentTimeMillis();
                        bytesInThreshold = 0;
                    }
                    updateDelta = System.currentTimeMillis() - updateStart;

                    if (System.currentTimeMillis() - start >= 8000) {
                        break;
                    }

                }

                downloadTime = (System.currentTimeMillis() - start);

                if (downloadTime == 0) {
                    downloadTime = 1;
                }
            } catch (MalformedURLException e) {
                error = true;
                Log.e(getClass().getName(), Util.getMessageErrorFromExcepetion(e));
            } catch (IOException e) {
                error = true;
                Log.e(getClass().getName(), Util.getMessageErrorFromExcepetion(e));
            } catch (RuntimeException e) {
                error = true;
                Log.e(getClass().getName(), Util.getMessageErrorFromExcepetion(e));
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException e) {
                    //Suppressed
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected synchronized void onPostExecute(Void params) {

            if (error) {
                if (!Util.isConnectionTrue(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_conexao), Toast.LENGTH_SHORT).show();
                }
                networkQuality.setDownload(0);
            } else {
                long bytespersecond = (bytesIn / downloadTime) * 1000;
//                double kilobits = bytespersecond * BYTE_TO_KILOBIT;
//                double megabits = kilobits * KILOBIT_TO_MEGABIT;

                networkQuality.setDownload(bytespersecond);
            }
            sendFileTestUpload();
        }
    }

    private void sendFileTestUpload() {
        File file = null;
        try {
            file = inputToFile(getApplicationContext().getAssets().open("5mb.rar"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("rb", file);

        final HttpEntity httpEntity = builder.build();

        MyRequest myRequest = new MyRequest(Request.Method.POST, "https://content.dropboxapi.com/2/files/upload", new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    Toast.makeText(getApplicationContext(), jsonString, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json;
                Log.i(getClass().getName(), error.toString() + error.getNetworkTimeMs() + Util.getMessageErrorFromExcepetion(error));

                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    switch (response.statusCode) {
                        case 400:
                            json = new String(response.data);
                            Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG).show();
                            break;
                    }

                    Log.i(getClass().getName(), "response MyRequest: " + response.toString() + " / code: " + response.statusCode + " / data: " + new String(response.data).toString());
                } else {
                    Log.d("ServiceNetworkQuality", "is_something_wrong");
                    //Toast.makeText(getActivity(), "is_something_wrong", Toast.LENGTH_LONG).show();
                }

                networkQuality.setUpload(0);
                endTest();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/octet-stream";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer EvMg5QR0vUIAAAAAAAAAQZZCB4vwBlFSiRfLKPeQdVK-j3Mr3Bx9hpjpvh_-E82y");
                headers.put("Dropbox-API-Arg", "{\"path\":\"/rashid.rar\"}");
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    httpEntity.writeTo(bos);
                } catch (IOException e) {
                    VolleyLog.e("IOException writing to ByteArrayOutputStream");
                }
                return bos.toByteArray();
            }
        };

        myRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        myRequest.setTag(ServiceNetworkQuality.class);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        start = System.currentTimeMillis();
        timeStart = System.currentTimeMillis();
        txStart = TrafficStats.getTotalTxBytes();
        calculateUploadTest();

        requestQueue.add(myRequest);
    }

    private void calculateUploadTest() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                try {

                    long TotalTxAfterTest = TrafficStats.getTotalTxBytes();
                    long AfterTime = System.currentTimeMillis();

                    long TimeDifference = AfterTime - timeStart;
                    long txDiff = TotalTxAfterTest - txStart;

                    if (TimeDifference >= 8000) {
                        requestQueue.cancelAll(ServiceNetworkQuality.class);
                        requestQueue.stop();
                        Log.i("____UPLOAD", "FIMMMMMM: " + 8000);

//                        double txBPS = (txDiff / (TimeDifference / 1000)); // total tx bytes per second.
                        final long bytespersecond = ((txDiff / TimeDifference) * 1000);
                        final double kilobits = bytespersecond * BYTE_TO_KILOBIT;
//                        final double megabits = kilobits * KILOBIT_TO_MEGABIT;

                        networkQuality.setUpload(bytespersecond);
                        endTest();
                    } else {
                        calculateUploadTest();
                    }
                } catch (RuntimeException e) {
                    endTest();
                }
            }
        }, 300);
    }

    public class MyRequest extends Request<NetworkResponse> {

        public MyRequest(int method, String url, Response.Listener<NetworkResponse> networkResponseListener, Response.ErrorListener listener) {
            super(method, url, listener);
        }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            Log.i(getClass().getName(), "response MyRequest: " + response.toString() + " / code: " + response.statusCode);
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            Log.i(getClass().getName(), "response MyRequest: " + response.toString() + " / code: " + response.statusCode);

            if (response.statusCode == 200) {
                try {
                    JSONObject jsonObjectResult = new JSONObject(new String(response.data));
                    Log.d("ServiceNetworkQuality", jsonObjectResult.toString());
                } catch (JSONException e) {
                    Log.e("ServiceNetworkQuality", "ERROR", e);
                }
            }

            Log.i("    TIME", "time: " + (System.currentTimeMillis() - start));
        }
    }

    private void endTest() {
        RuntimeExceptionDao<NetworkQuality, Integer> networkQualityRuntimeDao = OrmLiteHelper.getInstance(getApplicationContext()).getNetworkQualityRuntimeDao();
        networkQuality.setNetworkWifi(networkWifi);
        networkQuality.setDate(new DateTime().toDate());
        networkQuality.setName(networkWifi.getSsID());


        Log.i("    RESULT", "WIFI: " + networkWifi.toString());

        Log.i("    RESULT", "QUALITY: " + networkQuality.toString());

        networkQualityRuntimeDao.update(networkQuality);
        //networkQuality.setId(idResult);

        Collection<NetworkQuality> networkQualiites = networkWifi.getNetworkQualiites();
        if (networkQualiites == null) {
            networkQualiites = new ArrayList<>();
        }
        networkQualiites.add(networkQuality);
        networkWifiRuntimeExceptionDao.update(networkWifi);

        Log.i("    RESULT", "Network quality create");
        Log.i("    RESULT", "latency: " + networkQuality.getLatency());
        Log.i("    RESULT", "download: " + networkQuality.getDownload());
        Log.i("    RESULT", "upload: " + networkQuality.getUpload());

        Log.i("    RESULT", "network: " + networkWifi.getSsID());

        stopSelf();
    }

}