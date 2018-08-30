package com.checkmybill.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.entity.MedicaoDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor Guerra on 29/02/2016.
 */
public class AdapterAnalyticsSignalQuality extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<MedicaoDto> lista = new ArrayList<>();

    public AdapterAnalyticsSignalQuality(Context context, List<MedicaoDto> lista) {
        this.context = context;
        this.lista = lista;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    @Override
    public MedicaoDto getItem(int i) {
        return lista.get(i);
    }

    public void update(List<MedicaoDto> listaAux) {
        lista.clear();
        lista.addAll(listaAux);

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_signal_quality, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        MedicaoDto medicao = getItem(position);

        mViewHolder.txt_data.setText(medicao.getDatahora_fim_medicao());
        mViewHolder.txt_localizacao.setText(medicao.getTelefone_latitude() + ", " + medicao.getTelefone_longitude());
        mViewHolder.txt_sinal.setText(String.valueOf(medicao.getNivel_sinal()));
        mViewHolder.txt_operadora.setText(medicao.getOperadora());
        mViewHolder.txt_qualidade.setText(medicao.getNivel_sinal_text());
        if (medicao.getTipo_pacote().equals("MI")) {
            mViewHolder.layout_tempo_indisponivel.setVisibility(View.VISIBLE);
            mViewHolder.txt_tempo_indisponivel.setText(String.valueOf(medicao.getTempo_total_indisponivel()));
        } else {
            mViewHolder.layout_tempo_indisponivel.setVisibility(View.GONE);
        }

        int id_icon;
        if (medicao.getTipo_pacote().equals("MA")) {
            id_icon = R.drawable.icon_qualidade;
        } else {
            id_icon = R.drawable.medicao_icon;
        }
        BitmapDrawable d = (BitmapDrawable) context.getResources().getDrawable(id_icon);
        d.setLevel(1234);

        BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
        Bitmap b = bd.getBitmap();
        Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 4, b.getHeight() / 4, false);

        mViewHolder.img_header.setImageBitmap(bhalfsize);

        return convertView;
    }

    private class MyViewHolder {
        TextView txt_data, txt_localizacao, txt_sinal, txt_operadora, txt_tempo_indisponivel, txt_qualidade;
        LinearLayout layout_tempo_indisponivel;
        ImageView img_header;

        public MyViewHolder(View item) {
            txt_data = (TextView) item.findViewById(R.id.txt_data);
            txt_localizacao = (TextView) item.findViewById(R.id.txt_localizacao);
            txt_sinal = (TextView) item.findViewById(R.id.txt_nivel_sinal);
            txt_operadora = (TextView) item.findViewById(R.id.txt_operadora);
            txt_qualidade = (TextView) item.findViewById(R.id.txt_qualidade);
            txt_tempo_indisponivel = (TextView) item.findViewById(R.id.txt_tempo_indisponivel);

            layout_tempo_indisponivel = (LinearLayout) item.findViewById(R.id.layout_tempo_indisponivel);

            img_header = (ImageView) item.findViewById(R.id.img_header);

        }
    }
}
