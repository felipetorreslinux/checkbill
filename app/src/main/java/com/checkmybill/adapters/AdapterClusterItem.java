package com.checkmybill.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.R;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.maps.MyItem;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterClusterItem extends RecyclerView.Adapter<ClusterItemHolder> {

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private List<MyItem> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterClusterItem(Context context, List<MyItem> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public ClusterItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final ClusterItemHolder clusterItemHolder = new ClusterItemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customItemClickListener.onItemClick(v, clusterItemHolder.getAdapterPosition());
            }
        });

        return clusterItemHolder;
    }

    public void swap(List<NetworkQuality> dataUses) {
        notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ClusterItemHolder holder, int position) {
        //Name network, isp, date, up, down, latency, sync
        MyItem item = lista.get(position);

        holder.getTvwC1().setText("Isp");
        holder.getTvwV1().setText(item.getIsp());

        holder.getTvwC2().setText("N° de entradas");
        holder.getTvwV2().setText(String.valueOf(item.getNumEntradas()));

        holder.getTvwC3().setText("Download");
        holder.getTvwV3().setText(getStringBytesShow(item.getDownload()));

        holder.getTvwC4().setText("Upload");
        holder.getTvwV4().setText(getStringBytesShow(item.getUpload()));

        holder.getTvwC5().setText("Latência");
        holder.getTvwV5().setText(String.valueOf(item.getLatency()));

        holder.getTvwC6().setText("Lat e lng");
        holder.getTvwV6().setText(item.getLatLng().latitude + ", " + item.getLatLng().longitude);

        Picasso.with(context)
                .load(item.getUrlLogo())
                .placeholder(R.drawable.wifi_icon)
                .into(holder.getImgView());


    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private String getStringBytesShow(double bytes) {
        double kilobits = bytes * BYTE_TO_KILOBIT;
        double megabits = kilobits * KILOBIT_TO_MEGABIT;

        DecimalFormat mDecimalFormater = new DecimalFormat("##.##");

        if (kilobits < 1000) {
            return mDecimalFormater.format(kilobits) + " Kbps";
        } else {
            return mDecimalFormater.format(megabits) + " Mbps";
        }
    }
}
