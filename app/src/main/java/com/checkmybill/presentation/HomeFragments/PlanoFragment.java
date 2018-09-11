package com.checkmybill.presentation.HomeFragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.Plano;
import com.checkmybill.felipecode.Views.Avaliar_Plano;
import com.checkmybill.presentation.AvaliaPlanoActivity;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.CreateUserPlanActivity;
import com.checkmybill.presentation.GerCreditosPlanoActivity;
import com.checkmybill.presentation.GerPacotesActivity;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.request.PlanoRequester;
import com.checkmybill.tutorial.Tutorial;
import com.checkmybill.tutorial.TutorialException;
import com.checkmybill.tutorial.TutorialItem;
import com.checkmybill.util.Connectivity;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;
import com.checkmybill.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Espe on 03/08/2016 -> 23/11/2016.
 */

@EFragment(R.layout.fragment_plano)
public class PlanoFragment extends BaseFragment {
    //@ViewById(R.id.btnAvaliarPlano) protected  Button btnAvaliarPlano;

    @ViewById(R.id.btnCreditos) LinearLayout btnCreditos;
    @ViewById(R.id.btnSubstituirPlano) protected LinearLayout btnSubstituirPlano;
    @ViewById(R.id.layoutNoPlan) protected LinearLayout layoutNoPlan;
    @ViewById(R.id.layoutWithPlan) protected LinearLayout layoutWithPlan;
    @ViewById(R.id.layoutLoading) protected LinearLayout layoutLoading;
    @ViewById(R.id.layoutError) protected LinearLayout layoutError;
    @ViewById(R.id.layoutNotLogged) protected LinearLayout layoutNotLogged;

    // Elementos do Leyout de ERROR
    @ViewById(R.id.tvwErrorMsg) protected TextView tvwErrorMsg;

    // Elementos do Card para exibir as informações sobre o meu plano
    @ViewById(R.id.nome_plano) protected TextView myPlanCard_nomePlano;
    @ViewById(R.id.modalidade_plano) protected TextView myPlanCard_modalidadePlano;
    @ViewById(R.id.nome_operadora) protected  TextView myPlanCard_nomeOperadora;
    @ViewById(R.id.preco_plano) protected  TextView myPlanCard_precoPlano;
    @ViewById(R.id.prepago_det_container) protected  LinearLayout myPlanCard_prepagoDetailsContainer;
    @ViewById(R.id.num_recargas_realizadas) protected  TextView myPlanCard_numRecargasRealizadas;
    @ViewById(R.id.num_pacotes_anexados) protected  TextView myPlanCard_numPacotesAnexados;

