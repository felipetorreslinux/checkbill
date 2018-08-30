package com.checkmybill.entity;

/**
 * Created by Victor Guerra on 12/08/2016.
 */

public class Operadora {

    private Integer id;
    private String nomeOperadora;
    private String corOperadora;
    private String tipoOperadora;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getTipoOperadora() {
        return tipoOperadora;
    }

    public void setTipoOperadora(String tipoOperadora) {
        this.tipoOperadora = tipoOperadora;
    }
}
