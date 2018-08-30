package com.checkmybill.testview.ligacoes;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.testview.TestListItemHolder;

import java.util.List;

/**
 * Created by espe on 26/09/2016.
 */

public class AdapterLigacoesTest extends RecyclerView.Adapter<TestListItemHolder> {
    private List<CallMonitor> lista;
    private Context context;
    private int itemLayout;
    private CustomItemClickListener customItemClickListener;

    public AdapterLigacoesTest(Context context, List<CallMonitor> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
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

    public void swap(List<CallMonitor> dataUses) {
        notifyDataSetChanged();
    }

    private String convertSecondsToLongStrHoursMinutes(long seconds) {
        long hours = seconds / 3600;
        long minutes = ((seconds / 60) % 60);
        long secs = (seconds % 60);

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(TestListItemHolder holder, int position) {
        CallMonitor callData = lista.get(position);
        holder.getTvwC1().setText("ID Ligação");
        holder.getTvwV1().setText(String.valueOf( callData.getId()) );

        holder.getTvwC2().setText("Nº Telefone");
        holder.getTvwV2().setText( callData.getTelNumber() );

        holder.getTvwC3().setText("Duração da Chamada");
        holder.getTvwV3().setText( convertSecondsToLongStrHoursMinutes(callData.getElapsedTime()) );

        String sdt_ = DateFormat.format("dd-MM-yyyy  kk:mm", callData.getDateCad()).toString();
        holder.getTvwC4().setText("Dt. Realizada:");
        holder.getTvwV4().setText( sdt_ );
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
