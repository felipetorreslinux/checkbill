package com.checkmybill.testview.datause;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.TrafficMonitor_Mobile;
import com.checkmybill.entity.TrafficMonitor_WiFi;
import com.checkmybill.service.TrafficMonitor;
import com.checkmybill.testview.TestMainActivity;
import com.checkmybill.util.Util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Victor Guerra on 04/07/2016.
 */
public class DataUseTestFragment extends Fragment {
    private static String LOG_TAG;
    private static final long LIMIT = 10;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManagerRecyclerView;
    private ProgressBar progressBarPagination;
    private Spinner filterSpinner;

    private List<Object> dataUse;

    private long offSet;
    private long countOff;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public DataUseTestFragment() {
    }

    public static DataUseTestFragment newInstance(int sectionNumber) {
        LOG_TAG = DataUseTestFragment.class.getName();
        DataUseTestFragment fragment = new DataUseTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_datause, container, false);
        setUpBinders(rootView);

        LoadDataUses loadDataUses = new LoadDataUses();
        loadDataUses.execute();

        return rootView;
    }

    private boolean fragmentIsOnCurrentPager() {
        TestMainActivity mainActivity = (TestMainActivity) getActivity();
        if ( mainActivity == null ) return false;
        return ( this.getFragmentSectionNum() == (1 + mainActivity.getCurrentPageView()) );
    }

    private int getFragmentSectionNum() {
        return getArguments().getInt(ARG_SECTION_NUMBER);
    }

    private void setUpBinders(View view) {
        offSet = 0;
        countOff = 0;

        dataUse = new ArrayList<>();

        filterSpinner = (Spinner) view.findViewById(R.id.fragmentDataUse_filterSpinner);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBarPagination = (ProgressBar) view.findViewById(R.id.progressBarPagination);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManagerRecyclerView = new LinearLayoutManager(getActivity());

        setUpSpinner();
        setUpRecyclerView();
    }

    private void setUpSpinner() {
        ArrayList<String> spinnerItens = new ArrayList<>();
        spinnerItens.add( "Mobile(GSM)" );
        spinnerItens.add( "Banda Larga" );
        ArrayAdapter spinnerAdapter = new ArrayAdapter(getContext(), R.layout.simple_spinner_string_item, spinnerItens);
        filterSpinner.setAdapter( spinnerAdapter );
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataUse.clear(); // Cleaning...
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadDataUses loadDataUses = new LoadDataUses();
                        loadDataUses.execute();
                    }
                }, 300);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*Nothing*/}
        });
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
                if ( dy <= 0 ) return;
                if ( progressBarPagination.getVisibility() != View.GONE ) return;

                int visibleItemCount = mLayoutManagerRecyclerView.getChildCount();
                int totalItemCount = mLayoutManagerRecyclerView.getItemCount();
                int pastVisiblesItems = mLayoutManagerRecyclerView.findFirstVisibleItemPosition();
                if ( (visibleItemCount + pastVisiblesItems) < countOff ) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        progressBarPagination.setVisibility(View.VISIBLE);
                        offSet += LIMIT;

                        LoadDataUses loadDataUses = new LoadDataUses();
                        loadDataUses.execute();
                    }
                }
            }
        });
    }

    protected class LoadDataUses extends AsyncTask {
        private OrmLiteHelper orm;
        private int selectedFilter = 0;
        private List<?extends Object> dataUsesAux;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            selectedFilter = filterSpinner.getSelectedItemPosition();
            progressBar.setVisibility(View.VISIBLE);
            orm = OrmLiteHelper.getInstance(getContext());
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            dataUsesAux = this.calcularUsoDeDados();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if ( dataUsesAux != null && !dataUsesAux.isEmpty() ) {
                dataUse.addAll(dataUsesAux);
                AdapterDataUseTest adapterDataUseTest = (AdapterDataUseTest) recyclerView.getAdapter();
                if ( adapterDataUseTest == null ) {
                    Log.d(LOG_TAG, "Selected Filter -> " + selectedFilter);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapterDataUseTest = new AdapterDataUseTest(getContext(), dataUse, R.layout.list_data_use_test, (selectedFilter == 0), null);
                    recyclerView.setAdapter(adapterDataUseTest);
                } else {
                    adapterDataUseTest.setMobileMode((selectedFilter == 0));
                    adapterDataUseTest.notifyDataSetChanged();
                }
            } else if ( fragmentIsOnCurrentPager() ) {
                Toast.makeText(getActivity(), "Sem registros ;/", Toast.LENGTH_SHORT).show();
            }

            progressBarPagination.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }

        private List<? extends Object> calcularUsoDeDados() {
            List<? extends Object> returnArr;
            try {
                List<TrafficMonitor_Mobile> mobMonList;
                List<TrafficMonitor_WiFi> wifiMonList;
                if (selectedFilter == 0) { // GSM/Mobile
                    mobMonList = orm.getTrafficMonitorMobileDao().queryBuilder()
                            .offset(new Long(offSet))
                            .limit(new Long(LIMIT))
                            .orderBy("id_mob_monitor", false)
                            .query();
                    returnArr = mobMonList;
                } else { // BLarge/WiFi
                    wifiMonList = orm.getTrafficMonitorWifiDao().queryBuilder()
                            .offset(new Long(offSet))
                            .limit(new Long(LIMIT))
                            .orderBy("id_wifi_monitor", false)
                            .query();
                    returnArr = wifiMonList;
                }

                Log.d(LOG_TAG, "Total data -> " + returnArr.size());
            } catch ( SQLException ex ) {
                Log.e(LOG_TAG, ex.getMessage());
                returnArr = null;
            }

            return returnArr;
        }
    }

}
