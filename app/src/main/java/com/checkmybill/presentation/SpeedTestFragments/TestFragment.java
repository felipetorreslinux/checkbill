package com.checkmybill.presentation.SpeedTestFragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.request.MultipartRequester;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.apache.http.HttpEntity;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@EFragment(R.layout.fragment_test)
public class TestFragment extends BaseFragment {

    private static final String TAG = "TestFragment";
    private static final int EXPECTED_SIZE_IN_BYTES = 1048576;//1MB 1024*1024

    private final static int UPDATE_THRESHOLD = 300;
    private static final double EDGE_THRESHOLD = 176.0;
    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private final int MSG_UPDATE_STATUS = 0;
    private final int MSG_UPDATE_CONNECTION_TIME = 1;
    private final int MSG_COMPLETE_STATUS = 2;

    private static final String ARG_SECTION_NUMBER = "section_number";

    @ViewById(R.id.tvwNomeTeste)
    protected TextView tvwNomeTeste;

    @ViewById(R.id.tvwValue)
    protected TextView tvwValue;

    @ViewById(R.id.tvwLatencia)
    protected TextView tvwLatencia;

    @ViewById(R.id.tvwDownload)
    protected TextView tvwDownload;

    @ViewById(R.id.tvwUpload)
    protected TextView tvwUpload;

    @ViewById(R.id.btnIniciar)
    protected Button btnIniciar;

    @ViewById(R.id.progress)
    protected ProgressBar progress;

    @ViewById(R.id.btnShowRegisters)
    protected FloatingActionButton btnShowRegisters;

    private DecimalFormat mDecimalFormater;
    private RequestQueue requestQueue;
    private NetworkQuality networkQuality;
    private LatencySpeedTest latencySpeedTest;
    private WifiManager.WifiLock lock;
    private long timeStart;
    private long txStart;
    private long start;

    @Click
    public void btnShowRegisters() {
        Intent it = new Intent(IntentMap.NETWORK_QUALITY);
        startActivity(it);
    }

