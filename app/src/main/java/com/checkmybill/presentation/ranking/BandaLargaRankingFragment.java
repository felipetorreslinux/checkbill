package com.checkmybill.presentation.ranking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.adapters.ranking.AdapterRankingBandaLarga;
import com.checkmybill.adapters.ranking.AdapterRankingGsm;
import com.checkmybill.entity.ranking.BandaLargaRankingItem;
import com.checkmybill.entity.ranking.GsmRankingItem;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.request.RankingRequester;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

@EFragment(R.layout.fragment_ranking_banda_larga)
public class BandaLargaRankingFragment extends BaseFragment {

    @ViewById(R.id.list) protected RecyclerView list;
    @ViewById(R.id.tvwMessage) protected TextView tvwMessage;
    @ViewById(R.id.progress) protected ProgressBar progress;

    private RequestQueue requestQueue;
    private Response.Listener responseListener;
    private Response.ErrorListener erroListener;

    private String area;
    private String exibicao;
    private String inicioData;
    private String fimData;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public BandaLargaRankingFragment() {
    }

    public static BandaLargaRankingFragment newInstance(int sectionNumber) {
        BandaLargaRankingFragment fragment = new BandaLargaRankingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        LOG_TAG = "BandaLargaRankingFragment";

        initVolleyLinsteners();

        Intent it = getActivity().getIntent();
        Log.e(LOG_TAG, "INTENT");
        if(it.hasExtra(RankingActivity.EXTRA_AREA)){
            area = it.getStringExtra(RankingActivity.EXTRA_AREA);
            Log.e(LOG_TAG, "INTENT TRUE: " + area);
        }

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.setItemAnimator(new DefaultItemAnimator());
        list.setHasFixedSize(true);

        requestQueue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        tvwMessage.setVisibility(View.GONE);
        list.setVisibility(View.GONE);

        progress.setVisibility(View.VISIBLE);

        getRanking();
    }

    private void getRanking() {
        generateDefaultValues();
        JsonObjectRequest jsonObjectRequest = RankingRequester.prepareRankingBandaLargaRequest(responseListener, erroListener, getActivity(), area, exibicao, inicioData, fimData);
        jsonObjectRequest.setTag(LOG_TAG);

        requestQueue.add(jsonObjectRequest);
    }

    private void generateDefaultValues() {
        //Os parametros exibição, inicioData, fimData por hora estaram sendo definidos com valores padrão. Motivo 1: como o servidor tem poucos
        //dados o filtro por data acaba trazendo poucas infos então em inicioData e fimData serão definidos periodos mais longos
        //Motivo 2: exibicao, por hora, só tem um valor "qualidade_sinal"

        //pegando data atual
        DateTime currentDate = new DateTime();
        fimData = currentDate.yearOfEra().get() + "-" + currentDate.monthOfYear().get() + "-" + currentDate.dayOfMonth().get();
        inicioData = "2016-09-12";
        exibicao = "qualidade_sinal";

        Log.e(LOG_TAG, "DEAFULT VALUES: " + inicioData);
        Log.e(LOG_TAG, "DEAFULT VALUES: " + fimData);
        Log.e(LOG_TAG, "DEAFULT VALUES: " + exibicao);

    }

    private void initVolleyLinsteners(){
        responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(LOG_TAG, "response: " + response.toString());

                List<BandaLargaRankingItem> bandaLargaRankingItems = RankingRequester.parserRankingBandaLargaRequest(response);

                if (bandaLargaRankingItems != null && !bandaLargaRankingItems.isEmpty() ) {

                    AdapterRankingBandaLarga adapterRankingBandaLarga = new AdapterRankingBandaLarga(getActivity(), bandaLargaRankingItems, R.layout.list_ranking_banda_larga, new CustomItemClickListener() {
                        @Override
                        public void onItemClick(View v, int position) {
                            //nothing
                        }
                    });
                    list.setAdapter(adapterRankingBandaLarga);

                    progress.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                } else {
                    tvwMessage.setText("Nenhum resultado foi encontrado para essa região ;/");

                    progress.setVisibility(View.GONE);
                    tvwMessage.setVisibility(View.VISIBLE);
                }

            }
        };

        erroListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progress.setVisibility(View.GONE);
                    tvwMessage.setVisibility(View.VISIBLE);

                    if (Util.isConnectionTrue(getActivity())) {
                        tvwMessage.setText(getString(R.string.error_conexao));
                    } else {
                        tvwMessage.setText(getString(R.string.sem_conexao));
                    }
                } catch(Exception e) {
                    Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestQueue.cancelAll(LOG_TAG);
    }
}
