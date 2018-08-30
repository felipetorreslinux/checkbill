package com.checkmybill.entity;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Victor Guerra on 13/04/2016.
 */
public class Pacote implements Serializable {
    private int idPacote;
    private int idOperadora;
    private int idModalidadePlano;
    private int idTipoPlano;
    private int idDDD;
    private int smsInclusos;

    public int getIdPacoteUsuario() {
        return idPacoteUsuario;
    }

    public void setIdPacoteUsuario(int idPacoteUsuario) {
        this.idPacoteUsuario = idPacoteUsuario;
    }

    private int idPacoteUsuario;
    private int minMO, minIU, minOO, minFixo, limiteDadosWeb, idPlanoReferencia;
    private float smsExtras, valorPacote, bonus;
    private String nomePacote, observacao, nomeOperadora;
    private String descricaoTipoPlano, descricaoModalidadePlano, minMOStr, minIUStr;
    private String minOOStr, minFixoStr, smsInclusosStr, smsExtrasStr, limiteDadosWebStr;
    private Date dateCad;

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

    public int getIdPacote() {
        return idPacote;
    }

    public void setIdPacote(int idPacote) {
        this.idPacote = idPacote;
    }

    public float getValorPacote() {
        return valorPacote;
    }

    public void setValorPacote(float valorPacote) {
        this.valorPacote = valorPacote;
    }

    public String getNomePacote() {
        return nomePacote;
    }

    public void setNomePacote(String nomePacote) {
        this.nomePacote = nomePacote;
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

    public int getLimiteDadosWeb() {
        return limiteDadosWeb;
    }

    public void setLimiteDadosWeb(int limiteDadosWeb) {
        this.limiteDadosWeb = limiteDadosWeb;
    }

    public String getLimiteDadosWebStr() {
        return limiteDadosWebStr;
    }

    public void setLimiteDadosWebStr(String limiteDadosWebStr) {
        this.limiteDadosWebStr = limiteDadosWebStr;
    }

    public int getIdPlanoReferencia() {
        return idPlanoReferencia;
    }

    public void setIdPlanoReferencia(int idPlanoReferencia) {
        this.idPlanoReferencia = idPlanoReferencia;
    }

    public Date getDateCad() {
        return dateCad;
    }

    public void setDateCad(String dt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.dateCad = sdf.parse(dt);
        } catch (ParseException e) { }
    }
}
