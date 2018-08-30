package com.checkmybill.adapters.ranking;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.adapters.RankingListItemHolder;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.ranking.GsmRankingItem;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

public class AdapterRankingGsm extends RecyclerView.Adapter<RankingGsmHolder> {
    private List<GsmRankingItem> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterRankingGsm(Context context, List<GsmRankingItem> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public RankingGsmHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final RankingGsmHolder rankingGsmHolder = new RankingGsmHolder(v);
        /*v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customItemClickListener.onItemClick(v, netwokQualityListItemHolder.getAdapterPosition());
            }
        });*/

        return rankingGsmHolder;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(RankingGsmHolder holder, int position) {
        GsmRankingItem gsmRankingItem = lista.get(position);

        holder.getTvwPosition().setText(String.valueOf(position + 1));
        holder.getTvwOperatorName().setText(gsmRankingItem.getNomeOperadora());
        getSignalCategory(gsmRankingItem.getMediaNivelSinal(), holder.getTvwSignalCategory());

        if (position == 0) {
            holder.getHead().setBackground(ContextCompat.getDrawable(context, R.color.md_green_100));
        }

        holder.getTvwSignalValue().setText(gsmRankingItem.getMediaNivelSinal().toString());
        holder.getTvwInfo().setText("média de " + gsmRankingItem.getNumEntradas() + " medições");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<GsmRankingItem> getLista() {
        return lista;
    }

    public void setLista(List<GsmRankingItem> lista) {
        this.lista = lista;
    }

    public void clearList() {
        this.lista.clear();
    }

    private void getSignalCategory(int signal, TextView tvwSignalCategory){

        int asu = (signal + 113)/2;

        int valuePercent = (Math.abs(asu) * 100) / 31;
        if (valuePercent >= 0 && valuePercent < 14.3) {
            tvwSignalCategory.setText(context.getString(R.string.pessimo));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_red_A700));
        } else if (valuePercent >= 14.3 && valuePercent < 28.6) {
            tvwSignalCategory.setText(context.getString(R.string.muito_fraco));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_red_A700));
        } else if (valuePercent >= 28.6 && valuePercent < 42.9) {
            tvwSignalCategory.setText(context.getString(R.string.fraco));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_red_A700));
        } else if (valuePercent >= 42.9 && valuePercent < 57.2) {
            tvwSignalCategory.setText(context.getString(R.string.normal));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_blue_A700));
        } else if (valuePercent >= 57.2 && valuePercent < 71.5) {
            tvwSignalCategory.setText(context.getString(R.string.bom));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_blue_A700));
        } else if (valuePercent >= 71.5 && valuePercent < 85.8) {
            tvwSignalCategory.setText(context.getString(R.string.muito_bom));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_green_A700));
        } else if (valuePercent >= 85.8) {
            tvwSignalCategory.setText(context.getString(R.string.excelente));
            tvwSignalCategory.setTextColor(ContextCompat.getColor(context, R.color.md_green_A700));
        }
    }
}