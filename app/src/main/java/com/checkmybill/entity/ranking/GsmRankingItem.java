package com.checkmybill.entity.ranking;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

public class GsmRankingItem {

    private Integer numEntradas;
    private String nomeOperadora;
    private Integer mediaNivelSinal;

    public Integer getNumEntradas() {
        return numEntradas;
    }

    public void setNumEntradas(Integer numEntradas) {
        this.numEntradas = numEntradas;
    }

    public String getNomeOperadora() {
        return nomeOperadora;
    }

    public void setNomeOperadora(String nomeOperadora) {
        this.nomeOperadora = nomeOperadora;
    }

    public Integer getMediaNivelSinal() {
        return mediaNivelSinal;
    }

    public void setMediaNivelSinal(Integer mediaNivelSinal) {
        this.mediaNivelSinal = mediaNivelSinal;
    }
}
