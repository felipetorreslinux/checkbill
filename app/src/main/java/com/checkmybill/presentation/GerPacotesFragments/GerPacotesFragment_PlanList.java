package com.checkmybill.presentation.GerPacotesFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.checkmybill.adapters.AdapterPacote;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.Pacote;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.GerCreditosPlanoActivity;
import com.checkmybill.presentation.GerPacotesActivity;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.request.PacotesRequester;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Petrus A. (R@G3), ESPE... On 05/12/2016.
 */

@EFragment(R.layout.fragment_ger_pacotes_planlist)
public class GerPacotesFragment_PlanList extends BaseFragment {
    @ViewById(R.id.layoutLoading) protected LinearLayout layoutLoading;
    @ViewById(R.id.layoutContentBody) protected LinearLayout layoutContentBody;
    @ViewById(R.id.pacotesListView) protected RecyclerView pacotesListView;

    private ProgressDialog loadingWindow;
    private List<Pacote> listPacotesOperadora;
    private AdapterPacote pacoteAdapter;

    private GerPacotesActivity baseActivity;
    private RequestQueue requestQueue;
    private Context mContext;


    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    public void onStop() {
        requestQueue.cancelAll(getClass().getName());
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = getClass().getName();

        requestQueue = Volley.newRequestQueue(getActivity());
        mContext = getContext();
        baseActivity = (GerPacotesActivity) getActivity();

        listPacotesOperadora = new ArrayList<>();
        pacoteAdapter = new AdapterPacote(mContext, listPacotesOperadora, pacoteItemClickListener, pacoteItemLongClickListener);

        this.loadingWindow = NotifyWindow.CreateLoadingWindow(mContext, "Pacotes", "Adicionando pacote, aguarde...");
        this.loadingWindow.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                requestQueue.cancelAll(GerCreditosPlanoActivity.class.getName());
                dialogInterface.dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Definindo o LayoutManager e o Adapter
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        this.pacotesListView.setLayoutManager(llm);
        this.pacotesListView.setAdapter( this.pacoteAdapter );

        // Requisitando a lista de pacotes do plano do usuário atual...
        JsonObjectRequest pacotesRequester = PacotesRequester.prepareObterListaPacotes(pacotesSuccessListener, pacotesErrorListener, baseActivity.getPlanoUsuario(), mContext);
        pacotesRequester.setTag(getClass().getName());

        // Exibindo loading e executando pesquisa
        layoutContentBody.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        requestQueue.add( pacotesRequester );
    }

    // --------------------------------------------------------------------------------------------
    // Metodos privados da classe
    private void AdicionarPacote(int id_pacote, Date date) {
        JsonObjectRequest adicionarRequester = PacotesRequester.prepareAnexarPacotePlanoUsuario(anexarSuccessListener, pacotesErrorListener, id_pacote, date, mContext);
        adicionarRequester.setTag(getClass().getName());

        loadingWindow.show();
        requestQueue.add( adicionarRequester );
    }

