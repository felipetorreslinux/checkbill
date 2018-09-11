package com.checkmybill.adapters.avaliaplano;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.checkmybill.R;

/**
 * Created by Victor Guerra on 14/04/2016.
 */
public class AvaliaPlanoListItemHolder extends RecyclerView.ViewHolder {

    private TextView tvwNomePlano, tvwModalidade, tvwValorPlano, tvwDados, tvwPosition, tvwBestPrice, tvwValorVariable, tvwDadosVariable;
    private CardView tvwPositionCard;

    private Boolean isHeader;

    public AvaliaPlanoListItemHolder(View v) {
        super(v);
        tvwNomePlano = (TextView) v.findViewById(R.id.tvwNomePlano);
        tvwModalidade = (TextView) v.findViewById(R.id.tvwModalidade);
        tvwValorPlano = (TextView) v.findViewById(R.id.tvwValorPlano);
        tvwDados = (TextView) v.findViewById(R.id.tvwDados);
        tvwPositionCard = (CardView) v.findViewById(R.id.tvwPositionCard);
        tvwPosition = (TextView) v.findViewById(R.id.tvwPosition);
        tvwBestPrice = (TextView) v.findViewById(R.id.tvwBestPrice);
        tvwValorVariable = (TextView) v.findViewById(R.id.tvwValorVariable);
        tvwDadosVariable = (TextView) v.findViewById(R.id.tvwDadosVariable);
        isHeader = false;
    }

    public TextView getTvwNomePlano() {
        return tvwNomePlano;
    }

    public void setTvwNomePlano(TextView tvwNomePlano) {
        this.tvwNomePlano = tvwNomePlano;
    }

    public TextView getTvwModalidade() {
        return tvwModalidade;
    }

    public void setTvwModalidade(TextView tvwModalidade) {
        this.tvwModalidade = tvwModalidade;
    }

    public TextView getTvwValorPlano() {
        return tvwValorPlano;
    }

    public void setTvwValorPlano(TextView tvwValorPlano) {
        this.tvwValorPlano = tvwValorPlano;
    }

    public TextView getTvwDados() {
        return tvwDados;
    }

    public void setTvwDados(TextView tvwDados) {
        this.tvwDados = tvwDados;
    }

    public TextView getTvwPosition() {
        return tvwPosition;
    }

    public void setTvwPosition(TextView tvwPosition) {
        this.tvwPosition = tvwPosition;
    }

    public TextView getTvwBestPrice() {
        return tvwBestPrice;
    }

    public void setTvwBestPrice(TextView tvwBestPrice) {
        this.tvwBestPrice = tvwBestPrice;
    }

    public TextView getTvwValorVariable() {
        return tvwValorVariable;
    }

    public void setTvwValorVariable(TextView tvwValorVariable) {
        this.tvwValorVariable = tvwValorVariable;
    }

    public TextView getTvwDadosVariable() {
        return tvwDadosVariable;
    }

    public void setTvwDadosVariable(TextView tvwDadosVariable) {
        this.tvwDadosVariable = tvwDadosVariable;
    }

    public CardView getTvwPositionCard() {
        return tvwPositionCard;
    }

    public void setTvwPositionCard(CardView tvwPositionCard) {
        this.tvwPositionCard = tvwPositionCard;
    }

    public Boolean getHeader() {
        return isHeader;
    }

    public void setHeader(Boolean header) {
        isHeader = header;
    }
}
