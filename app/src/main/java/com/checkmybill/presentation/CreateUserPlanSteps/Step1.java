package com.checkmybill.presentation.CreateUserPlanSteps;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.ModalidadePlano;
import com.checkmybill.entity.TipoPlano;
import com.checkmybill.presentation.HomeActivity;
import com.checkmybill.util.NotifyWindow;

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
    public TextView info_tipo_plano;

    public Step1(LayoutInflater mInflater, final Context mContext) {
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
        info_tipo_plano = (TextView) this.layout.findViewById(R.id.info_tipo_plano);

        info_tipo_plano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlgBuilder = new NotifyWindow(mContext).getBuilder();
                dlgBuilder.setTitle("Entenda 'Tipo do plano'");
                dlgBuilder.setMessage("Tipo do plano não é a vigência do seu contrato, caso tenha um, com a operadora. Mas sim, é o período de duração do plano. Ex: No meu plano eu tenho 1GB de internet por mês. Para esse caso o tipo do plano é mensal mas existem também as opções: diário, semanal e anual.");
                dlgBuilder.setIcon(R.drawable.ic_warning_amber);
                dlgBuilder.setPositiveButton("Entendi!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Fechando dialogBox
                        dialogInterface.dismiss();
                    }
                });
                dlgBuilder.create().show();
            }
        });
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
