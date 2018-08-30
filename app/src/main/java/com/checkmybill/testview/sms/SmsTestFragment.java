package com.checkmybill.testview.sms;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.testview.TestMainActivity;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petrus Augusto on 26/09/2016.
 */
public class SmsTestFragment extends Fragment {
    private static final String COLUMN_DAT_USE_DATA = "SMS_DAT_CAD";
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final long LIMIT = 10;
    private static String LOG_TAG;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private CustomItemClickListener customItemClickListener;
    private LinearLayoutManager mLayoutManagerRecyclerView;
    private ProgressBar progressBarPagination;

    private long offSet;
    private long countOff;

    private List<SmsMonitor> smsData;
    private boolean onCreateView;


    public SmsTestFragment() {
        // Required empty public constructor
    }

    public static SmsTestFragment newInstance(int sectionNumber) {
        LOG_TAG = SmsTestFragment.class.getName();
        SmsTestFragment fragment = new SmsTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_main, container, false);

        setUpBinders(rootView);

        LoadSmsData smsDataLoader = new LoadSmsData();
        smsDataLoader.execute();

        return rootView;
    }

    private void setUpBinders(View view) {
        onCreateView = false;
        offSet = 0;
        countOff = 0;

        smsData = new ArrayList<>();

        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBarPagination = (ProgressBar) view.findViewById(R.id.progressBarPagination);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManagerRecyclerView = new LinearLayoutManager(getActivity());

        setUpClickListener();
        setUpRecyclerView();
    }

    private void setUpClickListener() {
        customItemClickListener = new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //nothing
            }
        };
    }

    private void setUpRecyclerView() {
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManagerRecyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    int visibleItemCount = mLayoutManagerRecyclerView.getChildCount();
                    int totalItemCount = mLayoutManagerRecyclerView.getItemCount();
                    int pastVisiblesItems = mLayoutManagerRecyclerView.findFirstVisibleItemPosition();

                    if (progressBarPagination.getVisibility() == View.GONE) {
                        if ((visibleItemCount + pastVisiblesItems) < countOff) {
                            if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount ) {
                                progressBarPagination.setVisibility(View.VISIBLE);
                                offSet += LIMIT;

                                LoadSmsData smsDataLoader = new LoadSmsData();
                                smsDataLoader.execute();
                            }
                        }
                    }
                }
            }
        });
    }

    private boolean fragmentIsOnCurrentPager() {
        TestMainActivity mainActivity = (TestMainActivity) getActivity();
        if ( mainActivity == null ) return false;
        return ( this.getFragmentSectionNum() == (1 + mainActivity.getCurrentPageView()) );
    }

    private int getFragmentSectionNum() {
        return getArguments().getInt(ARG_SECTION_NUMBER);
    }

    private class LoadSmsData extends AsyncTask <Void, Void, List<SmsMonitor>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<SmsMonitor> doInBackground(Void[] params) {

            // Obtendo os dados do bando
            List<SmsMonitor> smsReadedData = null;
            RuntimeExceptionDao<SmsMonitor, Integer> dataSmsMonitorDao = OrmLiteHelper.getInstance(getActivity()).getSmsMonitorRuntimeExceptionDao();
            try {
                countOff = dataSmsMonitorDao.queryBuilder().countOf();
                smsReadedData = dataSmsMonitorDao.queryBuilder()
                        .offset(new Long(offSet))
                        .limit(new Long(LIMIT))
                        .orderBy(COLUMN_DAT_USE_DATA, false)
                        .query();
            } catch ( Exception ex ) {
                Log.e(LOG_TAG, ex.getMessage());
            }

            return smsReadedData;
        }

        @Override
        protected void onPostExecute(List<SmsMonitor> r) {
            try {
                if (!r.isEmpty()) {
                    smsData.addAll(r);
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setVisibility(View.VISIBLE);
                        AdapterSmsTest adapterDataUseTest = new AdapterSmsTest(getActivity(), smsData, R.layout.list_data_use_test, customItemClickListener);
                        recyclerView.setAdapter(adapterDataUseTest);
                    } else {
                        AdapterSmsTest adapterDataUseTest = (AdapterSmsTest) recyclerView.getAdapter();
                        adapterDataUseTest.swap(smsData);
                    }

                    Log.i(LOG_TAG, "First: " + smsData.get(0).getId() + " / Last: " + smsData.get(smsData.size() - 1).getId());
                } else {
                    if (fragmentIsOnCurrentPager())
                        Toast.makeText(getActivity(), "Sem registros ;/", Toast.LENGTH_SHORT).show();
                }

                progressBarPagination.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

            }catch(IllegalArgumentException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }catch (RuntimeException e){
                Log.e(LOG_TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }
    }
}
