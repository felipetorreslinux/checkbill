package com.checkmybill.presentation.HomeFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.adapters.home.AdapterIndisDetailTest;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.IndisponibilidadeDetail;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.presentation.AvaliaPlanoActivity;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.ReclameAquiActivity;
import com.checkmybill.request.MedicoesRequester;
import com.checkmybill.service.TrafficMonitor;
import com.checkmybill.testview.datause.AdapterDataUseTest;
import com.checkmybill.testview.datause.DataUseTestFragment;
import com.checkmybill.util.Connectivity;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author: Petrus A. (R@g3)
 * Date: 30/06/2017
 */

@EFragment(R.layout.fragment_sinal)
public class SinalFragment extends BaseFragment implements View.OnClickListener {

    @ViewById(R.id.scrollview_sinal) ScrollView scrollview_sinal;
    // Indisponibilidade
    @ViewById(R.id.sp_filters_indisp) Spinner spFilterIndisp;
    @ViewById(R.id.sinal_indisp_gsm_container) LinearLayout sinalIndispGSMContainer;
    @ViewById(R.id.sinal_indisp_wifi_container) LinearLayout sinalIndispWiFiContainer;
    @ViewById(R.id.sinal_indisp_gsm_dispositivo) TextView sinalIndispGSMDispostivo;
    @ViewById(R.id.sinal_indisp_wifi_dispositivo) TextView sinalIndispWiFiDispositivo;
    @ViewById(R.id.sinal_indisp_gsm_regiao) TextView sinalIndispGSMRegiao;
    @ViewById(R.id.sinal_indisp_wifi_regiao) TextView sinalIndispWiFiRegiao;
    @ViewById(R.id.btnMediaRegiao_Indisp) Button btnMediaRegiaoIndisp;

    // Sinal & Dados
    @ViewById(R.id.sp_filters_dados) Spinner spFilterDados;
    @ViewById(R.id.sinal_dados_gsm_container) LinearLayout sinalDadosGSMContainer;
    @ViewById(R.id.sinal_dados_wifi_container) LinearLayout sinalDadosWiFiContainer;
    @ViewById(R.id.sinal_dados_gsm_rx) TextView sinalDadosGSMRX;
    @ViewById(R.id.sinal_dados_wifi_rx) TextView sinalDadosWiFiRX;
    @ViewById(R.id.tvwGsmSinal) TextView tvwGsmSinal;
    @ViewById(R.id.tvwGsmOperadora) TextView tvwGsmOperadora;
    @ViewById(R.id.tvwWifiSpeedAverage) TextView tvwWifiSpeedAverage;
    @ViewById(R.id.tvwWifiISPName) TextView tvwWifiISPName;
    @ViewById(R.id.sinal_gsm_regiao) TextView sinalGSMRegiao;
    @ViewById(R.id.sinal_wifi_regiao) TextView sinalWiFiRegiao;
    @ViewById(R.id.btnMediaRegiao_Sinal) Button btnMediaRegiaoSinal;

    // Dinamic Container
    @ViewById(R.id.containerMediaRegiao_Indisp) LinearLayout containerMediaRegiaoIndisp;
    @ViewById(R.id.containerMediaRegiao_Sinal) LinearLayout containerMediaRegiaoSinal;

    // Filter data today, 30 days, 60 days, my plan and total
    @ViewById(R.id.btn_filter_today_1) Button btn_filter_today_1;
    @ViewById(R.id.btn_filter_7_2) Button btn_filter_7_2;
    @ViewById(R.id.btn_filter_30_2) Button btn_filter_30_2;
    @ViewById(R.id.btn_filter_60_3) Button btn_filter_60_3;
    @ViewById(R.id.btn_filter_plan_4) Button btn_filter_plan_4;
    @ViewById(R.id.btn_filter_total_5) Button btn_filter_total_5;
    @ViewById(R.id.labelFilterDate) TextView labelFilterDate;

    // Felipe Torres
    // 12/09/2018
    @ViewById(R.id.txtTodaySinal) TextView txtTodaySinal;
    @ViewById(R.id.txtSevenDaySinal) TextView txtSevenDaySinal;
    @ViewById(R.id.txtThirtyDay) TextView txtThirtyDay;
    @ViewById(R.id.txtSixtyDay) TextView txtSixtyDay;

    //Anatel
    @ViewById(R.id.textViewConsultaAnatel) TextView textViewConsultaAnatel;

    //Reclame aqui
    @ViewById(R.id.btnReclameAqui) Button btnReclameAqui;