    @Click
    public void btnIniciar() {
        startTest();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDecimalFormater = new DecimalFormat("##.##");
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

//        WifiInfo a = wifiManager.getConnectionInfo();
//        DhcpInfo b = wifiManager.getDhcpInfo();

        lock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "LockTag");
        lock.acquire();
    }

    @Override
    public void onPause() {
        super.onPause();
        lock.release();
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

                String downloadFileUrl = Util.getSuperUrlSpeedTestDownload(getActivity());
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
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            } catch (IOException e) {
                error = true;
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            } catch (RuntimeException e) {
                error = true;
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
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
            tvwNomeTeste.setText("Download");

        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);

            try {
                long bytespersecond = values[0];
                double kilobits = bytespersecond * BYTE_TO_KILOBIT;
                double megabits = kilobits * KILOBIT_TO_MEGABIT;

                if (kilobits < 1000) {
                    tvwValue.setText(mDecimalFormater.format(kilobits) + " Kbps");
                } else {
                    tvwValue.setText(mDecimalFormater.format(megabits) + " Mbps");
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error onProgressUpdate. maybe the activity has been destroyed", e);
            }
        }

        @Override
        protected synchronized void onPostExecute(Void params) {
            try {
                if (error) {
                    tvwDownload.setText("n/a");
                    if (!Util.isConnectionTrue(getActivity())) {
                        Toast.makeText(getActivity(), getString(R.string.error_conexao), Toast.LENGTH_SHORT).show();
                    }
                    networkQuality.setDownload(0);
                } else {
                    long bytespersecond = (bytesIn / downloadTime) * 1000;
                    double kilobits = bytespersecond * BYTE_TO_KILOBIT;
                    double megabits = kilobits * KILOBIT_TO_MEGABIT;

                    if (kilobits < 1000) {
                        tvwDownload.setText(mDecimalFormater.format(kilobits) + " Kbps");
                    } else {
                        tvwDownload.setText(mDecimalFormater.format(megabits) + " Mbps");
                    }

                    networkQuality.setDownload(bytespersecond);
                }

                // Inicializando uplaod do arquivo
                sendFileTestUpload();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
            }
        }
    }

    private class LatencySpeedTest extends AsyncTask<Void, Long, Void> {

        private String latencyValue;
        private double latency;
        private boolean error = false;
        private Process process;

        @Override
        protected synchronized Void doInBackground(Void... params) {
            Log.i(getClass().getName(), "executeCommand");
            monitorLatencyTest();
            try {
                for (int i = 0; i < 4; i++) {
                    try {
                        publishProgress();
                        process = Runtime.getRuntime().exec("ping -c 1 google.com");
                        int mExitValue = process.waitFor();
                        Log.i(getClass().getName(), " mExitValue " + mExitValue);

                        String result = getStringFromInputStream(process.getInputStream());
                        Log.i(getClass().getName(), " RESULT: " + result);

                        if (!result.contains("100% packet loss")) {
                            latencyValue = result.substring(result.indexOf("time=") + 5, result.indexOf("ms")).trim();
                            Log.i(getClass().getName(), " LatencyValue: " + latencyValue);
                        } else {
                            latencyValue = "0";
                        }

                        latency += Double.parseDouble(latencyValue);

                        process.destroy();
                    } catch (RuntimeException e) {
                        Log.i(getClass().getName(), " Error: " + Util.getMessageErrorFromExcepetion(e));
                    } finally {
                        process.destroy();
                    }
                }

                networkQuality.setLatency((int) latency);

                Log.i(getClass().getName(), " Latency: " + latency + " ms");

            } catch (InterruptedException ignore) {
                error = true;
                ignore.printStackTrace();
                Log.i(getClass().getName(), " Exception:" + Util.getMessageErrorFromExcepetion(ignore));
            } catch (IOException e) {
                error = true;
                e.printStackTrace();
                Log.i(getClass().getName(), " Exception:" + Util.getMessageErrorFromExcepetion(e));
            } catch (RuntimeException e) {
                error = true;
                e.printStackTrace();
                Log.i(getClass().getName(), " Exception:" + Util.getMessageErrorFromExcepetion(e));
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvwNomeTeste.setText("Latência");
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            Log.i("       UP:", "Progresso update");
        }

        @Override
        protected synchronized void onPostExecute(Void params) {
            try {
                if (latency == 0) {
                    tvwLatencia.setText("n/a");
                } else {
                    DecimalFormat decimalFormat = new DecimalFormat("#");
                    latencyValue = decimalFormat.format(latency / 4);

                    tvwLatencia.setText(latencyValue + " ms");
                }

                DownloadSpeedTest downloadSpeedTest = new DownloadSpeedTest();
                downloadSpeedTest.execute();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
            }
        }
    }

    private Response.Listener<NetworkResponse> uploadSuccessResponse = new Response.Listener<NetworkResponse>() {
        @Override
        public void onResponse(NetworkResponse response) {
            final String strResponse = new String( response.data );
            Log.d(LOG_TAG, "Response->" + strResponse);
        }
    };
    private Response.ErrorListener uploadErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    } ;

    private void sendFileTestUpload() {
        requestQueue = Volley.newRequestQueue(getContext());
        MultipartRequester uploadRequester;
        try {
            AssetFileDescriptor  assetFileDescriptor = getActivity().getAssets().openFd("5mb.rar");
            uploadRequester = RequesterUtil.createMultipartRequest(
                    Request.Method.POST,
                    Util.getSuperUrlServiceRealizarTesteUpload(getContext()),
                    assetFileDescriptor,
                    uploadSuccessResponse, uploadErrorListener);
            uploadRequester.setTag(TestFragment.class.getName());
            uploadRequester.setRetryPolicy(new RetryPolicy() {
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
        } catch ( IOException e ) {
            new NotifyWindow(getContext()).showErrorMessage("Teste de Velocidade", "Não foi possível realizar o testar de upload", false);
            return;
        }

        start = System.currentTimeMillis();
        timeStart = System.currentTimeMillis();
        txStart = TrafficStats.getTotalTxBytes();
        calculateUploadTest();

        tvwNomeTeste.setText("Upload");
        requestQueue.add(uploadRequester);
    }

    private void calculateUploadTest() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                try {

                    long TotalTxAfterTest = TrafficStats.getTotalTxBytes();
                    Log.d(LOG_TAG, "TX -> " + TotalTxAfterTest);
                    long AfterTime = System.currentTimeMillis();

                    long TimeDifference = AfterTime - timeStart;
                    long txDiff = TotalTxAfterTest - txStart;

                    if (txDiff != 0) {
                        double txBPS = (txDiff / TimeDifference) * 1000; // total tx bytes per second.

                        Log.i("____UPLOAD", String.valueOf(txBPS) + "B/s. Total tx = " + txDiff);

                        long bytespersecond = ((txDiff / TimeDifference) * 1000);
                        final double kilobits = bytespersecond * BYTE_TO_KILOBIT;
                        final double megabits = kilobits * KILOBIT_TO_MEGABIT;

                        try {
                            synchronized (this) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (kilobits < 1000) {
                                            tvwValue.setText(mDecimalFormater.format(kilobits) + " Kbps");
                                        } else {
                                            tvwValue.setText(mDecimalFormater.format(megabits) + " Mbps");
                                        }
                                    }
                                });

                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }

                        Log.i("____UPLOAD", "bs: " + bytespersecond);
                        Log.i("____UPLOAD", "Ks: " + kilobits);
                        Log.i("____UPLOAD", "Ms: " + megabits);

                    } else {
                        Log.i("____UPLOAD", "No uploaded or downloaded bytes.");
                    }

                    if (TimeDifference >= 8000) {
                        requestQueue.cancelAll(TestFragment.class);
                        requestQueue.stop();
                        //uploadSpeedTest.cancel(true);

                        Log.i("____UPLOAD", "FIMMMMMM: " + 8000);

//                        double txBPS = (txDiff / (TimeDifference / 1000)); // total tx bytes per second.
                        final long bytespersecond = ((txDiff / TimeDifference) * 1000);
                        final double kilobits = bytespersecond * BYTE_TO_KILOBIT;
                        final double megabits = kilobits * KILOBIT_TO_MEGABIT;


                        try {
                            synchronized (this) {

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (kilobits < 1000) {
                                            tvwUpload.setText(mDecimalFormater.format(kilobits) + " Kbps");
                                        } else {
                                            tvwUpload.setText(mDecimalFormater.format(megabits) + " Mbps");
                                        }

                                        btnIniciar.setEnabled(true);
                                        progress.setVisibility(View.GONE);

                                        networkQuality.setUpload(bytespersecond);

                                        endTest();
                                    }
                                });

                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }

                    } else {
                        calculateUploadTest();
                    }
                } catch (RuntimeException e) {
                    //tvwUpload.setText("n/a");
                    Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                    if (!Util.isConnectionTrue(getActivity())) {
                        Toast.makeText(getActivity(), getString(R.string.error_conexao), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, 300);
    }

    private void startTest() {
        if (Util.isConnectionTrue(getActivity())) {
            networkQuality = new NetworkQuality();

            tvwNomeTeste.setText("");
            tvwValue.setText("-");
            tvwUpload.setText("-");
            tvwDownload.setText("-");
            tvwLatencia.setText("-");

            btnIniciar.setEnabled(false);
            progress.setVisibility(View.VISIBLE);

            latencySpeedTest = new LatencySpeedTest();
            latencySpeedTest.execute();
        } else {
            new NotifyWindow(getContext()).showErrorMessage("Teste de Velocidade", getString(R.string.error_conexao), false);
        }
    }

    private void endTest() {
        try {

            if (!(networkQuality.getLatency() == 0 && networkQuality.getUpload() == 0 && networkQuality.getDownload() == 0)) {
                networkQuality.setDate(new DateTime().toDate());

                TelephonyManager tManager = (TelephonyManager) getActivity().getBaseContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);
                String carrierName = tManager.getNetworkOperatorName();
                networkQuality.setName(carrierName);

                WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

                if (wifiManager.isWifiEnabled()) {
                    if (Util.isWifiConnectionTrue(getActivity())) {
                        networkQuality.setName(wifiManager.getConnectionInfo().getSSID());
                    }
                }

                RuntimeExceptionDao<NetworkQuality, Integer> networkQualityRuntimeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getNetworkQualityRuntimeDao();
                networkQualityRuntimeExceptionDao.create(networkQuality);
            }
        } catch (RuntimeException e) {
            //nothing
        }

    }

    public class MyRequest extends Request<String> {
        private Response.Listener<String> mListener;
        private Response.ErrorListener mEListener;
        private HttpEntity entity;
        private int cacheTimeToLive = 0;

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return super.getHeaders();
        }

        public MyRequest(int method, String url, Response.ErrorListener listener, Response.Listener<String> mListener, HttpEntity entity) {
            super(method, url, listener);
            this.mListener = mListener;
            this.entity = entity;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                // Volley does not handle null properly, so implement null response
                // check
                if (response.data.length == 0) {
                    byte[] responseData = "{}".getBytes("UTF8");
                    response = new NetworkResponse(response.statusCode,responseData, response.headers, response.notModified);
                }

                String jsonString = new String(response.data,HttpHeaderParser.parseCharset(response.headers));
                return Response.success(jsonString,parseIgnoreCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                System.out.println("VolleyQueue: Encoding Error for " + getTag()
                        + " (" + getSequence() + ")");
                return Response.error(new ParseError(e));
            }
        }

        @Override
        public String getBodyContentType() {
            return entity.getContentType().getValue();
        }

        @Override
        public void deliverError(VolleyError error) {
            super.deliverError(error);
        }

        @Override
        protected void deliverResponse(String response) {
            mListener.onResponse(response);
        }

        public Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
            long now = System.currentTimeMillis();

            Map<String, String> headers = response.headers;
            long serverDate = 0;
            String serverEtag = null;
            String headerValue;

            headerValue = headers.get("Date");
            if (headerValue != null) {
                serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
            }
            serverEtag = headers.get("ETag");

            final long cacheHitButRefreshed =15 * 60 * 1000; //Fifteen Minutes
            final long cacheExpired = cacheTimeToLive;
            final long softExpire = now + cacheHitButRefreshed;
            final long ttl = now + cacheExpired;

            Cache.Entry entry = new Cache.Entry();
            entry.data = response.data;
            entry.etag = serverEtag;
            entry.softTtl = softExpire;
            entry.ttl = ttl;
            entry.serverDate = serverDate;
            entry.responseHeaders = headers;

            return entry;
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
}
