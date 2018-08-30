package com.checkmybill.presentation.ComparacaoPlanoFragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.Plano;
import com.checkmybill.presentation.BaseFragment;
import com.checkmybill.presentation.ComparacaoPlanoActivity;
import com.layer_net.stepindicator.StepIndicator;

import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Petrus A. (R@G3), ESPE... On 10/01/2017.
 */
@EFragment(R.layout.fragment_comparacaoplano_home)
public class ComparacaoPlano_HomeFragment extends BaseFragment {
    private Context mContext;
    private ComparacaoPlanoActivity comparacaoPlanoActivity;

    @ViewById(R.id.homeMainActionButton) Button homeMainActionButton;
    @ViewById(R.id.homeRadioButtonGroup) RadioGroup homeRadioButtonGroup;

    /* ------------------------------------------------------------------------------------------ */
    // Metodos da classe (Construtores/Inicializadores/Eventos da Acitivty/Layout)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.LOG_TAG = "CmpPlano_HomeFragment";
        super.onCreate(savedInstanceState);

        this.comparacaoPlanoActivity = (ComparacaoPlanoActivity) getActivity();
        this.mContext = getContext();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Definindo evento de click para os radioButtons
        homeRadioButtonGroup.setOnCheckedChangeListener(this.radioGroupCheckedChangeListener);

        // Definindo valores do card de plano do usuario
        Plano planoUsuario = this.comparacaoPlanoActivity.getUserPlanoInfo();
        ((TextView)this.getView().findViewById(R.id.nome_plano)).setText(planoUsuario.getNomePlano());
        ((TextView)this.getView().findViewById(R.id.nome_operadora)).setText( planoUsuario.getNomeOperadora() );
        ((TextView)this.getView().findViewById(R.id.modalidade_plano)).setText( planoUsuario.getDescricaoModalidadePlano() );
        ((TextView)this.getView().findViewById(R.id.preco_plano)).setText( String.format("R$ %.2f", planoUsuario.getValorPlano()) );
    }

    /* ------------------------------------------------------------------------------------------ */
    // Propriedades e eventos internos da classe/View
    RadioGroup.OnCheckedChangeListener radioGroupCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if ( i == R.id.homeRadioButtonGroup_Radio0 ) {
                homeMainActionButton.setText("Continuar");
            } else {
                homeMainActionButton.setText("Definir Parâmetros");
            }
        }
    };

    @Click(R.id.homeMainActionButton)
    public void homeMainActionButtonClickEvent() {
        // Obtendo o RadioButton seleconando no momento
        // E modificando o fragment com base no Radio selecionado...
        final int checkedRadioID = homeRadioButtonGroup.getCheckedRadioButtonId();
        if ( checkedRadioID == R.id.homeRadioButtonGroup_Radio0 ) {
            // Realizando a pesquisa usando as informações do plano/operadora como parametro
            this.comparacaoPlanoActivity.changeFragment(ComparacaoPlanoActivity.FragmentList.PESQUISAR, null);
        }
        else {
            // Definindo os parametros manualmente...
            this.comparacaoPlanoActivity.changeFragment(ComparacaoPlanoActivity.FragmentList.DEFINICOES_PARAMETROS, null);
        }
    }
}
