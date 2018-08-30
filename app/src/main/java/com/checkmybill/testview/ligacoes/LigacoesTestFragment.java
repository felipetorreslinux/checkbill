package com.checkmybill.testview.ligacoes;

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
import com.checkmybill.entity.CallMonitor;
import com.checkmybill.entity.SmsMonitor;
import com.checkmybill.testview.TestMainActivity;
import com.checkmybill.testview.sms.AdapterSmsTest;
import com.checkmybill.testview.sms.SmsTestFragment;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petrus Augusto on 26/09/2016.
 */
public class LigacoesTestFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String COLUMN_DAT_USE_DATA = "CALL_DAT_CAD";
    private static final long LIMIT = 10;
    private static String LOG_TAG;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private CustomItemClickListener customItemClickListener;
    private LinearLayoutManager mLayoutManagerRecyclerView;
    private ProgressBar progressBarPagination;

    private long offSet;
    private long countOff;

    private List<CallMonitor> callData;
    private boolean onCreateView;

    public LigacoesTestFragment() {
        // Required empty public constructor
    }

    public static LigacoesTestFragment newInstance(int sectionNumber) {
        LOG_TAG = LigacoesTestFragment.class.getName();
        LigacoesTestFragment fragment = new LigacoesTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_main, container, false);

        setUpBinders(rootView);

        LoadCallData callDataLoader = new LoadCallData();
        callDataLoader.execute();

        return rootView;
    }

    private void setUpBinders(View view) {
        onCreateView = false;
        offSet = 0;
        countOff = 0;

        callData = new ArrayList<>();

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

                                LoadCallData callDataLoader = new LoadCallData();
                                callDataLoader.execute();
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

    private class LoadCallData extends AsyncTask<Void, Void, List<CallMonitor>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<CallMonitor> doInBackground(Void... params) {
            // Obtendo os dados do bando
            List<CallMonitor> callReadedData = null;
            RuntimeExceptionDao<CallMonitor, Integer> dataCallMonitorDao = OrmLiteHelper.getInstance(getActivity()).getCallMonitorRuntimeExceptionDao();
            try {
                countOff = dataCallMonitorDao.queryBuilder().countOf();
                callReadedData = dataCallMonitorDao.queryBuilder()
                        .offset(new Long(offSet))
                        .limit(new Long(LIMIT))
                        .orderBy(COLUMN_DAT_USE_DATA, false)
                        .query();
            } catch ( Exception ex ) {
                Log.e(LOG_TAG, ex.getMessage());
            }

            return callReadedData;
        }

        @Override
        protected void onPostExecute(List<CallMonitor> r) {
            try {
                if (!r.isEmpty()) {
                    callData.addAll(r);
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setVisibility(View.VISIBLE);
                        AdapterLigacoesTest adapterDataUseTest = new AdapterLigacoesTest(getActivity(), callData, R.layout.list_data_use_test, customItemClickListener);
                        recyclerView.setAdapter(adapterDataUseTest);
                    } else {
                        AdapterLigacoesTest adapterDataUseTest = (AdapterLigacoesTest) recyclerView.getAdapter();
                        adapterDataUseTest.swap(callData);
                    }

                    Log.i(LOG_TAG, "First: " + callData.get(0).getId() + " / Last: " + callData.get(callData.size() - 1).getId());
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
