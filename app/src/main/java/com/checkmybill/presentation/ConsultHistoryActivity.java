package com.checkmybill.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.checkmybill.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_consult_history)
public class ConsultHistoryActivity extends BaseActivity {

    public static final int REQUEST_CODE = 1225;
    private Context mContext;
    private ProgressDialog loadingWindow;

    // Filter data today, 30 days, 60 days, my plan and total
    @ViewById(R.id.btn_filter_today_1) Button btn_filter_today_1;
    @ViewById(R.id.btn_filter_30_2) Button btn_filter_30_2;
    @ViewById(R.id.btn_filter_60_3) Button btn_filter_60_3;
    @ViewById(R.id.btn_filter_plan_4) Button btn_filter_plan_4;

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
