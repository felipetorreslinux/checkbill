package com.checkmybill.felipecode.Models;

public class RankingModel {
    int id_plano;
    String nome_plano;
    int id_operadora;
    String nome_operadora;
    String observacao;
    long internet;
    String valor_plano;
    String tipo_contrato_plano;
    String modalidade_plano;
    boolean plano_usuario;

    public RankingModel(int id_plano, String nome_plano, int id_operadora, String nome_operadora, String observacao, long internet, String valor_plano, String tipo_contrato_plano, String modalidade_plano, boolean plano_usuario) {
        this.id_plano = id_plano;
        this.nome_plano = nome_plano;
        this.id_operadora = id_operadora;
        this.nome_operadora = nome_operadora;
        this.observacao = observacao;
        this.internet = internet;
        this.valor_plano = valor_plano;
        this.tipo_contrato_plano = tipo_contrato_plano;
        this.modalidade_plano = modalidade_plano;
        this.plano_usuario = plano_usuario;
    }

    public int getId_plano() {
        return id_plano;
    }

    public void setId_plano(int id_plano) {
        this.id_plano = id_plano;
    }

    public String getNome_plano() {
        return nome_plano;
    }

    public void setNome_plano(String nome_plano) {
        this.nome_plano = nome_plano;
    }

    public int getId_operadora() {
        return id_operadora;
    }

    public void setId_operadora(int id_operadora) {
        this.id_operadora = id_operadora;
    }

    public String getNome_operadora() {
        return nome_operadora;
    }

    public void setNome_operadora(String nome_operadora) {
        this.nome_operadora = nome_operadora;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public long getInternet() {
        return internet;
    }

    public void setInternet(long internet) {
        this.internet = internet;
    }

    public String getValor_plano() {
        return valor_plano;
    }

    public void setValor_plano(String valor_plano) {
        this.valor_plano = valor_plano;
    }

    public String getTipo_contrato_plano() {
        return tipo_contrato_plano;
    }

    public void setTipo_contrato_plano(String tipo_contrato_plano) {
        this.tipo_contrato_plano = tipo_contrato_plano;
    }

    public String getModalidade_plano() {
        return modalidade_plano;
    }

    public void setModalidade_plano(String modalidade_plano) {
        this.modalidade_plano = modalidade_plano;
    }

    public boolean isPlano_usuario() {
        return plano_usuario;
    }

    public void setPlano_usuario(boolean plano_usuario) {
        this.plano_usuario = plano_usuario;
    }
}
