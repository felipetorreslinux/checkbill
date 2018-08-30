package com.checkmybill.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.Pacote;

import java.util.List;

/**
 * Created by Petrus A. (R@G3), ESPE... On 06/12/2016.
 */

public class AdapterPacote extends RecyclerView.Adapter<AdapterPacote.ViewHolder>{
    private List<Pacote> lista;
    private Context context;
    private boolean showLimitesContainer;
    private CustomItemClickListener customItemClickListener, customItemLongClickListener;
    private int color;

    public AdapterPacote(Context context, List<Pacote> lista, CustomItemClickListener customItemClickListener, CustomItemClickListener customItemLongClickListener) {
        this.context = context;
        this.lista = lista;
        this.customItemClickListener = customItemClickListener;
        this.customItemLongClickListener = customItemLongClickListener;
        this.color = context.getResources().getColor(R.color.md_yellow_A700);
    }

    public AdapterPacote(Context context, List<Pacote> lista, CustomItemClickListener customItemClickListener, CustomItemClickListener customItemLongClickListener, int color) {
        this.context = context;
        this.lista = lista;
        this.customItemClickListener = customItemClickListener;
        this.customItemLongClickListener = customItemLongClickListener;
        this.color = color;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_pacote_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pacote pacote = lista.get(position);
        holder.nomePacote.setText( pacote.getNomePacote() );
        holder.modalidadePacote.setText( pacote.getDescricaoModalidadePlano() );
        holder.valorPacote.setText( String.format("R$ %.02f", pacote.getValorPacote()) );
        holder.descricaoPacote.setText( (pacote.getObservacao().length() <= 0) ? "-" : pacote.getObservacao() );

        // Exibir o container de limite?
        if ( this.showLimitesContainer ) {
            holder.minutagens.setText( String.format("%d/%d/%d/%d", pacote.getMinMO(), pacote.getMinOO(), pacote.getMinFixo(), pacote.getMinIU()) );
            holder.limiteDados.setText( pacote.getLimiteDadosWebStr() );
            holder.limiteSms.setText( pacote.getSmsInclusosStr() );
            holder.setVisibiliteLimitesContainer(true);
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<Pacote> getLista() {
        return lista;
    }

    public void setLista(List<Pacote> lista) {
        this.lista = lista;
    }

    public void clearList() {
        this.lista.clear();
    }

    public void setShowLimitesContainer(boolean showLimitesContainer) {
        this.showLimitesContainer = showLimitesContainer;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        public CardView cardItem;
        public TextView nomePacote, modalidadePacote, descricaoPacote, valorPacote, minutagens, limiteDados, limiteSms;
        private LinearLayout limitesInfoContainer;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            // Inicializando elementos filhos (editavel)
            cardItem = (CardView) view.findViewById(R.id.cardItem);
            nomePacote= (TextView) view.findViewById(R.id.pacoteNome);
            modalidadePacote = (TextView) view.findViewById(R.id.modalidade_pacote);
            descricaoPacote= (TextView) view.findViewById(R.id.descricao_pacote);
            valorPacote = (TextView) view.findViewById(R.id.preco_pacote);
            minutagens = (TextView) view.findViewById(R.id.minutagens);
            limiteDados = (TextView) view.findViewById(R.id.limite_dados);
            limiteSms = (TextView) view.findViewById(R.id.limite_sms);
            limitesInfoContainer = (LinearLayout) view.findViewById(R.id.limites_info_container);

            // Definindo cor
            nomePacote.setBackgroundColor( color );

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
