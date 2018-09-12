package com.checkmybill.felipecode.Services;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewStub;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.checkmybill.R;
import com.checkmybill.felipecode.Adapter.AdapterRankingPlano;
import com.checkmybill.felipecode.Models.RankingModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;

public class Services {

    String URL = "http://services.checkbill.com.br:1338";

    Activity activity;

    public Services(Activity activity){
        this.activity = activity;
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        AndroidNetworking.initialize(activity,okHttpClient);
    }

    public void listRanking(final JSONObject jsonObject, final RecyclerView recyclerView, final ViewStub loading_ranking) {
        AndroidNetworking.post(URL + "/services/obter-info-ranking-gsm")
            .addJSONObjectBody(jsonObject)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        String status = response.getString("status");
                        switch (status){
                            case "success":
                                JSONArray array = response.getJSONArray("data");
                                if(array.length() > 0){
                                    List<RankingModel> lista = new ArrayList<>();
                                    for (int i = 0; i < array.length(); i++){
                                        JSONObject dados = array.getJSONObject(i);
                                        RankingModel rankingModel = new RankingModel(
                                                dados.getInt("id_plano"),
                                                dados.getString("nome_plano"),
                                                dados.getInt("id_operadora"),
                                                dados.getString("nome_operadora"),
                                                dados.getString("observacao"),
                                                dados.getLong("internet"),
                                                dados.getString("valor_plano"),
                                                dados.getString("tipo_contrato_plano"),
                                                dados.getString("modalidade_plano"),
                                                dados.getBoolean("plano_usuario"));
                                        lista.add(rankingModel);
                                    }
                                    AdapterRankingPlano adapterRankingPlano = new AdapterRankingPlano(activity, lista);
                                    recyclerView.setAdapter(adapterRankingPlano);
                                    loading_ranking.setVisibility(View.GONE);
                                }
                                break;
                        }
                    }catch (JSONException e){}
                }

                @Override
                public void onError(ANError anError) {

                }
            });
    }

}
