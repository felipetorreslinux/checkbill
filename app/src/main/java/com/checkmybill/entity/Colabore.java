package com.checkmybill.entity;

import java.io.Serializable;

/**
 * Created by Victor Guerra on 25/10/2016.
 */

public class Colabore implements Serializable{

    private String access_key;
    private String subject;
    private String message;

    public String getAccess_key() {
        return access_key;
    }

    public void setAccess_key(String access_key) {
        this.access_key = access_key;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
