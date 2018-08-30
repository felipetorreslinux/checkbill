package com.checkmybill.presentation;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.checkmybill.R;
import com.checkmybill.adapters.AdapterNetworkQuality;
import com.checkmybill.adapters.CustomItemClickListener;
import com.checkmybill.db.OrmLiteHelper;
import com.checkmybill.entity.NetworkQuality;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.List;

@EActivity(R.layout.activity_show_network_quality)
public class ShowNetworkQuality extends BaseActivity {

    @ViewById(R.id.networkList)
    protected RecyclerView recyclerView;

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CustomItemClickListener networkItemClickListener = new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                //nothing
            }
        };

        RuntimeExceptionDao<NetworkQuality, Integer> networkQualityRuntimeExceptionDao = OrmLiteHelper.getInstance(ShowNetworkQuality.this).getNetworkQualityRuntimeDao();
        List<NetworkQuality> networkQualities = networkQualityRuntimeExceptionDao.queryForAll();
        Collections.reverse(networkQualities);
        if (!networkQualities.isEmpty()) {
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            this.recyclerView.setItemAnimator(new DefaultItemAnimator());
            this.recyclerView.setHasFixedSize(true);
            AdapterNetworkQuality adapterNetworkQuality = new AdapterNetworkQuality(this, networkQualities, R.layout.list_network_quality, networkItemClickListener);
            this.recyclerView.setAdapter(adapterNetworkQuality);
        } else {
            Toast.makeText(this, "Nenhum dado de teste registrado!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
