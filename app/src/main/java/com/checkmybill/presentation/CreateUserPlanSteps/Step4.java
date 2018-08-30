package com.checkmybill.presentation.CreateUserPlanSteps;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.checkmybill.presentation.CreateUserPlanSteps.UserPlanStepFragmentbase;
import com.checkmybill.R;


/**
 * Created by espe on 11/10/2016.
 */
public class Step4 extends UserPlanStepFragmentbase implements View.OnClickListener {
    private LayoutInflater mInflater;
    private View layout;
    private EditText sms_limite;
    private CheckBox sms_ilimitado;

    public Step4(LayoutInflater mInflater) {
        this.mInflater = mInflater;

        // Obtendo layout
        this.layout = mInflater.inflate(R.layout.adduserplan_fragment_step4, null, false);;
        sms_limite = (EditText) this.layout.findViewById(R.id.sms_limite);
        sms_ilimitado = (CheckBox) this.layout.findViewById(R.id.sms_ilimitado);

        // Definindo evento
        sms_ilimitado.setOnClickListener( this );
    }

    @Override
    public View getLayout() {
        return layout;
    }

    @Override
    public boolean validateStepFields() {
        this.validateErrorMessage = "Preencha o campo de SMS";
        if ( sms_limite.getText().length() <= 0 && sms_ilimitado.isChecked() == false )
            return false;

        return true;
    }

    @Override
    public void onClick(View view) {
        CheckBox checkBox = (CheckBox) view;
        boolean checked = checkBox.isChecked();
        final String hintText = (checked) ? "Ilimitado" : "Nº de MB";
        sms_limite.setEnabled( (checked) ? false : true);
        sms_limite.setText("");
        sms_limite.setHint( hintText );
    }

    public void setLimiteSMS(int limite) {
        if ( limite < 0 && sms_ilimitado.isChecked() == false ) sms_ilimitado.performClick(); // Ilimitado
        else if ( limite >= 0 ) sms_limite.setText( String.valueOf(limite) );
    }
    public int getLimiteSMS() {
        int value = (sms_ilimitado.isChecked()) ? -1 : Integer.parseInt(sms_limite.getText().toString());
        return value;
    }
}
