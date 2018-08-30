package com.checkmybill.presentation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.checkmybill.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_reclame)
public class ReclameActivity extends BaseActivity {

    @ViewById(R.id.toolbar)
    protected Toolbar toolbar;

    @ViewById(R.id.container)
    protected ViewPager mViewPager;

    @ViewById(R.id.tabs)
    protected TabLayout tabLayout;

    protected SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        this.initToolbar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);

        Dialog dialogFuncionalidadeBeta = createDialogFuncionalidadeBeta();
        dialogFuncionalidadeBeta.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reclame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            Toast.makeText(ReclameActivity.this, "Reclamação enviada com sucesso :D", Toast.LENGTH_LONG).show();
            onBackPressed();
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            BaseFragment frag = null;
            if (position == 0) {
                frag = new ReclameInfoFragment_();
            } else if (position == 1) {
                frag = new ReclameDadosFragment_();
            }
            if(frag != null) frag.setSectionNumber(position + 1);
            return frag;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "INFORMAÇÕES";
                case 1:
                    return "DADOS CHECKBILL";
            }
            return null;
        }
    }

    public Dialog createDialogFuncionalidadeBeta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReclameActivity.this);
        builder.setMessage("Essa funcionalidade é apenas uma demonstração.")
                .setTitle("Atenção")
                .setPositiveButton("Entendi!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
