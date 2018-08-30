package com.checkmybill.entity;

import java.util.Date;

/**
 * Created by ESPENOTE-06, ${CORP} on 23/11/2016.
 */

public class RecargasPlano {
    private int id;
    private float valorRecarga;
    private Date dataRecarga;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getValorRecarga() {
        return valorRecarga;
    }

    public void setValorRecarga(float valorRecarga) {
        this.valorRecarga = valorRecarga;
    }

    public Date getDataRecarga() {
        return dataRecarga;
    }

    public void setDataRecarga(Date dataRecarga) {
        this.dataRecarga = dataRecarga;
    }
}
