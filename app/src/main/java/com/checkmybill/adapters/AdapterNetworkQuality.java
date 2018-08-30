package com.checkmybill.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.entity.NetworkQuality;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterNetworkQuality extends RecyclerView.Adapter<NetwokQualityListItemHolder> {

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private List<NetworkQuality> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterNetworkQuality(Context context, List<NetworkQuality> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public NetwokQualityListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final NetwokQualityListItemHolder netwokQualityListItemHolder = new NetwokQualityListItemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customItemClickListener.onItemClick(v, netwokQualityListItemHolder.getAdapterPosition());
            }
        });

        return netwokQualityListItemHolder;
    }

    @Override
    public void onBindViewHolder(NetwokQualityListItemHolder holder, int position) {
        NetworkQuality networkQuality = lista.get(position);
        holder.getTvwNetworkName().setText(networkQuality.getName());
        holder.getTvwDate().setText(new DateTime(networkQuality.getDate()).toString(DateTimeFormat.mediumDateTime()));

        double bytespersecond = networkQuality.getDownload();
        double kilobits = bytespersecond * BYTE_TO_KILOBIT;
        double megabits = kilobits * KILOBIT_TO_MEGABIT;

        holder.getTvwLatencia().setText(networkQuality.getLatency() + "ms");

        DecimalFormat mDecimalFormater = new DecimalFormat("##.##");

        if (kilobits < 1000) {
            holder.getTvwDownload().setText(mDecimalFormater.format(kilobits) + " Kbps");
        } else {
            holder.getTvwDownload().setText(mDecimalFormater.format(megabits) + " Mbps");
        }

        bytespersecond = networkQuality.getUpload();
        kilobits = bytespersecond * BYTE_TO_KILOBIT;
        megabits = kilobits * KILOBIT_TO_MEGABIT;

        if (kilobits < 1000) {
            holder.getTvwUpload().setText(mDecimalFormater.format(kilobits) + " Kbps");
        } else {
            holder.getTvwUpload().setText(mDecimalFormater.format(megabits) + " Mbps");
        }


    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<NetworkQuality> getLista() {
        return lista;
    }

    public void setLista(List<NetworkQuality> lista) {
        this.lista = lista;
    }

    public void clearList() {
        this.lista.clear();
    }
}
