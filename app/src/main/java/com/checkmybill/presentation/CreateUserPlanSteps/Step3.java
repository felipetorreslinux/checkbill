package com.checkmybill.presentation.CreateUserPlanSteps;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.checkmybill.R;


/**
 * Created by espe on 11/10/2016.
 */
public class Step3 extends UserPlanStepFragmentbase implements View.OnClickListener {
    private LayoutInflater mInflater;
    private View layout;
    private EditText dados_limite;
    private CheckBox dados_ilimitado;
    private Spinner tipo_unidade_internet;

    public Step3(LayoutInflater mInflater) {
        this.mInflater = mInflater;

        // Obtendo layout
        this.layout = mInflater.inflate(R.layout.adduserplan_fragment_step3, null, false);;
        dados_limite = (EditText) this.layout.findViewById(R.id.dados_limite);
        dados_ilimitado = (CheckBox) this.layout.findViewById(R.id.dados_ilimitado);
        tipo_unidade_internet = (Spinner) this.layout.findViewById(R.id.tipo_unidade_internet);

        // Definindo evento
        dados_ilimitado.setOnClickListener( this );

        tipo_unidade_internet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final int position = tipo_unidade_internet.getSelectedItemPosition();
                if ( position == 0 ) {
                    final String buff_text = dados_limite.getText().toString().replaceAll("[.]", "");
                    dados_limite.setText(buff_text);
                    dados_limite.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else {
                    dados_limite.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    @Override
    public View getLayout() {
        return layout;
    }

    @Override
    public boolean validateStepFields() {
        this.validateErrorMessage = "Preencha o campo de dados WEB";
        if ( dados_limite.getText().length() <= 0 && dados_ilimitado.isChecked() == false )
            return false;

        return true;
    }

    @Override
    public void onClick(View view) {
        CheckBox checkBox = (CheckBox) view;
        boolean checked = checkBox.isChecked();
        final String hintText = (checked) ? "Ilimitado" : "Valor";
        dados_limite.setEnabled( (checked) ? false : true);
        tipo_unidade_internet.setEnabled( (checked) ? false : true );
        dados_limite.setText("");
        dados_limite.setHint( hintText );
    }

    public void setDados(long limite) {
        if ( limite < 0 && dados_ilimitado.isChecked() == false ) dados_ilimitado.performClick(); // Ilimitado
        else if ( limite >= 0 ) {
            if ( limite >= 1000 ) { // GB
                tipo_unidade_internet.setSelection(1);
                float value = (limite >= 1000) ? ((float) limite / 1000f) : (float) limite;
                final String textValue = String.format("%.2f", value);
                dados_limite.setText(textValue.replaceAll("[,]", "."));
            }
            else { // MB
                dados_limite.setText(String.valueOf(limite));
                tipo_unidade_internet.setSelection(0);
            }
        }
    }
    public long getDados() {
        if ( dados_ilimitado.isChecked() ) return -1;

        float value = Float.parseFloat(dados_limite.getText().toString());
        if ( tipo_unidade_internet.getSelectedItemPosition() == 1 ) value = value * 1000f;

        // Converting float to number
        final int final_value = Math.round(value);
        return final_value;
    }
}
