package com.checkmybill.presentation.CreateUserPlanSteps;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.checkmybill.R;


/**
 * Created by espe on 11/10/2016.
 */
public class Step2 extends UserPlanStepFragmentbase implements View.OnClickListener {
    private LayoutInflater mInflater;
    private View layout;
    public EditText mo_minutos, oo_minutos, iu_minutos, fixo_minutos;
    public CheckBox mo_ilimitado, oo_ilimitado, iu_ilimitado, fixo_ilimitado;

    public Step2(LayoutInflater mInflater) {
        this.mInflater = mInflater;

        // Obtendo layout
        this.layout = mInflater.inflate(R.layout.adduserplan_fragment_step2, null, false);;
        mo_minutos = (EditText) this.layout.findViewById(R.id.mo_minutos);
        oo_minutos = (EditText) this.layout.findViewById(R.id.oo_minutos);
        iu_minutos = (EditText) this.layout.findViewById(R.id.iu_minutos);
        fixo_minutos = (EditText) this.layout.findViewById(R.id.fixo_minutos);
        mo_ilimitado = (CheckBox) this.layout.findViewById(R.id.mo_ilimitado);
        oo_ilimitado = (CheckBox) this.layout.findViewById(R.id.oo_ilimitado);
        iu_ilimitado = (CheckBox) this.layout.findViewById(R.id.iu_ilimitado);
        fixo_ilimitado = (CheckBox) this.layout.findViewById(R.id.fixo_ilimitado);

        mo_ilimitado.setOnClickListener( this );
        oo_ilimitado.setOnClickListener( this );
        iu_ilimitado.setOnClickListener( this );
        fixo_ilimitado.setOnClickListener( this );
    }

    @Override
    public View getLayout() {
        return this.layout;
    }

    @Override
    public boolean validateStepFields() {
        // Checando o valor do campo
        this.validateErrorMessage = "Preencha os campos de minutagem";
        if ( mo_minutos.getText().length() <= 0 && mo_ilimitado.isChecked() == false )
            return false;
        if ( oo_minutos.getText().length() <= 0 && oo_ilimitado.isChecked() == false )
            return false;
        if ( iu_minutos.getText().length() <= 0 && iu_ilimitado.isChecked() == false )
            return false;
        if ( fixo_minutos.getText().length() <= 0 && fixo_ilimitado.isChecked() == false )
            return false;

        // Retornando sucesso
        return true;
    }

    @Override
    public void onClick(View view) {
        CheckBox checkBox = (CheckBox) view;
        boolean checked = checkBox.isChecked();
        final String hintText = (checked) ? "Ilimitado" : "Minutos";
        switch ( view.getId() ) {
            case R.id.mo_ilimitado:
                mo_minutos.setEnabled( (checked) ? false : true);
                mo_minutos.setText("");
                mo_minutos.setHint( hintText );
                break;
            case R.id.oo_ilimitado:
                oo_minutos.setEnabled( (checked) ? false : true);
                oo_minutos.setText("");
                oo_minutos.setHint( hintText );
                break;
            case R.id.iu_ilimitado:
                iu_minutos.setEnabled( (checked) ? false : true);
                iu_minutos.setText("");
                iu_minutos.setHint( hintText );
                break;
            case R.id.fixo_ilimitado:
                fixo_minutos.setEnabled( (checked) ? false : true);
                fixo_minutos.setText("");
                fixo_minutos.setHint( hintText );
                break;
        }
    }

    public void setMinutosMO(int minutos) {
        if ( minutos < 0 && mo_ilimitado.isChecked() == false ) mo_ilimitado.performClick(); // Ilimitado
        else if ( minutos >= 0 ) mo_minutos.setText( String.valueOf(minutos) );
    }
    public int getMinutosMO(){
        int value = (mo_ilimitado.isChecked()) ? -1 : Integer.parseInt(mo_minutos.getText().toString());
        return value;
    }

    public void setMinutosOO(int minutos) {
        if ( minutos < 0 && oo_ilimitado.isChecked() == false ) oo_ilimitado.performClick(); // Ilimitado
        else if ( minutos >= 0 ) oo_minutos.setText( String.valueOf(minutos) );
    }
    public int getMinutosOO(){
        int value = (oo_ilimitado.isChecked()) ? -1 : Integer.parseInt(oo_minutos.getText().toString());
        return value;
    }

    public void setMinutosIU(int minutos) {
        if ( minutos < 0 && iu_ilimitado.isChecked() == false ) iu_ilimitado.performClick(); // Ilimitado
        else if ( minutos >= 0 ) iu_minutos.setText( String.valueOf(minutos) );
    }
    public int getMinutosIU(){
        int value = (iu_ilimitado.isChecked()) ? -1 : Integer.parseInt(iu_minutos.getText().toString());
        return value;
    }

    public void setMinutosFixo(int minutos) {
        if ( minutos < 0 && fixo_ilimitado.isChecked() == false ) fixo_ilimitado.performClick(); // Ilimitado
        else if ( minutos >= 0 ) fixo_minutos.setText( String.valueOf(minutos) );
    }
    public int getMinutosFixo(){
        int value = (fixo_ilimitado.isChecked()) ? -1 : Integer.parseInt(fixo_minutos.getText().toString());
        return value;
    }
}
