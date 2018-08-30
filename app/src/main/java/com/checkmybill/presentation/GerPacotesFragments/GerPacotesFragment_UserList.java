package com.checkmybill.presentation.GerPacotesFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.checkmybill.request.PacotesRequester;
import com.checkmybill.util.DatePickers;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Petrus A., on 05/12/2016.
 */

@EFragment(R.layout.fragment_ger_pacotes_userlist)
public class GerPacotesFragment_UserList extends BaseFragment {
    @ViewById(R.id.layoutLoading) protected LinearLayout layoutLoading;
    @ViewById(R.id.layoutContentBody) protected SwipeRefreshLayout layoutContentBody;
    @ViewById(R.id.pacotesListView) protected RecyclerView pacotesListView;
    @ViewById(R.id.pacotes_valoTotalUsado) protected TextView pacotes_valoTotalUsado;
    @ViewById(R.id.filterDateRangeText) protected TextView filterDateRangeText;

    private List<Pacote> listPacotesPlano;
    private AdapterPacote pacoteAdapter;

    private Date[] dateLimits;

    private ProgressDialog loadingWindow;
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

        listPacotesPlano = new ArrayList<>();
        pacoteAdapter = new AdapterPacote(mContext, listPacotesPlano, pacoteItemClickListener, pacoteItemLongClickListener, baseActivity.getResources().getColor(R.color.md_cyan_A700));

        this.loadingWindow = NotifyWindow.CreateLoadingWindow(mContext, "Pacotes", "Removendo pacote, aguarde...");
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

        // Definindo evento do ScrollLoading
        this.layoutContentBody.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                obterListaPacotes();
            }
        });

        // Definindo valores iniciais do periodo a ser pesquisado
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            int monthMaxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            this.dateLimits = new Date[]{
                    sdf.parse(String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), 1 + c.get(Calendar.MONTH), 1)),
                    sdf.parse(String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), 1 + c.get(Calendar.MONTH), monthMaxDays))
            };
        } catch ( Exception e ) {
            this.dateLimits = new Date[]{ new Date(), new Date() };
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }

        obterListaPacotes();
    }

    // --------------------------------------------------------------------------------------------
    // Evento dos elementos visuais deste Fragment (como Buttons e Layouts)
    @Click(R.id.filterSettingsLayoutContent)
    public void filterSettingsLayoutContentClick() {
        DatePickers.DateRangeDatePicker dateRangeDatePicker = new DatePickers.DateRangeDatePicker(baseActivity);
        dateRangeDatePicker.setTitle("Período");
        dateRangeDatePicker.setPositiveEvent(new DatePickers.DateRangeDatePickerInterface.OnPositiveEvent() {
            @Override
            public void onEvent(Date firstDate, Date secondDate, DialogInterface dialogInterface, int i) {
                dateLimits[0] = firstDate;
                dateLimits[1] = secondDate;

                // Executando atualizacao dos itens...
                dialogInterface.dismiss();
                obterListaPacotes();
            }
        });
        dateRangeDatePicker.setNegativeEvent(new DatePickers.DateRangeDatePickerInterface.OnNegativeEvent() {
            @Override
            public void onEvent(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dateRangeDatePicker.showDialogWindow();
    }

    // --------------------------------------------------------------------------------------------
    // Evento de click para os plano/cards na tela
    CustomItemClickListener pacoteItemLongClickListener = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            final Pacote pacote = listPacotesPlano.get(position);
            // Confirmando remoção
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Remover Pacote");
            builder.setMessage("Deseja remover o pacote '" + pacote.getNomePacote() + "'?");
            builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //new NotifyWindow(mContext).showWarningMessage("Remover Pacote", "Recurso não suportado, ainda", false);
                    removerPacoteUsuario(pacote);
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        }
    };

    CustomItemClickListener pacoteItemClickListener = new CustomItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            // Exibindo detalhes do Pacote...
            final Pacote pacote = listPacotesPlano.get(position);
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
    private Response.Listener userDeletePacotesSuccessListener = new Response.Listener<JSONObject>() {
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
                new NotifyWindow(mContext).showMessage("Ger, Pacote", "Pacote removido", false, -1);
                obterListaPacotes();
            } catch (JSONException ex) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }
        }
    };
    private Response.Listener userPacotesSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            layoutLoading.setVisibility(View.GONE);
            layoutContentBody.setVisibility(View.VISIBLE);
            layoutContentBody.setRefreshing(false);
            Log.d(LOG_TAG, "JSON-RCV:" + response.toString());

            // Tratando respostas...
            try {
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    Log.e(LOG_TAG, "Error:" + response.getString("message"));
                    return;
                }

                // Obtendo os dados
                float totalValor = 0;
                listPacotesPlano.clear();
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
                    pacote.setIdPacoteUsuario( pkgInfo.getInt("id_pacote_plano_usuario") );

                    JSONObject pkgMinInfo = pkgInfo.getJSONObject("minutos_inclusos");
                    pacote.setMinMO( pkgMinInfo.getInt("min_mo"));
                    pacote.setMinMOStr( pkgMinInfo.getString("min_mo_str"));
                    pacote.setMinOO( pkgMinInfo.getInt("min_oo"));
                    pacote.setMinOOStr( pkgMinInfo.getString("min_oo_str"));
                    pacote.setMinIU( pkgMinInfo.getInt("min_iu"));
                    pacote.setMinIUStr( pkgMinInfo.getString("min_iu_str"));
                    pacote.setMinFixo( pkgMinInfo.getInt("min_fixo"));
                    pacote.setMinFixoStr( pkgMinInfo.getString("min_fixo_str"));

                    listPacotesPlano.add( pacote );

                    totalValor += pacote.getValorPacote();
                }
                pacotes_valoTotalUsado.setText(String.format("R$ %.2f", totalValor));

                Log.d(LOG_TAG, "Updating Card List");
                pacoteAdapter.setLista( listPacotesPlano );
                pacoteAdapter.notifyDataSetChanged();

                Log.d(LOG_TAG, "All Done ON: userPacotesSuccessListener");
            } catch (JSONException ex ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(ex));
            }
        }
    };

    private Response.ErrorListener userPacotesErrorListener = new Response.ErrorListener() {
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

    // --------------------------------------------------------------------------------------------
    // Metodos privados de uso interno da classe
    public void obterListaPacotes() {
        // Atualizando texto do filtro e do valor total usado no mmomento
        pacotes_valoTotalUsado.setText(String.format("R$ 0.00"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder(sdf.format(dateLimits[0]));
        sb.append(" à ");
        sb.append(sdf.format(dateLimits[1]));
        filterDateRangeText.setText( sb.toString() );

        // Limpando lista
        listPacotesPlano.clear();
        pacoteAdapter.notifyDataSetChanged();

        // Requisitando a lista de pacotes do plano do usuário atual...
        JsonObjectRequest userPacotesRequster = PacotesRequester.prepareObterPacotesPlanoUsuario(userPacotesSuccessListener, userPacotesErrorListener, dateLimits[0], dateLimits[1], mContext);
        userPacotesRequster.setTag(getClass().getName());

        // Exibindo loading e executando pesquisa
        layoutContentBody.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        requestQueue.add( userPacotesRequster );
    }

    private void removerPacoteUsuario(Pacote pacote) {
        this.loadingWindow.show();

        JsonObjectRequest request = PacotesRequester.prepareRemoverPacotePlanoUsuario(userDeletePacotesSuccessListener, userPacotesErrorListener, pacote.getIdPacoteUsuario(), mContext);
        request.setTag( getClass().getName() );
        requestQueue.add(request);
    }
}
