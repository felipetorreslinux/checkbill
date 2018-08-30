package com.checkmybill.entity.ranking;

/**
 * Created by Victor Guerra on 03/02/2017.
 */

public class BandaLargaRankingItem {

    private String nomeOperadora;
    private String corOperadora;
    private String urlLogo;
    private Integer totalEntradas;
    private Double qualiDownload;
    private Double qualiUpload;

    public String getNomeOperadora() {
        return nomeOperadora;
    }

    public void setNomeOperadora(String nomeOperadora) {
        this.nomeOperadora = nomeOperadora;
    }

    public String getCorOperadora() {
        return corOperadora;
    }

    public void setCorOperadora(String corOperadora) {
        this.corOperadora = corOperadora;
    }

    public String getUrlLogo() {
        return urlLogo;
    }

    public void setUrlLogo(String urlLogo) {
        this.urlLogo = urlLogo;
    }

    public Integer getTotalEntradas() {
        return totalEntradas;
    }

    public void setTotalEntradas(Integer totalEntradas) {
        this.totalEntradas = totalEntradas;
    }

    public Double getQualiDownload() {
        return qualiDownload;
    }

    public void setQualiDownload(Double qualiDownload) {
        this.qualiDownload = qualiDownload;
    }

    public Double getQualiUpload() {
        return qualiUpload;
    }

    public void setQualiUpload(Double qualiUpload) {
        this.qualiUpload = qualiUpload;
    }
}
