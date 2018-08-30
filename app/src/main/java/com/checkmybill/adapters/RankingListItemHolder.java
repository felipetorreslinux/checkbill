package com.checkmybill.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 14/04/2016.
 */
public class RankingListItemHolder extends RecyclerView.ViewHolder {

    private TextView tvwNetworkName, tvwId, tvwDownload;
    private LinearLayout head;

    public RankingListItemHolder(View v) {
        super(v);
        tvwNetworkName = (TextView) v.findViewById(R.id.tvwNetworkName);
        tvwDownload = (TextView) v.findViewById(R.id.tvwDownload);
        tvwId = (TextView) v.findViewById(R.id.tvwId);
        head = (LinearLayout) v.findViewById(R.id.head);

    }

    public TextView getTvwNetworkName() {
        return tvwNetworkName;
    }

    public void setTvwNetworkName(TextView tvwNetworkName) {
        this.tvwNetworkName = tvwNetworkName;
    }

    public TextView getTvwDownload() {
        return tvwDownload;
    }

    public void setTvwDownload(TextView tvwDownload) {
        this.tvwDownload = tvwDownload;
    }

    public TextView getTvwId() {
        return tvwId;
    }

    public void setTvwId(TextView tvwId) {
        this.tvwId = tvwId;
    }

    public LinearLayout getHead() {
        return head;
    }

    public void setHead(LinearLayout head) {
        this.head = head;
    }
}
