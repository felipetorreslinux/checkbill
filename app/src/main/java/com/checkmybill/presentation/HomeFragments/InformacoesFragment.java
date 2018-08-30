package com.checkmybill.presentation.HomeFragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.NetworkQualityAverageApi;
import com.checkmybill.entity.NetworkWifi;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.request.ObterMediaOperadorasRequester;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.service.TrafficMonitor;
import com.checkmybill.util.Connectivity;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@EFragment(R.layout.fragment_informacoes)
public class InformacoesFragment extends BaseFragment {

    private final static int ITEM_FILTER_HOJE = 0;
    private final static int ITEM_FILTER_7_DIAS = 1;
    private final static int ITEM_FILTER_30_Dias = 2;

    @ViewById(R.id.sp_filters_wifi)
    protected Spinner sp_filters_wifi;

    @ViewById(R.id.sp_filters_gsm)
    protected Spinner sp_filters_gsm;

    @ViewById(R.id.tvwGsmOperadora)
    protected TextView tvwGsmOperadora;

    @ViewById(R.id.tvwGsmSinal)
    protected TextView tvwGsmSinal;

    @ViewById(R.id.tvwUnavailability)
    protected TextView tvwUnavailability;

    @ViewById(R.id.tvwUnavailabilityLabel)
    protected TextView tvwUnavailabilityLabel;

    @ViewById(R.id.tvwGsmData)
    protected TextView tvwGsmData;

    @ViewById(R.id.tvwGsmDataLabel)
    protected TextView tvwGsmDataLabel;

    @ViewById(R.id.tvwWifiNetworkName)
    protected TextView tvwWifiNetworkName;

    @ViewById(R.id.tvwWifiSignal)
    protected TextView tvwWifiSignal;

    @ViewById(R.id.tvwWifiData)
    protected TextView tvwWifiData;

    @ViewById(R.id.tvwWifiDataLabel)
    protected TextView tvwWifiDataLabel;

    @ViewById(R.id.tvwWifiUnavailability)
    protected TextView tvwWifiUnavailability;

    @ViewById(R.id.tvwWifiUnavailabilityLabel)
    protected TextView tvwWifiUnavailabilityLabel;

    @ViewById(R.id.tvwWifiSpeedAverage)
    protected TextView tvwWifiSpeedAverage;

    @ViewById(R.id.tvwWifiSpeedAverageLabel)
    protected TextView tvwWifiSpeedAverageLabel;

    @ViewById(R.id.tvwGsmSinalMedia)
    protected TextView tvwGsmSinalMedia;

    @ViewById(R.id.tvwGSmUnavailabilityMedia)
    protected TextView tvwUnavailabilityMedia;

    @ViewById(R.id.tvwWifiSinalMedia)
    protected TextView tvwWifiSinalMedia;

    @ViewById(R.id.tvwWifiUnavailabilityMedia)
    protected TextView tvwWifiUnavailabilityMedia;

    @ViewById(R.id.tvwWifiISPName)
    protected TextView tvwWifiISPName;

    @ViewById(R.id.btnAvliePlano)
    protected Button btnAvliePlano;

    @ViewById(R.id.itemGsm)
    protected LinearLayout itemGsm;

    @ViewById(R.id.itemWifi)
    protected LinearLayout itemWifi;

    @ViewById(R.id.layoutMediaWifi)
    protected LinearLayout layoutMediaWifi;

    @ViewById(R.id.layoutMedia)
    protected LinearLayout layoutMedia;

    @ViewById(R.id.home_infoButton1)
    protected ImageView infoImgButton1;

    @ViewById(R.id.home_infoButton2)
    protected ImageView infoImgButton2;

    @ViewById(R.id.fragment_home_scrollview)
    protected ScrollView fragmentHomeScrollView;

    @ViewById(R.id.progressWifi)
    protected ProgressBar progressBarMediaWifi;

    @ViewById(R.id.progress)
    protected ProgressBar progressBarMediaGsm;

    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private MyLocationListener myLocationListener;
    private TelephonyManager tel;
    private MyPhoneStateListener myListener;