    private void AdicionarNovoPacote_DialogBox(final Pacote pacote) {
        final View pickerLayout = baseActivity.getLayoutInflater().inflate(R.layout.dialog_add_pacote_layout, null);
        final View manualDateContainer = pickerLayout.findViewById(R.id.manualDateContainer);
        final TextView nomePacote = (TextView) pickerLayout.findViewById(R.id.addPacote_nomePacote);
        final TextView valorPacote = (TextView) pickerLayout.findViewById(R.id.valor_pacote);
        final CheckBox usarDataAtualPacote = (CheckBox) pickerLayout.findViewById(R.id.usarDataAtual_pacote);
        final DatePicker datepickerDatePacote = (DatePicker) pickerLayout.findViewById(R.id.datepickerDate_pacote);

        // Definindo os textos...
        nomePacote.setText( pacote.getNomePacote() );
        valorPacote.setText( String.format("%.2f", pacote.getValorPacote()) );

        // Definindo evento docheckbox
        usarDataAtualPacote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = usarDataAtualPacote.isChecked();
                if ( isChecked ) manualDateContainer.setVisibility(View.GONE);
                else manualDateContainer.setVisibility(View.VISIBLE);
            }
        });

        // Criando DialoBox com os elementos definidos
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle("Adicionar Pacote");
        builder.setView(pickerLayout);
        builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Checando se o valor foi informado
                if ( valorPacote.getText().length() <= 0 ) {
                    valorPacote.requestFocus();
                    Toast.makeText(mContext, "Digite o valor do pacote", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Obtendo os dados
                final Date pacoteData;
                if ( usarDataAtualPacote.isChecked() == false ) {
                    GregorianCalendar calendar = new GregorianCalendar(datepickerDatePacote.getYear(), datepickerDatePacote.getMonth(), datepickerDatePacote.getDayOfMonth());
                    pacoteData = calendar.getTime();
                } else {
                    pacoteData = new Date();
                }

                // Ocultando interface e disparanco a exeucao do cadastro do plano...
                AdicionarPacote(pacote.getIdPacote(), pacoteData);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    // --------------------------------------------------------------------------------------------
    // Evento de click para os plano/cards na tela
    private AlertDialog confirmationDialog = null;
    CustomItemClickListener pacoteItemClickListener = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            final Pacote pacote = listPacotesOperadora.get(position);
            builder.setTitle("Anexar Pacote");
            builder.setMessage("Deseja anexar o pacote '" + pacote.getNomePacote() + "' a sua conta/plano?");
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                    AdicionarNovoPacote_DialogBox(pacote);
                }
            });
            builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });

            if ( confirmationDialog != null ) {
                confirmationDialog.dismiss();
                confirmationDialog = null;
            }

            confirmationDialog = builder.create();
            confirmationDialog.show();
        }
    };

    CustomItemClickListener pacoteItemLongClickListener = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            final Pacote pacote = listPacotesOperadora.get(position);
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_plano_item, null);
            CardView cardView = (CardView) view.findViewById(R.id.cardItem);
            TextView buffTV = (TextView) view.findViewById(R.id.planoNome);

            // Definindo opcoes para este modo de visualização
            cardView.setClickable(false);
            cardView.setUseCompatPadding(false);
            cardView.setRadius(0f);

            // -> Populando o nome do plano
            buffTV.setBackgroundColor(getResources().getColor(R.color.md_grey_100));
            buffTV.setText( pacote.getNomePacote() );

            // -> Populando a modalidade
            buffTV = (TextView) view.findViewById(R.id.modalidade_plano);
            buffTV.setText( pacote.getDescricaoModalidadePlano() );

            // -> Populando a operadora
            buffTV = (TextView) view.findViewById(R.id.nome_operadora);
            buffTV.setText( pacote.getNomeOperadora() );

            // -> Populando a Descricao
            buffTV = (TextView) view.findViewById(R.id.descricao_plano);
            buffTV.setText( pacote.getObservacao() );

            // -> Populando a Preco
            buffTV = (TextView) view.findViewById(R.id.preco_plano);
            buffTV.setText( String.format("R$ %.2f", pacote.getValorPacote()) );

            // -> Populando a Web
            buffTV = (TextView) view.findViewById(R.id.limite_dados);
            buffTV.setText( pacote.getLimiteDadosWebStr() );

            // -> Populando a SMS
            buffTV = (TextView) view.findViewById(R.id.limite_sms);
            buffTV.setText( pacote.getSmsInclusosStr() );

            // -> Populando a Minutos
            buffTV = (TextView) view.findViewById(R.id.minutagens);
            buffTV.setText( String.format("%s/%s/%s/%s",
                    pacote.getMinMOStr().replaceAll("[ ]min$", ""),
                    pacote.getMinOOStr().replaceAll("[ ]min$", ""),
                    pacote.getMinFixoStr().replaceAll("[ ]min$", ""),
                    pacote.getMinIUStr())
            );

            // Exibindo dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setView(view);
            builder.create().show();
        }
    };

    /* ------------------------------------------------------------------------------------------ */
    // Listeners do Volley
    private Response.Listener anexarSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if ( loadingWindow != null ) loadingWindow.dismiss();
            Log.d(LOG_TAG, response.toString());

            // Tratando respostas...
            try {
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "Error:" + response.getString("message"));
                    new NotifyWindow(mContext).showErrorMessage("Error", response.getString("message"), false);
                    return;
                }

                // Dados adicionados com sucesso...
                new NotifyWindow(mContext).showMessage("Ger, Pacote", "Pacote anexado com sucesso", false, -1);
                baseActivity.reloadUserList();
            } catch (JSONException ex) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }
        }
    };

    private Response.Listener pacotesSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            layoutLoading.setVisibility(View.GONE);
            layoutContentBody.setVisibility(View.VISIBLE);
            Log.d(LOG_TAG, "JSON-RCV:" + response.toString());

            // Tratando respostas...
            try {
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "Error:" + response.getString("message"));
                    new NotifyWindow(mContext).showErrorMessage("Error", response.getString("message"), false);
                    return;
                }

                // Obtendo os dados
                JSONArray data = response.getJSONArray("data");
                for ( int i = 0; i < data.length(); i++ ) {
                    JSONObject pkgInfo = data.getJSONObject(i);
                    Pacote pacote = new Pacote();
                    pacote.setIdPacote(pkgInfo.getInt("id_pacote"));
                    pacote.setIdOperadora( pkgInfo.getInt("id_operadora") );
                    pacote.setIdTipoPlano( pkgInfo.getInt("id_tipo") );
                    pacote.setIdModalidadePlano( pkgInfo.getInt("id_modalidade") );
                    pacote.setNomePacote( pkgInfo.getString("nome_pacote") );
                    pacote.setObservacao( pkgInfo.getString("observacao") );
                    pacote.setValorPacote( Float.parseFloat( pkgInfo.getString("valor_pacote")) );
                    pacote.setNomeOperadora( pkgInfo.getString("nome_operadora") );
                    pacote.setDescricaoTipoPlano(pkgInfo.getString("descricao_tipo_plano"));
                    pacote.setDescricaoModalidadePlano(pkgInfo.getString("descricao_modalidade_plano"));
                    pacote.setLimiteDadosWeb( pkgInfo.getInt("limite_dados_net") );
                    pacote.setLimiteDadosWebStr( pkgInfo.getString("dados_web_str") );
                    pacote.setSmsInclusos( pkgInfo.getInt("sms_inclusos") );
                    pacote.setSmsInclusosStr( pkgInfo.getString("sms_inclusos_str") );
                    pacote.setMinMO( pkgInfo.getInt("min_mo"));
                    pacote.setMinMOStr( pkgInfo.getString("min_mo_str"));
                    pacote.setMinOO( pkgInfo.getInt("min_oo"));
                    pacote.setMinOOStr( pkgInfo.getString("min_oo_str"));
                    pacote.setMinIU( pkgInfo.getInt("min_iu"));
                    pacote.setMinIUStr( pkgInfo.getString("min_iu_str"));
                    pacote.setMinFixo( pkgInfo.getInt("min_fixo"));
                    pacote.setMinFixoStr( pkgInfo.getString("min_fixo_str"));

                    listPacotesOperadora.add( pacote );
                }

                Log.d(LOG_TAG, "Updating Card List");
                pacoteAdapter.setLista( listPacotesOperadora );
                pacoteAdapter.notifyDataSetChanged();

                Log.d(LOG_TAG, "All Done ON: pacotesSuccessListener");
            } catch (JSONException ex ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }
        }
    };

    private Response.ErrorListener pacotesErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            layoutLoading.setVisibility(View.GONE);
            layoutContentBody.setVisibility(View.VISIBLE);

            try{
                String errorMsg = Util.getMessageErrorFromExcepetion(error);
                if (error.networkResponse.data != null) {
                    errorMsg += " | " + new String(error.networkResponse.data);
                }

                new NotifyWindow(mContext).showErrorMessage("Erro", errorMsg, false);
                Log.e(LOG_TAG, "error: " + errorMsg);
            }catch (Exception e){
                new NotifyWindow(mContext).showErrorMessage("FATAL Erro", Util.getMessageErrorFromExcepetion(e), false);
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    };
}
