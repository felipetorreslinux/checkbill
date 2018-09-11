package com.checkmybill.entity;

import java.io.Serializable;

/**
 * Created by Victor Guerra on 13/04/2016.
 */
public class Plano implements Serializable {
    private int idPlano, idOperadora, idModalidadePlano, idTipoPlano, idDDD, smsInclusos;
    private int minMO, minIU, minOO, minFixo, dtVencimento, idPlanoReferencia;
    private long limiteDadosWeb;
    private float smsExtras, valorPlano, bonus;
    private String nomePlano, observacao, nomeOperadora;
    private String descricaoTipoPlano, descricaoModalidadePlano, minMOStr, minIUStr;
    private String minOOStr, minFixoStr, smsInclusosStr, smsExtrasStr, limiteDadosWebStr;
    private int numPacotes, numRecargas;
    private float valorTotalPacotes, valorTotalRecargas;

    public int getIdPlano() {
        return idPlano;
    }

    public void setIdPlano(int idPlano) {
        this.idPlano = idPlano;
    }

    public int getIdOperadora() {
        return idOperadora;
    }

    public void setIdOperadora(int idOperadora) {
        this.idOperadora = idOperadora;
    }

    public int getIdModalidadePlano() {
        return idModalidadePlano;
    }

    public void setIdModalidadePlano(int idModalidadePlano) {
        this.idModalidadePlano = idModalidadePlano;
    }

    public int getIdTipoPlano() {
        return idTipoPlano;
    }

    public void setIdTipoPlano(int idTipoPlano) {
        this.idTipoPlano = idTipoPlano;
    }

    public int getIdDDD() {
        return idDDD;
    }

    public void setIdDDD(int idDDD) {
        this.idDDD = idDDD;
    }

    public int getSmsInclusos() {
        return smsInclusos;
    }

    public void setSmsInclusos(int smsInclusos) {
        this.smsInclusos = smsInclusos;
    }

    public int getMinMO() {
        return minMO;
    }

    public void setMinMO(int minMO) {
        this.minMO = minMO;
    }

    public int getMinIU() {
        return minIU;
    }

    public void setMinIU(int minIU) {
        this.minIU = minIU;
    }

    public int getMinOO() {
        return minOO;
    }

    public void setMinOO(int minOO) {
        this.minOO = minOO;
    }

    public int getMinFixo() {
        return minFixo;
    }

    public void setMinFixo(int minFixo) {
        this.minFixo = minFixo;
    }

    public float getSmsExtras() {
        return smsExtras;
    }

    public void setSmsExtras(float smsExtras) {
        this.smsExtras = smsExtras;
    }

    public float getValorPlano() {
        return valorPlano;
    }

    public void setValorPlano(float valorPlano) {
        this.valorPlano = valorPlano;
    }

    public String getNomePlano() {
        return nomePlano;
    }

    public void setNomePlano(String nomePlano) {
        this.nomePlano = nomePlano;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getNomeOperadora() {
        return nomeOperadora;
    }

    public void setNomeOperadora(String nomeOperadora) {
        this.nomeOperadora = nomeOperadora;
    }

    public String getDescricaoTipoPlano() {
        return descricaoTipoPlano;
    }

    public void setDescricaoTipoPlano(String descricaoTipoPlano) {
        this.descricaoTipoPlano = descricaoTipoPlano;
    }

    public String getDescricaoModalidadePlano() {
        return descricaoModalidadePlano;
    }

    public void setDescricaoModalidadePlano(String descricaoModalidadePlano) {
        this.descricaoModalidadePlano = descricaoModalidadePlano;
    }

    public String getMinMOStr() {
        return minMOStr;
    }

    public void setMinMOStr(String minMOStr) {
        this.minMOStr = minMOStr;
    }

    public String getMinIUStr() {
        return minIUStr;
    }

    public void setMinIUStr(String minIUStr) {
        this.minIUStr = minIUStr;
    }

    public String getMinOOStr() {
        return minOOStr;
    }

    public void setMinOOStr(String minOOStr) {
        this.minOOStr = minOOStr;
    }

    public String getMinFixoStr() {
        return minFixoStr;
    }

    public void setMinFixoStr(String minFixoStr) {
        this.minFixoStr = minFixoStr;
    }

    public float getBonus() {
        return bonus;
    }

    public void setBonus(float bonus) {
        this.bonus = bonus;
    }

    public String getSmsInclusosStr() {
        return smsInclusosStr;
    }

    public void setSmsInclusosStr(String smsInclusosStr) {
        this.smsInclusosStr = smsInclusosStr;
    }

    public String getSmsExtrasStr() {
        return smsExtrasStr;
    }

    public void setSmsExtrasStr(String smsExtrasStr) {
        this.smsExtrasStr = smsExtrasStr;
    }

    public long getLimiteDadosWeb() {
        return limiteDadosWeb;
    }

    public void setLimiteDadosWeb(long limiteDadosWeb) {
        this.limiteDadosWeb = limiteDadosWeb;
    }

    public String getLimiteDadosWebStr() {
        return limiteDadosWebStr;
    }

    public void setLimiteDadosWebStr(String limiteDadosWebStr) {
        this.limiteDadosWebStr = limiteDadosWebStr;
    }

    public int getDtVencimento() {
        return dtVencimento;
    }

    public void setDtVencimento(int dtVencimento) {
        this.dtVencimento = dtVencimento;
    }

    public int getIdPlanoReferencia() {
        return idPlanoReferencia;
    }

    public void setIdPlanoReferencia(int idPlanoReferencia) {
        this.idPlanoReferencia = idPlanoReferencia;
    }

    public int getNumPacotes() {
        return numPacotes;
    }

    public void setNumPacotes(int numPacotes) {
        this.numPacotes = numPacotes;
    }

    public int getNumRecargas() {
        return numRecargas;
    }

    public void setNumRecargas(int numRecargas) {
        this.numRecargas = numRecargas;
    }

    public float getValorTotalPacotes() {
        return valorTotalPacotes;
    }

    public void setValorTotalPacotes(float valorTotalPacotes) {
        this.valorTotalPacotes = valorTotalPacotes;
    }

    public float getValorTotalRecargas() {
        return valorTotalRecargas;
    }

    public void setValorTotalRecargas(float valorTotalRecargas) {
        this.valorTotalRecargas = valorTotalRecargas;
    }
}
