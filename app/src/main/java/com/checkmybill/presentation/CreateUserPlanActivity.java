package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.entity.ModalidadePlano;
import com.checkmybill.entity.Operadora;
import com.checkmybill.entity.Plano;
import com.checkmybill.entity.TipoPlano;
import com.checkmybill.presentation.CreateUserPlanSteps.*;
import com.checkmybill.request.ObterFiltrosRequester;
import com.checkmybill.request.PlanoRequester;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;


@EActivity(R.layout.activity_create_user_plan)
public class CreateUserPlanActivity extends BaseActivity implements VerticalStepperForm {
    @ViewById(R.id.vertical_stepper_form) protected VerticalStepperFormLayout verticalStepperForm;

    public static final int REQUEST_CODE = 1201;
    public static final String PLANO_EXTRA = "PLANO_EXTRA";
    public static final String PLANO_MODALIADADE_JSON = "PLANO_MODALIDADES_JSON_STR";
    public static final String PLANO_TIPOS_JSON = "PLANO_TIPOS_JSON_STR";
    public static final String REMOVER_PACOTES_RECARGAS = "REMOVER_PACOTES_RECARGAS";

    private Context mContext;
    private ProgressDialog loadingBox;
    private UserPlanStepFragmentbase[] stepLayouts;

    // Argumentos
    private Plano planoSelected;
    private String buff_listModalidadePlanosJSONStr, buff_listTiposPlanoJSONStr = null;
    private boolean removerPacoteRecargas = false;

    // Outros
    private RequestQueue requestQueue;
    private boolean doubleBackToExit = false;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores da Acitivty/Layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;

        planoSelected = (Plano) getIntent().getSerializableExtra(PLANO_EXTRA);
        buff_listModalidadePlanosJSONStr = (String) getIntent().getStringExtra(PLANO_MODALIADADE_JSON);
        buff_listTiposPlanoJSONStr = (String) getIntent().getStringExtra(PLANO_TIPOS_JSON);
        removerPacoteRecargas = getIntent().getBooleanExtra(REMOVER_PACOTES_RECARGAS, false);

        // Volley
        requestQueue = Volley.newRequestQueue(this);

