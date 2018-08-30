package com.checkmybill.testview.networkquality;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.testview.TestListItemHolder;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterNetworkQualityTest extends RecyclerView.Adapter<TestListItemHolder> {

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private List<NetworkQuality> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterNetworkQualityTest(Context context, List<NetworkQuality> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
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

    public void swap(List<NetworkQuality> dataUses) {
        notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(TestListItemHolder holder, int position) {
        //Name network, isp, date, up, down, latency, sync
        NetworkQuality networkQuality = lista.get(position);

        String sdt_ = DateFormat.format("dd-MM-yyyy  kk:mm", networkQuality.getDate()).toString();
        holder.getTvwC1().setText("Data");
        holder.getTvwV1().setText(sdt_);

        holder.getTvwC2().setText("Nome da rede");
        holder.getTvwV2().setText(networkQuality.getName());

        holder.getTvwC3().setText("Isp");
        if (networkQuality.getNetworkWifi() != null) {
            holder.getTvwV3().setText(networkQuality.getNetworkWifi().getIsp());
        } else {
            holder.getTvwV3().setText("---");
        }

        holder.getTvwC4().setText("Download");
        holder.getTvwV4().setText(getStringBytesShow(networkQuality.getDownload()));

        holder.getTvwC5().setText("Upload");
        holder.getTvwV5().setText(getStringBytesShow(networkQuality.getUpload()));

        holder.getTvwC6().setText("Latência");
        holder.getTvwV6().setText(String.valueOf(networkQuality.getLatency()));

        holder.getTvwC7().setText("Sync");
        holder.getTvwV7().setText(String.valueOf(networkQuality.isSync()));
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
