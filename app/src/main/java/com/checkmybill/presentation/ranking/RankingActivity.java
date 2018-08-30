package com.checkmybill.presentation.ranking;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.presentation.BaseActivity;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.CreateUserPlanActivity;
import com.checkmybill.presentation.GerCreditosPlanoActivity;
import com.checkmybill.presentation.GerPacotesActivity;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.presentation.HomeFragments.CoverageMapFragment;
import com.checkmybill.presentation.HomeFragments.CoverageMapFragment_;
import com.checkmybill.presentation.HomeFragments.InformacoesFragment_;
import com.checkmybill.presentation.HomeFragments.PainelConsumoFragment_;
import com.checkmybill.presentation.HomeFragments.PlanoFragment_;
import com.checkmybill.service.ServiceAutoStarter;
import com.checkmybill.service.ServiceInitialDataReader;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_ranking)
public class RankingActivity extends BaseActivity {

    public static final String EXTRA_AREA = "EXTRA_AREA";

    private List<BaseFragment> fragmentList;
    private RankingActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    @ViewById(R.id.container)
    protected ViewPager mViewPager;

    @ViewById(R.id.tabs)
    protected TabLayout tabLayout;

    @ViewById(R.id.toolbar)
    protected Toolbar toolbar;

    private int startPoint = 0;

    // Retorna a atual interface em visualizacao
    // -> Usado nas fragments filhas, para saber se elas estão visiveis ou não...
    public int getCurrentViewPageItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Criando lista de fragments ativos e armazenando-os...
        BaseFragment gsmRanking = GsmRankingFragment_.builder().build();
        gsmRanking.setSectionNumber(1);
        gsmRanking.setTabPosition(0);

        BaseFragment bandaLargaRanking = BandaLargaRankingFragment_.builder().build();
        bandaLargaRanking.setSectionNumber(2);
        bandaLargaRanking.setTabPosition(1);

        // Mudando a cor do statusBar
        if (Build.VERSION.SDK_INT >= 21 ) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
        }

        fragmentList = new ArrayList<>();
        fragmentList.add(gsmRanking);
        fragmentList.add(bandaLargaRanking);
    }

    @Override
    protected void onStart() {
        super.onStart();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new RankingActivity.SectionsPagerAdapter(getSupportFragmentManager());
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
        Log.d(LOG_TAG, "On Resume");

        this.initializeTabIcons();
    }

    @Override
    protected void onPause() {
        // Salvando posição atual, para quando for recuperado...
        this.startPoint = mViewPager.getCurrentItem();
        super.onPause();
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
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "GSM";
                case 1:
                    return "BANDA LARGA";
            }
            return null;
        }
    }

    private void initializeTabIcons() {
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}


