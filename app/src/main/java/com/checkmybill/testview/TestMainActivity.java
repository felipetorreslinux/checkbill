package com.checkmybill.testview;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.checkmybill.R;
import com.checkmybill.testview.datause.DataUseTestFragment;
import com.checkmybill.testview.ligacoes.LigacoesTestFragment;
import com.checkmybill.testview.networkquality.NetworkQualityTestFragment;
import com.checkmybill.testview.signalstrength.SignalStrengthTestFragment;
import com.checkmybill.testview.sms.SmsTestFragment;
import com.checkmybill.testview.unavailability.UnavailabilityTestFragment;

public class TestMainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getCurrentPageView() {
        return mViewPager.getCurrentItem();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return DataUseTestFragment.newInstance(position + 1);
            } else if (position == 1) {
                return NetworkQualityTestFragment.newInstance(position + 1);
            } else if (position == 2) {
                return SignalStrengthTestFragment.newInstance(position + 1);
            } else if (position == 3) {
                return UnavailabilityTestFragment.newInstance(position + 1);
            } else if ( position == 4 ) {
                return SmsTestFragment.newInstance(position + 1);
            } else if ( position == 5 ) {
                return LigacoesTestFragment.newInstance(position + 1);
            } else {
                //hardcode
                return DataUseTestFragment.newInstance(position + 1);
            }


        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "DADOS";
                case 1:
                    return "WIFI";
                case 2:
                    return "GSM";
                case 3:
                    return "INDIS.";
                case 4:
                    return "SMS";
                case 5:
                    return "LIG.";
            }
            return null;
        }
    }
}
