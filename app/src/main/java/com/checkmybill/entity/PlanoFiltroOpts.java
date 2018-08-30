package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by Petrus Augusto (Espe) on 19/04/2016.
 */
@DatabaseTable(tableName = "PLANO_FILTROS_OPCOES")
public class PlanoFiltroOpts implements Serializable {
    @DatabaseField(id = true, columnName = "PFP_DATA_ID")
    private Integer id;
    @DatabaseField(columnName = "PFP_TIPO_LIGACAO", dataType = DataType.STRING)
    private String tipoLigacao;
    @DatabaseField(columnName = "PFP_TIPO_PLANO", dataType = DataType.STRING)
    private String tipoPlano;
    @DatabaseField(columnName = "PFP_VIAGEM", dataType = DataType.STRING)
    private String viagem;
    @DatabaseField(columnName = "PFP_REGIAO", dataType = DataType.STRING)
    private String regiao;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoLigacao() {
        return tipoLigacao;
    }

    public void setTipoLigacao(String tipoLigacao) {
        this.tipoLigacao = tipoLigacao;
    }

    public String getTipoPlano() {
        return tipoPlano;
    }

    public void setTipoPlano(String tipoPlano) {
        this.tipoPlano = tipoPlano;
    }

    public String getViagem() {
        return viagem;
    }

    public void setViagem(String viagem) {
        this.viagem = viagem;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }


    // -> Obcoes usados para a requsicao por JSON
    /*public int getTipoPlano_Integer() {
        if ( this.tipoPlano.equalsIgnoreCase("prepago") ) return 1;
        else if ( this.tipoPlano.equalsIgnoreCase("pospago") ) return 2;
        else if ( this.tipoPlano.equalsIgnoreCase("controle") ) return 3;
        else return -1;
    }
    public int getTipoLigacao_Integer() {
        if ( this.tipoLigacao.equalsIgnoreCase("local") ) return 1;
        else if ( this.tipoLigacao.equalsIgnoreCase("interurbano") ) return 2;
        else if ( this.tipoLigacao.equalsIgnoreCase("inernacional") ) return 3;
        else return -1;
    }
    public int getViagem_Integer() {
        if ( this.viagem.equalsIgnoreCase("sim") ) return 1;
        else if ( this.viagem.equalsIgnoreCase("nao") ) return 2;
        return -1;
    }*/
}
