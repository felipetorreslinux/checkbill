package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.adapters.AdapterRecargas;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.adapters.avaliaplano.AdapterAvaliaPlano;
import com.checkmybill.adapters.home.AdapterIndisDetailTest;
import com.checkmybill.entity.IndisponibilidadeDetail;
import com.checkmybill.entity.Plano;
import com.checkmybill.entity.RecargasPlano;
import com.checkmybill.presentation.HomeFragments.SinalFragment;
import com.checkmybill.request.MedicoesRequester;
import com.checkmybill.request.PlanoRequester;
import com.checkmybill.util.DatePickers;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

@EActivity(R.layout.activity_avalia_plano)
public class AvaliaPlanoActivity extends BaseActivity {

    @ViewById(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;

    public static final int REQUEST_CODE = 1221;
    private Context mContext;
    private ProgressDialog loadingWindow;

    // Filter data today, 30 days, 60 days, my plan and total
    @ViewById(R.id.btn_filter_today_1) Button btn_filter_today_1;
    @ViewById(R.id.btn_filter_30_2) Button btn_filter_30_2;
    @ViewById(R.id.btn_filter_60_3) Button btn_filter_60_3;
    @ViewById(R.id.btn_filter_plan_4) Button btn_filter_plan_4;

    @ViewById(R.id.layoutList) LinearLayout layoutList;
    @ViewById(R.id.layoutNull) LinearLayout layoutNull;
    @ViewById(R.id.layoutLoad) LinearLayout layoutLoad;
    @ViewById(R.id.layoutErro) LinearLayout layoutErro;

    private RequestQueue requestQueue;

    private List<Plano> planos;

    @ViewById(R.id.recyclerView) RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManagerRecyclerView;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_TAG = getClass().getName();
    }

    @Override
    public void onStart() {
        super.onStart();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try{
                    layoutList.setVisibility(View.GONE);
                    layoutNull.setVisibility(View.GONE);
                    layoutLoad.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(true);
                } catch (Exception e) {
                    Util.getMessageErrorFromExcepetion(e);
                }
                startServiceAvalia();
            }
        });

        this.requestQueue = Volley.newRequestQueue(this);

        mLayoutManagerRecyclerView = new LinearLayoutManager(this);
        setUpRecyclerView();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                layoutList.setVisibility(View.GONE);
                layoutNull.setVisibility(View.GONE);
                layoutLoad.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        startServiceAvalia();
    }

    private void setUpRecyclerView() {
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManagerRecyclerView);
    }

    @Override
    public void onStop() {
        super.onStop();
        setResult( RESULT_CANCELED );
        finish();
    }

    private void startServiceAvalia(){
        JsonObjectRequest request = PlanoRequester.prepareAvaliaPlanoUsuarioRequest(analisaPlanoResp, null, this);
        request.setTag(getClass().getName() + "_service_avalia");

        // Iniciando requisicao
        requestQueue.cancelAll(getClass().getName() + "_service_avalia");
        requestQueue.add(request);
    }

    private Response.Listener<JSONObject> analisaPlanoResp = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.i(LOG_TAG, "analisaPlanoResp RCV -> " + response.toString());
            layoutLoad.setVisibility(View.GONE);
            try {
                // Checando reposta
                /*
                if (!response.getString("status").equalsIgnoreCase("success")) {
                    Log.d(LOG_TAG, response.getString("message"));
                    return;
                }*/

                /*
                *   nome do plano ----------- nome_plano
                    tipo do plano ----------- descricao_modalidade_plano
                    preço ----------- valor_plano
                    dados ----------- limite_net
                * */

                JSONArray data = response.getJSONArray("data");
                planos = new ArrayList<>();
                if(data.length() > 0){
                    layoutList.setVisibility(View.VISIBLE);
                    for (int i = 0; i < data.length(); i++) {
                        Plano plano = new Plano();
                        JSONObject job = data.getJSONObject(i);
                        try{
                            plano.setNomePlano(job.getString("nome_plano"));
                            plano.setDescricaoModalidadePlano(job.getString("descricao_modalidade_plano"));
                            plano.setValorPlano(Float.valueOf(String.valueOf(job.getDouble("valor_plano"))));
                            plano.setLimiteDadosWeb(job.getLong("limite_net"));
                        } catch ( JSONException ex ) {
                            Log.e(LOG_TAG, "error: " + Util.getMessageErrorFromExcepetion(ex));
                        }

                        planos.add(plano);
                    }

                    AdapterAvaliaPlano adapterAvaliaPlano = (AdapterAvaliaPlano) recyclerView.getAdapter();
                    adapterAvaliaPlano = new AdapterAvaliaPlano(AvaliaPlanoActivity.this, planos, R.layout.list_item_avaliaplano, null);
                    recyclerView.setAdapter(adapterAvaliaPlano);

                }else{
                    layoutNull.setVisibility(View.VISIBLE);
                    Toast.makeText(AvaliaPlanoActivity.this, "Vazio", Toast.LENGTH_LONG);
                }

            } catch (Exception ex) {
                layoutErro.setVisibility(View.VISIBLE);
                Util.getMessageErrorFromExcepetion(ex);
            }

            try{
                swipeRefreshLayout.setRefreshing(false);
            } catch (Exception e) {
                Util.getMessageErrorFromExcepetion(e);
            }
        }
    };

    @Click(R.id.btn_filter_today_1)
    public void btnFilterToday1() {
        setMenuFilter(btn_filter_today_1);
    }

    @Click(R.id.btn_filter_30_2)
    public void btnFilter302() {
        setMenuFilter(btn_filter_30_2);
    }

    @Click(R.id.btn_filter_60_3)
    public void btnFilter603() {
        setMenuFilter(btn_filter_60_3);
    }

    @Click(R.id.btn_filter_plan_4)
    public void btnFilterPlan4() {
        setMenuFilter(btn_filter_plan_4);
    }

    public void setMenuFilter(View v){
        btn_filter_today_1.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_30_2.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_60_3.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_plan_4.setBackgroundResource(R.drawable.btn_custom_border);

        v.setBackgroundResource(R.drawable.button_green_shape);
    }
}
