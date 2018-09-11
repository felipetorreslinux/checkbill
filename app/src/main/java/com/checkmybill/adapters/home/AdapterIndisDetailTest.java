package com.checkmybill.adapters.home;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.IndisponibilidadeDetail;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;
import com.checkmybill.service.TrafficMonitor;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */

public class AdapterIndisDetailTest extends RecyclerView.Adapter<DataIndisDetailListItemHolder> {
    private List<IndisponibilidadeDetail> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;
    private boolean isMobileMode;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public AdapterIndisDetailTest(Context context, List<IndisponibilidadeDetail> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public DataIndisDetailListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final DataIndisDetailListItemHolder dataIndisDetailListItemHolder = new DataIndisDetailListItemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( customItemClickListener != null ) {
                    customItemClickListener.onItemClick(v, dataIndisDetailListItemHolder.getAdapterPosition());
                }
            }
        });

        if(viewType == TYPE_HEADER){
            dataIndisDetailListItemHolder.setHeader(true);
        }

        return dataIndisDetailListItemHolder;
    }

    public void onBindViewHolder(DataIndisDetailListItemHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        IndisponibilidadeDetail data = lista.get(position);

        if(holder.getHeader()){
            holder.getTvwTime().setText("Quando");
            holder.getTvwTime().setTypeface(null, Typeface.BOLD);
            holder.getTvwNeigh().setText("Onde");
            holder.getTvwNeigh().setTypeface(null, Typeface.BOLD);
            holder.getTvwUnava().setText("Tempo");
            holder.getTvwUnava().setTypeface(null, Typeface.BOLD);
        }else{
            holder.getTvwTime().setText("--");
            holder.getTvwNeigh().setText(data.getNeigh());
            holder.getTvwUnava().setText("" + data.getUna());
        }


    }

    @Override
    public int getItemViewType(int position) {
        if(isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position){
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void setMobileMode(boolean mobileMode) {
        isMobileMode = mobileMode;
    }
}
