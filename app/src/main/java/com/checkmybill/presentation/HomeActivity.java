package com.checkmybill.presentation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.checkmybill.CheckBillApplication;
import com.checkmybill.R;
import com.checkmybill.presentation.HomeFragments.CoverageMapFragment;
import com.checkmybill.presentation.HomeFragments.CoverageMapFragment_;
import com.checkmybill.presentation.HomeFragments.InformacoesFragment_;
import com.checkmybill.presentation.HomeFragments.PainelConsumoFragment_;
import com.checkmybill.presentation.HomeFragments.PlanoFragment;
import com.checkmybill.presentation.HomeFragments.PlanoFragment_;
import com.checkmybill.presentation.HomeFragments.SinalFragment_;
import com.checkmybill.service.ServiceAutoStarter;
import com.checkmybill.service.ServiceInitialDataReader;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_home)
public class HomeActivity extends BaseActivity {
    public enum HomeTabNames {
        PAINEL(0), MAPA(1), SINAL(2), PLANO(3);
        public int valorPanel;
        HomeTabNames(int valor) { valorPanel = valor; }
    };

    private List<BaseFragment> fragmentList;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @ViewById(R.id.container)
    protected ViewPager mViewPager;

    @ViewById(R.id.tabs)
    protected TabLayout tabLayout;

    @ViewById(R.id.toolbar)
    protected Toolbar toolbar;

    private int startPoint = 0;

    @Override
    public void onBackPressed() {
        if ( this.getCurrentViewPageItem() != 0 ) {
            this.changePage(HomeTabNames.PAINEL);
        } else {
            super.onBackPressed();
        }
    }

    // Retorna a atual interface em visualizacao
    // -> Usado nas fragments filhas, para saber se elas estão visiveis ou não...
    public int getCurrentViewPageItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * @Important: Na # issue137, houve uma alteração importante neste código...
         * A lista de permissões (e inicialização de serviços parados) voltaram para este    Activity
         * mas, não apenas ele... Essas mesmas chamadas são executadas também no Activity 'Intro' ;)
         */
        // Permissões...
        this.RequestAllPermissions();

        // Inicializando servicos de monitoração e alarm de uploader..
        //new ServiceAutoStarter(this).initializeAllServiceAndAlarms();
        ServiceInitialDataReader.ObterSincronizarDadosUsuarioServidor(getBaseContext());

        // Criando lista de fragments ativos e armazenando-os...
        BaseFragment informacoes = SinalFragment_.builder().build();
        informacoes.setSectionNumber(1);
        informacoes.setTabPosition(0);

        BaseFragment map = CoverageMapFragment_.builder().build();
        map.setSectionNumber(2);
        map.setTabPosition(1);

        BaseFragment painel = PainelConsumoFragment_.builder().build();
        painel.setSectionNumber(3);
        painel.setTabPosition(2);

        BaseFragment plano = PlanoFragment_.builder().build();
        plano.setSectionNumber(4);
        plano.setTabPosition(3);

        fragmentList = new ArrayList<>();
        fragmentList.add(informacoes);
        fragmentList.add(map);
        fragmentList.add(painel);
        fragmentList.add(plano);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.initToolbar(this.toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);

        this.initializeTabIcons();

        // Checando statrPoint
        if (this.startPoint > 0) {
            this.mViewPager.setCurrentItem(startPoint);
            this.mSectionsPagerAdapter.notifyDataSetChanged();
        }

        // Definindo evento de click no tab...
        // Serve para poder chamar alguma funcao especifica ao clicar no tab do 'fragment'...
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                // Chamando uma funcao do BaseFragment que informa que o fragment esta em foco!!
                fragmentList.get(position).focusReceived();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        this.initializeTabIcons();
    }

    @Override
    protected void onPause() {
        // Salvando posição atual, para quando for recuperado...
        this.startPoint = mViewPager.getCurrentItem();
        super.onPause();
    }


    /**
     * Metodo para a definição dos icones das tabs
     */
    private void initializeTabIcons() {
        tabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            int icon = 0;
            switch (i) {
                case 0:
                    icon = R.mipmap.ic_info_white;
                    break;
                case 1:
                    icon = R.mipmap.ic_map_white_24dp;
                    break;
                case 2:
                    icon = R.mipmap.ic_show_chart_white_24dp;
                    break;
                case 3:
                    icon = R.mipmap.ic_monetization_on_white_24dp;
                    break;
            }
            tabLayout.getTabAt(i).setIcon(icon);
        }
    }

    public void changePage(HomeTabNames tabName) {
        final int position = tabName.valorPanel;
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        this.startPoint = position;
        tab.select();
        Log.d(LOG_TAG, "Page Changed:" + position);
    }

    public void changePage(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        this.startPoint = position;
        tab.select();
        Log.d(LOG_TAG, "Page Changed:" + position);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position > fragmentList.size()) return null;
            else return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.informacoes);
                case 1:
                    return getString(R.string.mapa);
                case 2:
                    return getString(R.string.menu_painel_consumo);
                case 3:
                    return getString(R.string.plano);
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "RequestCode: " + requestCode);
        switch (requestCode) {
            case CreateUserPlanActivity.REQUEST_CODE:
                this.startPoint = 3;
                fragmentList.get(3).onActivityResult(requestCode, resultCode, data);
                break;
            case GerCreditosPlanoActivity.REQUEST_CODE:
            case AvaliaPlanoActivity.REQUEST_CODE:
            case GerPacotesActivity.REQUEST_CODE:
                this.startPoint = 3;
                break;
            case CoverageMapFragment.REQUEST_CODE_LOCATION_ACTIVITY:
                fragmentList.get(2).onActivityResult(requestCode, resultCode , data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String perissionList[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                Log.i(LOG_TAG, "Permission Accepted");
                break;
        }
    }

    private void RequestAllPermissions() {
        String permissionList[] = ((CheckBillApplication) getApplication()).GetUnGrantedNecessaryPermissions();
        if( permissionList.length > 0 ) ActivityCompat.requestPermissions(this,permissionList, 1);
    }



}

