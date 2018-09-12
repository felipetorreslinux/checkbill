package com.checkmybill.felipecode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.checkmybill.R;
import com.checkmybill.felipecode.Services.Services;
import com.checkmybill.util.SharedPrefsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.checkmybill.presentation.HomeFragments.CoverageMapFragment.localLatitude;
import static com.checkmybill.presentation.HomeFragments.CoverageMapFragment.localLongitude;

public class RankingPlano extends AppCompatActivity implements View.OnClickListener{

    public static final int REQUEST_CODE = 1226;

    Toolbar toolbar;
    Spinner spinner_ddd;
    Spinner spinner_planos;
    ViewStub loading_ranking;
    RecyclerView recycler_ranking;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_ranking_plano);
        createToolbar(toolbar);

        spinner_ddd = findViewById(R.id.spinner_ddd);
        spinner_planos = findViewById(R.id.spinner_planos);

        loading_ranking = findViewById(R.id.loading_ranking);

        recycler_ranking = (RecyclerView) findViewById(R.id.recycler_ranking);
        recycler_ranking.setLayoutManager(new LinearLayoutManager(this));
        recycler_ranking.setNestedScrollingEnabled(false);
        recycler_ranking.setHasFixedSize(true);

        listaRankingPlanos();
        listDDD();
        listPlanos();

    }

    private void listDDD(){
        List<String> lista_ddd = new ArrayList<>();
        lista_ddd.clear();
        lista_ddd.add("DDD");
        for(int i = 11; i < 98; i++){
            lista_ddd.add(String.valueOf(i));
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lista_ddd);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner_ddd.setAdapter(arrayAdapter);
    }

    private void listPlanos(){
        List<String> lista_planos = new ArrayList<>();
        lista_planos.clear();
        lista_planos.add("Planos");
        lista_planos.add("Pré Pago");
        lista_planos.add("Pós Pago");
        lista_planos.add("Controle");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lista_planos);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner_planos.setAdapter(arrayAdapter);
    }

    private void listaRankingPlanos(){
        try{
            loading_ranking.setVisibility(View.VISIBLE);
            String accessKey = new SharedPrefsUtil(this).getAccessKey();
            String lat = String.valueOf(localLatitude);
            String lng = String.valueOf(localLongitude);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("access_key", accessKey);
            jsonObject.put("latitude", lat);
            jsonObject.put("longitude", lng);
            new Services(this).listRanking(jsonObject, recycler_ranking, loading_ranking);
        }catch (JSONException e){}
    }

    private void createToolbar(Toolbar toolbar) {
        Drawable backIconActionBar = getResources().getDrawable(R.drawable.ic_back_white);
        toolbar = (Toolbar) findViewById(R.id.toolbar_avaliar_plano);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_avaliar_plano);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(backIconActionBar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }
}
