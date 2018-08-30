package com.checkmybill.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 14/04/2016.
 */
public class ClusterItemHolder extends RecyclerView.ViewHolder {

    private TextView tvwC1, tvwV1;
    private TextView tvwC2, tvwV2;
    private TextView tvwC3, tvwV3;
    private TextView tvwC4, tvwV4;
    private TextView tvwC5, tvwV5;
    private TextView tvwC6, tvwV6;
    private TextView tvwC7, tvwV7;
    private TextView tvwC8, tvwV8;
    private ImageView imgView;

    public ClusterItemHolder(View v) {
        super(v);
        tvwC1 = (TextView) v.findViewById(R.id.c1);
        tvwV1 = (TextView) v.findViewById(R.id.v1);

        tvwC2 = (TextView) v.findViewById(R.id.c2);
        tvwV2 = (TextView) v.findViewById(R.id.v2);

        tvwC3 = (TextView) v.findViewById(R.id.c3);
        tvwV3 = (TextView) v.findViewById(R.id.v3);

        tvwC4 = (TextView) v.findViewById(R.id.c4);
        tvwV4 = (TextView) v.findViewById(R.id.v4);

        tvwC5 = (TextView) v.findViewById(R.id.c5);
        tvwV5 = (TextView) v.findViewById(R.id.v5);

        tvwC6 = (TextView) v.findViewById(R.id.c6);
        tvwV6 = (TextView) v.findViewById(R.id.v6);

        tvwC7 = (TextView) v.findViewById(R.id.c7);
        tvwV7 = (TextView) v.findViewById(R.id.v7);

        tvwC8 = (TextView) v.findViewById(R.id.c8);
        tvwV8 = (TextView) v.findViewById(R.id.v8);

        imgView = (ImageView) v.findViewById(R.id.imgView);
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

    public TextView getTvwC5() {
        return tvwC5;
    }

    public void setTvwC5(TextView tvwC5) {
        this.tvwC5 = tvwC5;
    }

    public TextView getTvwV5() {
        return tvwV5;
    }

    public void setTvwV5(TextView tvwV5) {
        this.tvwV5 = tvwV5;
    }

    public TextView getTvwC6() {
        return tvwC6;
    }

    public void setTvwC6(TextView tvwC6) {
        this.tvwC6 = tvwC6;
    }

    public TextView getTvwV6() {
        return tvwV6;
    }

    public void setTvwV6(TextView tvwV6) {
        this.tvwV6 = tvwV6;
    }

    public TextView getTvwC7() {
        return tvwC7;
    }

    public void setTvwC7(TextView tvwC7) {
        this.tvwC7 = tvwC7;
    }

    public TextView getTvwV7() {
        return tvwV7;
    }

    public void setTvwV7(TextView tvwV7) {
        this.tvwV7 = tvwV7;
    }

    public TextView getTvwC8() {
        return tvwC8;
    }

    public void setTvwC8(TextView tvwC8) {
        this.tvwC8 = tvwC8;
    }

    public TextView getTvwV8() {
        return tvwV8;
    }

    public void setTvwV8(TextView tvwV8) {
        this.tvwV8 = tvwV8;
    }

    public ImageView getImgView() {
        return imgView;
    }

    public void setImgView(ImageView imgView) {
        this.imgView = imgView;
    }
}
