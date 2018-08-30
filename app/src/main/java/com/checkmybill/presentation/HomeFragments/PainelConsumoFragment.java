package com.checkmybill.presentation.HomeFragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.Plano;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.entity.TipoPlano;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.presentation.RelatorioConsumoActivity;
import com.checkmybill.request.ObterFiltrosRequester;
import com.checkmybill.request.PlanoRequester;
import com.checkmybill.service.TrafficMonitor;
import com.checkmybill.tutorial.Tutorial;
import com.checkmybill.tutorial.TutorialException;
import com.checkmybill.tutorial.TutorialItem;
import com.checkmybill.util.DatePickers;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import az.plainpie.PieView;

@EFragment(R.layout.fragment_painel_consumo)
public class PainelConsumoFragment extends BaseFragment {
    private Context mContext;
    private SharedPrefsUtil sharedPrefsUtil;

    @ViewById(R.id.swipeRefreshLayout) protected SwipeRefreshLayout swipeRefreshLayout;
    @ViewById(R.id.layoutProgress) protected LinearLayout layoutProgress;
    @ViewById(R.id.layoutContentBody) protected ScrollView layoutContentBody;
    @ViewById(R.id.filterSettingsLayoutContent) LinearLayout filterSettingsLayoutContent;
    @ViewById(R.id.filterDateRangeText) TextView filterDateRangeText;
    @ViewById(R.id.cardAlertaCadastraPlano) CardView cardAlertaCadastraPlano;
    @ViewById(R.id.pconsumo_report_card) CardView pconsumo_report_card;

    // -> SMS
    @ViewById(R.id.pieView_sms) protected  PieView pieView_sms;
    @ViewById(R.id.sms_limiteDoPlano) protected TextView sms_limiteDoPlano;
    @ViewById(R.id.sms_restantes) protected TextView sms_restantes;
    @ViewById(R.id.sms_totalEnviados) protected TextView sms_totalEnviados;
    @ViewById(R.id.layoutDadosImprecisosSms) protected LinearLayout layoutDadosImprecisosSms;

    // -> NET
    @ViewById(R.id.pieView_net) protected  PieView pieView_net;
    @ViewById(R.id.net_limiteDoPlano) protected TextView net_limiteDoPlano;
    @ViewById(R.id.net_restantes) protected TextView net_restantes;
    @ViewById(R.id.net_totalUsados) protected TextView net_totalUsados;
    @ViewById(R.id.layoutDadosImprecisosWeb) protected LinearLayout layoutDadosImprecisosWeb;

    // -> CALL
    @ViewById(R.id.pieView_call) protected  PieView pieView_call;
    @ViewById(R.id.call_limiteDoPlano) protected TextView call_limiteDoPlano;
    @ViewById(R.id.call_restantes) protected TextView call_restantes;
    @ViewById(R.id.call_totalUsados) protected TextView call_totalUsados;
    @ViewById(R.id.layoutDadosImprecisosCall) protected LinearLayout layoutDadosImprecisosCall;

    // Outras...
    private List<String> tipoStringList;
    private List<TipoPlano> tipoPlanoList;
    private Spinner dlg_pconsumo_setting_periodo_spinner;

    private Plano meuPlano;
    private View dialog_painel_consumo_setting_layout;
    private TextView dlg_pconsumo_setting_range_start_text;
    private TextView dlg_pconsumo_setting_range_end_text;
    private AlertDialog settingWindow;
    private RequestQueue requestQueue;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = getClass().getName();
        this.sharedPrefsUtil = new SharedPrefsUtil(getActivity());

        // Uso geral da classe...
        //sharedPrefsUtil = new SharedPrefsUtil(getActivity());
        requestQueue = Volley.newRequestQueue(getActivity());
        mContext = getActivity();
        meuPlano =  null;

