package com.checkmybill.adapters.ranking;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

public class RankingBandaLargaHolder extends RecyclerView.ViewHolder {

    private TextView tvwPosition, tvwOperatorName, tvwSpeed, tvwInfo;
    private LinearLayout head;

    public RankingBandaLargaHolder(View v) {
        super(v);
        tvwPosition = (TextView) v.findViewById(R.id.tvwPosition);
        tvwOperatorName = (TextView) v.findViewById(R.id.tvwOperatorName);
        tvwSpeed = (TextView) v.findViewById(R.id.tvwSpeed);
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

    public TextView getTvwSpeed() {
        return tvwSpeed;
    }

    public void setTvwSpeed(TextView tvwSpeed) {
        this.tvwSpeed = tvwSpeed;
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
