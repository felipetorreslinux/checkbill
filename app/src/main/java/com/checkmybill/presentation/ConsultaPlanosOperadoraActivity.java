package com.checkmybill.presentation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.adapters.AdapterPlano;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.ModalidadePlano;
import com.checkmybill.entity.Operadora;
import com.checkmybill.entity.Plano;
import com.checkmybill.request.ObterFiltrosRequester;
import com.checkmybill.request.PlanoRequester;
import com.checkmybill.request.RequesterUtil;
import com.checkmybill.util.IntentMap;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_consulta_planos_operadora)
public class ConsultaPlanosOperadoraActivity extends BaseActivity  {
    @ViewById(R.id.plano_filtro_view_container) LinearLayout plano_filtro_view_container;
    @ViewById(R.id.plano_pesquisa_container) LinearLayout plano_pesquisa_container;
    @ViewById(R.id.planoList) RecyclerView planoList;
    @ViewById(R.id.plano_pesquisa_conteudo_container) SwipeRefreshLayout plano_pesquisa_conteudo_container;

    @ViewById(R.id.tvwPesquisa) TextView tvwPesquisa;

    @ViewById(R.id.spModeloPlano) Spinner spModeloPlano;
    @ViewById(R.id.spOperadoras) Spinner spOperadoras;

    private Context mContext;
    private AlertDialog confirmDialog;
    private ProgressDialog loadingBox;
    private String buff_listOperadoraGsmJSONStr = null;
    private String buff_listModalidadePlanosJSONStr = null;
    private String buff_listTiposPlanoJSONStr = null;
    private RequestQueue requestQueue;

    private List<Plano> listPlanosOperadora;
    private AdapterPlano planoAdapter;

    // Argumentos
    private boolean REMOVER_PACOTES_RECARGAS_ARG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        LOG_TAG = this.getClass().getName();

        this.listPlanosOperadora = new ArrayList<>();
        this.planoAdapter = new AdapterPlano(this, listPlanosOperadora, false, planoItemClickListener, planoItemLongClickListener);
        requestQueue = Volley.newRequestQueue(this);

