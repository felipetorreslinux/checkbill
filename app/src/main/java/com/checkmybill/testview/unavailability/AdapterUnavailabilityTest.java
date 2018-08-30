package com.checkmybill.testview.unavailability;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.testview.TestListItemHolder;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterUnavailabilityTest extends RecyclerView.Adapter<TestListItemHolder> {

    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private List<Unavailability> lista;
    private int itemLayout;
    private Context context;
    private CustomItemClickListener customItemClickListener;

    public AdapterUnavailabilityTest(Context context, List<Unavailability> lista, int itemLayout, CustomItemClickListener customItemClickListener) {
        this.context = context;
        this.lista = lista;
        this.itemLayout = itemLayout;
        this.customItemClickListener = customItemClickListener;
    }

    @Override
    public TestListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final TestListItemHolder testListItemHolder = new TestListItemHolder(v);

        return testListItemHolder;
    }

    public void swap(List<Unavailability> dataUses) {
        notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(TestListItemHolder holder, final int position) {
        //cidlacmnc, date, time, signal, qualidade, lat|lng, saved
        Unavailability unavailability = lista.get(position);

        String sdt_ = DateFormat.format("dd-MM-yyyy  kk:mm", unavailability.getDateStarted()).toString();
        holder.getTvwC1().setText("Data");
        holder.getTvwV1().setText(sdt_);

        holder.getTvwC2().setText("CID|LAC|MNC");
        holder.getTvwV2().setText(unavailability.getCellId());

        holder.getTvwC3().setText("Sinal (dbm)");
        holder.getTvwV3().setText(String.valueOf(unavailability.getValueSignal()));

        holder.getTvwC4().setText("Qualidade");
        holder.getTvwV4().setText(getQualidadeSinal((int) unavailability.getValueSignal()));

        holder.getTvwC5().setText("Sync");
        holder.getTvwV5().setText(String.valueOf(unavailability.isSaved()));

        holder.getTvwC6().setText("Posição");
        holder.getTvwV6().setText(String.valueOf(unavailability.getLat()) + ", " + String.valueOf(unavailability.getLng()));

        holder.getTvwC7().setText("Tempo indisponível");
        long startInMillis = new DateTime(unavailability.getDateStarted()).getMillis();
        long finishedInMillis = new DateTime(unavailability.getDateFinished()).getMillis();
        long diffInMillis = finishedInMillis - startInMillis;
        holder.getTvwV7().setText(getTimeUnavailability(diffInMillis));

        holder.changeVisibilityBottomArea(View.VISIBLE);
        Log.d(getClass().getName(), "Boolean Status:" + unavailability.isUsed());
        holder.setCheckboxAsChecked( unavailability.isUsed() );
        holder.getCheckboxAsCheckedView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customItemClickListener.onItemClick(view, position);
            }
        });
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

    private String getTimeUnavailability(long diffInMillis) {
        double seconds = diffInMillis / 1000;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (seconds < 60) {
            return String.valueOf(decimalFormat.format(seconds)) + " s";
        } else if (seconds >= 60) {
            double minutes = seconds / 60;
            if (minutes < 60) {
                return String.valueOf(decimalFormat.format(minutes)) + " min(s)";
            } else {
                double hours = minutes / 60;
                return String.valueOf(decimalFormat.format(hours)) + " hr(s)";
            }
        } else {
            return "n/a";
        }
    }
}
