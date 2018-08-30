package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by espe on 28/07/2016.
 */

@DatabaseTable(tableName = "SMS_MONITOR_TABLE")
public class SmsMonitor {
    @DatabaseField(id = true, columnName = "SMS_ID")
    private Long id;

    @DatabaseField(columnName = "SMS_TO_ADDRESS")
    private String toAddress;

    @DatabaseField(columnName = "SMS_SENDED", dataType = DataType.BOOLEAN, defaultValue = "false", canBeNull = false)
    private boolean smsSended;

    @DatabaseField(columnName = "SMS_DAT_CAD", dataType = DataType.DATE_STRING, format = "yyyy-MM-dd HH:mm:ss")
    private Date dateCad;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public Date getDateCad() {
        return dateCad;
    }

    public void setDateCad(Date dateCad) {
        this.dateCad = dateCad;
    }

    public boolean isSmsSended() {
        return smsSended;
    }

    public void setSmsSended(boolean smsSended) {
        this.smsSended = smsSended;
    }
}
