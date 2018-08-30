package com.checkmybill.testview.unavailability;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.Unavailability;
import com.checkmybill.testview.TestMainActivity;
import com.checkmybill.util.Util;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Guerra on 04/07/2016.
 */
public class UnavailabilityTestFragment extends Fragment {

    private static final String COLUMN_DAT_USE_DATA = "UNAVA_DATE_STARTED";
    private static final String TAG = "UnavailaTestFrag";
    private static final long LIMIT = 10;

    private Context mContext;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private CustomItemClickListener customItemClickListener;
    private LinearLayoutManager mLayoutManagerRecyclerView;
    private ProgressBar progressBarPagination;

    private List<Unavailability> unavailabilities;

    private long offSet;
    private long countOff;

    private boolean onCreateView;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public UnavailabilityTestFragment() {
    }

    public static UnavailabilityTestFragment newInstance(int sectionNumber) {
        UnavailabilityTestFragment fragment = new UnavailabilityTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_main, container, false);
        mContext = getContext();
        setUpBinders(rootView);

        LoadDataUses loadDataUses = new LoadDataUses();
        loadDataUses.execute();

        return rootView;
    }

    private void setUpBinders(View view) {
        onCreateView = false;
        offSet = 0;
        countOff = 0;

        unavailabilities = new ArrayList<>();

        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBarPagination = (ProgressBar) view.findViewById(R.id.progressBarPagination);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManagerRecyclerView = new LinearLayoutManager(getActivity());

        setUpClickListener();
        setUpRecyclerView();
    }

    private void setUpClickListener() {
        // Acao para o checkbox...
        customItemClickListener = new CustomItemClickListener() {
            @Override
            public void onItemClick(final View v, final int position) {
                // Exibindo interface perguntando o motivo da alteracao...
                CheckBox cb = (CheckBox) v;

                // Modificando o status do elemento para salvar as alteracoes no banco
                unavailabilities.get(position).setUsed( cb.isChecked() );
                final RuntimeExceptionDao<Unavailability, Integer> unavailabilityDao = OrmLiteHelper.getInstance(mContext).getUnavailabilityRuntimeDao();
                unavailabilityDao.update( unavailabilities.get(position) );
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

        private List<Unavailability> unavailabilitiesAux;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            unavailabilitiesAux = new ArrayList<>();
            unavailabilitiesAux = getSignalStrengthRegisters();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            try{
    //            double diffBytes = 0;
                if (!unavailabilitiesAux.isEmpty()) {

                    unavailabilities.addAll(unavailabilitiesAux);

                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setVisibility(View.VISIBLE);

                        AdapterUnavailabilityTest adapterUnavailabilityTest = new AdapterUnavailabilityTest(getActivity(), unavailabilities, R.layout.list_unavailability_test, customItemClickListener);
                        recyclerView.setAdapter(adapterUnavailabilityTest);
                    } else {
                        AdapterUnavailabilityTest adapterUnavailabilityTest = (AdapterUnavailabilityTest) recyclerView.getAdapter();
                        adapterUnavailabilityTest.swap(unavailabilities);
                    }

                    Log.i(getClass().getName(), "First: " + unavailabilitiesAux.get(0).getId() + " / Last: " + unavailabilitiesAux.get(unavailabilitiesAux.size() - 1).getId());

                } else {
                    if ( fragmentIsOnCurrentPager() )
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

        private List<Unavailability> getSignalStrengthRegisters() {
            List<Unavailability> unavailabilities = null;
            try{
                RuntimeExceptionDao<Unavailability, Integer> unavailabilityRuntimeExceptionDao = OrmLiteHelper.getInstance(getActivity()).getUnavailabilityRuntimeDao();
                try {
                    countOff = unavailabilityRuntimeExceptionDao.queryBuilder().countOf();
                    unavailabilities = unavailabilityRuntimeExceptionDao.queryBuilder()
                            .offset(new Long(offSet))
                            .limit(new Long(LIMIT))
                            .orderBy(COLUMN_DAT_USE_DATA, false)
                            .query();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }catch(IllegalArgumentException e){
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            }catch (RuntimeException e){
                Log.e(TAG, Util.getMessageErrorFromExcepetion(e));
            }


            return unavailabilities;
        }
    }

}
