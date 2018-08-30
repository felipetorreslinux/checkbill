package com.checkmybill.entity;

/**
 * Created by Victor Guerra on 04/08/2016.
 */

public class Account {

    private String nomeUsuario;
    private String loginEmail;
    private String telefoneNum;
    private String telefoneOperadora;
    private String senha;
    private String senha2;
    private String fbUserToken;
    private String fbUserId;

    public Account(String nomeUsuario, String loginEmail, String telefoneNum, String telefoneOperadora, String senha, String senha2, String fbUserToken, String fbUserId) {
        this.nomeUsuario = nomeUsuario;
        this.loginEmail = loginEmail;
        this.telefoneNum = telefoneNum;
        this.telefoneOperadora = telefoneOperadora;
        this.senha = senha;
        this.senha2 = senha2;
        this.fbUserToken = fbUserToken;
        this.fbUserId = fbUserId;
    }

    public String getFbUserId() {
        return this.fbUserId;
    }

    public void setFbUserId(String id) {
        this.fbUserId = id;
    }

    public Account() {

    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getTelefoneNum() {
        return telefoneNum;
    }

    public void setTelefoneNum(String telefoneNum) {
        this.telefoneNum = telefoneNum;
    }

    public String getTelefoneOperadora() {
        return telefoneOperadora;
    }

    public void setTelefoneOperadora(String telefoneOperadora) {
        this.telefoneOperadora = telefoneOperadora;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSenha2() {
        return senha2;
    }

    public void setSenha2(String senha2) {
        this.senha2 = senha2;
    }

    public String getFbUserToken() {
        return fbUserToken;
    }

    public void setFbUserToken(String fbUserToken) {
        this.fbUserToken = fbUserToken;
    }
}
