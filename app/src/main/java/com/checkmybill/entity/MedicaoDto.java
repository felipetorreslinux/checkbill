package com.checkmybill.entity;

/**
 * Created by Victor Guerra on 01/03/2016.
 */
public class MedicaoDto {

    Integer id_medicao;
    String tipo_pacote;
    String telefone_latitude;
    String telefone_longitude;
    Integer nivel_sinal;
    String operadora;
    String datahora_fim_medicao;
    String nivel_sinal_text;
    String tempo_total_indisponivel;

    public Integer getId_medicao() {
        return id_medicao;
    }

    public void setId_medicao(Integer id_medicao) {
        this.id_medicao = id_medicao;
    }

    public String getTipo_pacote() {
        return tipo_pacote;
    }

    public void setTipo_pacote(String tipo_pacote) {
        this.tipo_pacote = tipo_pacote;
    }

    public String getTelefone_latitude() {
        return telefone_latitude;
    }

    public void setTelefone_latitude(String telefone_latitude) {
        this.telefone_latitude = telefone_latitude;
    }

    public String getTelefone_longitude() {
        return telefone_longitude;
    }

    public void setTelefone_longitude(String telefone_longitude) {
        this.telefone_longitude = telefone_longitude;
    }

    public Integer getNivel_sinal() {
        return nivel_sinal;
    }

    public void setNivel_sinal(Integer nivel_sinal) {
        this.nivel_sinal = nivel_sinal;
    }

    public String getOperadora() {
        return operadora;
    }

    public void setOperadora(String operadora) {
        this.operadora = operadora;
    }

    public String getDatahora_fim_medicao() {
        return datahora_fim_medicao;
    }

    public void setDatahora_fim_medicao(String datahora_fim_medicao) {
        this.datahora_fim_medicao = datahora_fim_medicao;
    }

    public String getNivel_sinal_text() {
        return nivel_sinal_text;
    }

    public void setNivel_sinal_text(String nivel_sinal_text) {
        this.nivel_sinal_text = nivel_sinal_text;
    }

    public String getTempo_total_indisponivel() {
        return tempo_total_indisponivel;
    }

    public void setTempo_total_indisponivel(String tempo_total_indisponivel) {
        this.tempo_total_indisponivel = tempo_total_indisponivel;
    }


}
