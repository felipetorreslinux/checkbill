package com.checkmybill.adapters.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 14/04/2016.
 */
public class DataIndisDetailListItemHolder extends RecyclerView.ViewHolder {

    private TextView tvwTime, tvwNeigh, tvwUnava;

    private Boolean isHeader;

    public DataIndisDetailListItemHolder(View v) {
        super(v);
        tvwTime = (TextView) v.findViewById(R.id.tvwTime);
        tvwNeigh = (TextView) v.findViewById(R.id.tvwNeigh);
        tvwUnava = (TextView) v.findViewById(R.id.tvwUnava);
        isHeader = false;
    }

    public TextView getTvwTime() {
        return tvwTime;
    }

    public void setTvwTime(TextView tvwTime) {
        this.tvwTime = tvwTime;
    }

    public TextView getTvwNeigh() {
        return tvwNeigh;
    }

    public void setTvwNeigh(TextView tvwNeigh) {
        this.tvwNeigh = tvwNeigh;
    }

    public TextView getTvwUnava() {
        return tvwUnava;
    }

    public void setTvwUnava(TextView tvwUnava) {
        this.tvwUnava = tvwUnava;
    }

    public Boolean getHeader() {
        return isHeader;
    }

    public void setHeader(Boolean header) {
        isHeader = header;
    }
}
