package com.checkmybill.presentation.HomeFragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.CheckBillApplication;
import com.checkmybill.R;
import com.checkmybill.adapters.AdapterClusterItem;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.Operadora;
import com.checkmybill.maps.MultiDrawable;
import com.checkmybill.maps.MyItem;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.presentation.ranking.RankingActivity;
import com.checkmybill.request.OperadoraRequester;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.Connectivity;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.MultiSpinner;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petrus A. (R@G3), ESPE... On 15/12/2016.
 */
@EFragment(R.layout.fragment_coverage_map)
public class CoverageMapFragment extends BaseFragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final private static int HEARTMAP_RED_POSITION = 0;
    final private static int HEARTMAP_GREEN_POSITION = 1;

    public final static int REQUEST_CODE_LOCATION_ACTIVITY = 1;
    private static int RESULT_CODE_LOCATION_YES = -1;
    private static int RESULT_CODE_LOCATION_NO_AND_NEVER = 0;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @ViewById(R.id.mapView) protected MapView mapView;
    @ViewById(R.id.filter_exibicao) protected Spinner spFilterExibicao;
    @ViewById(R.id.filter_operadoras) protected MultiSpinner mspFilterOperadoras;
    @ViewById(R.id.progressLoadMap) protected ProgressBar progressLoadMap;
    @ViewById(R.id.customBtnRanking) protected LinearLayout customBtnRanking;

    private boolean refreshMapData = false;

    private GoogleMap googleMap;
    private HeatmapTileProvider[] heatmapTileProvider;
    private TileOverlay[] heatmapTileOverlay;
    private ClusterManager<MyItem> mClusterManager;
    private List<MyItem> clusterItensSelecteds;

    private Bundle savedInstanceState;
    private RequestQueue requestQueue;
    private Context mContext;

    private CoverageMapAreaClass coverageMapAreaClass;
    private String listaIdsOperadorasSelecionadas = "";
    private List<Operadora> listaOperadoras;
    private List<String> listaNomesOperadoras;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = getClass().getName();
        this.mContext = getContext();
        this.savedInstanceState = savedInstanceState;

        this.listaNomesOperadoras = new ArrayList<>();
        this.listaOperadoras = new ArrayList<>();

        // Criando elemento do Volley
        this.requestQueue = Volley.newRequestQueue(this.mContext);
        this.coverageMapAreaClass = new CoverageMapAreaClass();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        mLastLocation = null;

        this.requestQueue.cancelAll(getClass().getName());
        this.requestQueue.cancelAll(getClass().getName() + "_mapa");

        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Definindo as opcoes do spinner com os tipo de visualização (Sinal GSM ou BLarga)
        // Definindo spinner relacionado ao tipo de mapa visualizado momento,
        // que podem ser 'blarga' ou 'gsm'
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.filters_analytics_list_array, R.layout.spinner_filters_analytics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterExibicao.setAdapter(adapter);
        spFilterExibicao.setOnItemSelectedListener( this.spFilterExibicao_OnItemSeLectedListener );

        // Criando mapa...
        mapView.onCreate( this.savedInstanceState );
        mapView.onResume();
        mapView.getMapAsync(this);

        setUpGoogleLocationItens();
        setUpCustomBtnRanking();
    }

    private void setUpGoogleLocationItens() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleApiClient.connect();
    }

    private void setUpCustomBtnRanking() {
        customBtnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String area = "";
                try {
                    area = coverageMapAreaClass.ObterAreaCobertura();

                    Intent it = new Intent(IntentMap.RANKING);
                    it.putExtra(RankingActivity.EXTRA_AREA, area);
                    startActivity(it);
                }catch (Exception e){
                    if(getActivity() != null){
                        Toast.makeText(getActivity(), "Não foi possível abrir o ranking. Tente mais tarde.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if ( googleMap != null ) mapView.onResume();
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();

        if (googleMap != null) mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /* ------------------------------------------------------------------------------------------ */
    // Eventos relacionado aos elementos visuais(Views), como Click, Mapa, etc...
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        final float maxZoomValue = 16f;
        final float minZoomValue = 10f;

        // Definindo o estilo do mapa com base no tipo de conexao
        if ( Connectivity.isConnectedWifi(mContext) ) {
            // Exibindo o mapa com mais detalhes (nome de ruas, todas as ruas, etc)
            this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mContext, R.raw.gmaps_style_raw_wifi));
        } else {
            // Exibindo o mapa com menos detalhes
            this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mContext, R.raw.gmaps_style_raw_gsm));
        }

        // Centralizando a camera e definindo o limite de zoom do mapa
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(-8.063253f, -34.873255f));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(13.0f);
        this.googleMap.moveCamera(center);
        this.googleMap.moveCamera(zoom);
        this.googleMap.setMaxZoomPreference(maxZoomValue);
        this.googleMap.setMinZoomPreference(minZoomValue);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Definindo eventos (Camera Idle Event)
        this.googleMap.setOnCameraIdleListener(this.googleMap_OnCamareraIdleListener );
    }

    private AdapterView.OnItemSelectedListener spFilterExibicao_OnItemSeLectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            // Checando se ja foi obtidos os dados, se sim, indica que não é a primeira
            // execucao e entao, foi uma ação deliberado do usuario...
            Log.d(LOG_TAG, "spFilterExibicao -> Item Selected");
            if ( listaOperadoras.size() > 0 ) {
                // Indica que deve atualizar os dados do mapa apos obter as operadoras
                Log.d(LOG_TAG, "spFilterExibicao -> Changing to true");
                refreshMapData = true;
            }

            // Atualizando a lista de operadoras com base na opcao definida....
            obterListaOperadoras();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private GoogleMap.OnCameraIdleListener googleMap_OnCamareraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            Log.d(LOG_TAG, "Camera is IDLE");

            // Checando se a area atual ainda esta dentro da area de cobertura...
            if (!coverageMapAreaClass.RegiaoDentroAreaCoberturaAtual(googleMap)) {
                // Fora da area de cobertura, obtendo novos dados...
                coverageMapAreaClass.AtualizarAreaCoberturaMapa(googleMap);
                obterDadosMapas();
            } else {
                // Visualização ainda dentro da area de cobertura anterior
                // Fazendo nada...
                Log.d(LOG_TAG, "Area inside old coordenates square");
            }
        }
    };

    /* ------------------------------------------------------------------------------------------ */
    // Eventos aos Listeners do VOLLEY
    Response.Listener operadorasRequestListener = new Response.Listener<JSONObject>(){
        @Override
        public void onResponse(JSONObject response) {
            Log.d(LOG_TAG, response.toString());
            try {
                if (!response.getString("status").equalsIgnoreCase("success")) {
                    // Exibir erro (com base no tipo de fragment visivel no momento
                    if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                        new NotifyWindow(mContext).showErrorMessage("Mapa", response.getString("message"), false);
                    return;
                }

                // Gerando a array com os dados
                listaNomesOperadoras.clear();
                listaOperadoras = OperadoraRequester.parserListaOperadorasRequest(response);
                listaIdsOperadorasSelecionadas = "";
                for ( Operadora operadora : listaOperadoras ) {
                    listaNomesOperadoras.add( operadora.getNomeOperadora() );

                    if ( listaIdsOperadorasSelecionadas.length() > 0 ) listaIdsOperadorasSelecionadas += ",";
                    listaIdsOperadorasSelecionadas += String.valueOf( operadora.getId() );
                }

                // Alimetando o campo do MultiSpinner... (e definindo o evento de selecao...
                mspFilterOperadoras.setItems(listaNomesOperadoras, "Todas as Operadoras", new MultiSpinner.MultiSpinnerListener() {
                    @Override
                    public void onItemsSelected(boolean[] selected) {
                        listaIdsOperadorasSelecionadas = "";
                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                Log.e(LOG_TAG, "Selected: " + i);
                                if ( listaIdsOperadorasSelecionadas.length() > 0)
                                    listaIdsOperadorasSelecionadas += ",";

                                listaIdsOperadorasSelecionadas += String.valueOf(listaOperadoras.get(i).getId());
                            }

                        }

                        // Atualizando mapa...
                        obterDadosMapas();
                    }
                });

                // Checando se deve obter os novos dados do mapa
                if ( refreshMapData ) obterDadosMapas(); // Obtendo os dados do mapa
                else showLoadingInfoImage(false); // Ocultando loading e finalizando execucao
            } catch ( JSONException e ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));

                // Exibir erro? (com base no tipo de fragment visivel no momento
                if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                    new NotifyWindow(mContext).showErrorMessage("Mapa", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showLoadingInfoImage(false);
            Log.e(LOG_TAG, error.getMessage());

            // Exibir erro (com base no tipo de fragment visivel no momento
            if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() ) {
                String errMessage;
                if ( error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError )
                    errMessage = "Não foi possível se conectar, verifique sua conexão.";
                else if ( error instanceof ServerError )
                    errMessage = "O endereço não foi localizado, tente de novo mais tarde.";
                else
                    errMessage = "Houve um problema ao se conectar com o servidor.";

                new NotifyWindow(mContext).showErrorMessage("Erro", errMessage, false);
            }
        }
    };

    /* ------------------------------------------------------------------------------------------ */
    // Metodos privados de uso geral da classe
    private static final String PARAMETERS_REQUEST_EXIBICAO = "exibicao";
    private static final String PARAMETERS_REQUEST_AREA = "area";
    private static final String PARAMETERS_REQUEST_NUMERO_TELEFONE = "numero_telefone";
    private static final String PARAMETER_IDS_OPERADORAS = "ids_operadoras";
    private void obterDadosMapas() {
        // Checando se o fragment ainda esta ativo
        if ( spFilterExibicao == null )
            return;

        // cancelando todas as requisições anteriores
        requestQueue.cancelAll(getClass().getName() + "_mapa");
        final SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(mContext);

        final String numMyTelefone = sharedPrefsUtil.getUserPhone();
        final int selectedItemPosition = spFilterExibicao.getSelectedItemPosition();
        final JSONObject jsonParam = new JSONObject();
        final String url = ( selectedItemPosition == 0 ) ? Util.getSuperUrlServiceGetInfoGSM(mContext) : Util.getSuperUrlServiceGetInfoBandaLarga(mContext);
        try {
            jsonParam.put(PARAMETERS_REQUEST_EXIBICAO, "qualidade_sinal");
            jsonParam.put(PARAMETERS_REQUEST_AREA, coverageMapAreaClass.ObterAreaCobertura());
            jsonParam.put(PARAMETER_IDS_OPERADORAS, listaIdsOperadorasSelecionadas);

            // Usar meu numero de telefone
            if ( 1 == 0 && numMyTelefone.length() > 0 )
                jsonParam.put(PARAMETERS_REQUEST_NUMERO_TELEFONE, numMyTelefone);
        } catch ( JSONException ex ) {
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            new NotifyWindow(mContext).showErrorMessage("Mapa", Util.getMessageErrorFromExcepetion(ex), false);
            return;
        }

        // Debug
        Log.d(LOG_TAG, "req url -> " + url);
        Log.d(LOG_TAG, "req json -> " + jsonParam.toString());

        // Montando requisicao (e o callback de resposta, que deve ser definida aqui para melhor
        // tratamento com base no tipo informado...
        final JsonObjectRequest jsonObjectRequest = RequesterUtil.createGenericJsonObjectRequest(Request.Method.POST, url, jsonParam.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(LOG_TAG, response.toString());
                showLoadingInfoImage(false);
                try {
                    // Checando resposta
                    if (!response.getString("status").equalsIgnoreCase("success")) {
                        Log.e(LOG_TAG, response.getString("message"));
                        new NotifyWindow(mContext).showErrorMessage("Mapa", response.getString("message"), false);
                        return;
                    }

                    // Populando os dados com base no tipo informado
                    if ( selectedItemPosition == 0 ) populateMapaData_GSM(response); // GSM
                    else populateMapaData_BLarga(response); // BLarga
                } catch (JSONException ex) {
                    Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
                    new NotifyWindow(mContext).showErrorMessage("Mapa", Util.getMessageErrorFromExcepetion(ex), false);
                }
            }
        }, errorRequestListener);
        jsonObjectRequest.setTag(getClass().getName() + "_mapa");

        // Enviando a requisicao
        showLoadingInfoImage(true);
        requestQueue.add(jsonObjectRequest);
    }

    private void obterListaOperadoras() {
        showLoadingInfoImage(true); // Exibi o loading

        // Iniciando a requisição...
        final String tipoOperadora = ( spFilterExibicao.getSelectedItemPosition() == 0 ) ? "gsm" : "blarga";
        JsonObjectRequest jsonObjectRequest = OperadoraRequester.prepareListaOperadorasRequest(operadorasRequestListener, errorRequestListener, tipoOperadora, mContext);
        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
    }

    private void showLoadingInfoImage(final boolean show) {
        try {
            if ( show ) progressLoadMap.setVisibility(View.VISIBLE);
            else progressLoadMap.setVisibility(View.GONE);
        } catch(Exception e) {
            Log.e(LOG_TAG, "Error onPostExcecute. maybe the activity has been destroyed", e);
        }
    }

    private void populateMapaData_BLarga(JSONObject response) throws JSONException {
        JSONArray mapData = response.getJSONArray("map_data");
        final int mapDataLength = mapData.length();

        // Ocultando elementos antigos (se houver)
        if ( heatmapTileOverlay[HEARTMAP_GREEN_POSITION] != null ) {
            heatmapTileOverlay[HEARTMAP_GREEN_POSITION].remove();
            heatmapTileOverlay[HEARTMAP_GREEN_POSITION].clearTileCache();
            heatmapTileOverlay[HEARTMAP_GREEN_POSITION] = null;
            heatmapTileProvider[HEARTMAP_GREEN_POSITION] = null;
        }
        if ( heatmapTileOverlay[HEARTMAP_RED_POSITION] != null ) {
            heatmapTileOverlay[HEARTMAP_RED_POSITION].remove();
            heatmapTileOverlay[HEARTMAP_RED_POSITION].clearTileCache();
            heatmapTileOverlay[HEARTMAP_RED_POSITION] = null;
            heatmapTileProvider[HEARTMAP_RED_POSITION] = null;
        }

        // Criando/Limpando Cluster...
        if ( mClusterManager == null ) {
            mClusterManager = new ClusterManager<MyItem>(getActivity(), googleMap);
            mClusterManager.setRenderer(new CustomClusterItemRenderer());
            googleMap.setOnMarkerClickListener(mClusterManager);
            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
                @Override
                public boolean onClusterItemClick(MyItem myItem) {
                    clusterItensSelecteds = new ArrayList<MyItem>();
                    clusterItensSelecteds.add(myItem);
                    Dialog clusterItemShowDialog = onCreateDialogShowClusterItensBandaLarga(null);
                    clusterItemShowDialog.show();
                    return false;
                }
            });
            mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
                @Override
                public boolean onClusterClick(Cluster<MyItem> cluster) {
                    clusterItensSelecteds = new ArrayList<MyItem>();
                    clusterItensSelecteds.addAll(cluster.getItems());
                    Dialog clusterItemShowDialog = onCreateDialogShowClusterItensBandaLarga(null);
                    clusterItemShowDialog.show();
                    return false;
                }
            });
        } else {
            mClusterManager.clearItems();
        }

        // Percorrendo os dado recebidos e populando-os na tela
        for ( int i = 0; i < mapDataLength; i++ ) {
            JSONObject d = mapData.getJSONObject(i);
            final double latitude = d.getDouble("latitude");
            final double longitude = d.getDouble("longitude");
            JSONArray dataArray = d.getJSONArray("data");
            for ( int i2 = 0; i2 < dataArray.length(); i2++ ) {
                JSONObject data = dataArray.getJSONObject(i2);
                MyItem myItem = new MyItem(latitude, longitude);
                myItem.setIsp(data.getString("nome_operadora"));
                myItem.setUrlLogo(data.getString("url_logo"));
                myItem.setValue(data.getDouble("media_quali_download"));
                myItem.setDownload(data.getDouble("media_quali_download"));
                myItem.setUpload(data.getDouble("media_quali_upload"));
                myItem.setLatency(data.getInt("media_quali_latencia"));
                myItem.setNumEntradas(data.getInt("total_entradas"));
                myItem.setLatLng(new LatLng(latitude, longitude));
                mClusterManager.addItem(myItem);
            }
        }

        mClusterManager.cluster();
    }

    private void populateMapaData_GSM(JSONObject response) throws JSONException {
        JSONArray mapData = response.getJSONArray("map_data");
        final int mapDataLength = mapData.length();

        // Percorrendo dados recebidos e populando-os na tela
        //List<LatLng> heartmapPointers_red = new ArrayList<>();
        //List<LatLng> heartmapPointers_green = new ArrayList<>();
        List<WeightedLatLng> heartmapPointers_red = new ArrayList<>();
        List<WeightedLatLng> heartmapPointers_green = new ArrayList<>();
        for ( int i = 0; i < mapDataLength; i++ ) {
            JSONObject d = mapData.getJSONObject(i);
            final int weight = d.getInt("heatmap_weight");
            //LatLng pointer = new LatLng(d.getDouble("latitude"), d.getDouble("longitude"));
            WeightedLatLng pointer = new WeightedLatLng( new LatLng(d.getDouble("latitude"), d.getDouble("longitude")), d.getDouble("heatmap_weight"));
            if ( weight <= 5 ) heartmapPointers_red.add(pointer);
            else heartmapPointers_green.add(pointer);
        }

        // Criando/Limpando cluster (se houver)
        if ( mClusterManager != null ) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
            mClusterManager = null;
            googleMap.setInfoWindowAdapter(null);
        }

        // Checando se ja existe o Heartmap
        if ( heatmapTileProvider == null ) {
            // Criando os arrays ligados ao overlay do heartmap...
            heatmapTileProvider = new HeatmapTileProvider[2];
            heatmapTileOverlay = new TileOverlay[2];
        }

        // Criando os gradients (necessario para criar os elementos)
        final int[] colors_red = {Color.rgb(255, 0, 0)};
        final int[] colors_green = {Color.rgb(0, 225, 0)};
        final float[] startPoints = {0.1f};
        final Gradient gradient_red = new Gradient(colors_red, startPoints);
        final Gradient gradient_green = new Gradient(colors_green, startPoints);

        // Criando os elementos
        CreateHeartMapOverlay(HEARTMAP_RED_POSITION, heartmapPointers_red, gradient_red);
        CreateHeartMapOverlay(HEARTMAP_GREEN_POSITION, heartmapPointers_green, gradient_green);
    }

    private void CreateHeartMapOverlay(final int position, List<WeightedLatLng> data, Gradient gradient) {
        // removendo anterior (se já existir)
        if ( heatmapTileOverlay[position] != null ) {
            heatmapTileOverlay[position].remove();
            heatmapTileOverlay[position].clearTileCache();
            heatmapTileProvider[position] = null;
            heatmapTileOverlay[position] = null;
        }

        // Checando se há dados...
        if ( data == null || data.size() <= 0 )
            return;

        // Criando o overlay
        heatmapTileProvider[position] = new HeatmapTileProvider.Builder()
                .weightedData(data)
                .radius(15)
                .gradient(gradient)
                .opacity(0.6)
                .build();
        heatmapTileOverlay[position] = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider[position]));
    }

    private Dialog onCreateDialogShowClusterItensBandaLarga(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_show_cluster_itens_banda_larga, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        LinearLayoutManager mLayoutManagerRecyclerView = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManagerRecyclerView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        AdapterClusterItem adapterClusterItem = new AdapterClusterItem(getActivity(), clusterItensSelecteds, R.layout.list_cluster_item, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //nothing
            }
        });
        recyclerView.setAdapter(adapterClusterItem);
        builder.setView(view)
                .setTitle("Banda larga")
                .setMessage("Itens selecionados: " + clusterItensSelecteds.size())
                .setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    /* ------------------------------------------------------------------------------------------ */
    // Classe de uso interno dentro deste fragment (Classes locais)
    private class CustomClusterItemRenderer extends DefaultClusterRenderer<MyItem> {
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;
        private TextView tvwValueClusterOne;
        private TextView tvwValueUnitClusterOne;
        private TextView tvwValueClusterGroup;
        private TextView tvwValueUnitClusterGroup;
        public CustomClusterItemRenderer() {
            super(getActivity(), googleMap, mClusterManager);
            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.cluster_multi_registers, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
            tvwValueClusterGroup = (TextView) multiProfile.findViewById(R.id.tvwValue);
            tvwValueUnitClusterGroup = (TextView) multiProfile.findViewById(R.id.tvwValueUnit);
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            View oneProfile = getActivity().getLayoutInflater().inflate(R.layout.cluster_one_register, null);
            mIconGenerator.setContentView(oneProfile);
            mImageView = (ImageView) oneProfile.findViewById(R.id.image);
            tvwValueClusterOne = (TextView) oneProfile.findViewById(R.id.tvwValue);
            tvwValueUnitClusterOne = (TextView) oneProfile.findViewById(R.id.tvwValueUnit);
        }
        @Override
        protected void onBeforeClusterItemRendered(final MyItem myItem, MarkerOptions markerOptions) {
            String value;
            String unit;
            double kilobits = myItem.getValue() / 1024;
            double megabits = myItem.getValue() / 1049179.35;
            DecimalFormat mDecimalFormater = new DecimalFormat("#.##");
            if (megabits < 1) {
                value = mDecimalFormater.format(kilobits);
                unit = "Kb/s";
            } else {
                value = mDecimalFormater.format(megabits);
                unit = "Mb/s";
            }
            Picasso.with(getActivity())
                    .load(myItem.getUrlLogo())
                    .into(new com.squareup.picasso.Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mImageView.setImageBitmap(bitmap);
                            BitmapDrawable drawable = new BitmapDrawable(bitmap);
                            myItem.setDrawable(drawable);
                        }
                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            mImageView.setImageResource(R.drawable.wifi_icon);
                            myItem.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.wifi_icon));
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            mImageView.setImageResource(R.drawable.wifi_icon);
                            myItem.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.wifi_icon));
                        }
                    });
            tvwValueClusterOne.setText(value);
            tvwValueUnitClusterOne.setText(unit);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(myItem.getIsp());
        }
        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;
            for (MyItem p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = p.getDrawable();
                if (drawable != null) {
                    drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);
                }
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);
            int count = 0;
            double average = 0;
            for (MyItem myItem : cluster.getItems()) {
                average += myItem.getValue();
                count++;
            }
            average = average / count;
            String value;
            String unit;
            double kilobits = average / 1024;
            double megabits = average / 1049179.35;
            DecimalFormat mDecimalFormater = new DecimalFormat("#.##");
            if (megabits < 1) {
                value = mDecimalFormater.format(kilobits);
                unit = "Kb/s";
            } else {
                value = mDecimalFormater.format(megabits);
                unit = "Mb/s";
            }
            mClusterImageView.setImageDrawable(multiDrawable);
            tvwValueClusterGroup.setText(value);
            tvwValueUnitClusterGroup.setText(unit);
            Bitmap icon = mClusterIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    // Metodos referentes a configuração e iniciação do recurso de localização

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void startLocationRequest(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }else{
            setUpLocation();
        }
    }

    private void setUpLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    getActivity(),
                                    REQUEST_CODE_LOCATION_ACTIVITY);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(LOG_TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_LOCATION_ACTIVITY){
            if(resultCode == RESULT_CODE_LOCATION_YES){
                startLocationUpdates();
            }else if(resultCode == RESULT_CODE_LOCATION_NO_AND_NEVER){
                //nothing
            }

        }
    }

    protected void startLocationUpdates() {
        try {
            if (!(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                            @Override
                            public void onLocationChanged(final Location location) {
                                if (mLastLocation == null) {
                                    if (ContextCompat.checkSelfPermission(getActivity(),
                                            Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED) {
                                        googleMap.setMyLocationEnabled(true);

                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                                        //googleMap.animateCamera(cameraUpdate);

                                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 1000, null);

                                    }
                                }
                                mLastLocation = location;
                                Log.e(LOG_TAG, "Location detected:" + location.getLatitude() + " | " + location.getLongitude());
                            }
                        });
                return;
            }
        }catch (Exception e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }
    }

    public boolean checkLocationPermission(){
        if ( ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ) {
            String[] permissionList = ((CheckBillApplication) getActivity().getApplication()).GetUnGrantedNecessaryPermissions();
            ActivityCompat.requestPermissions(getActivity(),permissionList, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            setUpLocation();
            return true;
        }
    }

    @EditorAction(R.id.filter_map_address)
    public boolean searchMapLocationEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean handled = false;
        final String addressToSearch = view.getText().toString();
        if (actionId == EditorInfo.IME_ACTION_SEARCH ) {
            if ( addressToSearch.length() <= 0 ) {
                Log.d(LOG_TAG, "Endereço inválido");
                Toast.makeText(mContext, "Digite um endereço para pesquisar", Toast.LENGTH_SHORT).show();
            } else {
                handled = true;

                // Ocultando o teclado
                InputMethodManager in = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // Realizando a pesquisa
                try {
                    Geocoder gc = new Geocoder(mContext);
                    if ( gc.isPresent() ) {
                        List<Address> addresses = gc.getFromLocationName(addressToSearch, 1);
                        if ( addresses.size() <= 0 )
                            throw new Exception("Endereço não localizado.");

                        final Address addr = addresses.get(0);
                        final CameraUpdate topPosition = CameraUpdateFactory.newLatLng(new LatLng(addr.getLatitude(), addr.getLongitude()));
                        this.googleMap.moveCamera(topPosition);
                    } else {
                        throw new Exception("GeoCoding não ativo");
                    }
                } catch (IOException e) {
                    new NotifyWindow(mContext).showErrorMessage("Mapa", "Erro obtendo o endereço", false);
                    Log.e(LOG_TAG, "GeoCoding Exception -> " + e.getMessage());
                } catch (Exception e) {
                    new NotifyWindow(mContext).showErrorMessage("Mapa", e.getMessage(), false);
                    Log.e(LOG_TAG, "GeoCoding Exception -> " + e.getMessage());
                }
            }
        }

        return handled;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                for ( int i = 0; i < permissions.length; i++ ) {
                    String permName = permissions[i];
                    boolean isGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    if ( permName.equals(Manifest.permission.ACCESS_FINE_LOCATION) ) {
                        if ( !isGranted ) {
                            boolean showRationale = shouldShowRequestPermissionRationale( permName );
                            if (!showRationale) {
                                Log.e(LOG_TAG, "user denied flagging NEVER ASK AGAIN");
                            } else if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permName)) {
                                Log.e(LOG_TAG, "user denied WITHOUT never ask again");
                            }
                        }
                        else {
                            setUpLocation();
                        }
                    }
                }
            }
        }

        // All Done
    }
}
