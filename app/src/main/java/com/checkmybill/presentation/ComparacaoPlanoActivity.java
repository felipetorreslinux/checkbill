package com.checkmybill.presentation;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.checkmybill.R;
import com.checkmybill.entity.Plano;
import com.checkmybill.presentation.ComparacaoPlanoFragments.*;

import org.androidannotations.annotations.EActivity;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_comparacao_plano)
public class ComparacaoPlanoActivity extends BaseActivity {
    // Definindo Enum
    public enum FragmentList {
        HOME(0), DEFINICOES_PARAMETROS(1), PESQUISAR(2);
        public int valor;
        FragmentList(int valor) { this.valor = valor; }
    }

    // Constantes
    final int FRAGMENT_LAYOUT_ID = R.id.comparacao_plano_fragment_container;

    // Propriedades (variaveis) da classe
    private List<BaseFragment> fragmentList;
    private FragmentManager fragmentManager;
    private FragmentList currentFragment, previousFragment;

    // Variaveis com alguns dados que podem ser obtidos em algumas telas,
    // e são armazenadas aki para evitar o carregamente (uso) constante da rede (internet)
    public JSONArray lista_modalidades_plano, lista_tipos_plano, lista_operadora_gsm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = this.getClass().getName();

        // Inicializando Fragments...
        this.fragmentList = new ArrayList<>();
        this.fragmentList.add(ComparacaoPlano_HomeFragment_.builder().build());
        this.fragmentList.add(ComparacaoPlano_ParametrosFragment_.builder().build());
        this.fragmentList.add(ComparacaoPlano_PesquisarFragment_.builder().build());

        // Obtendo o Fragment manager e anexando o primeiro Activity
        this.fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();
        fragmentTransaction.add(FRAGMENT_LAYOUT_ID, this.fragmentList.get(0));
        this.currentFragment = FragmentList.HOME;
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if ( this.currentFragment.valor != 0 ) {
            this.changeFragment(this.previousFragment, null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public Plano getUserPlanoInfo() {
        return (Plano) getIntent().getSerializableExtra("PLANO");
    }

    public void changeFragment(FragmentList fragment, Bundle parameters) {
        // Definindo o argumento
        if ( parameters != null ) this.fragmentList.get(fragment.valor).setArguments( parameters );

        final FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();

        // Modificando o fragment
        fragmentTransaction.replace(FRAGMENT_LAYOUT_ID, this.fragmentList.get(fragment.valor));

        // Checando qual era o anterior, se for o 'Pesquisa', sera removido
        if ( this.currentFragment.valor == FragmentList.PESQUISAR.valor ) {
            Log.d(LOG_TAG, "Removing PESQUISA Fragment");
            fragmentTransaction.remove(this.fragmentList.get(this.currentFragment.valor));
        }

        // Definindo qual sera o proximo Fragment ao pressionando o BackButton, necessario para o
        // correto funcionamento do BackButton
        if (this.currentFragment.valor != 2 )
            this.previousFragment = this.currentFragment;
        else if ( fragment.valor == 1 ) {
            this.previousFragment = FragmentList.HOME;
        } else {
            this.previousFragment = null;
        }

        // Definindo qual é o atual e modificando a tela (commit)
        this.currentFragment = fragment;
        fragmentTransaction.commit();
    }
}
