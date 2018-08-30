package com.checkmybill.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.RecargasPlano;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by ESPENOTE-06, ${CORP} on 23/11/2016.
 */

public class AdapterRecargas extends RecyclerView.Adapter<AdapterRecargas.ViewHolder> {
    private List<RecargasPlano> lista;
    private Context context;
    private CustomItemClickListener removeRecargaClickListener;

    public AdapterRecargas(List<RecargasPlano> lista, Context context, CustomItemClickListener removeRecargaClickListener) {
        this.lista = lista;
        this.context = context;
        this.removeRecargaClickListener = removeRecargaClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_recarga_item, parent, false);
        return new AdapterRecargas.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RecargasPlano recarga = lista.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        holder.valorRecarga.setText( String.format("R$ %.2f", recarga.getValorRecarga()));
        holder.dataRecarga.setText( sdf.format(recarga.getDataRecarga())) ;
        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeRecargaClickListener.onItemClick(view, position);
            }
        });
    }

    public List<RecargasPlano> getLista() {
        return lista;
    }

    public void setLista(List<RecargasPlano> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        public TextView valorRecarga, dataRecarga;
        public ImageView removeBtn;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            this.valorRecarga = (TextView) view.findViewById(R.id.recarga_valor);
            this.dataRecarga = (TextView) view.findViewById(R.id.recarga_dataCad);
            this.removeBtn = (ImageView) view.findViewById(R.id.recarga_removeImgBtn);
        }

        public View getView () { return this.view; }
    }
}
