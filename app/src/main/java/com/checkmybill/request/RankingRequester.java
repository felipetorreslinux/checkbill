package com.checkmybill.request;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.checkmybill.entity.Colabore;
import com.checkmybill.entity.ranking.BandaLargaRankingItem;
import com.checkmybill.entity.ranking.GsmRankingItem;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Guerra on 25/10/2016.
 */

public class RankingRequester {
    private static final String TAG = "RankingRequester";
    private static final String AREA = "area";
    private static final String EXIBICAO = "exibicao";
    private static final String INICIO_DATA = "inicio_data";
    private static final String FIM_DATA = "fim_data";
    private static final String RANKING_DATA = "ranking_data";
    private static final String NUMERO_ENTRADAS = "numero_entradas";
    private static final String NOME_OPERADORA = "nome_operadora";
    private static final String MEDIA_NIVEL_SINAL = "media_nivel_sinal";
    private static final String STATUS = "status";
    private static final String MAP_DATA = "map_data";
    private static final String COR_OPERADORA = "cor_operadora";
    private static final String URL_LOGO = "url_logo";
    private static final String TOTAL_ENTRADAS = "total_entradas";
    private static final String QUALI_DOWNLOAD = "quali_download";
    private static final String QUALI_UPLOAD = "quali_upload";

    public static JsonObjectRequest prepareRankingGsmRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Context context,
                                                             String area, String exibicao, String inicioData, String fimData) {
        final String url = Util.getSuperUrlServiceObterRankingGsm(context);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AREA, area);
            jsonObject.put(EXIBICAO, exibicao);
            jsonObject.put(INICIO_DATA, inicioData);
            jsonObject.put(FIM_DATA, fimData);
        } catch (JSONException e) {
            Log.e(TAG, "prepareRankingGsmRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static List<GsmRankingItem> parserRankingGsmRequest(JSONObject mainJsonObject){
        List<GsmRankingItem> gsmRankingItems = new ArrayList<>();

        try {
            JSONArray jsonArray = mainJsonObject.getJSONArray(RANKING_DATA);

            for(int i = 0; i < jsonArray.length(); i++){
                GsmRankingItem gsmRankingItem = new GsmRankingItem();
                try {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);

                    try {
                        Integer numEntradas = jsonObjectItem.getInt(NUMERO_ENTRADAS);
                        gsmRankingItem.setNumEntradas(numEntradas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        String nomeOperadora = jsonObjectItem.getString(NOME_OPERADORA);
                        gsmRankingItem.setNomeOperadora(nomeOperadora);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        Integer mediaNivelSinal = jsonObjectItem.getInt(MEDIA_NIVEL_SINAL);
                        gsmRankingItem.setMediaNivelSinal(mediaNivelSinal);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    gsmRankingItems.add(gsmRankingItem);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return gsmRankingItems;
    }

    public static JsonObjectRequest prepareRankingBandaLargaRequest(Response.Listener responseListener, Response.ErrorListener erroListener, Context context,
                                                             String area, String exibicao, String inicioData, String fimData) {
        final String url = Util.getSuperUrlServiceObterRankingBandaLarga(context);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(AREA, area);
            jsonObject.put(EXIBICAO, exibicao);
            jsonObject.put(INICIO_DATA, inicioData);
            jsonObject.put(FIM_DATA, fimData);
        } catch (JSONException e) {
            Log.e(TAG, "prepareRankingBandaLargaRequest | error: " + Util.getMessageErrorFromExcepetion(e));
        }

        Log.i(TAG, "prepared request to: " + url);
        Log.i(TAG, "json prepared: " + jsonObject.toString());

        JsonObjectRequest genericJsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonObject.toString(), responseListener, erroListener);
        return genericJsonObjectRequest;
    }

    public static List<BandaLargaRankingItem> parserRankingBandaLargaRequest(JSONObject mainJsonObject){
        List<BandaLargaRankingItem> bandaLargaRankingItems = new ArrayList<>();

        try {
            JSONArray jsonArray = mainJsonObject.getJSONArray(MAP_DATA);

            for(int i = 0; i < jsonArray.length(); i++){
                BandaLargaRankingItem bandaLargaRankingItem = new BandaLargaRankingItem();
                try {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);

                    try {
                        String nomeOperadora = jsonObjectItem.getString(NOME_OPERADORA);
                        bandaLargaRankingItem.setNomeOperadora(nomeOperadora);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        String corOperadora = jsonObjectItem.getString(COR_OPERADORA);
                        bandaLargaRankingItem.setCorOperadora(corOperadora);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        Double qualiDownload = jsonObjectItem.getDouble(QUALI_DOWNLOAD);
                        bandaLargaRankingItem.setQualiDownload(qualiDownload);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        Double qualiUpload = jsonObjectItem.getDouble(QUALI_UPLOAD);
                        bandaLargaRankingItem.setQualiUpload(qualiUpload);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        Integer totalEntradas = jsonObjectItem.getInt(TOTAL_ENTRADAS);
                        bandaLargaRankingItem.setTotalEntradas(totalEntradas);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        String urlLogo = jsonObjectItem.getString(URL_LOGO);
                        bandaLargaRankingItem.setUrlLogo(urlLogo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    bandaLargaRankingItems.add(bandaLargaRankingItem);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bandaLargaRankingItems;
    }

}