    private SharedPrefsUtil sharedPrefsUtil;
    private Tutorial tutorial;
    private Context mContext;
    private RequestQueue requestQueue;
    private Plano meuPlano;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)


    //Felipe Torres 11/09;2018
    static final int REQUEST_AVALIAR_PLANO = 1010;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.LOG_TAG = "PlanoFragment";
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(getActivity());
        mContext = getContext();
        this.sharedPrefsUtil = new SharedPrefsUtil(mContext);

    }


    @Override
    public void onResume() {
        super.onResume();
        sendRequestObterPlanoUsuario();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
        requestQueue.cancelAll(getClass().getName());

        if ( tutorial != null && tutorial.isVisible() ) tutorial.hideNow();
    }

    /* ------------------------------------------------------------------------------------------ */
    // Eventos dos elementos/Views
    @Click(R.id.noLoggedArea_DoLoginBtn)
    public void noLoggedAreaDoLoginBtnClick() {
        Intent it = new Intent(IntentMap.INTRO);
        it.putExtra("HIDDEN_JUMP_BUTTON", true);
        getActivity().startActivity(it);
    }

    @Click(R.id.btnCreditos)
    public void btnCreditosClick() {
        Intent it = new Intent(IntentMap.CREDITOS_PLANO);
        getActivity().startActivityForResult(it, GerCreditosPlanoActivity.REQUEST_CODE);
    }

    @Click(R.id.layoutBtnCadastrarPlano)
    public void layoutBtnCadastrarPlanoClick() {
        Intent it = new Intent(IntentMap.CONSULTA_PLANOS_OPERADORA);
        getActivity().startActivityForResult(it, CreateUserPlanActivity.REQUEST_CODE);
    }

    @Click(R.id.btnAvaliarPlano)
    public void btnAvaliarPlanoClick() {
        // Realiza uma avaliação com os seus recursos atuais para obter o melhor plano ao seu
        // perfil...
        //Intent it = new Intent(IntentMap.COMPARACAO_PLANO);
        //it.putExtra("PLANO", meuPlano);
        //getActivity().startActivity(it);

        //new NotifyWindow(mContext).showWarningMessage("Avaliar Plano", "Serviço indisponível no momento", false, true);

//        Intent it = new Intent(IntentMap.AVALIA_PLANO);
//        getActivity().startActivityForResult(it, AvaliaPlanoActivity.REQUEST_CODE);


        //Felipe Torres
        Intent intent_avaliar_plano = new Intent(getActivity(), Avaliar_Plano.class);
        startActivityForResult(intent_avaliar_plano, REQUEST_AVALIAR_PLANO);
    }

    @Click(R.id.btnPacotes)
    public void btnPacotesClick() {
        //new NotifyWindow(mContext).showWarningMessage("Pacotes", "Serviço indisponível no momento", false, true);
        Intent it =  new Intent(IntentMap.PACOTES_PLANO);
        it.putExtra(GerPacotesActivity.PLANO_EXTRA, meuPlano);  // Plano Selecionado
        getActivity().startActivityForResult(it, GerPacotesActivity.REQUEST_CODE);
    }

    private AlertDialog substituirPlanoQuestionDialog;
    @Click(R.id.btnSubstituirPlano)
    public void btnSubstituirPlanoClick() {
        // Checando se cotem pacotes/creditos
        if ( substituirPlanoQuestionDialog != null )
            substituirPlanoQuestionDialog.dismiss();

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Substituir Plano");
        builder.setMessage("Qual plano base você deseja usar?");
        builder.setPositiveButton("Meu Plano", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Abrindo a tela de cadastro com o plano selecionado
                Intent it = new Intent(IntentMap.CREATE_USER_PLAN);
                it.putExtra(CreateUserPlanActivity.PLANO_EXTRA, meuPlano);  // Plano Selecionado
                getActivity().startActivityForResult(it, CreateUserPlanActivity.REQUEST_CODE);
            }
        });
        builder.setNegativeButton("Novo Plano", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Checando se deve avisar da substituicao dos pacotes & recarga
                if ( meuPlano.getNumPacotes() > 0 || meuPlano.getNumRecargas() > 0 ) {
                    // Avisando ao usuario sobre este ponto e questionando se ele deseja
                    // continuar a operação...
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
                    builder2.setTitle("Substituição do Plano");
                    builder2.setMessage("Você tem dados de pacotes & recargas cadasatrados, substituir o plano ira remover estes dados.\n\n Deseja continuar?");
                    builder2.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Diaprando evento para cadastrar um novo plano
                            Intent it = new Intent(IntentMap.CONSULTA_PLANOS_OPERADORA);
                            it.putExtra("REMOVER_PACOTES_RECARGAS", true);
                            getActivity().startActivityForResult(it, CreateUserPlanActivity.REQUEST_CODE);
                        }
                    });
                    builder2.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Nothind to do,
                            // Apenas cancelando a ação...
                        }
                    });
                    builder2.create().show();
                } else {
                    // Diaprando evento para cadastrar um novo plano
                    layoutBtnCadastrarPlanoClick();
                }
            }
        });
        substituirPlanoQuestionDialog = builder.create();
        substituirPlanoQuestionDialog.show();
    }

    @Click(R.id.myPlanCardItem)
    public void myPlanCardItemClick() {
        // Exibindo os detalhes do plano do usuário...
    }

    @LongClick(R.id.myPlanCardItem)
    public void myPlanCardItemOnLongClick() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_plano_item, null);
        CardView cardView = (CardView) view.findViewById(R.id.cardItem);
        TextView buffTV = (TextView) view.findViewById(R.id.planoNome);

        // Definindo opcoes para este modo de visualização
        cardView.setClickable(false);
        cardView.setUseCompatPadding(false);
        cardView.setRadius(0f);

        // -> Populando o nome do plano
        buffTV.setBackgroundColor(getResources().getColor(R.color.md_grey_100));
        buffTV.setText( meuPlano.getNomePlano() );

        // -> Populando a modalidade
        buffTV = (TextView) view.findViewById(R.id.modalidade_plano);
        buffTV.setText( meuPlano.getDescricaoModalidadePlano() );

        // -> Populando a operadora
        buffTV = (TextView) view.findViewById(R.id.nome_operadora);
        buffTV.setText( meuPlano.getNomeOperadora() );

        // -> Populando a Descricao
        buffTV = (TextView) view.findViewById(R.id.descricao_plano);
        buffTV.setText( meuPlano.getObservacao() );

        // -> Populando a Preco
        buffTV = (TextView) view.findViewById(R.id.preco_plano);
        buffTV.setText( String.format("R$ %.2f", meuPlano.getValorPlano()) );

        // -> Populando a Web
        buffTV = (TextView) view.findViewById(R.id.limite_dados);
        buffTV.setText( meuPlano.getLimiteDadosWebStr() );

        // -> Populando a SMS
        buffTV = (TextView) view.findViewById(R.id.limite_sms);
        buffTV.setText( meuPlano.getSmsInclusosStr() );

        // -> Populando a Minutos
        buffTV = (TextView) view.findViewById(R.id.minutagens);
        buffTV.setText( String.format("%s/%s/%s/%s", meuPlano.getMinMOStr(), meuPlano.getMinOOStr(), meuPlano.getMinFixoStr(), meuPlano.getMinIUStr()) );

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

    /* ------------------------------------------------------------------------------------------ */
    // Listeners do Volley
    private Response.Listener verifyUserPlanResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            layoutLoading.setVisibility(View.GONE);
            try {
                // Verificando o status (retorno)
                if ( !response.getString("status").equalsIgnoreCase("success") ) {
                    Log.e(LOG_TAG, response.getString("message"));

                    // Exibindo mensagem de erro
                    if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                        new NotifyWindow(mContext).showErrorMessage("Plano", response.getString("message"), false);

                    layoutError.setVisibility(View.VISIBLE);
                    tvwErrorMsg.setText(String.format("Error: %s", response.getString("message")));
                    return;
                }

                // Obtendo o objeto com os planos do usuario, e checando se existe...
                JSONObject user_plan_info = response.getJSONObject("user_plan_info");
                if ( user_plan_info.isNull("id_plano_usuario") ) {
                    // Não há planos cadastrado para este usuario
                    // Parando a execucao deste metodo
                    // Exibindo mensagem
                    layoutWithPlan.setVisibility(View.GONE);
                    layoutNoPlan.setVisibility(View.VISIBLE);
                    return;
                }

                // Populando os dados da classe Plano do usuario
                meuPlano = new Plano();
                meuPlano.setIdPlano(user_plan_info.getInt("id_plano_usuario") );
                meuPlano.setIdOperadora(user_plan_info.getInt("id_operadora"));
                meuPlano.setNomeOperadora(user_plan_info.getString("nome_operadora"));
                meuPlano.setNomePlano(user_plan_info.getString("nome_plano"));
                meuPlano.setDescricaoModalidadePlano(user_plan_info.getString("descricao_modalidade_plano"));
                meuPlano.setDescricaoTipoPlano(user_plan_info.getString("descricao_tipo_plano"));
                meuPlano.setIdTipoPlano(user_plan_info.getInt("id_tipo_plano"));
                meuPlano.setIdModalidadePlano(user_plan_info.getInt("id_modalidade_plano"));
                meuPlano.setIdDDD(user_plan_info.getInt("id_ddd"));
                meuPlano.setValorPlano( Float.parseFloat(user_plan_info.getString("valor_plano")) );
                meuPlano.setMinFixo(user_plan_info.getInt("limite_call_fixo"));
                meuPlano.setMinIU(user_plan_info.getInt("limite_call_iu"));
                meuPlano.setMinOO(user_plan_info.getInt("limite_call_oo"));
                meuPlano.setMinMO(user_plan_info.getInt("limite_call_mo"));
                meuPlano.setSmsInclusos(user_plan_info.getInt("limite_sms"));
                meuPlano.setDtVencimento(user_plan_info.getInt("dt_vencimento"));
                meuPlano.setIdPlanoReferencia(user_plan_info.getInt("id_plano_operadora_ref"));
                meuPlano.setLimiteDadosWeb(user_plan_info.getInt("limite_net"));

                // Definindo STR(String) dos limites
                meuPlano.setSmsInclusosStr((meuPlano.getSmsInclusos()) < 0 ? "ilimitado" : String.valueOf(meuPlano.getSmsInclusos()));
                meuPlano.setMinFixoStr((meuPlano.getMinFixo()) < 0 ? "ilimitado" : String.valueOf(meuPlano.getMinFixo()));
                meuPlano.setMinIUStr((meuPlano.getMinIU()) < 0 ? "ilimitado" : String.valueOf(meuPlano.getMinIU()));
                meuPlano.setMinOOStr((meuPlano.getMinOO()) < 0 ? "ilimitado" : String.valueOf(meuPlano.getMinOO()));
                meuPlano.setMinMOStr((meuPlano.getMinMO()) < 0 ? "ilimitado" : String.valueOf(meuPlano.getMinMO()));
                meuPlano.setLimiteDadosWebStr((meuPlano.getLimiteDadosWeb()) < 0 ? "ilimitado" : String.valueOf(meuPlano.getLimiteDadosWeb()) + " MB");

                // Pacotes e recargas
                meuPlano.setNumPacotes(user_plan_info.getInt("num_pacotes_anexados"));
                meuPlano.setValorTotalPacotes(Float.parseFloat(user_plan_info.getString("valor_total_pacotes")));
                meuPlano.setNumRecargas(user_plan_info.getInt("num_recargas_realizadas"));
                meuPlano.setValorTotalRecargas(Float.parseFloat(user_plan_info.getString("valor_gasto_recargas")));

                // Salvando os dados de plano do usuario
                sharedPrefsUtil.setMeuPlanoClass(meuPlano);

                // Completando dados
                PopulateScreenElements(meuPlano);
            } catch ( Exception e ) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
                if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                    new NotifyWindow(mContext).showErrorMessage("FATAL Erro", Util.getMessageErrorFromExcepetion(e), false);
            }
        }
    };

    private Response.ErrorListener verifyUserPlanErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            /*try {
                String errorMsg = Util.getMessageErrorFromExcepetion(error);
                if (error.networkResponse.data != null) {
                    errorMsg += " | " + new String(error.networkResponse.data);
                }

                if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                    new NotifyWindow(mContext).showErrorMessage("Erro", errorMsg, false);

                Log.e(LOG_TAG, "error: " + errorMsg);
            } catch (Exception e) {
                if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() )
                    new NotifyWindow(mContext).showErrorMessage("FATAL Erro", Util.getMessageErrorFromExcepetion(e), false);

                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }*/
            layoutNotLogged.setVisibility(View.GONE);
            layoutLoading.setVisibility(View.GONE);
            layoutError.setVisibility(View.VISIBLE);
            String errMessage;
            if ( error instanceof NetworkError || error instanceof NoConnectionError || error instanceof TimeoutError)
                errMessage = "Não foi possível se conectar, verifique sua conexão.";
            else if ( error instanceof ServerError)
                errMessage = "O endereço não foi localizado, tente de novo mais tarde.";
            else
                errMessage = "Houve um problema ao se conectar com o servidor.";

            new NotifyWindow(mContext).showErrorMessage("Erro", errMessage, false);
        }
    };

    /* ------------------------------------------------------------------------------------------ */
    // Metodos privados da classe
    private void startTutorial() {
        Log.d(LOG_TAG, "Starting tutorial");
        Activity baseActivity = getActivity();
        try {
            tutorial = new Tutorial(baseActivity);
            List<TutorialItem> tutorialItemList = new ArrayList<>();
            tutorialItemList.add( new TutorialItem(myPlanCard_nomePlano, "Card do Plano", "Aqui se encontra um resumo com as informações básicas sobre o plano que você definiu para o seu uso."));
            tutorialItemList.add( new TutorialItem(getActivity(), R.id.bottomButtonContainer, "Ações para o Plano", "Contém as princípais funções para manipular o seu plano, como substituir, avaliar ou adicionar créditos (apenas PréPagos & Controle)."));
            tutorial.setItemList( tutorialItemList );
            tutorial.setListener(new Tutorial.TutorialListener() {
                @Override
                public void onDone() {
                    Log.d(LOG_TAG, "Tutorial Is Done");
                    new SharedPrefsUtil(mContext).setPlanoIsFirstVisualization(false);
                }
            });
            tutorial.startTutorial();
        } catch (TutorialException e) {
            Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
        }
    }

    private void sendRequestObterPlanoUsuario() {
        // Checando se o usuario estál logado
        final String accessKey = new SharedPrefsUtil(mContext).getAccessKey();
        if ( accessKey == null || accessKey.length() <= 0 ) {
            // usuário não está logado
            layoutNotLogged.setVisibility(View.VISIBLE);
            layoutNoPlan.setVisibility(View.GONE);
            layoutWithPlan.setVisibility(View.GONE);
            layoutLoading.setVisibility(View.GONE);
            return;
        }

        // Checando o tipo de conexao (se for WiFi ou se o plano não foi obtido, baixa-lo novamente)
        this.meuPlano = sharedPrefsUtil.getMeuPlanoClass();
        if ( this.meuPlano == null || Connectivity.isConnectedWifi(mContext) ) {
            // Modificando o layout a ser exibio (Exibindo o loading)
            layoutNoPlan.setVisibility(View.GONE);
            layoutWithPlan.setVisibility(View.GONE);
            layoutLoading.setVisibility(View.VISIBLE);

            JsonObjectRequest jsonObjectRequest = PlanoRequester.prepareObterPlanoUsuarioRequest(false, verifyUserPlanResponseListener, verifyUserPlanErrorListener, getActivity());
            jsonObjectRequest.setTag(getClass().getName());
            requestQueue.add(jsonObjectRequest);
        } else {
            // Garantindo que o Loading não está visivel
            layoutLoading.setVisibility(View.GONE);

            // Populando elementos
            PopulateScreenElements(this.meuPlano);
        }
    }

    private void PopulateScreenElements(Plano plan) {
        myPlanCard_nomePlano.setText( plan.getNomePlano() );
        myPlanCard_modalidadePlano.setText( plan.getDescricaoModalidadePlano() );
        myPlanCard_nomeOperadora.setText( plan.getNomeOperadora() );
        myPlanCard_precoPlano.setText( String.format("R$ %.2f", plan.getValorPlano()) );

        //float totalGastoComPacotes = Float.parseFloat(user_plan_info.getString("valor_total_pacotes"));
        String pacotesText = String.format("%s (R$ %.2f)", meuPlano.getNumPacotes(), meuPlano.getValorTotalPacotes());
        myPlanCard_numPacotesAnexados.setText( pacotesText );

        // Checando a modalidade do plano
        if ( !meuPlano.getDescricaoModalidadePlano().toLowerCase().contains("pós") ) {
            // Planos nas modalidades 'Pré-Pago e Controle', nese modo, exite a opção
            // de recarga.. Exibindo a informação no Card e o botão de adicionar/remover
            // recargas realizadas para este plano
            String recargaText = String.format("%s (R$ %.2f)", meuPlano.getNumRecargas(), meuPlano.getValorTotalRecargas());
            myPlanCard_numRecargasRealizadas.setText( recargaText );
            myPlanCard_prepagoDetailsContainer.setVisibility(View.VISIBLE);
            btnCreditos.setVisibility(View.VISIBLE);
        } else {
            // Plano PósPago, ocultando informações relacionados a recarga de créditos
            myPlanCard_prepagoDetailsContainer.setVisibility(View.GONE);
            btnCreditos.setVisibility(View.GONE);
        }

        // Mudando o layout a ser exibido
        layoutNoPlan.setVisibility(View.GONE);
        layoutWithPlan.setVisibility(View.VISIBLE);

        // Checando se esta em focus...
        if ( ((HomeActivity)getActivity()).getCurrentViewPageItem() == getTabPosition() ) {
            // Checando se e a primeira visualização...
            Log.d(LOG_TAG, "Tutorial");
            if ( new SharedPrefsUtil(mContext).getPlanoIsFirstVisualization() ) {
                startTutorial();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_AVALIAR_PLANO:
                if(resultCode == Activity.RESULT_OK){

                }
                break;
        }
    }
}
