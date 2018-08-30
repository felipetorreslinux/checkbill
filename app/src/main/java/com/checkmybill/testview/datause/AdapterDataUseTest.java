package com.checkmybill.testview.datause;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;
import com.checkmybill.service.TrafficMonitor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by Victor Guerra on 29/02/2016.
 */

public class AdapterDataUseTest extends RecyclerView.Adapter<DataUseTestListItemHolder> {
    private List<? extends Object> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;
    private boolean isMobileMode;

    public AdapterDataUseTest(Context context, List<?extends Object> lista, int itemLayout, boolean isMobile, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
        this.isMobileMode = isMobile;
    }

    @Override
    public DataUseTestListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final DataUseTestListItemHolder dataUseTestListItemHolder = new DataUseTestListItemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customItemClickListener.onItemClick(v, dataUseTestListItemHolder.getAdapterPosition());
            }
        });

        return dataUseTestListItemHolder;
    }

    public void onBindViewHolder(DataUseTestListItemHolder holder, int position) {
        long transBytes;
        Date periodDate;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Object dataUse = lista.get(position);
        boolean isSync;

        holder.getTvwC1().setText("Data");
        holder.getTvwC2().setText("Categoria");
        holder.getTvwC3().setText("Transf.(Rx & Tx)");
        holder.getTvwC4().setText("Sincronizado");

        if ( isMobileMode ) {
            holder.getTvwV2().setText("GSM/Mob");
            TrafficMonitor_Mobile monData = (TrafficMonitor_Mobile) dataUse;
            periodDate = monData.getDatePeriodo();
            transBytes =  monData.getCurrentReceivedBytes_end() - monData.getCurrentSendedBytes_start();
            transBytes += monData.getCurrentSendedBytes_end() - monData.getCurrentSendedBytes_start();
            isSync = monData.getDataSyncronized();
        } else {
            holder.getTvwV2().setText("WiFi");
            TrafficMonitor_WiFi monData = (TrafficMonitor_WiFi) dataUse;
            periodDate = monData.getDatePeriodo();
            transBytes =  monData.getCurrentReceivedBytes_end() - monData.getCurrentSendedBytes_start();
            transBytes += monData.getCurrentSendedBytes_end() - monData.getCurrentSendedBytes_start();
            isSync = monData.getDataSyncronized();
        }

        holder.getTvwV1().setText(sdf.format(periodDate));
        holder.getTvwV3().setText(TrafficMonitor.FormatBytes(context, transBytes));
        holder.getTvwV4().setText((isSync) ? "Sim" : "Não");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public void setMobileMode(boolean mobileMode) {
        isMobileMode = mobileMode;
    }
}
