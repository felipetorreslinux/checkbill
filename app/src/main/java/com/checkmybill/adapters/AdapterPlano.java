package com.checkmybill.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.Plano;

import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterPlano extends RecyclerView.Adapter<AdapterPlano.ViewHolder> {

    private List<Plano> lista;
    private Context context;
    private boolean showLimitesContainer;
    private CustomItemClickListener customItemClickListener, customItemLongClickListener;

    public AdapterPlano(Context context, List<Plano> lista, boolean showLimitesContainer, CustomItemClickListener itemClick, CustomItemClickListener longItemClick) {
        this.context = context;
        this.lista = lista;
        this.showLimitesContainer = showLimitesContainer;
        this.customItemClickListener = itemClick;
        customItemLongClickListener = longItemClick;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_plano_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Plano plano = lista.get(position);
        holder.nomePlano.setText( plano.getNomePlano() );
        holder.modalidadePlano.setText( plano.getDescricaoModalidadePlano() );
        holder.valorPlano.setText( String.format("R$ %.02f", plano.getValorPlano()) );
        holder.nomeOperadora.setText( plano.getNomeOperadora() );
        holder.descricaoPlano.setText( (plano.getObservacao().length() <= 0) ? "-" : plano.getObservacao() );

        // Exibir o container de limite?
        if ( this.showLimitesContainer ) {
            holder.minutagens.setText( String.format("%d/%d/%d/%d", plano.getMinMO(), plano.getMinOO(), plano.getMinFixo(), plano.getMinIU()) );
            holder.limiteDados.setText( plano.getLimiteDadosWebStr() );
            holder.limiteSms.setText( plano.getSmsInclusosStr() );
            holder.setVisibiliteLimitesContainer(true);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<Plano> getLista() {
        return lista;
    }

    public void setLista(List<Plano> lista) {
        this.lista = lista;
    }

    public void clearList() {
        this.lista.clear();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        public CardView cardItem;
        public TextView nomePlano, modalidadePlano, nomeOperadora, descricaoPlano, valorPlano, minutagens, limiteDados, limiteSms;
        private LinearLayout limitesInfoContainer;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            // Inicializando elementos filhos (editavel)
            cardItem = (CardView) view.findViewById(R.id.cardItem);
            nomePlano = (TextView) view.findViewById(R.id.planoNome);
            modalidadePlano = (TextView) view.findViewById(R.id.modalidade_plano);
            nomeOperadora = (TextView) view.findViewById(R.id.nome_operadora);
            descricaoPlano = (TextView) view.findViewById(R.id.descricao_plano);
            valorPlano = (TextView) view.findViewById(R.id.preco_plano);
            minutagens = (TextView) view.findViewById(R.id.minutagens);
            limiteDados = (TextView) view.findViewById(R.id.limite_dados);
            limiteSms = (TextView) view.findViewById(R.id.limite_sms);
            limitesInfoContainer = (LinearLayout) view.findViewById(R.id.limites_info_container);

            // Por padrao, oculta o elemento
            setVisibiliteLimitesContainer(false);

            // Definindo evento (se houver)
            if ( customItemClickListener != null ) {
                cardItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        customItemClickListener.onItemClick(view, getAdapterPosition());
                    }
                });
            }
            if ( customItemLongClickListener != null ) {
                cardItem.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        customItemLongClickListener.onItemClick(view, getAdapterPosition());
                        return true;
                    }
                });
            }
        }

        public void setVisibiliteLimitesContainer(boolean visible) {
            if ( visible ) limitesInfoContainer.setVisibility(View.VISIBLE);
            else limitesInfoContainer.setVisibility(View.GONE);
        }

        public View getView() {
            return this.view;
        }
    }
}
