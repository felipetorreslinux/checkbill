package com.checkmybill.presentation.ComparacaoPlanoFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.entity.ModalidadePlano;
import com.checkmybill.entity.Operadora;
import com.checkmybill.entity.TipoPlano;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.ComparacaoPlanoActivity;
import com.checkmybill.presentation.CreateUserPlanActivity;
import com.checkmybill.presentation.CreateUserPlanSteps.Step1;
import com.checkmybill.request.ObterFiltrosRequester;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petrus A. (R@G3), ESPE... On 10/01/2017.
 */

@EFragment(R.layout.fragment_comparacaoplano_parametros)
public class ComparacaoPlano_ParametrosFragment extends BaseFragment {
    private Context mContext;
    private ComparacaoPlanoActivity comparacaoPlanoActivity;
    private ProgressDialog loadingBox;
    private RequestQueue requestQueue;

    @ViewById(R.id.parametrosActionButton) Button parametrosActionButton;
    @ViewById(R.id.tvAreaCobertura) TextView tvAreaCobertura;
    @ViewById(R.id.spOperadora) Spinner spOperadora;
    @ViewById(R.id.spModalidade) Spinner spModalidade;
    @ViewById(R.id.spTipoLinha) Spinner spTipoLinha;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.LOG_TAG = "CmpPlano_ParametrosFragment";
        super.onCreate(savedInstanceState);

        this.comparacaoPlanoActivity = (ComparacaoPlanoActivity) getActivity();
        this.mContext = getContext();

        // Volley
        requestQueue = Volley.newRequestQueue(mContext);

