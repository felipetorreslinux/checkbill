package com.checkmybill.presentation.IntroFragments;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.checkmybill.presentation.IntroFragments.IntroFragment;

/**
 * Created by Victor Guerra on 09/03/2016.
 */
public class IntroAdapter extends FragmentPagerAdapter {
    private boolean showOnlyLoginFragment;

    public IntroAdapter(FragmentManager fm) {
        super(fm);
        this.showOnlyLoginFragment = false;
    }

    public IntroAdapter(FragmentManager fm, boolean showOnlyLoginFragment) {
        super(fm);
        this.showOnlyLoginFragment = showOnlyLoginFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if ( showOnlyLoginFragment ) return IntroFragment.newInstance(Color.parseColor("#6d7578"), position); // Login
                else return IntroFragment.newInstance(Color.parseColor("#03A9F4"), position); // Step1, blue
            case 1:
                return IntroFragment.newInstance(Color.parseColor("#4CAF50"), position); // Step2, green
            case 2:
                return IntroFragment.newInstance(Color.parseColor("#01547a"), position); // Step3,
            default:
                return IntroFragment.newInstance(Color.parseColor("#6d7578"), position);// Step4, gray (Login)
        }
    }

    @Override
    public int getCount() {
        if ( this.showOnlyLoginFragment ) return 1;
        else return 4;
    }

}
