package com.checkmybill.presentation.CreateUserPlanSteps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.ModalidadePlano;
import com.checkmybill.entity.TipoPlano;

import java.util.List;


/**
 * Created by espe on 11/10/2016.
 */
public class Step1 extends UserPlanStepFragmentbase {
    private LayoutInflater mInflater;
    private Context mContext;
    private View layout;
    public EditText nome_plano, valor_plano, operadora;
    public Spinner modalidade_plano, tipo_plano, dt_vencimento;

    public Step1(LayoutInflater mInflater, Context mContext) {
        this.mInflater = mInflater;
        this.mContext = mContext;

        // Obtendo layout
        this.layout = mInflater.inflate(R.layout.adduserplan_fragment_step1, null, false);;
        nome_plano = (EditText) this.layout.findViewById(R.id.nome_plano);
        operadora = (EditText) this.layout.findViewById(R.id.operadora);
        valor_plano = (EditText) this.layout.findViewById(R.id.valor_plano);
        modalidade_plano = (Spinner) this.layout.findViewById(R.id.modalidade_plano);
        tipo_plano = (Spinner) this.layout.findViewById(R.id.tipo_plano);
        dt_vencimento = (Spinner) this.layout.findViewById(R.id.dt_vencimento);
    }

    @Override
    public View getLayout() {
        return layout;
    }

    @Override
    public boolean validateStepFields() {
        // Checando se todos os campos obrigatorios estão preenchidos
        // Nota: Os campos obrigatorios podem mudar com base no tipo de Modalidade escolhido
        final String modalidadePlano = ((String) this.modalidade_plano.getSelectedItem()).toLowerCase();
        if ( this.nome_plano.getText().length() <= 0 ) {
            this.validateErrorMessage = "Defina o nome do plano";
            return false;
        }
        if ( this.valor_plano.getText().length() <= 0 ) {
            this.validateErrorMessage = "Defina o valor do plano";
            return false;
        }

        // Validando campos que interferem com base no tipo informado...
        if ( modalidadePlano.contains("controle") || modalidadePlano.contains("pós") ) {
            // Checando vencimento (é obrigatorio definir a data de vencimento para estas
            // modalidades
            if ( this.dt_vencimento.getSelectedItemPosition() == 0 ) {
                this.validateErrorMessage = "Defina a data de vencimento deste plano";
                return false;
            }
        }

        return true;
    }

    // -. Utils
    public void setSelectionModalidadeByID(int id_modalidade, List<ModalidadePlano> listModalidadeBase) {
        int position = -1;
        for ( int i = 0; i < listModalidadeBase.size(); i++ ) {
            final int id = listModalidadeBase.get(i).getId();
            if ( id == id_modalidade ) {
                position = i;
                break;
            }
        }

        if ( position < 0 ) return; // Not Located
        else modalidade_plano.setSelection(position);
    }
    public void setSelectionTipoByID(int id_tipo, List<TipoPlano> listTipoBase) {
        int position = -1;
        for ( int i = 0; i < listTipoBase.size(); i++ ) {
            final int id = listTipoBase.get(i).getId();
            if ( id == id_tipo ) {
                position = i;
                break;
            }
        }

        if ( position < 0 ) return; // Not Located
        else tipo_plano.setSelection(position);
    }
}
