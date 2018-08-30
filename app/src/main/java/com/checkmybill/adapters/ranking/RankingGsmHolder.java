package com.checkmybill.adapters.ranking;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

public class RankingGsmHolder extends RecyclerView.ViewHolder {

    private TextView tvwPosition, tvwOperatorName, tvwSignalCategory, tvwSignalValue, tvwInfo;
    private LinearLayout head;

    public RankingGsmHolder(View v) {
        super(v);
        tvwPosition = (TextView) v.findViewById(R.id.tvwPosition);
        tvwOperatorName = (TextView) v.findViewById(R.id.tvwOperatorName);
        tvwSignalCategory = (TextView) v.findViewById(R.id.tvwSignalCategory);
        tvwSignalValue = (TextView) v.findViewById(R.id.tvwSignalValue);
        tvwInfo = (TextView) v.findViewById(R.id.tvwInfo);
        head = (LinearLayout) v.findViewById(R.id.head);

    }

    public TextView getTvwPosition() {
        return tvwPosition;
    }

    public void setTvwPosition(TextView tvwPosition) {
        this.tvwPosition = tvwPosition;
    }

    public TextView getTvwOperatorName() {
        return tvwOperatorName;
    }

    public void setTvwOperatorName(TextView tvwOperatorName) {
        this.tvwOperatorName = tvwOperatorName;
    }

    public TextView getTvwSignalCategory() {
        return tvwSignalCategory;
    }

    public void setTvwSignalCategory(TextView tvwSignalCategory) {
        this.tvwSignalCategory = tvwSignalCategory;
    }

    public TextView getTvwSignalValue() {
        return tvwSignalValue;
    }

    public void setTvwSignalValue(TextView tvwSignalValue) {
        this.tvwSignalValue = tvwSignalValue;
    }

    public TextView getTvwInfo() {
        return tvwInfo;
    }

    public void setTvwInfo(TextView tvwInfo) {
        this.tvwInfo = tvwInfo;
    }

    public LinearLayout getHead() {
        return head;
    }

    public void setHead(LinearLayout head) {
        this.head = head;
    }
}
