package com.yottaflex.supplychain.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class HealthCheckResponse {

    private String status;
    private String applicationName;
    private String version;
    private LocalDateTime timestamp;
    private String databaseStatus;
    private String databaseProduct;
    private Map<String, Long> entityTableCounts;
    private Map<String, Object> environmentInfo;

    public HealthCheckResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public String getDatabaseProduct() {
        return databaseProduct;
    }

    public void setDatabaseProduct(String databaseProduct) {
        this.databaseProduct = databaseProduct;
    }

    public Map<String, Long> getEntityTableCounts() {
        return entityTableCounts;
    }

    public void setEntityTableCounts(Map<String, Long> entityTableCounts) {
        this.entityTableCounts = entityTableCounts;
    }

    public Map<String, Object> getEnvironmentInfo() {
        return environmentInfo;
    }

    public void setEnvironmentInfo(Map<String, Object> environmentInfo) {
        this.environmentInfo = environmentInfo;
    }
}

