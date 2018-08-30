package com.checkmybill.presentation.ComparacaoPlanoFragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.entity.Pacote;
import com.checkmybill.entity.Plano;

/**
 * Created by Petrus A. (R@G3), ESPE... On 12/01/2017.
 */

public class CmpPlanoPesquisa_ItemFragment extends Fragment {
    // Elementos de uso interno da classe (como outras classes, e etc)
    private Activity parentActivity;
    private Plano plano;
    private Pacote pacote;

    // Elementos visuais do Fragment
    private TextView planoNome, planoOperadora, modalidadePlano, tipoPlano, precoPlano;
    private TextView nomePacote, minutagens, limiteNet, limiteSms, precoFinal;

    // -> Construtor da classe (implementado de modo que seja possivel passar argumentos a esta)
    public static Fragment newInstance(Plano plano, Pacote pacote) {
        Fragment fragment = new CmpPlanoPesquisa_ItemFragment();

        // Definindo os argumentos do Fragment
        Bundle args = new Bundle();
        args.putSerializable("PLANO", plano);
        args.putSerializable("PACOTE", pacote);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Definindo os argumentos
        this.parentActivity = getActivity();
        this.plano = (Plano) getArguments().getSerializable("PLANO");
        this.pacote = (Pacote) getArguments().getSerializable("PACOTE");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.cmpplano_pesquisa_item_layout, container, false);

        // Obtendo os elementos da view
        // 1-> Definindo o evento do 'MoreInfo'
        final TextView moreInfoTextView = (TextView) rootView.findViewById(R.id.moreInfoTextView);
        moreInfoTextView.setOnClickListener( this.moreInfoTextClickEvent );

        // 2-> Obtendo os elementos visuais e populando-os...
        planoNome = (TextView) rootView.findViewById(R.id.plano_nome);
        planoOperadora = (TextView) rootView.findViewById(R.id.plano_operadora);
        modalidadePlano = (TextView) rootView.findViewById(R.id.modalidade_plano);
        tipoPlano = (TextView) rootView.findViewById(R.id.tipo_plano);
        precoPlano = (TextView) rootView.findViewById(R.id.preco_plano);
        nomePacote = (TextView) rootView.findViewById(R.id.nome_pacote);
        minutagens = (TextView) rootView.findViewById(R.id.minutagens);
        limiteNet = (TextView) rootView.findViewById(R.id.limite_net);
        limiteSms = (TextView) rootView.findViewById(R.id.limite_sms);
        precoFinal = (TextView) rootView.findViewById(R.id.preco_final);

        // 3-> Definindo valor combinado dos campos...
        final float precoFinalValue = plano.getValorPlano() + pacote.getValorPacote();

        // 4-> Populando valores dos elementos...
        planoNome.setText( plano.getNomePlano() );
        planoOperadora.setText( plano.getNomeOperadora() );
        modalidadePlano.setText( plano.getDescricaoModalidadePlano() );
        tipoPlano.setText( plano.getDescricaoTipoPlano() );
        precoPlano.setText( String.format("R$ %.2f", plano.getValorPlano()) );
        nomePacote.setText( pacote.getNomePacote() );
        precoFinal.setText( String.format("R$ %.2f", precoFinalValue) );

        // Retornando a View do Fragment
        return rootView;
    }

    // Evento de click para os elementos desta View
    final private View.OnClickListener moreInfoTextClickEvent = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(parentActivity, "Click Event", Toast.LENGTH_SHORT).show();
        }
    };
}