        // Loading Box
        this.loadingBox = new ProgressDialog(this);
        this.loadingBox.setTitle("Carregando...");
        this.loadingBox.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                requestQueue.cancelAll(CreateUserPlanActivity.class.getName());
                dialogInterface.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if ( this.doubleBackToExit ) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExit = true;
        Toast.makeText(mContext, "Aperter 'Voltar' mais uma vez para sair", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { doubleBackToExit = false; }
        }, 2000);
    }

    @Override
    public void onStart() {
        super.onStart();
        if ( stepLayouts == null ) {
            // Inicializando elementos visuais do Steppers
            String[] stepsTitles = {"Geral", "Minutos Inclusos", "Internet", "SMS"};
            String[] stepsSubtitles = {"Informações Gerais", "Dados sobre os minutos inclusos do plano", "Informações sobre o uso da internet do plano", "Informações sobre SMS do plano"};

            LayoutInflater inflater = LayoutInflater.from(this);
            stepLayouts = new UserPlanStepFragmentbase[ stepsTitles.length ];
            stepLayouts[0] = new Step1( getLayoutInflater(), this );
            stepLayouts[1] = new Step2( getLayoutInflater() );
            stepLayouts[2] = new Step3( getLayoutInflater() );
            stepLayouts[3] = new Step4( getLayoutInflater() );

            VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, stepsTitles, this, this)
                    .primaryColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .primaryDarkColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .displayBottomNavigation(true)
                    .stepsSubtitles(stepsSubtitles)
                    .showVerticalLineWhenStepsAreCollapsed(false)
                    .init();
        }

        // Checando se foi informado os dados para os spinners..., se não, obtem diretamente do
        // servidor (aumenta o consumo de dados, deveria ser passado anteriormente como argmento) :(
        if ( buff_listModalidadePlanosJSONStr == null ) {
            // Iniciando a requisicao dos filtros para as operadoras
            this.loadingBox.setMessage("Aguarde, carregando filtros...");
            this.loadingBox.show();
            JsonObjectRequest jsonObjectRequest = ObterFiltrosRequester.prepareObterFiltrosBasePlanos(filtrosOpSuccessListener, errorRequestListener, "modalidade_plano,tipo_plano", this);
            jsonObjectRequest.setTag(getClass().getName());
            requestQueue.add(jsonObjectRequest);
            Log.d(LOG_TAG, "Plan Filter Request: Started");
            return;
        } else {
            try {
                // Gerando os dados JSON e inicializando spinners...
                JSONArray lista_modalidades_plano = new JSONArray(buff_listModalidadePlanosJSONStr);
                JSONArray lista_tipos_plano = new JSONArray(buff_listTiposPlanoJSONStr);
                initModeloSpinner(lista_modalidades_plano);
                initTipoSpinner(lista_tipos_plano);
            } catch (JSONException e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        // Obtendo os dados passados como argumento (pre-populando os campos)
        if ( planoSelected == null ) return;
        else populateFormContent();

    }

    /* ------------------------------------------------------------------------------------------ */
    // Eventos dos elementos/Views
    @Override
    public View createStepContentView(int stepNumber) {
        return stepLayouts[stepNumber].getLayout();
    }

    @Override
    public void onStepOpening(int stepNumber) {
        verticalStepperForm.setActiveStepAsCompleted();
    }

    @Override
    public void sendData() {
        // Validando campos para serem enviados ao servidor (cadastro)
        for ( int step = 0; step < stepLayouts.length; step++ ) {
            if ( stepLayouts[step].validateStepFields() == false ) {
                Log.d(LOG_TAG, "Uncompleted Step:" + step);
                verticalStepperForm.goToStep(step, false);
                Toast.makeText(this, "Erro: " + stepLayouts[step].validateErrorMessage, Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Exibindo o loading e disparando o Volley para a adicao dos dados
        this.loadingBox.setMessage("Salvando o plano, aguarde...");
        this.loadingBox.setCancelable(false);
        this.loadingBox.show();

        // Enviando requisição para armazenar/salvar o plano
        Plano myPlan = this.generatePlanoClassWithContent();
        JsonObjectRequest planAddRequest = PlanoRequester.prepareSalvarPlanoUsuarioRequest(savePlanSuccessListener, errorRequestListener, myPlan, removerPacoteRecargas, mContext);
        planAddRequest.setTag(getClass().getName());
        requestQueue.add( planAddRequest );
    }

    /* ------------------------------------------------------------------------------------------ */
    // Metodos proprios da classe
    private void populateFormContent(){
        // Dados do Step1 (Info. Geral)
        final int dt_vencimento = (planoSelected.getDtVencimento() > 0) ? planoSelected.getDtVencimento() : 0;
        ((Step1)stepLayouts[0]).nome_plano.setText( planoSelected.getNomePlano() );
        ((Step1)stepLayouts[0]).valor_plano.setText( String.format("%.2f", planoSelected.getValorPlano()) );
        ((Step1)stepLayouts[0]).dt_vencimento.setSelection( dt_vencimento );
        ((Step1)stepLayouts[0]).operadora.setText( planoSelected.getNomeOperadora() );
        ((Step1)stepLayouts[0]).setSelectionModalidadeByID( planoSelected.getIdModalidadePlano(), listaModalidadesPlano );
        ((Step1)stepLayouts[0]).setSelectionTipoByID( planoSelected.getIdTipoPlano(), listaTiposPlano );

        // Dados do Step2 (Minutos Inclusos)
        ((Step2)stepLayouts[1]).setMinutosMO( planoSelected.getMinMO() );
        ((Step2)stepLayouts[1]).setMinutosOO( planoSelected.getMinOO() );
        ((Step2)stepLayouts[1]).setMinutosFixo( planoSelected.getMinFixo() );
        ((Step2)stepLayouts[1]).setMinutosIU( planoSelected.getMinIU() );

        // Dados do Step3 (Internet)
        ((Step3)stepLayouts[2]).setDados( planoSelected.getLimiteDadosWeb() );

        // Dados do Step4 (SMS)
        ((Step4)stepLayouts[3]).setLimiteSMS( planoSelected.getSmsInclusos() );
    }

    // --------------------------------------------------------------------------------------------
    // Variaveis para o tratamento das requisições do Volley
    Response.Listener savePlanSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            loadingBox.dismiss();
            loadingBox.setCancelable(true);
            try {
                // Checando resposta...
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "FATAL ERROR, Plan not saved");
                    Log.d(LOG_TAG, "PLAN JSON-RCV:" + response.toString());
                    Toast.makeText(mContext, "Error: Não foi possível salvar o plano", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Definindo o tipo atualmente selecionando (para já estar selecionando no Painel)
                final Step1 stepClass = (Step1) stepLayouts[0];
                int selectedPosition = stepClass.tipo_plano.getSelectedItemPosition();
                if ( selectedPosition >= 3 ) selectedPosition = 2; // Mensal
                new SharedPrefsUtil(mContext).setSelectedPConsumoFilterPosition(selectedPosition);

                // Plano salvo com sucesso
                Toast.makeText(mContext, "Seu plano foi salvo com sucesso!", Toast.LENGTH_LONG).show();

                // Fechando a janela!
                setResult(RESULT_OK);
                finish();
            } catch ( JSONException ex ) { Log.e(LOG_TAG, ex.getMessage()); }
        }
    };
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
                JSONArray lista_modalidades_plano = response.getJSONArray("lista_modalidades_plano");
                JSONArray lista_tipos_plano = response.getJSONArray("lista_tipos_plano");

                // Montando os Elementos visuais do filtro
                initModeloSpinner(lista_modalidades_plano);
                initTipoSpinner(lista_tipos_plano);
                Log.d(LOG_TAG, "All Done ON: filtrosOpSuccessListener");

                // Obtendo os dados passados como argumento (pre-populando os campos)
                if ( planoSelected != null ) populateFormContent();
            } catch ( JSONException ex ) { Log.e(LOG_TAG, ex.getMessage()); }
        }
    };
    Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            loadingBox.dismiss();
            loadingBox.setCancelable(true);
            Log.e(LOG_TAG, error.getMessage());
            Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    // --------------------------------------------------------------------------------------------
    // Inicializador/Populator de conteudo para esta View
    private List<String> listaNomesTiposPlano, listaNomesModalidadesPlano;
    private List<ModalidadePlano> listaModalidadesPlano;
    private List<TipoPlano> listaTiposPlano;

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
        ((Step1)stepLayouts[0]).modalidade_plano.setAdapter( spinnerArrayAdapter );
    }

    private void initTipoSpinner(JSONArray lista_tipos_plano) throws JSONException {
        // Inicializando variaveis da classe (acesso geral ao longo desta)
        listaNomesTiposPlano = new ArrayList<>();
        listaTiposPlano = new ArrayList<>();

        final int size = lista_tipos_plano.length();
        for ( int i = 0; i < size; i++ ) {
            final JSONObject opInfo = lista_tipos_plano.getJSONObject(i);
            final String descricaoTipoPlano = opInfo.getString("descricao_tipo_plano");
            if ( descricaoTipoPlano.toLowerCase().equals("plano") ) continue;

            TipoPlano tipoPlano = new TipoPlano();
            tipoPlano.setId( opInfo.getInt("id_tipo_plano") );
            tipoPlano.setDescricaoTipoPlano( descricaoTipoPlano );

            listaTiposPlano.add(tipoPlano);
            listaNomesTiposPlano.add( tipoPlano.getDescricaoTipoPlano() );
        }

        // Inicializando spinner
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, R.layout.simple_spinner_string_item, listaNomesTiposPlano);
        ((Step1)stepLayouts[0]).tipo_plano.setAdapter( spinnerArrayAdapter );
    }

    // --------------------------------------------------------------------------------------------
    // Metodos privados de uso geral da classe
    private Plano generatePlanoClassWithContent() {
        Plano plano = new Plano();

        // Obtendo os dados e alimentando a classe que de plano que sera retornada
        // -> Step1
        final Step1 step1Class = (Step1) stepLayouts[0];
        plano.setIdPlanoReferencia( planoSelected.getIdPlano() );
        plano.setNomePlano(step1Class.nome_plano.getText().toString());
        plano.setIdModalidadePlano(listaModalidadesPlano.get(step1Class.modalidade_plano.getSelectedItemPosition()).getId());
        plano.setIdTipoPlano(listaTiposPlano.get(step1Class.tipo_plano.getSelectedItemPosition()).getId());
        plano.setDtVencimento(step1Class.dt_vencimento.getSelectedItemPosition());
        plano.setValorPlano(Float.valueOf(step1Class.valor_plano.getText().toString().replace(",",".")));
        plano.setIdOperadora(planoSelected.getIdOperadora());

        // -> Step2
        Step2 step2Class = (Step2) stepLayouts[1];
        plano.setMinMO(step2Class.getMinutosMO());
        plano.setMinIU(step2Class.getMinutosIU());
        plano.setMinOO(step2Class.getMinutosOO());
        plano.setMinFixo(step2Class.getMinutosFixo());

        // -> Step3
        Step3 step3Class = (Step3) stepLayouts[2];
        plano.setLimiteDadosWeb( step3Class.getDados() );

        // -> Step3
        Step4 step4Class = (Step4) stepLayouts[3];
        plano.setSmsInclusos( step4Class.getLimiteSMS() );

        return plano;
    }
}
