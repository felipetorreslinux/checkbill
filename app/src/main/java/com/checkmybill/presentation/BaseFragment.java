package com.checkmybill.presentation;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by guinetik on 8/21/16.
 */
public class BaseFragment extends Fragment {
    public String LOG_TAG;
    private int tabPosition;

    public BaseFragment() {
        super();
        LOG_TAG = getClass().getName();
    }

    private static final String ARG_SECTION_NUMBER = "section_number";

    public void setSectionNumber(int sectionNumber) {
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        this.setArguments(args);
    }

    public void setTabPosition(int position) {
        this.tabPosition = position;
    }

    public int getTabPosition() {
        return this.tabPosition;
    }

    public void focusReceived() {
    }
}