    private LatLng currentLatLng;

    @Override
    public void onStart(){
        LOG_TAG = getClass().getName();
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil( getContext() );
        Log.i(LOG_TAG, "Home Fragment: OnStart");
        super.onStart();
        requestQueue = Volley.newRequestQueue(getActivity());
        myLocationListener = new MyLocationListener();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.filters_indispo, R.layout.spinner_filters_analytics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //
        sp_filters_wifi.setAdapter(adapter);
        sp_filters_wifi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(LOG_TAG, "Filter Wifi");
                Date[] range = generateDateRangeFromPositionSpinner(position);
                LoadCardWifi loadCardWifi = new LoadCardWifi();
                loadCardWifi.execute(range[0], range[1]);
                sendRequestMediaOperadoras_Wifi(range[1], range[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //
        sp_filters_gsm.setAdapter(adapter);
        sp_filters_gsm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(LOG_TAG, "Filter GSM");
                Date[] range = generateDateRangeFromPositionSpinner(position);
                LoadCardGsm loadCardGsm = new LoadCardGsm();
                loadCardGsm.execute(range[0], range[1]);
                sendRequestMediaOperadoras_GSM(range[1], range[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //
        myListener = new MyPhoneStateListener();
        tel = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //
        if ( sharedPrefsUtil.getConfMob2g() == -1 ) sharedPrefsUtil.setConfMob2g(-105);
        if ( sharedPrefsUtil.getConfMob4g() == -1 ) sharedPrefsUtil.setConfMob4g(-125);
        if ( sharedPrefsUtil.getConfMobTimeUnavailability() == -1 ) sharedPrefsUtil.setConfMobTimeUnavailability(10000);
    }

    @Click
    public void itemGsm(){
        ((HomeActivity) getActivity()).changePage(HomeActivity.HomeTabNames.SINAL);
    }

    @Click
    public void itemWifi(){
        ((HomeActivity) getActivity()).changePage(HomeActivity.HomeTabNames.SINAL);
    }

    @Click
    public void btnAvliePlano(){
        /*((HomeActivity) getActivity()).changePage(2);
        Fragment planoFragment = ((HomeActivity) getActivity()).fragmentList.get(2);
        ((PlanoFragment)planoFragment).btnAvaliarPlano();*/
        //Intent it = new Intent(IntentMap.AVALIE_SEU_PLANO);
        //getActivity().startActivity(it);
    }

    @Click
    public void btnMediaRegiao(){
        if (layoutMedia.getVisibility() == View.VISIBLE) {
            layoutMedia.setVisibility(View.GONE);
        } else {
            layoutMedia.setVisibility(View.VISIBLE);
        }

        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
    }

    @Click
    public void btnMediaRegiaoWifi(){
        if (layoutMediaWifi.getVisibility() == View.VISIBLE) {
            layoutMediaWifi.setVisibility(View.GONE);
        } else {
            layoutMediaWifi.setVisibility(View.VISIBLE);
            // Não houve modo de fazer a nao ser esse, pois, quando é chamado o focus down
            // o objeto ainda não foi renderizado... Logo, vai para o bottom antes dele
            // existir, impedindo que seja exibido... O modo foi dar um pequeno delay para
            // sanar isso...
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragmentHomeScrollView.fullScroll(View.FOCUS_DOWN);
                }
            }, 300);
        }
    }

    @Click
    public void home_infoButton1(){
        NotifyWindow nw = new NotifyWindow(getContext());
        nw.getBuilder().setTitle("Ajuda");
        nw.getBuilder().setMessage("Mensagem de ajuda do item 1");
        nw.getBuilder().setIcon(R.mipmap.ic_info_blue);
        nw.getBuilder().setNeutralButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        nw.show();
        Log.d(LOG_TAG, "Show tooltip 1");
    }

    @Click
    public void home_infoButton2(){
        NotifyWindow nw = new NotifyWindow(getContext());
        nw.getBuilder().setTitle("Ajuda");
        nw.getBuilder().setMessage("Mensagem de ajuda do item 2 ");
        nw.getBuilder().setIcon(R.mipmap.ic_info_blue);
        nw.getBuilder().setNeutralButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        nw.show();
        Log.d(LOG_TAG, "Show tooltip 2");
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "OnR esume");
        super.onResume();
        tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        registerBroadcastNetworkReceiver();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        buildAlertMessageNoGps();

        // Obtendo os dados dos cards
        //LoadCardWifi loadCardWifi = new LoadCardWifi();
        //loadCardWifi.execute( sp_filters_wifi.getSelectedItemPosition() );
        //LoadCardGsm loadCardGsm = new LoadCardGsm();
        //loadCardGsm.execute( sp_filters_gsm.getSelectedItemPosition() );

    }

