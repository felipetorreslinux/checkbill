package com.checkmybill.adapters.ranking;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.ranking.BandaLargaRankingItem;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

public class AdapterRankingBandaLarga extends RecyclerView.Adapter<RankingBandaLargaHolder> {

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private List<BandaLargaRankingItem> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterRankingBandaLarga(Context context, List<BandaLargaRankingItem> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public RankingBandaLargaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final RankingBandaLargaHolder rankingBandaLargaHolder = new RankingBandaLargaHolder(v);
        /*v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customItemClickListener.onItemClick(v, netwokQualityListItemHolder.getAdapterPosition());
            }
        });*/

        return rankingBandaLargaHolder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(RankingBandaLargaHolder holder, int position) {
        BandaLargaRankingItem bandaLargaRankingItem = lista.get(position);

        holder.getTvwPosition().setText(String.valueOf(position + 1));
        holder.getTvwOperatorName().setText(bandaLargaRankingItem.getNomeOperadora());

        if (position == 0) {
            holder.getHead().setBackground(ContextCompat.getDrawable(context, R.color.md_green_100));
        }

        double bytespersecond = bandaLargaRankingItem.getQualiDownload();
        double kilobits = bytespersecond * BYTE_TO_KILOBIT;
        double megabits = kilobits * KILOBIT_TO_MEGABIT;

        DecimalFormat mDecimalFormater = new DecimalFormat("##.##");

        if (kilobits < 1000) {
            holder.getTvwSpeed().setText(mDecimalFormater.format(kilobits) + " Kbps");
        } else {
            holder.getTvwSpeed().setText(mDecimalFormater.format(megabits) + " Mbps");
        }

        holder.getTvwInfo().setText("* Média de " + bandaLargaRankingItem.getTotalEntradas() + " medições");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<BandaLargaRankingItem> getLista() {
        return lista;
    }

    public void setLista(List<BandaLargaRankingItem> lista) {
        this.lista = lista;
    }

    public void clearList() {
        this.lista.clear();
    }
}
