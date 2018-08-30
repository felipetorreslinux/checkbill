package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by espe on 29/07/2016.
 */

@DatabaseTable(tableName = "CALL_MONITOR_TABLE")
public class CallMonitor {
    @DatabaseField(generatedId = true, columnName = "CALL_ID")
    private Long id;

    @DatabaseField(columnName = "CALL_NUMTEL")
    private String telNumber;

    @DatabaseField(columnName = "CALL_TIME")
    private Long elapsedTime;

    @DatabaseField(columnName = "CALL_TYPE")
    private String callType;

    @DatabaseField(columnName = "CALL_SENDED", dataType = DataType.BOOLEAN, defaultValue = "false", canBeNull = false)
    private boolean callSended;

    @DatabaseField(columnName = "CALL_DAT_CAD", dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
    private Date dateCad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public Long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Date getDateCad() {
        return dateCad;
    }

    public void setDateCad(Date dateCad) {
        this.dateCad = dateCad;
    }

    public Boolean getCallSended() {
        return callSended;
    }

    public void setCallSended(Boolean callSended) {
        this.callSended = callSended;
    }
}