    private void registerBroadcastNetworkReceiver() {
        getActivity().registerReceiver(mNetworkReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if (!Connectivity.isConnectedWifi(getActivity())) {
            tvwWifiNetworkName.setText("n/a");
            tvwWifiSignal.setText("Desconectado");
            tvwWifiSignal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));
        }
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "Home Fragment: OnPause");
        super.onPause();
        tel.listen(myListener, PhoneStateListener.LISTEN_NONE);
        getActivity().unregisterReceiver(mNetworkReceiver);
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "PERMISSION ERROR");
        } else {
            locationManager.removeUpdates(myLocationListener);
        }

    }

    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            try {
                TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                //GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();

                tvwGsmOperadora.setText(telephonyManager.getNetworkOperatorName());

                String networkOperator = telephonyManager.getNetworkOperator();
                /*if (networkOperator.length() > 0) {
                    String mcc = networkOperator.substring(0, 3);
                    String mnc = networkOperator.substring(3);
                }*/

                int valueRssi;
                int valueASU;
                double valuePercent;

                int networkType = telephonyManager.getNetworkType();
                if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                    //4G
                    String[] arraySignalValues = signalStrength.toString().split(" ");
                    valueRssi = Integer.valueOf(arraySignalValues[9]);
                    valueASU = valueRssi + 140;

                    //3 a 95 ASU
                    valuePercent = (valueASU * 100) / 95;
                } else {
                    //2G & 3G
                    valueRssi = (signalStrength.getGsmSignalStrength() * 2) - 113;
                    valueASU = signalStrength.getGsmSignalStrength();

                    //0 a 31 ASU
                    valuePercent = (valueASU * 100) / 31;
                }
                //String.valueOf(valueRssi));
                //String.valueOf(valueASU));

                if (valuePercent >= 0 && valuePercent < 14.3) {
                    tvwGsmSinal.setText(getString(R.string.pessimo));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                } else if (valuePercent >= 14.3 && valuePercent < 28.6) {
                    tvwGsmSinal.setText(getString(R.string.muito_fraco));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                } else if (valuePercent >= 28.6 && valuePercent < 42.9) {
                    tvwGsmSinal.setText(getString(R.string.fraco));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                } else if (valuePercent >= 42.9 && valuePercent < 57.2) {
                    tvwGsmSinal.setText(getString(R.string.normal));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A700));
                } else if (valuePercent >= 57.2 && valuePercent < 71.5) {
                    tvwGsmSinal.setText(getString(R.string.bom));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A700));
                } else if (valuePercent >= 71.5 && valuePercent < 85.8) {
                    tvwGsmSinal.setText(getString(R.string.muito_bom));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_green_A700));
                } else if (valuePercent >= 85.8) {
                    tvwGsmSinal.setText(getString(R.string.excelente));
                    tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_green_A700));
                }

            } catch (RuntimeException e) {
                Log.i(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(e));
            }
        }
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean flagConnect = false;
            if (Connectivity.isConnected(getActivity())) {
                Log.e(LOG_TAG, "NETWORK CONNECT");
                if (Connectivity.isConnectedWifi(getActivity())) {
                    Log.e(LOG_TAG, "NETWORK WIFI");
                    flagConnect = true;
                }
            }

            if (flagConnect) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    tvwWifiNetworkName.setText(wifiInfo.getSSID().replaceAll("\\\"", ""));
                    int color = 0;
                    String label = "";
                    if (wifiInfo.getRssi() > -50) {
                        label = "Excelente";
                        color = R.color.md_green_A700;
                    } else if (wifiInfo.getRssi() <= -50 && wifiInfo.getRssi() > -60) {
                        label = "Bom";
                        color = R.color.md_blue_A700;
                    } else if (wifiInfo.getRssi() <= -60 && wifiInfo.getRssi() > -70) {
                        label = "Razoável";
                        color = R.color.md_blue_A700;
                    } else if (wifiInfo.getRssi() <= -70) {
                        label = "Fraco";
                        color = R.color.md_red_A700;
                    }
                    tvwWifiSignal.setText(label);
                    tvwWifiSignal.setTextColor(ContextCompat.getColor(getActivity(), color));
                }

            } else {
                tvwWifiNetworkName.setText("n/a");
                tvwWifiSignal.setText("Desconectado");
                tvwWifiSignal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));
            }
        }
    };

    private Date[] generateDateRangeFromPositionSpinner(int position) {
        DateTime startDate = new DateTime();
        DateTime endDate = new DateTime();
        switch (position) {
            //Hoje
            case ITEM_FILTER_HOJE:
                endDate = endDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                break;
            //Há 7 dias
            case ITEM_FILTER_7_DIAS:
                endDate = endDate.minusDays(7);
                endDate = endDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                break;
            //Há 30 dias
            case ITEM_FILTER_30_Dias:
                endDate = endDate.minusDays(30);
                endDate = endDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                break;
        }

        return new Date[]{startDate.toDate(), endDate.toDate()};
    }

    class LoadCardGsm extends AsyncTask<Date, Void, String> {

        private String unavailability;
        private String unavailabilityUnit;
        private String dataUse;
        private String dataUseUnit;

        protected String doInBackground(Date... range) {
            Log.d("LoadCardGsm", "Running -> doInBackground");
            calcularIndisponibilidade(range[0], range[1]);
            calcularUsoDeDados(range[0], range[1]);
            return null;
        }

        protected void onPostExecute(String feed) {
            try{
                try {
                    tvwUnavailability.setText(unavailability);
                    tvwUnavailabilityLabel.setText("Tempo indisponível (*" + unavailabilityUnit + ")");
                } catch(Exception e) {
                    e.printStackTrace();
                }

                try {
                    tvwGsmData.setText(dataUse);
                    tvwGsmDataLabel.setText("Dados consumidos");
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }catch(IllegalArgumentException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private void calcularIndisponibilidade(Date start, Date end) {
            try{
                List<Unavailability> listUnavailabilities = null;
                RuntimeExceptionDao<Unavailability, Integer> unavailabilitiesDao = OrmLiteHelper.getInstance(getActivity()).getUnavailabilityRuntimeDao();
                try {
                    listUnavailabilities = unavailabilitiesDao.queryBuilder()
                            .where().between("UNAVA_DATE_STARTED", end, start)
                            .and().eq("UNAVA_USED", true)
                            .query();
                    Log.d("LoadCardGSM", "Unavailability:" + String.valueOf(listUnavailabilities.size()));
                } catch (SQLException e) {
                    Log.e("LoadCardGSM", e.getMessage());
                }

                double minutes = 0;
                if (listUnavailabilities != null && !listUnavailabilities.isEmpty()) {
                    for (Unavailability unavailability : listUnavailabilities) {
                        DateTime dateInicio = new DateTime(unavailability.getDateStarted());
                        DateTime dateFim = new DateTime(unavailability.getDateFinished());

                        double secondsDif = (dateFim.getMillis() - dateInicio.getMillis()) / 1000;
                        double minutesDif = secondsDif / 60;
                        minutes += minutesDif;
                    }
                }

                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                if (minutes > 99.9) {
                    minutes /= 60;
                    unavailability = String.valueOf(decimalFormat.format(minutes));
                    unavailabilityUnit = "hrs";
                } else {
                    unavailability = String.valueOf(decimalFormat.format(minutes));
                    unavailabilityUnit = "mins";
                }
            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        private void calcularUsoDeDados(Date start, Date end) {
            TrafficMonitor trafficMonitor = new TrafficMonitor(getContext());
            long mobUsedBytes = trafficMonitor.getTotalMobileDataTransfer(start, end);
            dataUse = TrafficMonitor.FormatBytes(getContext(), mobUsedBytes);
        }
    }

    class LoadCardWifi extends AsyncTask<Date, Void, String> {

        private String unavailability;
        private String unavailabilityUnit;
        private String dataUse;
        private String speed;
        private String speedUnit;
        private String networkISPName;

        protected String doInBackground(Date... range) {
            Log.d("LoadCardWifi", "Running -> doInBackground");
            calcularIndisponibilidade(range[0], range[1]);
            calcularUsoDeDados(range[0], range[1]);
            calcularVelocidade(range[0], range[1]);
            obterNomeISP();
            return null;
        }

        protected void onPostExecute(String feed) {
            try{
                try {
                    tvwWifiUnavailability.setText(unavailability);
                    tvwWifiUnavailabilityLabel.setText("Tempo indisponível (*" + unavailabilityUnit + ")");
                } catch(Exception e) {
                    e.printStackTrace();
                }

                try {
                    tvwWifiData.setText(dataUse);
                    tvwWifiDataLabel.setText("Dados consumidos");
                } catch(Exception e) {
                    e.printStackTrace();
                }

                try {
                    tvwWifiSpeedAverage.setText(speed);
                    tvwWifiSpeedAverageLabel.setText("Velocidade média");
                } catch(Exception e) {
                    e.printStackTrace();
                }

                try {
                    tvwWifiISPName.setText(networkISPName);
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }catch(IllegalArgumentException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private void obterNomeISP() {
            try {
                // Indo no servidor e obtendo o nome do ISP da rede wireless atual
                Log.d(LOG_TAG, "Obtendo o nome do ISP");

                // Verificando se esta conectado na rede WiFi
                ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connManager.getActiveNetworkInfo();
                if (netInfo == null || !netInfo.isConnected() || netInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                    networkISPName = "Não Conectado";
                    return;
                }

                // Obtendo o nome da rede Wifi atualmente conectado
                URL url = null;
                try {
                    url = new URL(Util.getSuperUrlServiceObterNomePorvedor(getActivity()));
                    URLConnection urlConn = url.openConnection();

                    urlConn.setDoInput(true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    urlConn.connect();

                    // Preparando obtencao das repostas
                    InputStream inputa = urlConn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputa));
                    StringBuilder result = new StringBuilder();
                    String line;

                    // Obtendo conteudo e anexando ao StringBuilder...
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Convertendo a String no objeto JSon...
                    Log.d("HomeFragment JSON-RCV", result.toString());
                    JSONObject jo = new JSONObject(result.toString());

                    // Checando a resposta..
                    final String responseStatus = jo.getString("status");
                    if (!responseStatus.equalsIgnoreCase("success"))
                        throw new Exception("Error on Respose:" + jo.getString("message"));

                    // Obtendo o nome do ISP
                    networkISPName = jo.getString("isp");
                } catch (Exception e) {
                    networkISPName = "[Error]";
                    Log.d("HomeFragment Ex", Util.getMessageErrorFromExcepetion(e));
                }
            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        private void calcularIndisponibilidade(Date start, Date end) {
            try{
                int count = 0;
                List<NetworkQuality> networkQualities = null;
                RuntimeExceptionDao<NetworkQuality, Integer> dataUseeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getNetworkQualityRuntimeDao();
                try {
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", end, start)
                            .and()
                            .eq("NET_QUALI_LATENCY", 0)
                            .query();
                    count += networkQualities.size();
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", end, start)
                            .and()
                            .eq("NET_QUALI_DOWNLOAD", 0)
                            .query();

                    count += networkQualities.size();
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", end, start)
                            .and()
                            .eq("NET_QUALI_UPLOAD", 0)
                            .query();

                    count += networkQualities.size();
                    Log.i("LoadCardWifi", "COUNT INDIS WIFI: " + count);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                double minutes = count * 0.13;
                if (minutes != 0) {
                    //minutes = count/60;
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    if (minutes > 99.9) {
                        //minutes /= 60;
                        unavailability = String.valueOf(decimalFormat.format(minutes));
                        unavailabilityUnit = "hrs";
                    } else {
                        unavailability = String.valueOf(decimalFormat.format(minutes));
                        unavailabilityUnit = "mins";
                    }
                } else {
                    unavailability = "0";
                    unavailabilityUnit = "mins";
                }

            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        private void calcularUsoDeDados(Date start, Date end) {
            final TrafficMonitor trafficMonitor = new TrafficMonitor(getContext());
            long wifiUsedBytes = trafficMonitor.getTotalWifiDataTransfer(start, end);
            dataUse = TrafficMonitor.FormatBytes(getContext(), wifiUsedBytes);
            //dataUseUnit = usedValuesStrArray[1];
        }

        private void calcularVelocidade(Date start, Date end) {
            try{
                List<NetworkQuality> networkQualities = null;
                RuntimeExceptionDao<NetworkQuality, Integer> dataUseeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getNetworkQualityRuntimeDao();
                try {
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", end, start)
                            .query();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                double speedAverage = 0;
                int count = 0;
                for (NetworkQuality networkQuality : networkQualities) {
                    speedAverage += networkQuality.getDownload();
                    count++;
                }

                if (speedAverage != 0) {
                    speedAverage = speedAverage / count;

                    double kilobits = speedAverage / 1024;
                    double megabits = speedAverage / 1049179.35;

                    DecimalFormat mDecimalFormater = new DecimalFormat("#.##");
                    if (megabits < 1) {
                        speed = mDecimalFormater.format(kilobits);
                        speedUnit = "Kb/s";
                    } else {
                        speed = mDecimalFormater.format(megabits);
                        speedUnit = "Mb/s";
                    }
                } else {
                    speed = "0";
                    speedUnit = "Mb/s";
                }

            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Date[] rangeGSM = generateDateRangeFromPositionSpinner(sp_filters_gsm.getSelectedItemPosition());
            Date[] rangeWifi = generateDateRangeFromPositionSpinner(sp_filters_wifi.getSelectedItemPosition());
            sendRequestMediaOperadoras_GSM(rangeGSM[1], rangeGSM[0]);
            sendRequestMediaOperadoras_Wifi(rangeWifi[1], rangeWifi[0]);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private void buildAlertMessageNoGps() {
        if (!(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, myLocationListener);

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(getActivity(), "Gps desativado", Toast.LENGTH_SHORT).show();
            } else {
                progressBarMediaGsm.setVisibility(View.VISIBLE);
                progressBarMediaWifi.setVisibility(View.VISIBLE);
                //sendRequestMediaOperadoras_GSM();
                //sendRequestMediaOperadoras_Wifi();
            }
        }
    }

    private void sendRequestMediaOperadoras_GSM(Date start, Date end) {
        // Checando se existe uma posicao definida...
        if ( currentLatLng == null ) {
            Log.d(LOG_TAG, "GSM -> No Position Computed");
            return;
        }

        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "PERMISSION ERROR");
        } else {
            locationManager.removeUpdates(myLocationListener);
        }

        // Montando o JSON a ser enviado
        JSONObject jsonParam = new JSONObject();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            String nomeOperadora = (telephonyManager != null && telephonyManager.getNetworkOperatorName().length() > 0) ? telephonyManager.getNetworkOperatorName() : "";

            jsonParam.put("operadora_gsm", nomeOperadora);
            jsonParam.put("latitude", currentLatLng.latitude);
            jsonParam.put("longitude", currentLatLng.longitude);
            jsonParam.put("inicio_data", sdf.format(start));
            jsonParam.put("fim_data", sdf.format(end));
            Log.d(LOG_TAG, "MediaGSMParams -> " + jsonParam.toString());
        } catch (JSONException e) {
            new NotifyWindow(getContext()).showErrorMessage("Informações", "Erro requitiando dados de média", false);
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            return;
        }

        // Montando Volley Requester...
        Log.i(LOG_TAG, "MediaGSM SND -> " + jsonParam.toString());
        final JsonObjectRequest jsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(JsonObjectRequest.Method.POST, Util.getSuperUrlServiceObterMediaRegiao(getActivity()), jsonParam.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(LOG_TAG, "MediaGSM RCV -> " + response.toString());
                try {
                    // Checando resposta
                    NetworkQualityAverageApi networkQualityAverageApi = ObterMediaOperadorasRequester.getGsmInfoFromJsonResponse(response);
                    if ( networkQualityAverageApi != null ) {
                        int valuePercent = (Math.abs(networkQualityAverageApi.getSignal()) * 100) / 31;
                        if ( networkQualityAverageApi.getNumEntradas() == 0 ) {
                            tvwGsmSinalMedia.setText(getString(R.string.normal));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A700));
                        }
                        else if (valuePercent >= 0 && valuePercent < 14.3) {
                            tvwGsmSinalMedia.setText(getString(R.string.pessimo));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                        } else if (valuePercent >= 14.3 && valuePercent < 28.6) {
                            tvwGsmSinalMedia.setText(getString(R.string.muito_fraco));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                        } else if (valuePercent >= 28.6 && valuePercent < 42.9) {
                            tvwGsmSinalMedia.setText(getString(R.string.fraco));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                        } else if (valuePercent >= 42.9 && valuePercent < 57.2) {
                            tvwGsmSinalMedia.setText(getString(R.string.normal));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A700));
                        } else if (valuePercent >= 57.2 && valuePercent < 71.5) {
                            tvwGsmSinalMedia.setText(getString(R.string.bom));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A700));
                        } else if (valuePercent >= 71.5 && valuePercent < 85.8) {
                            tvwGsmSinalMedia.setText(getString(R.string.muito_bom));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_green_A700));
                        } else if (valuePercent >= 85.8) {
                            tvwGsmSinalMedia.setText(getString(R.string.excelente));
                            tvwGsmSinalMedia.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_green_A700));
                        }

                        String unavailabilityLabel;
                        if (networkQualityAverageApi.getUnavailability() > 0) {
                            unavailabilityLabel = PainelConsumoFragment.PConsumoUtils.getFormmatedSecondsToStrTime(networkQualityAverageApi.getUnavailability());
                        } else {
                            unavailabilityLabel = "0 mins";
                        }
                        tvwUnavailabilityMedia.setText(unavailabilityLabel);
                    }

                    progressBarMediaGsm.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (getActivity() != null) {
                        if (!Util.isConnectionTrue(getActivity())) {
                            Toast.makeText(getActivity(), getString(R.string.sem_conexao), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error_conexao), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBarMediaGsm.setVisibility(View.GONE);
                    progressBarMediaWifi.setVisibility(View.GONE);

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
                }
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

        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
    }

    private void sendRequestMediaOperadoras_Wifi(Date start, Date end) {
        // Checando se a conexao WiFi esta ativa...
        NetworkWifi currentWifiConnected = getCurrentWifiConnected();
        if ( currentWifiConnected == null ) {
            Log.d(LOG_TAG, "Wifi is disabled");
            return;
        }

        // Checando se existe uma posicao definida...
        if ( currentLatLng == null ) {
            Log.d(LOG_TAG, "WiFi -> No Position Computed");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG_TAG, "PERMISSION ERROR");
        } else {
            locationManager.removeUpdates(myLocationListener);
        }

        // Montando o JSON a ser enviado
        JSONObject jsonParam = new JSONObject();
        try {
            jsonParam.put("operadora_blarga", currentWifiConnected.getIsp());
            jsonParam.put("latitude", currentLatLng.latitude);
            jsonParam.put("longitude", currentLatLng.longitude);
            jsonParam.put("inicio_data", sdf.format(start));
            jsonParam.put("fim_data", sdf.format(end));
            Log.d(LOG_TAG, "MediaWifiParams -> " + jsonParam.toString());
        } catch (JSONException e) {
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            new NotifyWindow(getContext()).showErrorMessage("Informações", "Erro requitiando dados de média", false);
            return;
        }

        // Montando Volley Requester...
        Log.i(LOG_TAG, "MediaBLarga SND -> " + jsonParam.toString());
        final JsonObjectRequest jsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(JsonObjectRequest.Method.POST, Util.getSuperUrlServiceObterMediaRegiao(getActivity()), jsonParam.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(LOG_TAG, "MediaBLarga RCV -> " +response.toString());
                try {
                    NetworkQualityAverageApi networkQualityAverageApi = ObterMediaOperadorasRequester.getBLargaInfoFromJsonResponse(response);
                    if (networkQualityAverageApi != null) {
                        String unavailabilityLabel;
                        if (networkQualityAverageApi.getUnavailability() > 0) {
                            double minutes = networkQualityAverageApi.getUnavailability() / 60;
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            if (minutes > 99.9) {
                                //minutes /= 60;
                                unavailabilityLabel = String.valueOf(decimalFormat.format(minutes)) + " hrs";
                            } else {
                                unavailabilityLabel = String.valueOf(decimalFormat.format(minutes)) + " mins";
                            }
                        } else {
                            unavailabilityLabel = "0 mins";
                        }
                        tvwWifiUnavailabilityMedia.setText(unavailabilityLabel);
                        String speedAverageLabel;
                        double speedAverage = networkQualityAverageApi.getDownload();
                        if (networkQualityAverageApi.getDownload() != 0) {
                            double kilobits = speedAverage / 1024;
                            double megabits = speedAverage / 1049179.35;
                            DecimalFormat mDecimalFormater = new DecimalFormat("#.##");
                            if (megabits < 1) {
                                speedAverageLabel = mDecimalFormater.format(kilobits) + " Kb/s";
                            } else {
                                speedAverageLabel = mDecimalFormater.format(megabits) + " Mb/s";
                            }
                        } else {
                            speedAverageLabel = "0 Mb/s";
                        }
                        tvwWifiSinalMedia.setText(speedAverageLabel);
                    }

                    progressBarMediaWifi.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try{
                    if (getActivity() != null) {
                        if (!Util.isConnectionTrue(getActivity())) {
                            Toast.makeText(getActivity(), getString(R.string.sem_conexao), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.error_conexao), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBarMediaGsm.setVisibility(View.GONE);
                    progressBarMediaWifi.setVisibility(View.GONE);

                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
                }
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

        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
    }

    private NetworkWifi getCurrentWifiConnected() {
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(getActivity());
        NetworkWifi networkWifi = null;

        if (sharedPrefsUtil.getSfCurrentWifi() != -1) {
            RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getNetworkWifiRuntimeExceptionDao();

            try {
                List<NetworkWifi> networkWifis = networkWifiRuntimeExceptionDao.queryBuilder()
                        .where().eq("NET_WIF_ID", sharedPrefsUtil.getSfCurrentWifi()).query();
                Log.i("ServiWifiMonitor.class", "    ID WIFI:  " + sharedPrefsUtil.getSfCurrentWifi());

                if (!networkWifis.isEmpty()) {
                    networkWifi = networkWifis.get(0);
                }

            } catch (SQLException e) {
                Log.i("ServiWifiMonitor.class", "Error: " + Util.getMessageErrorFromExcepetion(e));
            } catch (RuntimeException e) {
                Log.i("ServiWifiMonitor.class", "Error: " + Util.getMessageErrorFromExcepetion(e));
            }
        }

        return networkWifi;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
        if(requestQueue!= null){
            requestQueue.cancelAll(getClass().getName());
        }
    }
}