        // Inicializando SettingWindow
        this.dialog_painel_consumo_setting_layout = getActivity().getLayoutInflater().inflate(R.layout.dialog_painel_consumo_setting, null);
        dlg_pconsumo_setting_periodo_spinner = (Spinner) this.dialog_painel_consumo_setting_layout.findViewById(R.id.dlg_pconsumo_setting_periodo_spinner);
        dlg_pconsumo_setting_range_start_text = (TextView) dialog_painel_consumo_setting_layout.findViewById(R.id.dlg_pconsumo_setting_range_start_text);
        dlg_pconsumo_setting_range_end_text = (TextView) dialog_painel_consumo_setting_layout.findViewById(R.id.dlg_pconsumo_setting_range_end_text);
        this.initializeSettingWindow();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.requestQueue.cancelAll( getClass().getName() );
    }

    @Override
    public void onStart(){
        super.onStart();

        // Definindo evento de refresh para o SwipeLayout
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadStaticsData();
            }
        });

        // Inicializando as cores das Pie's
        this.initializePieViewColorConfig();

        // Atualizando texto do filtro
        final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dlg_pconsumo_setting_range_start_text.setText( sdf.format(Util.DatesUtil.getDecrementedDate(1)) );
        dlg_pconsumo_setting_range_end_text.setText( sdf.format(Util.DatesUtil.getCurrentDate()) );
        updateFilterDateRangeText();

        // Mudando o layout para exibir o loading...
        showLoadingLayout(true);

        // Obtendo os dados de filtro de periodo
        TipoPlano[] tipoPlanosArr = sharedPrefsUtil.getTipoPlanoList();
        if ( tipoPlanosArr == null ) {
            JsonObjectRequest jsonObjectRequest = ObterFiltrosRequester.prepareObterFiltrosBasePlanos(filtrosSuccessListener, errorRequestListener, "tipo_plano", mContext);
            jsonObjectRequest.setTag(getClass().getName());
            requestQueue.add(jsonObjectRequest);
        } else {
            initializeSettingWindowSpinner(tipoPlanosArr);
            this.meuPlano = sharedPrefsUtil.getMeuPlanoClass();
            if ( this.meuPlano == null ) {
                if ( new SharedPrefsUtil(mContext).getAccessKey() != null ) {
                    // Usuario cadastrado, obtendo o plano do usario
                    cardAlertaCadastraPlano.setVisibility(View.GONE);
                    pconsumo_report_card.setVisibility(View.VISIBLE);
                    showLoadingLayout(true);
                    JsonObjectRequest jsonObjectRequest = PlanoRequester.prepareObterPlanoUsuarioRequest(planoUsuarioSuccessListener, errorRequestListener, getActivity());
                    jsonObjectRequest.setTag(getClass().getName());
                    requestQueue.add(jsonObjectRequest);
                } else {
                    // Não esta logado... Apenasobtendo os dados estatisticos locais
                    meuPlano = null;
                    pconsumo_report_card.setVisibility(View.GONE);
                    cardAlertaCadastraPlano.setVisibility(View.VISIBLE);
                    LoadStaticsData();
                }
            } else {
                // All Done, executando acao para contabilizar os dados
                LoadStaticsData();
            }
        }
    }

    @Override
    public void focusReceived(){
        // Checando se é a primeira visualização...
        if ( getTabPosition() > 1 && new SharedPrefsUtil(mContext).getPConsumoIsFirstVisualization() ) {
            this.startTutorial();
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    // Eventos dos elementos/Views
    @Click(R.id.layoutDadosImprecisosCall)
    public void layoutImprecissoCallClick() {
        new NotifyWindow(mContext).showWarningMessage("Dados de 'Ligações' imprecisos", "O seu consumo está maior do que o valor contratado para o plano.\n\nO que pode ter ocorrido:\n\n1 - Você cadastrou incorretamente o valor contratado do plano;\n\n2 - Você não cadastrou pacotes ao plano ou não registrou recargas;\n\n3 - Caso não seja nenhum dos pontos acima, pode ter ocorrido falha da sua opedradora. Recomendadmos que entre em contato com ela para averiguar.", false, true);
    }

    @Click(R.id.layoutDadosImprecisosWeb)
    public void layoutImprecissoWebClick() {
        new NotifyWindow(mContext).showWarningMessage("Dados de 'WEB/NET' imprecisos", "O seu consumo está maior do que o valor contratado para o plano.\n\nO que pode ter ocorrido:\n\n1 - Você cadastrou incorretamente o valor contratado do plano;\n\n2 - Você não cadastrou pacotes ao plano ou não registrou recargas;\n\n3 - Caso não seja nenhum dos pontos acima, pode ter ocorrido falha da sua opedradora. Recomendadmos que entre em contato com ela para averiguar.", false, true);
    }

    @Click(R.id.layoutDadosImprecisosSms)
    public void layoutImprecissoSmsClick() {
        new NotifyWindow(mContext).showWarningMessage("Dados de 'SMS' imprecisos", "O seu consumo está maior do que o valor contratado para o plano.\n\nO que pode ter ocorrido:\n\n1 - Você cadastrou incorretamente o valor contratado do plano;\n\n2 - Você não cadastrou pacotes ao plano ou não registrou recargas;\n\n3 - Caso não seja nenhum dos pontos acima, pode ter ocorrido falha da sua opedradora. Recomendadmos que entre em contato com ela para averiguar.", false, true);
    }

    @Click(R.id.cardAlertaCadastraPlano)
    public void cardAlertaCadastraPlanoClick() {
        ((HomeActivity) getActivity()).changePage(HomeActivity.HomeTabNames.PLANO);
    }

    @Click(R.id.filterSettingsLayoutContent)
    public void filterSettingsLayoutContentClick() {
        if ( this.settingWindow != null ) this.settingWindow.show();
    }

    @Click(R.id.pconsumo_report_card)
    public void pConsumoReportCardClick() {
        // Verificandd se há plano cadastrado
        if ( meuPlano == null ) {
            AlertDialog.Builder dlgBuilder = new NotifyWindow(mContext).getBuilder();
            dlgBuilder.setTitle("Relatório");
            dlgBuilder.setMessage("Você precisa ter um plano cadastrado para utilizar este recurso.");
            dlgBuilder.setIcon(R.drawable.ic_warning_amber);
            dlgBuilder.setPositiveButton("Adicionar Plano", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Fechando dialogBox
                    dialogInterface.dismiss();

                    // Modificando para o fragment com os planos
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    homeActivity.changePage(HomeActivity.HomeTabNames.PLANO);
                }
            });
            dlgBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dlgBuilder.create().show();
        } else {
            final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                AlertDialog.Builder dlgBuilder = new NotifyWindow(mContext).getBuilder();
                dlgBuilder.setTitle("GPS Indisponível");
                dlgBuilder.setMessage("Você precisa está com o gps disponível para acessar essa funcionalidade.\n\nAtive o gps nas configurações do celular e tente novamente.");
                dlgBuilder.setIcon(R.drawable.ic_warning_amber);
                dlgBuilder.setPositiveButton("ATIVAR GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
                dlgBuilder.setNegativeButton("FECHAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dlgBuilder.create().show();
            }else{
                Intent it = new Intent(IntentMap.RELATORIO_CONSUMO);
                it.putExtra(RelatorioConsumoActivity.PLANO_EXTRA, meuPlano);
                getActivity().startActivity(it);
            }
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    // Listeners do Volley
    private Response.Listener filtrosSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            showLoadingLayout(false);
            try {
                // Checando a resposta
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                        new NotifyWindow(mContext).showErrorMessage("Painel de Consumo", response.getString("message"), false);

                    Log.e(LOG_TAG, response.getString("message"));
                    return;
                }

                // Definindo filtros para o spinner do SettingWindow...
                //Log.d(LOG_TAG, response.toString());
                JSONArray lista_tipos_plano = response.getJSONArray("lista_tipos_plano");
                TipoPlano[] tipoPlanosArr = new TipoPlano[lista_tipos_plano.length()];
                for ( int i = 0; i < lista_tipos_plano.length(); i++ ) {
                    JSONObject jsonObject = lista_tipos_plano.optJSONObject(i);
                    TipoPlano tipoPlano = new TipoPlano();
                    tipoPlano.setId( jsonObject.getInt("id_tipo_plano"));
                    tipoPlano.setDescricaoTipoPlano( jsonObject.getString("descricao_tipo_plano"));
                    tipoPlanosArr[i] = tipoPlano;
                }
                sharedPrefsUtil.setTipoPlanoList(tipoPlanosArr);
                initializeSettingWindowSpinner(tipoPlanosArr);
                //initializeSettingWindowSpinner(lista_tipos_plano);
                dlg_pconsumo_setting_periodo_spinner.setSelection( sharedPrefsUtil.getSelectedPConsumoFilterPosition() );
                dlgSettingsSpinnerEvent_OnItemSelected.onItemSelected(null, null, sharedPrefsUtil.getSelectedPConsumoFilterPosition(), 0);
                updateFilterDateRangeText();


                // Obtendo o plano do usuario (se ele for cadastrado)
                if ( new SharedPrefsUtil(mContext).getAccessKey() != null ) {
                    // Usuario cadastrado, obtendo o plano do usario
                    cardAlertaCadastraPlano.setVisibility(View.GONE);
                    pconsumo_report_card.setVisibility(View.VISIBLE);
                    showLoadingLayout(true);
                    JsonObjectRequest jsonObjectRequest = PlanoRequester.prepareObterPlanoUsuarioRequest(planoUsuarioSuccessListener, errorRequestListener, getActivity());
                    jsonObjectRequest.setTag(getClass().getName());
                    requestQueue.add(jsonObjectRequest);
                } else {
                    // Não esta logado... Apenasobtendo os dados estatisticos locais
                    meuPlano = null;
                    pconsumo_report_card.setVisibility(View.GONE);
                    cardAlertaCadastraPlano.setVisibility(View.VISIBLE);
                    LoadStaticsData();
                }
            } catch ( JSONException e ) {
                if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                    new NotifyWindow(mContext).showErrorMessage("FATAL Erro", Util.getMessageErrorFromExcepetion(e), false);

                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    };
    private Response.Listener planoUsuarioSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            showLoadingLayout(false);
            try {
                // Verificando o status (retorno)
                if ( !response.getString("status").equalsIgnoreCase("success") ) {
                    if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                        new NotifyWindow(mContext).showErrorMessage("Painel de Consumo", response.getString("message"), false);

                    Log.e(LOG_TAG, response.getString("message"));
                    return;
                }

                // Obtendo o objeto com os planos do usuario, e checando se existe...
                JSONObject user_plan_info = response.getJSONObject("user_plan_info");
                if ( user_plan_info.isNull("id_plano_usuario") ) {
                    // Não há planos cadastrado para este usuario
                    // Parando a execucao deste metodo
                    Log.i(LOG_TAG, "Não há planos cadastrados para este usuário");
                    if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() ) {
                        //new NotifyWindow(mContext).showWarningMessage("Painel de Consumo", "Não há planos cadastrados para este usuário", false);
                        pconsumo_report_card.setVisibility(View.GONE);
                        cardAlertaCadastraPlano.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Populando os dados da classe Plano do usuario
                    meuPlano = new Plano();
                    meuPlano.setIdPlano(user_plan_info.getInt("id_plano_usuario"));
                    meuPlano.setIdOperadora(user_plan_info.getInt("id_operadora"));
                    meuPlano.setNomeOperadora(user_plan_info.getString("nome_operadora"));
                    meuPlano.setNomePlano(user_plan_info.getString("nome_plano"));
                    meuPlano.setDescricaoModalidadePlano(user_plan_info.getString("descricao_modalidade_plano"));
                    meuPlano.setDescricaoTipoPlano(user_plan_info.getString("descricao_tipo_plano"));
                    meuPlano.setIdTipoPlano(user_plan_info.getInt("id_tipo_plano"));
                    meuPlano.setIdModalidadePlano(user_plan_info.getInt("id_modalidade_plano"));
                    meuPlano.setIdDDD(user_plan_info.getInt("id_ddd"));
                    meuPlano.setValorPlano(Float.parseFloat(user_plan_info.getString("valor_plano")));
                    meuPlano.setMinFixo(user_plan_info.getInt("limite_call_fixo"));
                    meuPlano.setMinIU(user_plan_info.getInt("limite_call_iu"));
                    meuPlano.setMinOO(user_plan_info.getInt("limite_call_oo"));
                    meuPlano.setMinMO(user_plan_info.getInt("limite_call_mo"));
                    meuPlano.setSmsInclusos(user_plan_info.getInt("limite_sms"));
                    meuPlano.setDtVencimento(user_plan_info.getInt("dt_vencimento"));
                    meuPlano.setIdPlanoReferencia(user_plan_info.getInt("id_plano_operadora_ref"));
                    meuPlano.setLimiteDadosWeb(user_plan_info.getInt("limite_net"));

                    // Definindo STR(String) dos limites
                    meuPlano.setSmsInclusosStr((meuPlano.getSmsInclusos()) < 0 ? "ilimiteado" : String.valueOf(meuPlano.getSmsInclusos()));
                    meuPlano.setMinFixoStr((meuPlano.getMinFixo()) < 0 ? "ilimiteado" : String.valueOf(meuPlano.getMinFixo()));
                    meuPlano.setMinIUStr((meuPlano.getMinIU()) < 0 ? "ilimiteado" : String.valueOf(meuPlano.getMinIU()));
                    meuPlano.setMinOOStr((meuPlano.getMinOO()) < 0 ? "ilimiteado" : String.valueOf(meuPlano.getMinOO()));
                    meuPlano.setMinMOStr((meuPlano.getMinMO()) < 0 ? "ilimiteado" : String.valueOf(meuPlano.getMinMO()));
                    meuPlano.setLimiteDadosWebStr((meuPlano.getLimiteDadosWeb()) < 0 ? "ilimiteado" : String.valueOf(meuPlano.getLimiteDadosWeb()) + " MB");

                    // Salvando dados do plano...
                    sharedPrefsUtil.setMeuPlanoClass(meuPlano);
                }

                // All Done, executando acao para contabilizar os dados
                LoadStaticsData();
            } catch ( Exception e ) {
                if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                    new NotifyWindow(mContext).showErrorMessage("FATAL Erro", Util.getMessageErrorFromExcepetion(e), false);

                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    };
    private Response.ErrorListener errorRequestListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showLoadingLayout(false);
            if ( error == null ) return;
            Log.e(LOG_TAG, "error: " + error.getMessage());
            if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                new NotifyWindow(mContext).showErrorMessage("Erro", error.getMessage(), false);
        }
    };

    /* ------------------------------------------------------------------------------------------ */
    // Metodos privados da classe
    private void initializePieViewColorConfig() {
        this.pieView_sms.setPercentageBackgroundColor(Color.parseColor("#26B362"));
        this.pieView_call.setPercentageBackgroundColor(Color.parseColor("#FFA500"));
        this.pieView_net.setPercentageBackgroundColor(Color.parseColor("#0079DB"));
    }

    private void updateFilterDateRangeText() {
        // Obtendo os valores de de range atual e definindo na tela/interface
        StringBuilder newRangeTextBuilder = new StringBuilder();
        newRangeTextBuilder.append(dlg_pconsumo_setting_range_start_text.getText().toString() );
        newRangeTextBuilder.append(" à ");
        newRangeTextBuilder.append(dlg_pconsumo_setting_range_end_text.getText().toString() );
        filterDateRangeText.setText( newRangeTextBuilder.toString() );
    }

    private void showLoadingLayout(boolean show) {
        if ( layoutContentBody == null || layoutProgress == null )
            return;

        if ( show ) {
            layoutContentBody.setVisibility(View.GONE);
            layoutProgress.setVisibility(View.VISIBLE);
        } else {
            layoutProgress.setVisibility(View.GONE);
            layoutContentBody.setVisibility(View.VISIBLE);
        }
    }

    private void initializeSettingWindow() {
        // Elementos visuais
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Filtro de período/datas");
        builder.setView(dialog_painel_consumo_setting_layout);
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final int selectedPosition = dlg_pconsumo_setting_periodo_spinner.getSelectedItemPosition();
                if ( selectedPosition <= 3 ) sharedPrefsUtil.setSelectedPConsumoFilterPosition(selectedPosition);

                updateFilterDateRangeText();
                LoadStaticsData();
                dialogInterface.dismiss();
            }
        });
        settingWindow = builder.create();
    }

    private void initializeSettingWindowSpinner(TipoPlano[] tipoPlanosArr) {
        this.tipoStringList = new ArrayList<>();
        this.tipoPlanoList = new ArrayList<>();

        // Modalidando lista
        for ( int i = 0; i < tipoPlanosArr.length; i++ ) {
            tipoPlanoList.add( tipoPlanosArr[i] );
            tipoStringList.add( tipoPlanosArr[i].getDescricaoTipoPlano() );
        }

        // Adicionando o último ('Personalizado')
        TipoPlano personalizado = new TipoPlano();
        personalizado.setId(0);
        personalizado.setDescricaoTipoPlano("Personalizado");
        tipoPlanoList.add( personalizado );
        tipoStringList.add("Personalizado");

        // Montando Adapter e anexando-o ao Spinner
        final ArrayAdapter<String> settingsSpinnerAdapter = new ArrayAdapter<>(mContext, R.layout.simple_spinner_string_item, tipoStringList);
        dlg_pconsumo_setting_periodo_spinner.setAdapter(settingsSpinnerAdapter);

        // Definindo evento de click (para disparar ao selecionar o periodo)
        dlg_pconsumo_setting_periodo_spinner.setOnItemSelectedListener(this.dlgSettingsSpinnerEvent_OnItemSelected);
    }

    private AdapterView.OnItemSelectedListener dlgSettingsSpinnerEvent_OnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            TipoPlano tipoPlano = tipoPlanoList.get(position);
            final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            if ( tipoPlano.getId() == 0 ) { // Personalizado
                // Exibindo uma nova tela, para o usuário poder escolher o periodo desejado
                // Exibindo tela para a definição do range a ser exbido
                DatePickers.DateRangeDatePicker dateRangeDatePicker = new DatePickers.DateRangeDatePicker(getActivity());
                dateRangeDatePicker.setTitle("Range Personalizado");

                // Definindo eventos...
                dateRangeDatePicker.setPositiveEvent(new DatePickers.DateRangeDatePickerInterface.OnPositiveEvent() {
                    @Override
                    public void onEvent(Date firstDate, Date secondDate, DialogInterface dialogInterface, int i) {
                        dlg_pconsumo_setting_range_start_text.setText(sdf.format(firstDate));
                        dlg_pconsumo_setting_range_end_text.setText(sdf.format(secondDate));
                        dialogInterface.dismiss();
                    }
                });
                dateRangeDatePicker.setNegativeEvent(new DatePickers.DateRangeDatePickerInterface.OnNegativeEvent() {
                    @Override
                    public void onEvent(DialogInterface dialogInterface, int i) {
                        dlg_pconsumo_setting_periodo_spinner.setSelection(2);
                        dlg_pconsumo_setting_range_start_text.setText( sdf.format(Util.DatesUtil.getDecrementedMonth(1)) );
                        dlg_pconsumo_setting_range_end_text.setText( sdf.format(Util.DatesUtil.getCurrentDate()) );
                        dialogInterface.dismiss();
                    }
                });

                // Definindo valores padrao atual
                Date startDate = null;
                try {
                    startDate = sdf.parse(dlg_pconsumo_setting_range_start_text.getText().toString());
                    Date endDate = sdf.parse(dlg_pconsumo_setting_range_end_text.getText().toString());
                    dateRangeDatePicker.setInitialDateRangeValues(startDate, endDate);
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Error pasing date: " + Util.getMessageErrorFromExcepetion(e));
                }


                // Exibindo Window
                dateRangeDatePicker.showDialogWindow();
            } else {
                // Verificando o tipo e definindo ação com base no selecionado
                Log.i(LOG_TAG, "Setting Window:" + tipoPlano.getDescricaoTipoPlano().toLowerCase());
                switch ( tipoPlano.getDescricaoTipoPlano().toLowerCase() ) {
                    case "diário":
                    case "diario":
                        dlg_pconsumo_setting_range_start_text.setText( sdf.format(Util.DatesUtil.getDecrementedDate(1)) );
                        dlg_pconsumo_setting_range_end_text.setText( sdf.format(Util.DatesUtil.getCurrentDate()) );
                        break;
                    case "semanal":
                        dlg_pconsumo_setting_range_start_text.setText( sdf.format(Util.DatesUtil.getDecrementedDate(7)) );
                        dlg_pconsumo_setting_range_end_text.setText( sdf.format(Util.DatesUtil.getCurrentDate()) );
                        break;
                    case "mensal":
                        dlg_pconsumo_setting_range_start_text.setText( sdf.format(Util.DatesUtil.getDecrementedMonth(1)) );
                        dlg_pconsumo_setting_range_end_text.setText( sdf.format(Util.DatesUtil.getCurrentDate()) );
                        break;
                    case "anual":
                        dlg_pconsumo_setting_range_start_text.setText( sdf.format(Util.DatesUtil.getDecrementedMonth(12)) );
                        dlg_pconsumo_setting_range_end_text.setText( sdf.format(Util.DatesUtil.getCurrentDate()) );
                        break;
                }
            }

            // Disparando execucao para atualizar a exibicao dos dados dentro este periodo...
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { /* Nothing to do */ }
    };

    private void LoadStaticsData() {
        swipeRefreshLayout.setRefreshing(false);
        Date[] dateLimites = GetCurrentDateLimites();
        if ( dateLimites == null ) return;

        // Executando dados...
        LoadStaticInfos loadStaticInfos = new LoadStaticInfos();
        loadStaticInfos.execute(dateLimites[0], dateLimites[1]);
    }

    private void PopulateCallCard(long usedSeconds) {
        // Checando se existe plano do usuario...
        if ( meuPlano == null ) {
            final String consummedMinText = PConsumoUtils.getFormmatedSecondsToStrTime( usedSeconds );
            call_limiteDoPlano.setText( getString(R.string.pconsumo_noplan) );
            call_restantes.setText( getString(R.string.pconsumo_noplan) );
            call_totalUsados.setText( consummedMinText );
            return; // Para a exeucao padrão deste metodo...
        }

        // Obtendo os limites de seconds do plano e a diferença de dias entre as datas e o % usado
        long limiteAtDay_InSeconds = PConsumoUtils.getLimiteAtDay(meuPlano.getMinMO() * 60, meuPlano);
        Date[] dateLimits = this.GetCurrentDateLimites();
        int diffrenceBetweenDateLimits = (int) PConsumoUtils.getDaysInsideRange(dateLimits[0], dateLimits[1]);
        long restSeconds;
        float percentValue;

        // Calculando consumo dentro deste periodo
        String totalMinText;
        if (PConsumoUtils.selectedModeIsSamePlanMode(dlg_pconsumo_setting_periodo_spinner.getSelectedItemPosition(), meuPlano)) {
            Log.d(LOG_TAG, "Selecionado");
            totalMinText = PConsumoUtils.getFormmatedSecondsToStrTime( (meuPlano.getMinMO() * 60) ) + String.format(" - %2d Dia(s)", diffrenceBetweenDateLimits);
            restSeconds = (meuPlano.getMinMO() * 60) - usedSeconds;
            percentValue = PConsumoUtils.getPercentValue((meuPlano.getMinMO() * 60), usedSeconds);
        } else {
            totalMinText = PConsumoUtils.getFormmatedSecondsToStrTime((limiteAtDay_InSeconds * diffrenceBetweenDateLimits)) + String.format(" - %2d Dia(s)", diffrenceBetweenDateLimits);
            restSeconds = (limiteAtDay_InSeconds * diffrenceBetweenDateLimits) - usedSeconds;
            percentValue = PConsumoUtils.getPercentValue((limiteAtDay_InSeconds * diffrenceBetweenDateLimits), usedSeconds);
        }
        if ( meuPlano.getMinMO() < 0 ) totalMinText = "Ilimitado";
        if ( restSeconds < 0 ) restSeconds = 0;
        final String consummedMinText = PConsumoUtils.getFormmatedSecondsToStrTime( usedSeconds );
        final String restantMinText = PConsumoUtils.getFormmatedSecondsToStrTime( restSeconds );

        // Definindo os textos do cards
        call_limiteDoPlano.setText(totalMinText);
        call_restantes.setText( restantMinText );
        call_totalUsados.setText( consummedMinText );

        // Verificando se os dados estão imprecisos
        try{
            long contrato;
            if (PConsumoUtils.selectedModeIsSamePlanMode(dlg_pconsumo_setting_periodo_spinner.getSelectedItemPosition(), meuPlano)) {
                Log.d(LOG_TAG, "Selecionado");
                contrato  = meuPlano.getMinMO() * 60;
            } else {
                contrato = (limiteAtDay_InSeconds * diffrenceBetweenDateLimits);
            }

            long consumido = usedSeconds;
            if(consumido > contrato){
                layoutDadosImprecisosCall.setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }

        // Definindo valor da PieView
        pieView_call.setmPercentage( percentValue );
        if ( meuPlano.getMinMO() < 0 ) pieView_call.setInnerText("LIG.\n-");
        else pieView_call.setInnerText("LIG.\n" + String.format("%.1f%%", percentValue));
    }

    private void PopulateWebCard(long usedBytes) {
        // Checando se existe plano do usuario...
        if ( meuPlano == null ) {
            final String consummedBytesText = Util.convertBytesToStr(mContext, usedBytes);
            net_limiteDoPlano.setText( getString(R.string.pconsumo_noplan) );
            net_restantes.setText( getString(R.string.pconsumo_noplan) );
            net_totalUsados.setText( consummedBytesText );
            return; // Para a exeucao padrão deste metodo...
        }

        // Obtendo os limites de seconds do plano e a diferença de dias entre as datas e o % usado
        long limiteAtDay_InBytes = PConsumoUtils.getLimiteAtDay((meuPlano.getLimiteDadosWeb() * 1024 * 1024), meuPlano);
        Date[] dateLimits = this.GetCurrentDateLimites();
        long diffrenceBetweenDateLimits = PConsumoUtils.getDaysInsideRange(dateLimits[0], dateLimits[1]);
        long restBytes;
        float percentValue;

        // Calculando consumo dentro deste periodo
        String totalBytesText;
        if (PConsumoUtils.selectedModeIsSamePlanMode(dlg_pconsumo_setting_periodo_spinner.getSelectedItemPosition(), meuPlano)) {
            long planBytes = (meuPlano.getLimiteDadosWeb() * 1024);
            planBytes = planBytes * 1024;
            Log.d("convertBytesToStr", "[FULL]Current Value-> " + planBytes);
            totalBytesText = Util.convertBytesToStr(mContext, planBytes) + String.format(" - %2d Dia(s)", diffrenceBetweenDateLimits);
            restBytes = planBytes - usedBytes;
            percentValue = PConsumoUtils.getPercentValue(planBytes, usedBytes);
        } else {
            long planBytes = limiteAtDay_InBytes;
            planBytes = planBytes * diffrenceBetweenDateLimits;
            Log.d("convertBytesToStr", "[DAYS]Current Value-> " + planBytes);
            totalBytesText = Util.convertBytesToStr(mContext, planBytes)  + String.format(" - %2d Dia(s)", diffrenceBetweenDateLimits);
            restBytes = planBytes - usedBytes;
            percentValue = PConsumoUtils.getPercentValue(planBytes, usedBytes);
        }

        Log.d("convertBytesToStr", "restante -> " + restBytes);

        if ( meuPlano.getLimiteDadosWeb() < 0 ) totalBytesText = "Ilimitado";
        if ( restBytes < 0 ) restBytes = 0;
        final String consummedBytesText = Util.convertBytesToStr(mContext, usedBytes);
        final String restantBytesText = Util.convertBytesToStr(mContext, restBytes);

        // Definindo os textos do cards
        net_limiteDoPlano.setText(totalBytesText);
        net_restantes.setText( restantBytesText );
        net_totalUsados.setText( consummedBytesText );

        // Verificando se os dados estão imprecisos
        try{
            long contrato = (meuPlano.getLimiteDadosWeb() * 1024) * 1024;
            long consumido = usedBytes;
            Log.e("DIFFF WEB", "contratado: " + contrato + " | consumido: " + consumido);
            if(consumido > contrato){
                layoutDadosImprecisosWeb.setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }

        // Definindo valor da PieView
        pieView_net.setmPercentage( percentValue );
        if ( meuPlano.getLimiteDadosWeb() < 0 ) pieView_net.setInnerText("NET.\n-");
        else pieView_net.setInnerText("NET.\n" + String.format("%.1f%%", percentValue));
    }

    private void PopulateSMSCard(long usedSMS) {
        // Checando se existe plano do usuario...
        if ( meuPlano == null ) {
            final String consummedSmsText = String.valueOf(usedSMS);
            sms_restantes.setText( getString(R.string.pconsumo_noplan) );
            sms_limiteDoPlano.setText( getString(R.string.pconsumo_noplan) );
            sms_totalEnviados.setText( consummedSmsText );
            return; // Para a exeucao padrão deste metodo...
        }

        // Obtendo os limites de seconds do plano e a diferença de dias entre as datas e o % usado
        long limiteAtDay_InSms = PConsumoUtils.getLimiteAtDay(meuPlano.getSmsInclusos(), meuPlano);
        Date[] dateLimits = this.GetCurrentDateLimites();
        int diffrenceBetweenDateLimits = (int) PConsumoUtils.getDaysInsideRange(dateLimits[0], dateLimits[1]);
        long restSms;
        float percentValue;

        // Calculando consumo dentro deste periodo
        String totalSmsText;
        if ( PConsumoUtils.selectedModeIsSamePlanMode(dlg_pconsumo_setting_periodo_spinner.getSelectedItemPosition(), meuPlano) ) {
            totalSmsText = meuPlano.getSmsInclusos() + String.format(" - %2d Dia(s)", diffrenceBetweenDateLimits);
            restSms = meuPlano.getSmsInclusos() - usedSMS;
            percentValue = PConsumoUtils.getPercentValue(meuPlano.getSmsInclusos(), usedSMS);
        } else {
            totalSmsText = Math.ceil(limiteAtDay_InSms * diffrenceBetweenDateLimits) + String.format(" - %2d Dia(s)", diffrenceBetweenDateLimits);
            restSms = (limiteAtDay_InSms * diffrenceBetweenDateLimits) - usedSMS;
            percentValue = PConsumoUtils.getPercentValue((limiteAtDay_InSms * diffrenceBetweenDateLimits), usedSMS);
        }
        if ( meuPlano.getSmsInclusos() < 0 ) totalSmsText = "Ilimitado";
        if ( restSms < 0 ) restSms = 0;
        final String consummedSmsText = String.valueOf(usedSMS);
        final String restantSmsText = String.valueOf(restSms);

        // Definindo os textos do cards
        sms_limiteDoPlano.setText(totalSmsText);
        sms_restantes.setText( restantSmsText );
        sms_totalEnviados.setText( consummedSmsText );

        // Verificando se os dados estão imprecisos
        try{
            int contrato = meuPlano.getSmsInclusos();
            long consumido = usedSMS;
            Log.e("DIFFF SMS", "contratado: " + contrato + " | consumido: " + consumido);
            if(consumido > contrato){
                layoutDadosImprecisosSms.setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }

        // Definindo valor da PieView
        pieView_sms.setmPercentage( percentValue );
        if ( meuPlano.getSmsInclusos() < 0 ) pieView_sms.setInnerText("SMS.\n-");
        else pieView_sms.setInnerText("SMS.\n" + String.format("%.1f%%", percentValue));
    }

    private Date[] GetCurrentDateLimites() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date[] dateLimits = new Date[2];
            dateLimits[0] = sdf.parse(dlg_pconsumo_setting_range_start_text.getText().toString() + " 00:00:00");
            dateLimits[1] = sdf.parse(dlg_pconsumo_setting_range_end_text.getText().toString() + " 23:59:59");
            Log.d(LOG_TAG, "StartDate -> " + sdf.format(dateLimits[0]));
            Log.d(LOG_TAG, "EndDate -> " + sdf.format(dateLimits[1]));
            return dateLimits;
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Erro converting date:" + Util.getMessageErrorFromExcepetion(e));
            return null;
        }
    }

    private void startTutorial(){
        Log.d(LOG_TAG, "Starting tutorial");
        Activity baseActivity = getActivity();
        try {
            Tutorial tutorial = new Tutorial(baseActivity);
            List<TutorialItem> tutorialItemList = new ArrayList<>();
            tutorialItemList.add( new TutorialItem(getActivity(), R.id.filterSettingsLayoutContent, "Filtro do período", "Filtro para a exibição dos dados."));
            tutorialItemList.add( new TutorialItem(getActivity(), R.id.call_header_periodo_text, "Seção/Grupo", "Cada card, é separado pr grupo, totalizando três ao todo, 'Ligação, Net e SMS'."));
            tutorialItemList.add( new TutorialItem(pieView_net, "Gráfico de uso", "Representação visual do uso dos dados com base no período informado."));
            tutorialItemList.add( new TutorialItem(net_restantes, "Contéudo do card", "Aqui se encontra o contéudo do card, com as informações referentes ao grupo deste"));
            tutorial.setItemList( tutorialItemList );
            tutorial.setListener(new Tutorial.TutorialListener() {
                @Override
                public void onDone() {
                    Log.d(LOG_TAG, "Tutorial Is Done");
                    new SharedPrefsUtil(mContext).setPConsumoIsFirstVisualization(false);
                }
            });
            tutorial.startTutorial();
        } catch (TutorialException e) {
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    // Classe AsyncTask para obtenção dos dados direto do Banco de Dados
    private class LoadStaticInfos extends AsyncTask<Date, Void, Object> {
        private class ExecutionResponseClass {
            private long totalSmsEnviados = 0;
            private long totalBytesTransferidos = 0;
            public long totalSegundosUsados = 0;
        }

        OrmLiteHelper orm;

        @Override
        public void onPreExecute() {
            orm = OrmLiteHelper.getInstance(mContext);

            // Exibindo loadingBox
            showLoadingLayout(true);
        }

        @Override
        protected Object doInBackground(Date... params) {
            if ( params.length < 2 ) return null;
            Date startDate = params[0];
            Date endDate = params[1];

            ExecutionResponseClass responseClass = new ExecutionResponseClass();

            // Obtendo os dados do banco
            try {
                // -> Obtendo os dados de ligacao
                List<CallMonitor> callMonitorData = orm.getCallMonitorDao().queryBuilder().
                        where().between("CALL_DAT_CAD", startDate, endDate).
                        and().eq("CALL_TYPE", "OUTGOING").query();
                Log.d(LOG_TAG, "PainelConsumoFragment: CALLCount -> " + callMonitorData.size());
                for (CallMonitor callInfo : callMonitorData) {
                    responseClass.totalSegundosUsados += callInfo.getElapsedTime();
                }

                // -> Obtendo os dados de SMS
                List<SmsMonitor> smsMonitorData = orm.getSmsMonitorDao().queryBuilder().
                        where().between("SMS_DAT_CAD", startDate, endDate).query();
                Log.d(LOG_TAG, "PainelConsumoFragment: SMSCount -> " + smsMonitorData.size());
                responseClass.totalSmsEnviados = smsMonitorData.size();

                // -> Obtendo os dados de NEWORK
                responseClass.totalBytesTransferidos = new TrafficMonitor(mContext).getTotalMobileDataTransfer(startDate, endDate);
            } catch (SQLException e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                responseClass = null;
            }

            return responseClass;
        }

        @Override
        public void onPostExecute(Object result) {
            showLoadingLayout(false);

            try {
                if (result == null || pieView_call == null )
                    return;


                // Realizando um typeCast para o objeto correto (e não um generico)
                ExecutionResponseClass r = (ExecutionResponseClass) result;

                // Montando exibicao dos cards com base nos resultado obtido
                PopulateCallCard(r.totalSegundosUsados);
                PopulateWebCard(r.totalBytesTransferidos);
                PopulateSMSCard(r.totalSmsEnviados);

                // Checando se é a primeira visualização...
                if ( new SharedPrefsUtil(mContext).getPConsumoIsFirstVisualization() ) {
                    startTutorial();
                }

                Log.i(LOG_TAG, "All Done");
            } catch (Exception e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    }

    /* ------------------------------------------------------------------------------------------ */
    // Classe com 'Utils' de uso exclusivo desta classe...
    public static class PConsumoUtils {
        public static boolean selectedModeIsSamePlanMode(final int selPosition, Plano plano) {
            final String descricaoMode = plano.getDescricaoTipoPlano().toLowerCase();
            Log.d(PConsumoUtils.class.getName(), String.format("Pos -> %d, Plan -> %s", selPosition, descricaoMode));
            if ( selPosition == 0 && descricaoMode.contains("diario") ) return true;
            else if ( selPosition == 1 && descricaoMode.contains("semanal") ) return true;
            else if ( selPosition == 2 && descricaoMode.contains("mensal") ) return true;
            else if ( selPosition == 3 && descricaoMode.contains("anual") ) return true;
            else return false;
        }

        public static String getFormmatedSecondsToStrTime(long seconds) {
            Log.d(PConsumoUtils.class.getName(), "Seconds Received -> " + seconds);
            long hours = seconds / 3600;
            long minutes = ((seconds / 60) % 60);
            long secs = (seconds % 60);
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }

        public static long getDaysInsideRange(Date start, Date end) {
            // Retornando a diferença de dias entre as datas
            return ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
        }

        // Nota: Os limites seram sempre afinado com base no mes atual,
        //       só muda em caso de 'limite_diario'....
        // Nota: No futuro, pretendo adicionar o suporte a gerar estes dados com base na data
        //       de vencimento, entre outros... No momento, apenas um modo mais simples
        public static long getLimiteAtDay(long limite, Plano plano) {
            long limiteAtDay = 0;
            final String descricaoTipoPlano = plano.getDescricaoTipoPlano().toLowerCase();
            if ( descricaoTipoPlano.contains("diario") )
                limiteAtDay = limite;
            else if ( descricaoTipoPlano.contains("semanal") ) {
                limiteAtDay = (limite <= 0) ? 0 : (limite / 7);
            }
            else if ( descricaoTipoPlano.contains("mensal") ){ // Mensal
                Calendar c = Calendar.getInstance();
                int monthMaxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                limiteAtDay = (limite <= 0) ? 0 : (limite / monthMaxDays);
            } else { // Anual
                Calendar c = Calendar.getInstance();
                int yearMaxDays = c.getActualMaximum(Calendar.DAY_OF_YEAR);
                limiteAtDay = (limite <= 0) ? 0 : (limite / yearMaxDays);
            }

            return limiteAtDay;
        }

        public static float getPercentValue(long total, long used) {
            if ( used >= total ) return 100f;
            else if ( total == 0 ) return 100f;
            else if ( used == 0 ) return 0.1f;
            float base = (float) used / total;
            return (base * 100f);
        }

        public static float getPercentValue(BigDecimal total, BigDecimal used) {
            if ( used.compareTo(total) >= 0 ) return 100f;
            else if ( total.compareTo(BigDecimal.ZERO) == 0 ) return 100f;
            else if ( used.compareTo(BigDecimal.ZERO) == 0 ) return 0.1f;
            float base = (float) used.floatValue() / total.floatValue();
            return (base * 100f);
        }
    }
}
