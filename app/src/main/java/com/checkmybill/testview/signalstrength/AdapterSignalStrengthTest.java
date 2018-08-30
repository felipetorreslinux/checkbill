package com.checkmybill.testview.signalstrength;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.SignalStrengthAverage;
import com.checkmybill.testview.TestListItemHolder;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterSignalStrengthTest extends RecyclerView.Adapter<TestListItemHolder> {

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private List<SignalStrengthAverage> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterSignalStrengthTest(Context context, List<SignalStrengthAverage> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
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

    public void swap(List<SignalStrengthAverage> dataUses) {
        notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(TestListItemHolder holder, int position) {
        //cid, lac, mnc, date, value, qualidade, lat|lng, saved
        SignalStrengthAverage signalStrengthAverage = lista.get(position);

        String sdt_ = DateFormat.format("dd-MM-yyyy  kk:mm", signalStrengthAverage.getDate()).toString();
        holder.getTvwC1().setText("Data");
        holder.getTvwV1().setText(sdt_);

        holder.getTvwC4().setText("CID|LAC|MNC");
        holder.getTvwV4().setText(signalStrengthAverage.getCellId());

        holder.getTvwC5().setText("Sinal (dbm)");
        holder.getTvwV5().setText(String.valueOf(signalStrengthAverage.getValue()));

        holder.getTvwC6().setText("Qualidade");
        holder.getTvwV6().setText(getQualidadeSinal((int) signalStrengthAverage.getValue()));

        holder.getTvwC7().setText("Sync");
        holder.getTvwV7().setText(String.valueOf(signalStrengthAverage.isSaved()));

        holder.getTvwC8().setText("Posição");
        holder.getTvwV8().setText(String.valueOf(signalStrengthAverage.getLat()) + ", " + String.valueOf(signalStrengthAverage.getLng()));
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

    private String getQualidadeSinal(int signal) {
        int valueASU = (signal + 133) / 2;
        int valuePercent = (valueASU * 100) / 31;

        if (valuePercent >= 0 && valuePercent < 14.3) {
            return context.getString(R.string.pessimo);
        } else if (valuePercent >= 14.3 && valuePercent < 28.6) {
            return context.getString(R.string.muito_fraco);
        } else if (valuePercent >= 28.6 && valuePercent < 42.9) {
            return context.getString(R.string.fraco);
        } else if (valuePercent >= 42.9 && valuePercent < 57.2) {
            return context.getString(R.string.normal);
        } else if (valuePercent >= 57.2 && valuePercent < 71.5) {
            return context.getString(R.string.bom);
        } else if (valuePercent >= 71.5 && valuePercent < 85.8) {
            return context.getString(R.string.muito_bom);
        } else if (valuePercent >= 85.8) {
            return context.getString(R.string.excelente);
        } else {
            return "n/a";
        }

    }
}
