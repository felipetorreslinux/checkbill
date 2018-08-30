package com.checkmybill.testview.datause;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 14/04/2016.
 */
public class DataUseTestListItemHolder extends RecyclerView.ViewHolder {

    private TextView tvwC1, tvwV1;
    private TextView tvwC2, tvwV2;
    private TextView tvwC3, tvwV3;
    private TextView tvwC4, tvwV4;


    public DataUseTestListItemHolder(View v) {
        super(v);
        tvwC1 = (TextView) v.findViewById(R.id.c1);
        tvwV1 = (TextView) v.findViewById(R.id.v1);

        tvwC2 = (TextView) v.findViewById(R.id.c2);
        tvwV2 = (TextView) v.findViewById(R.id.v2);

        tvwC3 = (TextView) v.findViewById(R.id.c3);
        tvwV3 = (TextView) v.findViewById(R.id.v3);

        tvwC4 = (TextView) v.findViewById(R.id.c4);
        tvwV4 = (TextView) v.findViewById(R.id.v4);
    }

    public TextView getTvwC1() {
        return tvwC1;
    }

    public void setTvwC1(TextView tvwC1) {
        this.tvwC1 = tvwC1;
    }

    public TextView getTvwV1() {
        return tvwV1;
    }

    public void setTvwV1(TextView tvwV1) {
        this.tvwV1 = tvwV1;
    }

    public TextView getTvwC2() {
        return tvwC2;
    }

    public void setTvwC2(TextView tvwC2) {
        this.tvwC2 = tvwC2;
    }

    public TextView getTvwV2() {
        return tvwV2;
    }

    public void setTvwV2(TextView tvwV2) {
        this.tvwV2 = tvwV2;
    }

    public TextView getTvwC3() {
        return tvwC3;
    }

    public void setTvwC3(TextView tvwC3) {
        this.tvwC3 = tvwC3;
    }

    public TextView getTvwV3() {
        return tvwV3;
    }

    public void setTvwV3(TextView tvwV3) {
        this.tvwV3 = tvwV3;
    }

    public TextView getTvwC4() {
        return tvwC4;
    }

    public void setTvwC4(TextView tvwC4) {
        this.tvwC4 = tvwC4;
    }

    public TextView getTvwV4() {
        return tvwV4;
    }

    public void setTvwV4(TextView tvwV4) {
        this.tvwV4 = tvwV4;
    }
}
