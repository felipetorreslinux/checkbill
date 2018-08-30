package com.checkmybill.presentation;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.checkmybill.R;
import com.checkmybill.presentation.SpeedTestFragments.TestFragment;
import com.checkmybill.presentation.SpeedTestFragments.TestFragment_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Espe on 03/08/2016.
 */
@EActivity(R.layout.activity_speed_test)
public class SpeedTestActivity extends BaseActivity {

    @ViewById(R.id.toolbar)
    protected Toolbar toolbar;

    TestFragment testSpeedFragment;

    @Override
    public void onStart() {
        super.onStart();
        //
        this.initToolbar(toolbar);
        // get an instance of FragmentTransaction from your Activity
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //add a fragment
        testSpeedFragment = new TestFragment_();
        fragmentTransaction.add(R.id.fragment_container, testSpeedFragment);
        fragmentTransaction.commit();
    }
}