    @ViewById(R.id.indisponibilidadeMain) TextView indisponibilidadeMain;
    @ViewById(R.id.indisponibilidadeMainVariable) TextView indisponibilidadeMainVariable;

    @ViewById(R.id.indisponibilidadeMainRegion) TextView indisponibilidadeMainRegion;
    @ViewById(R.id.indisponibilidadeMainRegionVariable) TextView indisponibilidadeMainRegionVariable;

    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManagerRecyclerView;
    @ViewById(R.id.alertLogin) LinearLayout alertLogin;
    private boolean isLogged;
    @ViewById(R.id.alertDetailNull) LinearLayout alertDetailNull;
    @ViewById(R.id.layoutListDetail) LinearLayout layoutListDetail;
    @ViewById(R.id.noLoggedArea_DoLoginBtn) Button noLoggedArea_DoLoginBtn;

    private boolean deviceInAirplaneMode;
    private MyPhoneStateListener myPhoneStateListener;
    private TelephonyManager telManager;
    private RequestQueue requestQueue;

    //victor
    private Date sD;
    private Date eD;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestQueue = Volley.newRequestQueue(getContext());
        deviceInAirplaneMode = Settings.System.getInt(getContext().getContentResolver(),Settings.System.AIRPLANE_MODE_ON,0) != 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getContext().unregisterReceiver(airplaneModeReceive);
        }catch(RuntimeException e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // verificando se o usuário está logado
        isLogged();

        // Inicializando text view anatel
        textViewConsultaAnatel.setClickable(true);
        textViewConsultaAnatel.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://www.anatel.gov.br/consumidor/telefonia-celular/direitos/interrupcao-do-servico'> Consulte aqui </a>";
        textViewConsultaAnatel.setText(Html.fromHtml(text));

        // Inicializando spinners...
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.filters_indispo, R.layout.spinner_filters_analytics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterIndisp.setAdapter(adapter);
        spFilterIndisp.setOnItemSelectedListener(this.spinnerIndisp_SelectListener);
        spFilterDados.setAdapter(adapter);
        spFilterDados.setOnItemSelectedListener(this.spinnerDados_SelectListener);

        mLayoutManagerRecyclerView = new LinearLayoutManager(getActivity());
        setUpRecyclerView();

        PopulateNetworkCard();

        //Preparando datas
        DateTime auxD = new DateTime();
        auxD = auxD.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        sD = auxD.toDate();
        auxD = new DateTime();
        eD = auxD.toDate();
        setLabelFilterDate();

        IndisponibilidadeAsyncClass indispClass = new IndisponibilidadeAsyncClass();
        indispClass.execute();

        // Registrando receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        getContext().registerReceiver(airplaneModeReceive, intentFilter);

        myPhoneStateListener = new MyPhoneStateListener();
        telManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        btnFilterToday1();
    }

    private void isLogged(){
        final String accessKey = new SharedPrefsUtil(getActivity()).getAccessKey();
        Log.e("Sinal Fragment", "isLogged: " + accessKey);
        if(accessKey != null  && accessKey.length() > 0){
            isLogged = true;
            alertLogin.setVisibility(View.GONE);
        }else{
            isLogged = false;
            alertLogin.setVisibility(View.VISIBLE);
        }
    }

    private void setUpRecyclerView() {
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManagerRecyclerView);
    }

    // ---------------------------------------------------------------------------------------------
    // METODOS PRIVADOS (USO INTERNO)
    private Date[] GetPeriodFilterDateRange(int position) {
        DateTime startDate = new DateTime();
        DateTime endDate = new DateTime();
        switch (position) {
            // Hoje
            case 0:
                startDate = startDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                break;
            // Últimos 7 dias
            case 1:
                startDate = startDate.minusDays(7);
                startDate = startDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                break;
            // Últimos 30 dias
            case 2:
                startDate = startDate.minusDays(30);
                startDate = startDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
                break;
        }

        return new Date[]{startDate.toDate(), endDate.toDate()};
    }

    private void PopulateNetworkCard() {
        Date[] dateRange = GetPeriodFilterDateRange(spFilterDados.getSelectedItemPosition());
        TrafficMonitor trafficMonitor = new TrafficMonitor(getContext());
        long mobUsedBytes = trafficMonitor.getTotalMobileDataTransfer(dateRange[0], dateRange[1]);
        long wifiUsedBytes = trafficMonitor.getTotalWifiDataTransfer(dateRange[0], dateRange[1]);

        sinalDadosGSMRX.setText(TrafficMonitor.FormatBytes(getContext(), mobUsedBytes));
        sinalDadosWiFiRX.setText(TrafficMonitor.FormatBytes(getContext(), wifiUsedBytes));

        this.obterNomeISP();
        this.calcularVelocidade(dateRange[0], dateRange[1]);
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
                    tvwWifiSpeedAverage.setText(String.format(Locale.getDefault(), "%.2f Kb/s", kilobits));
                } else {
                    tvwWifiSpeedAverage.setText(String.format(Locale.getDefault(), "%.2f Mb/s", megabits));
                }
            } else {
                tvwWifiSpeedAverage.setText("0 Mb/s");
            }

        }catch (RuntimeException e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }
    }

    private void obterNomeISP() {
        try {
            // Indo no servidor e obtendo o nome do ISP da rede wireless atual
            Log.d(LOG_TAG, "Obtendo o nome do ISP");

            // Verificando se esta conectado na rede WiFi
            ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connManager.getActiveNetworkInfo();
            if (netInfo == null || !netInfo.isConnected() || netInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                tvwWifiISPName.setText("Não Conectado");
                tvwWifiSpeedAverage.setText("Não Conectado");
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
                String sIsp = jo.getString("isp");
                tvwWifiISPName.setText(sIsp);
            } catch ( Exception e ) {
                Log.d("HomeFragment Ex", Util.getMessageErrorFromExcepetion(e));
            }
        }catch (RuntimeException e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }
    }

    @Click(R.id.noLoggedArea_DoLoginBtn)
    public void btnNoLogged() {
        Intent it = new Intent(IntentMap.INTRO);
        it.putExtra("HIDDEN_JUMP_BUTTON", true);
        startActivity(it);
    }

    @Click(R.id.btnReclameAqui)
    public void btnReclameAquiClick() {
        Intent it = new Intent(IntentMap.RECLAME_AQUI);
        getActivity().startActivityForResult(it, ReclameAquiActivity.REQUEST_CODE);
    }

    @Click(R.id.txtTodaySinal)
    public void btnFilterToday1() {
        DateTime auxD = new DateTime();
        auxD = auxD.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        sD = auxD.toDate();

        auxD = new DateTime();
        eD = auxD.toDate();

        setMenuFilter(btn_filter_today_1);

        txtTodaySinal.setBackgroundColor(getResources().getColor(R.color.primary));
        txtTodaySinal.setTextColor(getResources().getColor(R.color.colorWhite));

        txtSevenDaySinal.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtSevenDaySinal.setTextColor(getResources().getColor(R.color.primary));

        txtThirtyDay.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtThirtyDay.setTextColor(getResources().getColor(R.color.primary));

        txtSixtyDay.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtSixtyDay.setTextColor(getResources().getColor(R.color.primary));

        scrollview_sinal.fullScroll(ScrollView.FOCUS_UP);

    }

    @Click(R.id.txtSevenDaySinal)
    public void btnFilter72() {

        DateTime auxSd = new DateTime();
        auxSd = auxSd.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        auxSd = auxSd.minusDays(7);
        sD = auxSd.toDate();

        DateTime auxEd = new DateTime();
        eD = auxEd.toDate();

        setMenuFilter(btn_filter_7_2);

        txtTodaySinal.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtTodaySinal.setTextColor(getResources().getColor(R.color.primary));

        txtSevenDaySinal.setBackgroundColor(getResources().getColor(R.color.primary));
        txtSevenDaySinal.setTextColor(getResources().getColor(R.color.colorWhite));

        txtThirtyDay.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtThirtyDay.setTextColor(getResources().getColor(R.color.primary));

        txtSixtyDay.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtSixtyDay.setTextColor(getResources().getColor(R.color.primary));

        scrollview_sinal.fullScroll(ScrollView.FOCUS_UP);
    }

    @Click(R.id.txtThirtyDay)
    public void btnFilter302() {
        DateTime auxSd = new DateTime();
        auxSd = auxSd.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        auxSd = auxSd.minusDays(30);
        sD = auxSd.toDate();

        DateTime auxEd = new DateTime();
        eD = auxEd.toDate();

        setMenuFilter(btn_filter_30_2);

        txtTodaySinal.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtTodaySinal.setTextColor(getResources().getColor(R.color.primary));

        txtSevenDaySinal.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtSevenDaySinal.setTextColor(getResources().getColor(R.color.primary));

        txtThirtyDay.setBackgroundColor(getResources().getColor(R.color.primary));
        txtThirtyDay.setTextColor(getResources().getColor(R.color.colorWhite));

        txtSixtyDay.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtSixtyDay.setTextColor(getResources().getColor(R.color.primary));

        scrollview_sinal.fullScroll(ScrollView.FOCUS_UP);
    }

    @Click(R.id.txtSixtyDay)
    public void btnFilter603() {
        DateTime auxSd = new DateTime();
        auxSd = auxSd.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
        auxSd = auxSd.minusDays(60);
        sD = auxSd.toDate();

        DateTime auxEd = new DateTime();
        eD = auxEd.toDate();

        setMenuFilter(btn_filter_60_3);

        txtTodaySinal.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtTodaySinal.setTextColor(getResources().getColor(R.color.primary));

        txtSevenDaySinal.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtSevenDaySinal.setTextColor(getResources().getColor(R.color.primary));

        txtThirtyDay.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        txtThirtyDay.setTextColor(getResources().getColor(R.color.primary));

        txtSixtyDay.setBackgroundColor(getResources().getColor(R.color.primary));
        txtSixtyDay.setTextColor(getResources().getColor(R.color.colorWhite));

        scrollview_sinal.fullScroll(ScrollView.FOCUS_UP);

    }

    @Click(R.id.btn_filter_plan_4)
    public void btnFilterPlan4() {
        setMenuFilter(btn_filter_plan_4);
    }

    @Click(R.id.btn_filter_total_5)
    public void btnFilterTotal5() {
        setMenuFilter(btn_filter_total_5);
    }

    public void setMenuFilter(View v){
        btn_filter_today_1.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_7_2.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_30_2.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_60_3.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_plan_4.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_total_5.setBackgroundResource(R.drawable.btn_custom_border);

        v.setBackgroundResource(R.drawable.button_green_shape);
        setLabelFilterDate();

        IndisponibilidadeAsyncClass indispClass = new IndisponibilidadeAsyncClass();
        indispClass.execute();
    }

    public void setLabelFilterDate(){
        String label = "";
        DateTime sDtTime = new DateTime(sD);
        DateTime eDtTime = new DateTime(eD);
        if(sD.compareTo(eD) != 0){
            label = "De " + sDtTime.toDateTime().toString("dd/MM/yy") + " até " + eDtTime.toDateTime().toString("dd/MM/yy");
        }else{
            label = "Em " + sDtTime.toDateTime().toString("dd/MM/yy");
        }
        labelFilterDate.setText(label);

    }

    private void showMediaRegiaoIndisp() {
        // Verificando se esta no modo Aviao
        if ( deviceInAirplaneMode ) {
            new NotifyWindow(getContext()).showErrorMessage("Média da Região", "O Dispositivo está no modo avião, desative este modo e tente novamente.", false);
            return;
        }

        // Exibindo painel e iniciando requisicao
        containerMediaRegiaoIndisp.setVisibility(View.VISIBLE);
        //final Date[] dateRange = GetPeriodFilterDateRange(spFilterIndisp.getSelectedItemPosition());
        final Date[] dateRange = new Date[]{sD, eD};

        String nomeOperadoraGSM = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperatorName = telephonyManager.getNetworkOperatorName();
            if (networkOperatorName == null || networkOperatorName.length() == 0) {
                networkOperatorName = "";
            }
            nomeOperadoraGSM = networkOperatorName;
        }catch(RuntimeException e){
            //nothing
        }


        final String nomeOperadoraWiFi = (Connectivity.isConnectedWifi(getContext())) ? tvwWifiISPName.getText().toString() : "";

        // Montando requisicao
        JsonObjectRequest request = MedicoesRequester.prepareInfoIndispRegiaoRequest(dateRange, nomeOperadoraGSM, nomeOperadoraWiFi, mediaRegiaoIndispResp, null, getContext());
        request.setTag(getClass().getName() + "_indis_region");

        // Iniciando requisicao
        requestQueue.cancelAll(getClass().getName() + "_indis_region");
        requestQueue.add(request);
    }

    private void showMediaRegiaoSinal() {
        // Verificando se esta no modo Aviao
        if ( deviceInAirplaneMode ) {
            new NotifyWindow(getContext()).showErrorMessage("Média da Região", "O Dispositivo está no modo avião, desative este modo e tente novamente.", false);
            return;
        }

        // Exibindo painel e iniciando requisicao
        containerMediaRegiaoSinal.setVisibility(View.VISIBLE);
        final Date[] dateRange = GetPeriodFilterDateRange(spFilterDados.getSelectedItemPosition());
        final String nomeOperadoraGSM = tvwGsmOperadora.getText().toString();
        final String nomeOperadoraWiFi = (Connectivity.isConnectedWifi(getContext())) ? tvwWifiISPName.getText().toString() : "";

        // Montando requisicao
        JsonObjectRequest request = MedicoesRequester.prepareInfoSinalRegiaoRequest(dateRange, nomeOperadoraGSM, nomeOperadoraWiFi, mediaRegiaoSinalResp, null, getContext());
        request.setTag(getClass().getName());

        // Iniciando requisicao
        requestQueue.add(request);
    }

    private void showIndispDetail() {
        if(isLogged){
            final Date[] dateRange = new Date[]{sD, eD};
            JsonObjectRequest request = MedicoesRequester.prepareIndispDetailRequest(dateRange, indispDetailResp, null, getContext());
            request.setTag(getClass().getName() + "_indis_detail");

            // Iniciando requisicao
            // requestQueue.cancelAll(getClass().getName() + "_indis_detail");
            requestQueue.add(request);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // LISTENERS
    @Click(R.id.btnMediaRegiao_Indisp)
    public void btnMediaRegiao_IndispClickEvent() {
        // Checando se deve ocultar
        if ( containerMediaRegiaoIndisp.getVisibility() == View.VISIBLE ) {
            btnMediaRegiaoIndisp.setText("Exibir Dados da Região");
            containerMediaRegiaoIndisp.setVisibility(View.GONE);
            return;
        }

        // Checando se esta em conexao WiFi
        if ( !Connectivity.isConnectedWifi(getContext()) ) {
            new NotifyWindow(getContext()).showConfirmMessage("Média da Região","Este recurso ira consumir dados do seu plano, deseja continuar?","Não", "Sim",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            btnMediaRegiaoIndisp.setText("Ocultar Dados da Região");
                            showMediaRegiaoIndisp();
                        }
                }
            );
        } else {
            showMediaRegiaoIndisp();
        }
    }

    @Click(R.id.btnMediaRegiao_Sinal)
    public void btnMediaRegiao_SinalClickEvent() {
        // Checando se deve ocultar
        if ( containerMediaRegiaoSinal.getVisibility() == View.VISIBLE ) {
            btnMediaRegiaoSinal.setText("Exibir Dados da Região");
            containerMediaRegiaoSinal.setVisibility(View.GONE);
            return;
        }

        // Checando se esta em conexao WiFi
        if ( !Connectivity.isConnectedWifi(getContext()) ) {
            new NotifyWindow(getContext()).showConfirmMessage("Média da Região","Este recurso ira consumir dados do seu plano, deseja continuar?","Não", "Sim",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            btnMediaRegiaoSinal.setText("Ocultar Dados da Região");
                            showMediaRegiaoSinal();
                        }
                    }
            );
        } else {
            showMediaRegiaoSinal();
        }
    }

    private Response.Listener<JSONObject> mediaRegiaoSinalResp = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.i(LOG_TAG, "mediaRegiaoSinalResp RCV -> " +response.toString());
            try {
                // Checando reposta
                if ( !response.getString("status").equalsIgnoreCase("success") || response.isNull("gsm") ) {
                    Log.d(LOG_TAG, response.getString("message"));
                    sinalGSMRegiao.setText("Erro");
                    sinalWiFiRegiao.setText("Erro");
                    sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                    sinalWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                    return;
                }

                // Obtendo valores/resposta
                int sinalGsm = response.getInt("gsm");
                long txDownloadBLarga = response.getJSONObject("blarga").getLong("download");
                long txUploadBLarga = response.getJSONObject("blarga").getLong("download");

                // Definindo os valores
                if ( sinalGsm >= -70 ) {
                    sinalGSMRegiao.setText("Ótimo");
                    sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_green_A700));
                } else if ( sinalGsm >= -80 ) {
                    sinalGSMRegiao.setText("Bom");
                    sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A400));
                } else if ( sinalGsm >= -100 ) {
                    sinalGSMRegiao.setText("Normal");
                    sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A400));
                } else if ( sinalGsm >= -110 ) {
                    sinalGSMRegiao.setText("Ruim");
                    sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_900));
                } else {
                    sinalGSMRegiao.setText("Péssimo");
                    sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                }

                // Taxa de BlandaLarga (se conectado)
                if ( Connectivity.isConnectedWifi(getContext()) ) {
                    if (txDownloadBLarga > 0 && txUploadBLarga > 0) {
                        sinalWiFiRegiao.setText("Normal");
                        sinalWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_blue_A700));
                    } else {
                        sinalWiFiRegiao.setText("Ruim");
                        sinalWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                    }
                } else {
                    sinalWiFiRegiao.setText("[OFF]");
                    sinalWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));
                }
            } catch ( Exception ex ) {
                Util.getMessageErrorFromExcepetion(ex);
                sinalGSMRegiao.setText("Erro");
                sinalWiFiRegiao.setText("Erro");
                sinalGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                sinalWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
            }
        }
    };

    private Response.Listener<JSONObject> mediaRegiaoIndispResp = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.i(LOG_TAG, "mediaRegiaoIndispResp RCV -> " + response.toString());
            try {
                // Checando reposta
                if (!response.getString("status").equalsIgnoreCase("success")) {
                    Log.d(LOG_TAG, response.getString("message"));
                    sinalIndispGSMRegiao.setText("Erro");
                    sinalIndispWiFiRegiao.setText("Erro");
                    sinalIndispGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                    sinalIndispWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));

                    //indisponibilidadeMainRegion.setText("E!");
                    //indisponibilidadeMainRegion.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));

                    return;
                }

                // Tratando as respostas
                long indispGsm = response.getLong("gsm");
                long indispWiFi = 0;
                if (!response.isNull("blarga")) {
                    indispWiFi = response.getLong("blarga");
                }

                // Definindo texto do tempo...
                sinalIndispGSMRegiao.setText(PainelConsumoFragment.PConsumoUtils.getFormmatedSecondsToStrTime(indispGsm));
                sinalIndispWiFiRegiao.setText(PainelConsumoFragment.PConsumoUtils.getFormmatedSecondsToStrTime(indispWiFi));
                sinalIndispGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));
                sinalIndispWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));

                double seconds = indispGsm;
                String variable = "";
                if (seconds < 60) {
                    variable = " segundo(s)";
                } else if (seconds >= 60) {
                    double minutes = seconds / 60;
                    if (minutes < 60) {
                        variable = "minuto(s)";
                    } else {
                        double hours = minutes / 60;
                        variable = "hora(s)";
                    }
                }

                //indisponibilidadeMainRegion.setText(String.format(Locale.getDefault(), "%.0f", seconds));
                //indisponibilidadeMainRegionVariable.setText(variable);


            } catch (Exception ex) {
                Util.getMessageErrorFromExcepetion(ex);
                try{
                    sinalIndispGSMRegiao.setText("Erro");
                    sinalIndispWiFiRegiao.setText("Erro");
                    sinalIndispGSMRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                    sinalIndispWiFiRegiao.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));

                    //indisponibilidadeMainRegion.setText("Erro");
                    //indisponibilidadeMainRegion.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_red_A700));
                } catch (Exception exTwo) {
                    Util.getMessageErrorFromExcepetion(exTwo);
                }
            }
        }
    };

    private Response.Listener<JSONObject> indispDetailResp = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            //response = getMockIndisDetail();
            Log.i(LOG_TAG, "indispDetailResp RCV -> " + response.toString());
            try {
                // Checando reposta
                if (!response.getString("status").equalsIgnoreCase("success")) {
                    Log.d(LOG_TAG, response.getString("message"));
                    return;
                }

                List<IndisponibilidadeDetail> indisponibilidadeDetails = new ArrayList<>();
                JSONObject aux = response.getJSONObject("data");
                JSONArray ar = aux.getJSONArray("usuario_data");
                for (int i = 0; i < ar.length(); i++) {
                    IndisponibilidadeDetail indis = new IndisponibilidadeDetail();
                    indis.setTime("");
                    indis.setNeigh(ar.getJSONObject(i).getString("bairro"));
                    indis.setUna(ar.getJSONObject(i).getInt("segundos_indisponivel"));
                    indisponibilidadeDetails.add(indis);
                }

                //Indisponibilidade da região
                if(!indisponibilidadeDetails.isEmpty()){
                    AdapterIndisDetailTest adapterIndisDetailTest = (AdapterIndisDetailTest) recyclerView.getAdapter();
                    adapterIndisDetailTest = new AdapterIndisDetailTest(getActivity(), indisponibilidadeDetails, R.layout.list_indis_detail, null);
                    recyclerView.setAdapter(adapterIndisDetailTest);

                    alertDetailNull.setVisibility(View.GONE);
                    layoutListDetail.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else{
                    layoutListDetail.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    alertDetailNull.setVisibility(View.VISIBLE);
                }


                double seconds = aux.getInt("total_segundos_indisponivel_regiao");
                String variable = "";
                if (seconds < 60) {
                    variable = " segundo(s)";
                } else if (seconds >= 60) {
                    double minutes = seconds / 60;
                    if (minutes < 60) {
                        variable = "minuto(s)";
                        seconds = minutes;
                    } else {
                        double hours = minutes / 60;
                        variable = "hora(s)";
                        seconds = hours;
                    }
                }

                indisponibilidadeMainRegion.setText(String.format(Locale.getDefault(), "%.0f", seconds));
                indisponibilidadeMainRegionVariable.setText(variable);

            } catch (Exception ex) {
                Util.getMessageErrorFromExcepetion(ex);
            }
        }
    };

    private AdapterView.OnItemSelectedListener spinnerIndisp_SelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(LOG_TAG, "Filter 'Indisponibilidade'");
            IndisponibilidadeAsyncClass indispClass = new IndisponibilidadeAsyncClass();
            indispClass.execute();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    };

    private AdapterView.OnItemSelectedListener spinnerDados_SelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d(LOG_TAG, "Filter 'Consumo de Dados'");
            PopulateNetworkCard();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    };

    // Broadcast, Airplane Mode
    private BroadcastReceiver airplaneModeReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Is AirPlan mode
            deviceInAirplaneMode = intent.getBooleanExtra("state", false);
            if ( deviceInAirplaneMode ) {
                tvwGsmSinal.setText("???");
                tvwGsmOperadora.setText(getString(R.string.aviao));
                tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));
            }
        }
    };

    // Felipe Torres
    // 12/09/2018

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.txtTodaySinal:
                IndisponibilidadeAsyncClass indispClass = new IndisponibilidadeAsyncClass();
                indispClass.execute();
                break;
        }
    }

    // PhoneStateListener Class
    private class MyPhoneStateListener extends PhoneStateListener {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            // Checando se esta no modo Aviao
            if ( deviceInAirplaneMode ) {
                tvwGsmSinal.setText("???");
                tvwGsmOperadora.setText(getString(R.string.aviao));
                tvwGsmSinal.setTextColor(ContextCompat.getColor(getActivity(), R.color.md_black_1000));
                return;
            }

            try {
                TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                String networkOperatorName = telephonyManager.getNetworkOperatorName();
                if(networkOperatorName == null || networkOperatorName.length() == 0){
                    networkOperatorName = "Operadora não identificada";
                }
                tvwGsmOperadora.setText(networkOperatorName);
                /*String networkOperator = telephonyManager.getNetworkOperator();
                if (networkOperator.length() > 0) {
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

    // ---------------------------------------------------------------------------------------------
    // ASYNC-TASKs (Dados de indisponibilidade)
    private class IndisponibilidadeAsyncClass extends AsyncTask<Void, Void, Boolean> {
        private Date[] dateRange;
        private double gsmLepMinutes, wifiLepMinutes;
        @Override
        protected void onPreExecute() {
            dateRange = GetPeriodFilterDateRange(spFilterIndisp.getSelectedItemPosition());
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            gsmLepMinutes = ObterIndispGSM_InSecs(sD, eD);
            wifiLepMinutes = ObterIndispWiFi_InSecs(dateRange[0], dateRange[1]);

            return (gsmLepMinutes >= 0 && wifiLepMinutes >= 0);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if ( !aBoolean ) {
                Log.e(LOG_TAG, "Erro obendo indisponibilidade");
                return;
            }

            String variable = "";
            double value = gsmLepMinutes;
            if (value < 60) {
                variable = " segundo(s)";
            } else if (value >= 60) {
                double minutes = value / 60;
                if (minutes < 60) {
                    value = minutes;
                    variable = "minuto(s)";
                } else {
                    double hours = minutes / 60;
                    value = hours;
                    variable = "hora(s)";
                }
            }

            indisponibilidadeMain.setText(String.format(Locale.getDefault(), "%.0f", value));
            indisponibilidadeMainVariable.setText(variable);

            gsmLepMinutes = (gsmLepMinutes > 0) ? (gsmLepMinutes / 60) : gsmLepMinutes;
            // Definindo valores com base no tipo recebido
            if ( gsmLepMinutes > 99.9 ) {
                gsmLepMinutes /= 60;
                sinalIndispGSMDispostivo.setText(String.format(Locale.getDefault(), "%.02f hrs", gsmLepMinutes));
            } else {
                sinalIndispGSMDispostivo.setText(String.format(Locale.getDefault(), "%.02f mins", gsmLepMinutes));
            }

            if ( wifiLepMinutes > 99.9 ) {
                wifiLepMinutes /= 60;
                sinalIndispWiFiDispositivo.setText(String.format(Locale.getDefault(), "%.02f hrs", wifiLepMinutes));
            } else {
                sinalIndispWiFiDispositivo.setText(String.format(Locale.getDefault(), "%.02f mins", wifiLepMinutes));
            }

            showMediaRegiaoIndisp();
            showIndispDetail();

            //sinalIndispGSMDispostivo.setText(getFormmatedSecondsToStrTime(gsmLepSecods));
            //sinalIndispWiFiDispositivo.setText(getFormmatedSecondsToStrTime(wifiLepSeconds));
        }

        private double ObterIndispWiFi_InSecs(Date start, Date end) {
            double elapsedMinutes;
            try{
                int count = 0;
                List<NetworkQuality> networkQualities;
                RuntimeExceptionDao<NetworkQuality, Integer> dataUseeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getNetworkQualityRuntimeDao();
                try {
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", start, end)
                            .and()
                            .eq("NET_QUALI_LATENCY", 0)
                            .query();
                    count += networkQualities.size();
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", start, end)
                            .and()
                            .eq("NET_QUALI_DOWNLOAD", 0)
                            .query();

                    count += networkQualities.size();
                    networkQualities = dataUseeExceptionDao.queryBuilder()
                            .where().between("NET_QUALI_DATE", start, end)
                            .and()
                            .eq("NET_QUALI_UPLOAD", 0)
                            .query();

                    count += networkQualities.size();
                    Log.i("LoadCardWifi", "COUNT INDIS WIFI: " + count);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                double minutes = count * 0.13;
                elapsedMinutes = minutes;
            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                elapsedMinutes = -1;
            }

            return elapsedMinutes;
        }

        private double ObterIndispGSM_InSecs(Date start, Date end) {
            double elapsedSeconds;
            try {
                elapsedSeconds = 0;
                List<Unavailability> listUnavailabilities;
                RuntimeExceptionDao<Unavailability, Integer> unavailabilitiesDao = OrmLiteHelper.getInstance(getActivity()).getUnavailabilityRuntimeDao();
                listUnavailabilities = unavailabilitiesDao.queryBuilder()
                        .where().between("UNAVA_DATE_STARTED", start, end)
                        .and().eq("UNAVA_USED", true)
                        .query();

                if (listUnavailabilities != null && !listUnavailabilities.isEmpty()) {
                    for (Unavailability unavailability : listUnavailabilities) {
                        DateTime dateInicio = new DateTime(unavailability.getDateStarted());
                        DateTime dateFim = new DateTime(unavailability.getDateFinished());

                        double secondsDif = (dateFim.getMillis() - dateInicio.getMillis()) / 1000;
                        elapsedSeconds += secondsDif;
                    }
                }
            }catch (Exception e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                elapsedSeconds = -1;
            }

            //return (elapsedSeconds > 0) ? (elapsedSeconds / 60) : elapsedSeconds;
            return elapsedSeconds;
        }
    }

    private JSONObject getMockIndisDetail(){
        // criar array 1 - usuario_data
        JSONArray usuario_data = new JSONArray();

        JSONObject ud1 = new JSONObject();
        try{
            ud1.put("uf", "PE");
            ud1.put("nome_estado", "Pernambuco");
            ud1.put("cidade", "Recife");
            ud1.put("bairro", "Boa Viagem");
            ud1.put("segundos_indisponivel", 30);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        JSONObject ud2 = new JSONObject();
        try{
            ud2.put("uf", "PE");
            ud2.put("nome_estado", "Pernambuco");
            ud2.put("cidade", "Recife");
            ud2.put("bairro", "Ilha do Leite");
            ud2.put("segundos_indisponivel", 10);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        JSONObject ud3 = new JSONObject();
        try{
            ud3.put("uf", "PE");
            ud3.put("nome_estado", "Pernambuco");
            ud3.put("cidade", "Recife");
            ud3.put("bairro", "Casa Forte");
            ud3.put("segundos_indisponivel", 8);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        try{
            usuario_data.put(ud1);
            usuario_data.put(ud2);
            usuario_data.put(ud3);
        } catch ( RuntimeException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        // criar array 2 - regiao_data
        JSONArray regiao_data = new JSONArray();

        JSONObject rd1 = new JSONObject();
        try{
            rd1.put("uf", "PE");
            rd1.put("nome_estado", "Pernambuco");
            rd1.put("cidade", "Recife");
            rd1.put("bairro", "Boa Viagem");
            rd1.put("segundos_indisponivel", 10);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        try{
            regiao_data.put(rd1);
        } catch ( RuntimeException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        // criar data & add arrays
        JSONObject data = new JSONObject();
        try{
            data.put("usuario_data", usuario_data);
            data.put("regiao_data", regiao_data);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        //criar object main & add data + variaveis
        JSONObject main = new JSONObject();
        try{
            main.put("status", "success");
            main.put("data", data);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
        }

        //return
        return main;
    }

}
