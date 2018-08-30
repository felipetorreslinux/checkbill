package com.checkmybill.entity;

/**
 * Created by Victor Guerra on 24/11/2015.
 */

/**
 * {
 * o tipo_pacote: (Texto) Código com o tipo de pacote (consultar acima)
 * <p/>
 * o datahora_inicio_medicao: (Texto)Data & Hora no formato(dd-mm-YYYY HH:MM:SS)
 * <p/>
 * o datahora_fim_medicao: (Texto) Data & Hora no formato (dd-mm-YYYY HH:MM:SS)
 * <p/>
 * o nivel_sinal: (Numérico) Valor contendo o nível do sinal (consultar acima)
 * <p/>
 * o celula_gsm: (Numérico) Código da célula GSM
 * <p/>
 * o operadora: (Texto) Nome da Operadora
 * <p/>
 * o latitude: (Numérico) Coordenadas latitudinais
 * <p/>
 * o longitude: (Numérico) Coordenadas longitudinais
 * <p/>
 * o velocidade_download: (Numérico) Velocidade média de download em MB’s
 * <p/>
 * o velocidade_upload: (Numérico) Velocidade média de upload em MB’s
 * <p/>
 * o perda_pacotes: (Numérico) Média do nº de perdas de pacotes
 * <p/>
 * o latencia: (Numérico) Média da latência
 * <p/>
 * o jitter: (Numérico) Diferença entre os picos de latência
 * <p/>
 * }
 */

public class Medicao {

    private String tipo_pacote;
    private String datahora_inicio_medicao;
    private String datahora_fim_medicao;
    private int nivel_sinal;
    private int celula_gsm;
    private String operadora;
    private double latitude;
    private double longitude;
    private double velocidade_download;
    private double velocidade_upload;
    private int perda_pacotes;
    private int latencia;
    private int jitter;
    private String time;
    private int heatmapWeight;

    public String getTipo_pacote() {
        return tipo_pacote;
    }

    public void setTipo_pacote(String tipo_pacote) {
        this.tipo_pacote = tipo_pacote;
    }

    public String getDatahora_inicio_medicao() {
        return datahora_inicio_medicao;
    }

    public void setDatahora_inicio_medicao(String datahora_inicio_medicao) {
        this.datahora_inicio_medicao = datahora_inicio_medicao;
    }

    public String getDatahora_fim_medicao() {
        return datahora_fim_medicao;
    }

    public void setDatahora_fim_medicao(String datahora_fim_medicao) {
        this.datahora_fim_medicao = datahora_fim_medicao;
    }

    public int getNivel_sinal() {
        return nivel_sinal;
    }

    public void setNivel_sinal(int nivel_sinal) {
        this.nivel_sinal = nivel_sinal;
    }

    public int getCelula_gsm() {
        return celula_gsm;
    }

    public void setCelula_gsm(int celula_gsm) {
        this.celula_gsm = celula_gsm;
    }

    public String getOperadora() {
        return operadora;
    }

    public void setOperadora(String operadora) {
        this.operadora = operadora;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getVelocidade_download() {
        return velocidade_download;
    }

    public void setVelocidade_download(double velocidade_download) {
        this.velocidade_download = velocidade_download;
    }

    public double getVelocidade_upload() {
        return velocidade_upload;
    }

    public void setVelocidade_upload(double velocidade_upload) {
        this.velocidade_upload = velocidade_upload;
    }

    public int getPerda_pacotes() {
        return perda_pacotes;
    }

    public void setPerda_pacotes(int perda_pacotes) {
        this.perda_pacotes = perda_pacotes;
    }

    public int getLatencia() {
        return latencia;
    }

    public void setLatencia(int latencia) {
        this.latencia = latencia;
    }

    public int getJitter() {
        return jitter;
    }

    public void setJitter(int jitter) {
        this.jitter = jitter;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getHeatmapWeight() {
        return heatmapWeight;
    }

    public void setHeatmapWeight(int heatmapWeight) {
        this.heatmapWeight = heatmapWeight;
    }
}
