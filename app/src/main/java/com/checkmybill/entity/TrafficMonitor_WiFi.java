package com.checkmybill.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Petrus A. (R@G3), ESPE... On 08/05/2017.
 */

@DatabaseTable(tableName = "wifi_traffic_monitor")
public class TrafficMonitor_WiFi {
    @DatabaseField(allowGeneratedIdInsert = true, generatedId = true, canBeNull = false, columnName = "id_wifi_monitor") private long idMonitor;
    @DatabaseField(columnName = "current_received_bytes_start", dataType = DataType.LONG) private long currentReceivedBytes_start;
    @DatabaseField(columnName = "current_received_bytes_end", dataType = DataType.LONG) private long currentReceivedBytes_end;
    @DatabaseField(columnName = "current_sended_bytes_start", dataType = DataType.LONG) private long currentSendedBytes_start;
    @DatabaseField(columnName = "current_sended_bytes_end", dataType = DataType.LONG) private long currentSendedBytes_end;
    @DatabaseField(columnName = "date_period", dataType = DataType.DATE_STRING, format="yyyy-MM-dd") private Date datePeriodo;
    @DatabaseField(columnName = "data_sync", dataType = DataType.BOOLEAN, defaultValue = "false", canBeNull = false) private boolean dataSyncronized;

    public long getIdMonitor() {
        return idMonitor;
    }

    public void setIdMonitor(long idMonitor) {
        this.idMonitor = idMonitor;
    }

    public long getCurrentReceivedBytes_start() {
        return currentReceivedBytes_start;
    }

    public void setCurrentReceivedBytes_start(long currentReceivedBytes_start) {
        this.currentReceivedBytes_start = currentReceivedBytes_start;
    }

    public long getCurrentReceivedBytes_end() {
        return currentReceivedBytes_end;
    }

    public void setCurrentReceivedBytes_end(long currentReceivedBytes_end) {
        this.currentReceivedBytes_end = currentReceivedBytes_end;
    }

    public long getCurrentSendedBytes_start() {
        return currentSendedBytes_start;
    }

    public void setCurrentSendedBytes_start(long currentSendedBytes_start) {
        this.currentSendedBytes_start = currentSendedBytes_start;
    }

    public long getCurrentSendedBytes_end() {
        return currentSendedBytes_end;
    }

    public void setCurrentSendedBytes_end(long currentSendedBytes_end) {
        this.currentSendedBytes_end = currentSendedBytes_end;
    }

    public Date getDatePeriodo() {
        return datePeriodo;
    }

    public void setDatePeriodo(Date datePeriodo) {
        this.datePeriodo = datePeriodo;
    }

    public Boolean getDataSyncronized() {
        return dataSyncronized;
    }

    public void setDataSyncronized(Boolean dataSyncronized) {
        this.dataSyncronized = dataSyncronized;
    }
}
