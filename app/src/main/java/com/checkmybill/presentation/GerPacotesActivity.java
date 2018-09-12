package com.checkmybill.presentation;

import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.checkmybill.R;
import com.checkmybill.entity.Plano;
import com.checkmybill.presentation.GerPacotesFragments.GerPacotesFragment_PlanList_;
import com.checkmybill.presentation.GerPacotesFragments.GerPacotesFragment_UserList;
import com.checkmybill.presentation.GerPacotesFragments.GerPacotesFragment_UserList_;
import com.checkmybill.util.NotifyWindow;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.PageSelected;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_ger_pacotes)
public class GerPacotesActivity extends BaseActivity {
    public static final int REQUEST_CODE = 1202;
    public static final String PLANO_EXTRA = "PLANO_EXTRA";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Plano planoUsuario;

    @ViewById(R.id.toolbar) protected Toolbar toolbar;
    @ViewById(R.id.tabs) protected TabLayout tabLayout;
    @ViewById(R.id.container) protected ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_TAG = getClass().getName();

        // Obtendo plano selecionado...
        planoUsuario = (Plano) getIntent().getSerializableExtra(PLANO_EXTRA);

    }

    @Override
    public void onStart() {
        super.onStart();
        this.initToolbar(this.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Pacotes");

        this.mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        this.mViewPager.setAdapter(mSectionsPagerAdapter);
        this.mViewPager.setOffscreenPageLimit(1);

        this.tabLayout.setupWithViewPager(this.mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if ( mViewPager.getCurrentItem() != 0 ) {
            mViewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    @PageSelected(R.id.container)
    void onPageSelected(ViewPager view, int state) {
        final int position = view.getCurrentItem();
        // Disparando evento de 'focusRecived' do BaseFragment
        BaseFragment focusedFragment = (BaseFragment) this.mSectionsPagerAdapter.getItem(position);
        focusedFragment.focusReceived();
    }

    public Plano getPlanoUsuario() {
        return planoUsuario;
    }

    public void reloadUserList() {
        mViewPager.setCurrentItem(0, true);
        ((GerPacotesFragment_UserList)mSectionsPagerAdapter.getItem(0)).obterListaPacotes();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private List<BaseFragment> fragmentList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            // Criando a lista de fragments
            this.fragmentList = new ArrayList<>();
            this.fragmentList.add(GerPacotesFragment_UserList_.builder().build());
            this.fragmentList.add(GerPacotesFragment_PlanList_.builder().build());
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Meus Pacotes";
                case 1:
                    return "Pacotes do Plano";
            }
            return null;
        }
    }
}
