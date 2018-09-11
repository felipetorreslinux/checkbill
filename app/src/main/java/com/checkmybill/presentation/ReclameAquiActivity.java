package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.checkmybill.R;
import com.checkmybill.util.IntentMap;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_reclame_aqui)
public class ReclameAquiActivity extends BaseActivity {

    public static final int REQUEST_CODE = 1222;
    private Context mContext;
    private ProgressDialog loadingWindow;

    // Filter data today, 30 days, 60 days, my plan and total
    @ViewById(R.id.btn_filter_today_1) Button btn_filter_today_1;
    @ViewById(R.id.btn_filter_30_2) Button btn_filter_30_2;
    @ViewById(R.id.btn_filter_60_3) Button btn_filter_60_3;
    @ViewById(R.id.btn_filter_plan_4) Button btn_filter_plan_4;

    @ViewById(R.id.btn_consult_history) Button btn_consult_history;

    @ViewById(R.id.btn_tim) Button btn_tim;
    @ViewById(R.id.btn_anatel) Button btn_anatel;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG_TAG = getClass().getName();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Click(R.id.btn_tim)
    public void btnTim() {
        Uri uri = Uri.parse("http://www.tim.com.br/pe/para-voce/atendimento");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Click(R.id.btn_anatel)
    public void btnAnatel() {
        Uri uri = Uri.parse("https://sistemas.anatel.gov.br/sis/cadastrosimplificado/pages/acesso/login.xhtml?i=0&codSistema=649");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Click(R.id.btn_consult_history)
    public void btnConsultHistory() {

        Intent it = new Intent(IntentMap.CONSULT_HISTORY);
        startActivityForResult(it, ConsultHistoryActivity.REQUEST_CODE);
    }

    @Click(R.id.btn_filter_today_1)
    public void btnFilterToday1() {
        setMenuFilter(btn_filter_today_1);
    }

    @Click(R.id.btn_filter_30_2)
    public void btnFilter302() {
        setMenuFilter(btn_filter_30_2);
    }

    @Click(R.id.btn_filter_60_3)
    public void btnFilter603() {
        setMenuFilter(btn_filter_60_3);
    }

    @Click(R.id.btn_filter_plan_4)
    public void btnFilterPlan4() {
        setMenuFilter(btn_filter_plan_4);
    }

    public void setMenuFilter(View v){
        btn_filter_today_1.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_30_2.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_60_3.setBackgroundResource(R.drawable.btn_custom_border);
        btn_filter_plan_4.setBackgroundResource(R.drawable.btn_custom_border);

        v.setBackgroundResource(R.drawable.button_green_shape);
    }
}
