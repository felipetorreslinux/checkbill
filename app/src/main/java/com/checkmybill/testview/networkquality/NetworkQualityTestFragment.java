package com.checkmybill.testview.networkquality;

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
import com.checkmybill.entity.NetworkQuality;
import com.checkmybill.entity.NetworkWifi;
import com.checkmybill.testview.TestMainActivity;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Guerra on 04/07/2016.
 */
public class NetworkQualityTestFragment extends Fragment {

    private static final String COLUMN_DAT_USE_DATA = "NET_QUALI_DATE";
    private static final String TAG = "NetworkQualiTestFrag";
    private static final long LIMIT = 10;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private CustomItemClickListener customItemClickListener;
    private LinearLayoutManager mLayoutManagerRecyclerView;
    private ProgressBar progressBarPagination;

    private List<NetworkQuality> networkQualities;

    private long offSet;
    private long countOff;

    private boolean onCreateView;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public NetworkQualityTestFragment() {
    }

    public static NetworkQualityTestFragment newInstance(int sectionNumber) {
        NetworkQualityTestFragment fragment = new NetworkQualityTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_main, container, false);

        setUpBinders(rootView);

        LoadDataUses loadDataUses = new LoadDataUses();
        loadDataUses.execute();

        return rootView;
    }

    private void setUpBinders(View view) {
        onCreateView = false;
        offSet = 0;
        countOff = 0;

        networkQualities = new ArrayList<>();

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
        //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                progressBarPagination.setVisibility(View.VISIBLE);
                                offSet += LIMIT;
                                //sendProductsRequest();

                                LoadDataUses loadDataUses = new LoadDataUses();
                                loadDataUses.execute();

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

    protected class LoadDataUses extends AsyncTask {

        private List<NetworkQuality> networkQualitiesAux;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            networkQualitiesAux = new ArrayList<>();
            networkQualitiesAux = getNetworlQualityRegisters();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try {

                //            double diffBytes = 0;
                if (!networkQualitiesAux.isEmpty()) {

                    networkQualities.addAll(networkQualitiesAux);

                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setVisibility(View.VISIBLE);

                        AdapterNetworkQualityTest adapterNetworkQualityTest = new AdapterNetworkQualityTest(getActivity(), networkQualities, R.layout.list_network_quality_test, customItemClickListener);
                        recyclerView.setAdapter(adapterNetworkQualityTest);
                    } else {
                        AdapterNetworkQualityTest adapterNetworkQualityTest = (AdapterNetworkQualityTest) recyclerView.getAdapter();
                        adapterNetworkQualityTest.swap(networkQualities);
                    }

                    Log.i(getClass().getName(), "First: " + networkQualitiesAux.get(0).getId() + " / Last: " + networkQualitiesAux.get(networkQualitiesAux.size() - 1).getId());

                } else {
                    if (fragmentIsOnCurrentPager())
                        Toast.makeText(getActivity(), "Sem registros ;/", Toast.LENGTH_SHORT).show();
                }

                progressBarPagination.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

            }catch(IllegalArgumentException e){
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            }catch (RuntimeException e){
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            }
        }

        private List<NetworkQuality> getNetworlQualityRegisters() {
            List<NetworkQuality> networkQualities = null;
            try{
                RuntimeExceptionDao<NetworkQuality, Integer> networkQualityRuntimeDao = OrmLiteHelper.getInstance(getActivity()).getNetworkQualityRuntimeDao();
                try {
                    countOff = networkQualityRuntimeDao.queryBuilder().countOf();
                    networkQualities = networkQualityRuntimeDao.queryBuilder()
                            .offset(new Long(offSet))
                            .limit(new Long(LIMIT))
                            .orderBy(COLUMN_DAT_USE_DATA, false)
                            .query();

                    RuntimeExceptionDao<NetworkWifi, Integer> networkWifiRuntimeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getNetworkWifiRuntimeExceptionDao();
                    for (NetworkQuality networkQuality : networkQualities) {
                        networkWifiRuntimeExceptionDao.refresh(networkQuality.getNetworkWifi());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }catch(IllegalArgumentException e){
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            }catch (RuntimeException e){
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            }

            return networkQualities;
        }
    }

}
