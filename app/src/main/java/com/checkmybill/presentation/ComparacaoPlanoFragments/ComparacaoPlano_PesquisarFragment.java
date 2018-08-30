package com.checkmybill.presentation.ComparacaoPlanoFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.checkmybill.R;
import com.checkmybill.entity.Pacote;
import com.checkmybill.entity.Plano;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.ComparacaoPlanoActivity;
import com.checkmybill.request.ComparacaoPlanoRequester;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.Util;
import com.layer_net.stepindicator.StepIndicator;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PageSelected;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Petrus A. (R@G3), ESPE... On 10/01/2017.
 */

@EFragment(R.layout.fragment_comparacaoplano_pesquisar)
public class ComparacaoPlano_PesquisarFragment extends BaseFragment {
    private Context mContext;
    private ComparacaoPlanoActivity comparacaoPlanoActivity;

    private RequestQueue requestQueue;
    private PagerAdapter pagerAdapter;
    private List<Fragment> pageItemList;

    @ViewById(R.id.loadingLayoutContainer) LinearLayout loadingLayoutContainer;
    @ViewById(R.id.errorTextLayout) LinearLayout errorTextLayout;
    @ViewById(R.id.viewPagerIndicator) StepIndicator viewPagerIndicator;
    @ViewById(R.id.contentViewPager) ViewPager contentViewPager;
    @ViewById(R.id.labelItemCountText) TextView labelItemCountText;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.LOG_TAG = "CmpPlano_PesquisaFragment";
        super.onCreate(savedInstanceState);

        // Inicializando classe...
        this.comparacaoPlanoActivity = (ComparacaoPlanoActivity) getActivity();
        this.mContext = getContext();

        // Inicializando Volley
        this.requestQueue = Volley.newRequestQueue(mContext);

        // Inicializando Adapter
        this.pageItemList = new ArrayList<>();
        //this.pageItemList.add( CmpPlanoPesquisa_ItemFragment.newInstance() );
        //this.pageItemList.add( CmpPlanoPesquisa_ItemFragment.newInstance() );
        //this.pageItemList.add( CmpPlanoPesquisa_ItemFragment.newInstance() );
        this.pagerAdapter = new ScreenSlidePagerAdapter(this.comparacaoPlanoActivity.getSupportFragmentManager(), this.pageItemList);
    }

    @PageSelected(R.id.contentViewPager)
    public void contentViewPagerOnPageSelectedEvent(ViewPager view) {
        final int position = view.getCurrentItem();
        this.viewPagerIndicator.setCurrentStepPosition(position);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "OnStop Fragment RUN");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "OnStart Fragment RUN");

        // Definindo adapter do ViewPager (Content)
        this.contentViewPager.setAdapter(this.pagerAdapter);

        // Definindo eventos de click pageIndicator
        this.viewPagerIndicator.setOnClickListener(new StepIndicator.OnClickListener() {
            @Override
            public void onClick(int position) {
                contentViewPager.setCurrentItem(position);
            }
        });

        // Checando se ha argumentos
        final int idOperadora, idModalidade, idTipoPlano;
        final String regiao;
        final Bundle arguments = getArguments();
        if ( arguments != null && arguments.size() == 4 ) {
            Log.d(LOG_TAG, "Processando COM argumentos");
            idOperadora = arguments.getInt("ID_OPERADORA");
            idModalidade = arguments.getInt("ID_MODALIDADE_PLANO");
            idTipoPlano = arguments.getInt("ID_TIPO_PLANO");
            regiao = arguments.getString("REGIAO");
        } else {
            Log.d(LOG_TAG, "Processando SEM argumentos");
            final Plano plano = comparacaoPlanoActivity.getUserPlanoInfo();
            idOperadora = plano.getIdOperadora();
            idModalidade = plano.getIdModalidadePlano();
            idTipoPlano = plano.getIdTipoPlano();
            regiao = "";
        }

        // Montando requisicao (Volley request)
        JsonObjectRequest request = ComparacaoPlanoRequester.prepareCompararPlanoRequest(this.requestSuccessListener, this.requestErrorListener, idOperadora, idModalidade, idTipoPlano, regiao, mContext);
        request.setTag(getClass().getName());

        // Enviando requisicao
        //this.showLoading();
        //requestQueue.add(request);
        updateCardListPageView();
    }

    // ---------------------------------------------------------------------------------------------
    // Variaveis com os listeners do volley
    private Response.ErrorListener requestErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            hideLoading();
            comparacaoPlanoActivity.changeFragment(ComparacaoPlanoActivity.FragmentList.HOME, null);
            new NotifyWindow(mContext).showErrorMessage("Fatal Error", "Não foi possivel obter os dados", false);
        }
    };

    private Response.Listener requestSuccessListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            hideLoading();
            Log.d(LOG_TAG, response.toString());
            try {
                if ( response.getString("status").equalsIgnoreCase("success") == false ) {
                    final String message = response.getString("message");
                    Log.e(LOG_TAG, message);
                    return;
                }

                // Limpando o fragmentList
                pageItemList.clear();

                // Obtendo os dados
                JSONArray dataArr = response.getJSONArray("data");
                for ( int i = 0; i < dataArr.length(); i++ ) {
                    JSONObject planoInfoObj = dataArr.getJSONObject(i).getJSONObject("plano");
                    JSONObject pacoteInfoObj = dataArr.getJSONObject(i).getJSONObject("pacote");

                    // Alimetendando as classes a serem inseridas no fragment
                    final Pacote pacote = new Pacote();
                    final Plano plano = new Plano();

                    // Anexando nova FragmentItem
                    pageItemList.add( CmpPlanoPesquisa_ItemFragment.newInstance(plano, pacote) );
                }

                // Disparando atualizando dos dados do CardList
                updateCardListPageView();
            } catch (JSONException e) {
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    };

    // ---------------------------------------------------------------------------------------------
    // Metodos privados da classe
    private void showLoading() {
        if ( this.loadingLayoutContainer == null ) return;
        this.contentViewPager.setVisibility(View.GONE);
        this.errorTextLayout.setVisibility(View.GONE);
        this.viewPagerIndicator.setVisibility(View.GONE);
        this.loadingLayoutContainer.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if ( this.loadingLayoutContainer == null ) return;
        this.loadingLayoutContainer.setVisibility(View.GONE);
        this.contentViewPager.setVisibility(View.VISIBLE);
        this.errorTextLayout.setVisibility(View.VISIBLE);
        this.viewPagerIndicator.setVisibility(View.VISIBLE);
    }

    private void updateCardListPageView() {
        // Checando se deve popular e exibir o PageIndicator (apenas acima de 1 iten)
        final int pageItemListSize = this.pageItemList.size();
        if ( pageItemListSize == 0 ) {
            contentViewPager.setVisibility(View.GONE);
            this.viewPagerIndicator.setVisibility(View.GONE);
            errorTextLayout.setVisibility(View.VISIBLE);
            return; // Cancelando eceucao padrao, pois, nao ha dados...
        }
        else if ( pageItemListSize > 1 ) {
            this.viewPagerIndicator.setStepsCount(pageItemListSize);
            this.viewPagerIndicator.setCurrentStepPosition(0);
        } else {
            this.viewPagerIndicator.setVisibility(View.GONE);
        }

        // Disparando evento para atualizar o card...
        this.pagerAdapter.notifyDataSetChanged();
    }

    // ---------------------------------------------------------------------------------------------
    // Classe do Adapter da mudança de pagina
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList;
        public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return this.fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