        this.loadingBox = new ProgressDialog(mContext);
        this.loadingBox.setTitle("Carregando...");
    }

    @Override
    public void onStart() {
        super.onStart();

        // Obtendo dados de filtros para ser exibido...
        // Checando se existe os campos
        if ( comparacaoPlanoActivity.lista_modalidades_plano == null ) {
            JsonObjectRequest jsonObjectRequest = ObterFiltrosRequester.prepareObterFiltrosBasePlanos(filtrosOpSuccessListener, errorRequestListener, "modalidade_plano,tipo_plano,operadoras_gsm", mContext);
            jsonObjectRequest.setTag(getClass().getName());

            this.loadingBox.setCancelable(false);
            this.loadingBox.setMessage("Aguarde, carregando filtros...");
            this.loadingBox.show();
            requestQueue.add(jsonObjectRequest);
        } else {
            try {
                initModeloSpinner(comparacaoPlanoActivity.lista_modalidades_plano);
                initTipoSpinner(comparacaoPlanoActivity.lista_tipos_plano);
                initOperadoraSpinner(comparacaoPlanoActivity.lista_operadora_gsm);
            } catch (JSONException ex) {
                Log.e(LOG_TAG, "Error:" + Util.getMessageErrorFromExcepetion(ex));
            }
        } // if,else...
    }

    // --------------------------------------------------------------------------------------------
    // Variaveis para o tratamento das requisições do Volley
    @Click(R.id.parametrosActionButton)
    public void parametrosActionButtonClickEvent() {
        // Obtendo dados de parametros e passando eles como argumentos ao nvo fragment
        final Bundle bundle = new Bundle();
        Operadora operadora = listaOperadora.get( spOperadora.getSelectedItemPosition() );
        TipoPlano tipoPlano = listaTiposPlano.get( spTipoLinha.getSelectedItemPosition() );
        ModalidadePlano modalidadePlano = listaModalidadesPlano.get( spModalidade.getSelectedItemPosition() );

        bundle.putInt("ID_OPERADORA", operadora.getId());
        bundle.putInt("ID_TIPO_PLANO", tipoPlano.getId());
        bundle.putInt("ID_MODALIDADE_PLANO", modalidadePlano.getId());
        bundle.putString("REGIAO", tvAreaCobertura.getText().toString());

        // Modificando o fragment passando os argumentos
        this.comparacaoPlanoActivity.changeFragment(ComparacaoPlanoActivity.FragmentList.PESQUISAR, bundle);
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
                JSONArray lista_modalidades_plano = response.getJSONArray("lista_modalidades_plano");
                JSONArray lista_tipos_plano = response.getJSONArray("lista_tipos_plano");
                JSONArray lista_operadora_gsm = response.getJSONArray("lista_operadoras_gsm");

                // Montando os Elementos visuais do filtro
                initModeloSpinner(lista_modalidades_plano);
                initTipoSpinner(lista_tipos_plano);
                initOperadoraSpinner(lista_operadora_gsm);

                // Salvando no activity
                comparacaoPlanoActivity.lista_modalidades_plano = lista_modalidades_plano;
                comparacaoPlanoActivity.lista_tipos_plano = lista_tipos_plano;
                comparacaoPlanoActivity.lista_operadora_gsm = lista_operadora_gsm;

                Log.d(LOG_TAG, "All Done ON: filtrosOpSuccessListener");
                // Obtendo os dados passados como argumento (pre-populando os campos)
                //if ( planoSelected != null ) populateFormContent();
            } catch ( JSONException ex ) { Log.e(LOG_TAG, ex.getMessage()); }
        }
    };
    Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            loadingBox.dismiss();
            loadingBox.setCancelable(true);
            Log.e(LOG_TAG, error.getMessage());
            new NotifyWindow(mContext).showErrorMessage("Parâmetros", "Não foi possível obter os dados necessários!", false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    // Disparando evento de 'BackPressed' para fechar a tela
                    comparacaoPlanoActivity.onBackPressed();
                }
            });
        }
    };

    // --------------------------------------------------------------------------------------------
    // Inicializador/Populator de conteudo para esta View
    private List<String> listaNomesTiposPlano, listaNomesModalidadesPlano, listaNomesOperadoras;
    private List<ModalidadePlano> listaModalidadesPlano;
    private List<TipoPlano> listaTiposPlano;
    private List<Operadora> listaOperadora;
    private void initOperadoraSpinner(JSONArray lista_operadoras_gsm) throws JSONException {
        listaNomesOperadoras = new ArrayList<>();
        listaOperadora = new ArrayList<>();

        final int size = lista_operadoras_gsm.length();
        for ( int i= 0; i < size; i++ ) {
            JSONObject opInfo = lista_operadoras_gsm.getJSONObject(i);
            Operadora operadora = new Operadora();
            operadora.setId(opInfo.getInt("id_operadora"));
            operadora.setNomeOperadora(opInfo.getString("nome_operadora"));

            listaNomesOperadoras.add( operadora.getNomeOperadora() );
            listaOperadora.add( operadora );
        }

        // Criando o arrayAdapter
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(mContext, R.layout.simple_spinner_string_item, listaNomesOperadoras);
        spOperadora.setAdapter( spinnerArrayAdapter );
    }

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
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(mContext, R.layout.simple_spinner_string_item, listaNomesModalidadesPlano);
        spModalidade.setAdapter(spinnerArrayAdapter);
    }

    private void initTipoSpinner(JSONArray lista_tipos_plano) throws JSONException {
        // Inicializando variaveis da classe (acesso geral ao longo desta)
        listaNomesTiposPlano = new ArrayList<>();
        listaTiposPlano = new ArrayList<>();

        final int size = lista_tipos_plano.length();
        for ( int i = 0; i < size; i++ ) {
            JSONObject opInfo = lista_tipos_plano.getJSONObject(i);
            TipoPlano tipoPlano = new TipoPlano();
            tipoPlano.setId( opInfo.getInt("id_tipo_plano") );
            tipoPlano.setDescricaoTipoPlano( opInfo.getString("descricao_tipo_plano") );

            if ( tipoPlano.getDescricaoTipoPlano().equalsIgnoreCase("plano") == true )
                continue;

            listaTiposPlano.add(tipoPlano);
            listaNomesTiposPlano.add( tipoPlano.getDescricaoTipoPlano() );
        }

        // Inicializando spinner
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(mContext, R.layout.simple_spinner_string_item, listaNomesTiposPlano);
        spTipoLinha.setAdapter(spinnerArrayAdapter);
    }
}