        this.loadingBox = new ProgressDialog(this);
        this.loadingBox.setTitle("Carregando...");
        this.loadingBox.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                requestQueue.cancelAll(ConsultaPlanosOperadoraActivity.class.getName());
                dialogInterface.dismiss();
            }
        });

        REMOVER_PACOTES_RECARGAS_ARG = getIntent().getBooleanExtra("REMOVER_PACOTES_RECARGAS", false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Definindo o LayoutManager e o Adapter
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        this.planoList.setLayoutManager(llm);
        this.planoList.setAdapter( this.planoAdapter );

        // Checando se deve obter a lista de filtros para os planos
        if ( buff_listOperadoraGsmJSONStr == null ) {
            // Iniciando a requisicao dos filtros para as operadoras
            this.loadingBox.setMessage("Aguarde, carregando filtros...");
            this.loadingBox.show();
            JsonObjectRequest jsonObjectRequest = ObterFiltrosRequester.prepareObterFiltrosBasePlanos(filtrosOpSuccessListener, errorRequestListener, "operadoras_gsm,modalidade_plano,tipo_plano", this);
            jsonObjectRequest.setTag(getClass().getName());
            requestQueue.add(jsonObjectRequest);
            Log.d(LOG_TAG, "Plan Filter Request: Started");
        }

        // Definindo evento do Swiper
        this.plano_pesquisa_conteudo_container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                btnConsultaPlano();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Checando qual secao se encontra, se a lista de planso está visivel,
        // ira ser ocult e exibir o filtro novamente, após isso, ira parar esta função...
        if ( plano_filtro_view_container.getVisibility() != View.VISIBLE ) {
            plano_pesquisa_container.setVisibility(View.GONE);
            plano_filtro_view_container.setVisibility(View.VISIBLE);
            planoAdapter.clearList();
            listPlanosOperadora.clear();
            return;
        }
        else {
            // Caso contrario, permite o fechamendo deste tela/activity (que seria a funcao padrao do 'onBack'
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Verificando se o plano foi cadastrado com sucesso...
        if ( resultCode == RESULT_OK && requestCode == CreateUserPlanActivity.REQUEST_CODE ) {
            Log.d(LOG_TAG, "Plano Cadastrado com sucesso");
            setResult(RESULT_OK);
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // --------------------------------------------------------------------------------------------
    // Eventos dos elementos anexados/relacionados a View atual...
    @Click(R.id.btnConsultaPlano)
    public void btnConsultaPlano() {
        this.loadingBox.setMessage("Aguarde, pesquisando planos...");
        this.loadingBox.show();

        // Obtendo os elementos para iniciar a requisicao...
        final int operadoraItemPosition = spOperadoras.getSelectedItemPosition();
        final int modalidadeItemPosition = spModeloPlano.getSelectedItemPosition();

        // Modificando os textos de exibicao para a lista de itens...
        tvwPesquisa.setText("Buscar por: " + spOperadoras.getSelectedItem() + " | " + spModeloPlano.getSelectedItem());

        // Enviando requisicao para obter a lista de operadoras
        JsonObjectRequest jsonObjectRequest = PlanoRequester.prepareConsultaPlanosOperadoraRequest(
                listaPlanosSuccessListener, errorRequestListener,
                listaModalidadesPlano.get(modalidadeItemPosition).getId(),
                listaOperadoras.get(operadoraItemPosition).getId(),
                1, this
        );
        jsonObjectRequest.setTag(getClass().getName());
        requestQueue.add(jsonObjectRequest);
        Log.d(LOG_TAG, "Plan Lista Request: Started");
    }

    // --------------------------------------------------------------------------------------------
    // Variaveis para o tratamento das requisições do Volley
    Response.Listener filtrosOpSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingBox.dismiss();
            try {
                // Checando resposta
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "FATAL ERROR, No Filter Located");
                    return;
                } else Log.d(LOG_TAG, response.toString());

                // Obtendo os dados para preparar os adapters necessarios
                // Nota: Eles também seram passada como argumento para a proxima tela (no modo string)
                JSONArray lista_operadoras_gsm = response.getJSONArray("lista_operadoras_gsm");
                buff_listOperadoraGsmJSONStr = lista_operadoras_gsm.toString();
                JSONArray lista_modalidades_plano = response.getJSONArray("lista_modalidades_plano");
                buff_listModalidadePlanosJSONStr = lista_modalidades_plano.toString();
                JSONArray lista_tipos_plano = response.getJSONArray("lista_tipos_plano");
                buff_listTiposPlanoJSONStr = lista_tipos_plano.toString();

                // Montando os Elementos visuais do filtro
                initModeloSpinner(lista_modalidades_plano);
                initOperadorasSpinner(lista_operadoras_gsm);
                Log.d(LOG_TAG, "All Done ON: filtrosOpSuccessListener");
            } catch ( JSONException ex ) { Log.e(LOG_TAG, ex.getMessage()); }
        }
    };
    Response.Listener listaPlanosSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingBox.dismiss();
            plano_pesquisa_conteudo_container.setRefreshing(false);
            try {
                // Checando resposta
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "FATAL ERROR, No Filter Located");
                    return;
                } else Log.d(LOG_TAG, response.toString());

                // Lista obtida, vamos popula-las e exibir na tela em forma de cards...
                // Mas primeiro, vamos limpar o anterior e modificar a visualização xD
                planoAdapter.clearList();
                listPlanosOperadora.clear();
                plano_filtro_view_container.setVisibility(View.GONE);
                plano_pesquisa_container.setVisibility(View.VISIBLE);

                // Lets do it ^_^
                JSONArray data = response.getJSONArray("data");
                final int size = data.length();
                for ( int i = 0; i < size; i++ ) {
                    JSONObject rcvPlanInfo = data.getJSONObject(i);
                    Plano plano = new Plano();
                    plano.setIdPlano( rcvPlanInfo.getInt("id_plano_operadora") );
                    plano.setIdOperadora( rcvPlanInfo.getInt("id_operadora") );
                    plano.setIdTipoPlano( rcvPlanInfo.getInt("id_tipo_plano") );
                    plano.setIdModalidadePlano( rcvPlanInfo.getInt("id_modalidade_plano") );
                    plano.setNomePlano( rcvPlanInfo.getString("nome_plano") );
                    plano.setObservacao( rcvPlanInfo.getString("observacao") );
                    plano.setValorPlano( Float.parseFloat( rcvPlanInfo.getString("valor_plano")) );
                    plano.setNomeOperadora( rcvPlanInfo.getString("nome_operadora") );
                    plano.setDescricaoTipoPlano(rcvPlanInfo.getString("descricao_tipo_plano"));
                    plano.setDescricaoModalidadePlano(rcvPlanInfo.getString("descricao_modalidade_plano"));
                    plano.setLimiteDadosWeb( rcvPlanInfo.getInt("limite_dados_net") );
                    plano.setLimiteDadosWebStr( rcvPlanInfo.getString("dados_web_str") );
                    plano.setSmsInclusos( rcvPlanInfo.getInt("sms_inclusos") );
                    plano.setSmsInclusosStr( rcvPlanInfo.getString("sms_inclusos_str") );
                    listPlanosOperadora.add( plano );
                }

                Log.d(LOG_TAG, "Updating Card List");
                planoAdapter.setLista( listPlanosOperadora );
                planoAdapter.notifyDataSetChanged();

                Log.d(LOG_TAG, "All Done ON: listaPlanosSuccessListener");
            } catch ( Exception ex ) { Log.e(LOG_TAG, ex.getMessage()); }
        }
    };
    Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };

    // --------------------------------------------------------------------------------------------
    // Evento de click para os plano/cards na tela
    CustomItemClickListener planoItemClickListener = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            final Plano selectedPlano = listPlanosOperadora.get(position);
            if ( confirmDialog != null ) {
                confirmDialog.dismiss();
                confirmDialog = null;
            }

            // Confirmando acao
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Usar o plano '" + selectedPlano.getNomePlano() + "' como base para o cadastro do seu plano?");
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Fecando dialog
                    dialogInterface.dismiss();
                    confirmDialog = null;

                    // Abrindo a tela de cadastro com o plano selecionado
                    Intent it = new Intent(IntentMap.CREATE_USER_PLAN);
                    it.putExtra(CreateUserPlanActivity.PLANO_EXTRA, selectedPlano);  // Plano Selecionado
                    it.putExtra(CreateUserPlanActivity.PLANO_MODALIADADE_JSON, buff_listModalidadePlanosJSONStr);
                    it.putExtra(CreateUserPlanActivity.PLANO_TIPOS_JSON, buff_listTiposPlanoJSONStr);
                    it.putExtra(CreateUserPlanActivity.REMOVER_PACOTES_RECARGAS, REMOVER_PACOTES_RECARGAS_ARG);
                    startActivityForResult(it, CreateUserPlanActivity.REQUEST_CODE);
                }
            });
            builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    confirmDialog = null;
                }
            });
            confirmDialog = builder.create();
            confirmDialog.show();
        }
    };
    CustomItemClickListener planoItemLongClickListener = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            Log.d(LOG_TAG, "Plano Long Clicked: " + position);
        }
    };

    // --------------------------------------------------------------------------------------------
    // Inicializador/Populator de conteudo para esta View
    private List<String> listaNomesOperadoras, listaNomesModalidadesPlano;
    private List<Operadora> listaOperadoras;
    private List<ModalidadePlano> listaModalidadesPlano;

    private void initModeloSpinner(JSONArray lista_modalidades_plano) throws JSONException {
        // Inicializando variaveis da classe (acesso geral ao longo desta)
        listaNomesModalidadesPlano = new ArrayList<>();
        listaModalidadesPlano = new ArrayList<>();

        final int size = lista_modalidades_plano.length();
        for ( int i = 0; i < size; i++ ) {
            JSONObject modInfo = lista_modalidades_plano.getJSONObject(i);
            ModalidadePlano modPlano = new ModalidadePlano();
            modPlano.setId( modInfo.getInt("id_modalidade_plano") );
            modPlano.setDescricao( modInfo.getString("descricao_modalidade_plano") );

            listaNomesModalidadesPlano.add( modPlano.getDescricao() );
            listaModalidadesPlano.add( modPlano );
        }

        // Inicializando spinner
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, R.layout.simple_spinner_string_item, listaNomesModalidadesPlano);
        this.spModeloPlano.setAdapter( spinnerArrayAdapter );
    }

    private void initOperadorasSpinner(JSONArray lista_operadoras) throws JSONException {
        // Inicializando variaveis da classe (acesso geral ao longo desta)
        listaNomesOperadoras = new ArrayList<>();
        listaOperadoras = new ArrayList<>();

        final int size = lista_operadoras.length();
        for ( int i = 0; i < size; i++ ) {
            JSONObject opInfo = lista_operadoras.getJSONObject(i);
            Operadora operadora = new Operadora();
            operadora.setId( opInfo.getInt("id_operadora") );
            operadora.setNomeOperadora( opInfo.getString("nome_operadora") );

            listaOperadoras.add(operadora);
            listaNomesOperadoras.add( operadora.getNomeOperadora() );
        }

        // Inicializando spinner
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, R.layout.simple_spinner_string_item, listaNomesOperadoras);
        this.spOperadoras.setAdapter( spinnerArrayAdapter );
    }
}
