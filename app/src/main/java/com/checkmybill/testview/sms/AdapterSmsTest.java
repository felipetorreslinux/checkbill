package com.checkmybill.testview.sms;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.SignalStrengthAverage;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.testview.TestListItemHolder;

import java.util.List;

/**
 * Created by espe on 26/09/2016.
 */

public class AdapterSmsTest extends RecyclerView.Adapter<TestListItemHolder> {
    private List<SmsMonitor> lista;
    private Context context;
    private int itemLayout;
    private CustomItemClickListener customItemClickListener;

    public AdapterSmsTest(Context context, List<SmsMonitor> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public TestListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final TestListItemHolder testListItemHolder = new TestListItemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customItemClickListener.onItemClick(v, testListItemHolder.getAdapterPosition());
            }
        });

        return testListItemHolder;
    }

    public void swap(List<SmsMonitor> dataUses) {
        notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(TestListItemHolder holder, int position) {
        SmsMonitor smsData = lista.get(position);

        holder.getTvwC1().setText("ID SMS");
        holder.getTvwV1().setText( String.valueOf( smsData.getId()) );

        holder.getTvwC2().setText("Tel. Destino:");
        holder.getTvwV2().setText( smsData.getToAddress() );

        String sdt_ = DateFormat.format("dd-MM-yyyy  kk:mm", smsData.getDateCad()).toString();
        holder.getTvwC3().setText("Dt. Envio:");
        holder.getTvwV3().setText( sdt_ );

        holder.getTvwC4().setText("Status da Mensagem:");
        holder.getTvwV4().setText( "Enviado" );
    }

    @Override
    public int getItemCount() {
        return this.lista.size();
    }
}
