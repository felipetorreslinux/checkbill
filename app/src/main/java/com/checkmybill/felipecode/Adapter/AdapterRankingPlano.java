package com.checkmybill.felipecode.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.checkmybill.R;
import com.checkmybill.felipecode.Models.RankingModel;

import java.util.List;

public class AdapterRankingPlano extends RecyclerView.Adapter<AdapterRankingPlano.RankingPlano> {

    Activity activity;
    List<RankingModel> lista_ranking;

    public AdapterRankingPlano(Activity activity, List<RankingModel> lista_ranking){
        this.activity = activity;
        this.lista_ranking = lista_ranking;
    }

    @Override
    public RankingPlano onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking_plano, parent, false);
        return new AdapterRankingPlano.RankingPlano(view);
    }

    @Override
    public void onBindViewHolder(RankingPlano holder, int position) {
        RankingModel rankingModel = lista_ranking.get(position);

        holder.nome_plano.setText(rankingModel.getNome_plano());
        holder.operadora.setText(rankingModel.getNome_operadora());
        holder.observacao.setText(rankingModel.getObservacao());
        holder.velocidade.setText(String.valueOf(rankingModel.getInternet() / 1000)+"MB");
        holder.valor_plano.setText(rankingModel.getValor_plano());
        holder.tipo_plano.setText(rankingModel.getTipo_contrato_plano());
        holder.modalidade_plano.setText(rankingModel.getModalidade_plano());
    }

    @Override
    public int getItemCount() {
        return lista_ranking != null ? lista_ranking.size() : 0;
    }

    public class RankingPlano extends RecyclerView.ViewHolder {

        TextView nome_plano;
        TextView operadora;
        TextView observacao;
        TextView velocidade;
        TextView valor_plano;
        TextView tipo_plano;
        TextView modalidade_plano;

        public RankingPlano(View itemView) {
            super(itemView);

            nome_plano = itemView.findViewById(R.id.nome_plano);
            operadora = itemView.findViewById(R.id.operadora);
            observacao = itemView.findViewById(R.id.observacao);
            velocidade = itemView.findViewById(R.id.velocidade);
            valor_plano = itemView.findViewById(R.id.valor_plano);
            tipo_plano = itemView.findViewById(R.id.tipo_plano);
            modalidade_plano = itemView.findViewById(R.id.modalidade_plano);

        }
    }
}
