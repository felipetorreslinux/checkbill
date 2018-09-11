package com.checkmybill.entity;

/**
 * Created by ESPENOTE-06 on 14/11/2016.
 */

public class TipoPlano {
    private int id;
    private String descricaoTipoPlano;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricaoTipoPlano() {
        return descricaoTipoPlano;
    }

    public void setDescricaoTipoPlano(String descricaoTipoPlano) {
        this.descricaoTipoPlano = descricaoTipoPlano;
    }

    public TipoPlano() {
    }

    public TipoPlano(int id, String descricaoTipoPlano) {
        this.id = id;
        this.descricaoTipoPlano = descricaoTipoPlano;
    }
}
