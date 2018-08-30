package com.checkmybill.presentation;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.checkmybill.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayOutputStream;

/**
 * Created by Victor Guerra on 01/06/2016.
 */
@EFragment(R.layout.fragment_reclame_info)
public class ReclameInfoFragment extends BaseFragment {

    @ViewById(R.id.imgReclame)
    protected ImageView imgReclame;

    @Override
    public void onStart(){
        super.onStart();
        try {
            Bitmap bitImg = (Bitmap) getActivity().getIntent().getExtras().get("img");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imgReclame.setImageBitmap(bitImg);
        } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR", e);
            this.getActivity().finish();
        }
    }
}
