package com.checkmybill.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 14/04/2016.
 */
public class NetwokQualityListItemHolder extends RecyclerView.ViewHolder {

    private TextView tvwNetworkName, tvwDate, tvwLatencia, tvwUpload, tvwDownload;

    public NetwokQualityListItemHolder(View v) {
        super(v);
        tvwNetworkName = (TextView) v.findViewById(R.id.tvwNetworkName);
        tvwDate = (TextView) v.findViewById(R.id.tvwDate);
        tvwLatencia = (TextView) v.findViewById(R.id.tvwLatencia);
        tvwUpload = (TextView) v.findViewById(R.id.tvwUpload);
        tvwDownload = (TextView) v.findViewById(R.id.tvwDownload);

    }

    public TextView getTvwNetworkName() {
        return tvwNetworkName;
    }

    public void setTvwNetworkName(TextView tvwNetworkName) {
        this.tvwNetworkName = tvwNetworkName;
    }

    public TextView getTvwDate() {
        return tvwDate;
    }

    public void setTvwDate(TextView tvwDate) {
        this.tvwDate = tvwDate;
    }

    public TextView getTvwLatencia() {
        return tvwLatencia;
    }

    public void setTvwLatencia(TextView tvwLatencia) {
        this.tvwLatencia = tvwLatencia;
    }

    public TextView getTvwUpload() {
        return tvwUpload;
    }

    public void setTvwUpload(TextView tvwUpload) {
        this.tvwUpload = tvwUpload;
    }

    public TextView getTvwDownload() {
        return tvwDownload;
    }

    public void setTvwDownload(TextView tvwDownload) {
        this.tvwDownload = tvwDownload;
    }
}
