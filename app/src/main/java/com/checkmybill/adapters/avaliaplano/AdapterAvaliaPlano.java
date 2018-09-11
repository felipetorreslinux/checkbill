package com.checkmybill.adapters.avaliaplano;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.IndisponibilidadeDetail;
import com.checkmybill.entity.Plano;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */

public class AdapterAvaliaPlano extends RecyclerView.Adapter<AvaliaPlanoListItemHolder> {
    private List<Plano> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;
    private boolean isMobileMode;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public AdapterAvaliaPlano(Context context, List<Plano> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public AvaliaPlanoListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final AvaliaPlanoListItemHolder avaliaPlanoListItemHolder = new AvaliaPlanoListItemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( customItemClickListener != null ) {
                    customItemClickListener.onItemClick(v, avaliaPlanoListItemHolder.getAdapterPosition());
                }
            }
        });

        if(viewType == TYPE_HEADER){
            avaliaPlanoListItemHolder.setHeader(true);
        }

        return avaliaPlanoListItemHolder;
    }

    public void onBindViewHolder(AvaliaPlanoListItemHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Plano data = lista.get(position);

        holder.getTvwNomePlano().setText(data.getNomePlano());
        holder.getTvwModalidade().setText(data.getDescricaoModalidadePlano());
        holder.getTvwValorPlano().setText(String.format("%.2f", data.getValorPlano()));

        double dadosGb = data.getLimiteDadosWeb();
        dadosGb = dadosGb * (1/1024);
        if(dadosGb >= 1){
            holder.getTvwDadosVariable().setText("GB(Gigabyte)");
            holder.getTvwDados().setText(String.format("%.0f", dadosGb));
        }else{
            double dadosMb = data.getLimiteDadosWeb();
            holder.getTvwDadosVariable().setText("MB(Megabyte)");
            holder.getTvwDados().setText(String.format("%.0f", dadosMb));
        }

        if(position == 0){
            holder.getTvwBestPrice().setVisibility(View.VISIBLE);
            holder.getTvwPosition().setTextColor(Color.parseColor("#009b36"));
        }

        int index = position + 1;
        holder.getTvwPosition().setText(index + "°");
    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position){
        //return position == 0;
        return false;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void setMobileMode(boolean mobileMode) {
        isMobileMode = mobileMode;
    }
}
