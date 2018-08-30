package com.checkmybill.presentation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.checkmybill.CheckBillApplication;
import com.checkmybill.R;

import com.checkmybill.presentation.IntroFragments.IntroAdapter;
import com.checkmybill.presentation.IntroFragments.IntroPageTransformer;
import com.checkmybill.service.ServiceAutoStarter;
import com.checkmybill.service.ServiceInitialDataReader;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.SharedPrefsUtil;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Victor Guerra on 09/03/2016.
 */
@EActivity(R.layout.intro_layout)
public class IntroActivity extends BaseActivity {

    @ViewById(R.id.viewpager)
    protected ViewPager mViewPager;

    @ViewById(R.id.introJumpButton)
    protected Button introJumpButton;

    protected boolean showOnlyLoginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOnlyLoginFragment = getIntent().getBooleanExtra("HIDDEN_JUMP_BUTTON", false) == true;

        /**
         * @Important: Na # issue137, houve uma alteração importante neste código...
         * A lista de permissões (e inicialização de serviços parados) também são chamados neste
         * Activity, assim, garante que a resuisição sempre será feita corretamente...
         */
        // Permissões...
        this.RequestAllPermissions();

        // Inicializando servicos de monitoração e alarm de uploader..
        //new ServiceAutoStarter(this).initializeAllServiceAndAlarms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int position = mViewPager.getCurrentItem();
        if ( position == 3 || showOnlyLoginFragment ) {
            //introJumpButton.setText("Fechar Introdução");
            introJumpButton.setVisibility(View.GONE);
        }
        else {
            introJumpButton.setVisibility(View.VISIBLE);
            //introJumpButton.setText("Pular Introdução");
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        mViewPager.setAdapter(new IntroAdapter(getSupportFragmentManager(), showOnlyLoginFragment));
        mViewPager.setPageTransformer(false, new IntroPageTransformer());
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if ( position == 3 || showOnlyLoginFragment ) {
                    new SharedPrefsUtil(getBaseContext()).setShowIntroduction(false);
                    introJumpButton.setVisibility(View.GONE);
                    //introJumpButton.setText("Fechar Introdução");
                }
                else {
                    introJumpButton.setVisibility(View.VISIBLE);
                    //introJumpButton.setText("Pular Introdução");
                }

                // Mudando a cor da statusBar com base na page selecionada
                if (Build.VERSION.SDK_INT >= 21 ) {
                    int color;
                    if ( showOnlyLoginFragment ) color = getResources().getColor(R.color.intoScreen4ScreenPrimaryDark);
                    else if ( position == 0) color = getResources().getColor(R.color.intoScreen1ScreenPrimaryDark);
                    else if ( position == 1) color = getResources().getColor(R.color.intoScreen2ScreenPrimaryDark);
                    else if (position == 2 ) color = getResources().getColor(R.color.intoScreen3ScreenPrimaryDark);
                    else color = getResources().getColor(R.color.intoScreen4ScreenPrimaryDark);

                    // Definindo a nova cor
                    getWindow().setStatusBarColor(color);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // Definindo a cor para o item1 (padrão)
        if (Build.VERSION.SDK_INT >= 21 ) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.intoScreen1ScreenPrimaryDark));
        }

        // Pular a introdução?
        if ( new SharedPrefsUtil(getBaseContext()).getShowIntroduction() == false ) {
            // Pulando para a pagina de 'Começe a usar'
            mViewPager.setCurrentItem(3, false);
        }
        if ( showOnlyLoginFragment ) {
            // Ocultando o botao de 'Jump' e modificando a cor do StatusBar (se possivel)
            introJumpButton.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= 21 ) {
                final int color = getResources().getColor(R.color.intoScreen4ScreenPrimaryDark);
                getWindow().setStatusBarColor(color);
            }
        }
    }

    @Click
    protected void introJumpButton() {
        //Intent it = new Intent(IntentMap.LOGIN);
        //startActivity(it);
        mViewPager.setCurrentItem(3, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissionList[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101: // Multiple Permission List
                // Logging permission responses...
                for ( int i = 0; i < permissionList.length; i++ ) {
                    final String permName = permissionList[i];
                    boolean isGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                    Log.d(LOG_TAG, String.format("Permission %s is %s", permName, (isGranted ? "Granted":"UnGranted")));
                }
                break;
        }
    }

    private void RequestAllPermissions() {
        String[] permissionsToRequest = ((CheckBillApplication)getApplication()).GetUnGrantedNecessaryPermissions();
        if ( permissionsToRequest.length > 0 ) ActivityCompat.requestPermissions(this, permissionsToRequest, 101);
    }
}