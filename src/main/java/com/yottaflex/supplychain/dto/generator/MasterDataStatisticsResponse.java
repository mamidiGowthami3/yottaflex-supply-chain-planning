package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;

/**
 * DTO providing statistics on master data records currently residing in the database.
 */
public class MasterDataStatisticsResponse {

    private long totalLocations;
    private long totalSuppliers;
    private long totalSkus;
    private long totalSyntheticLocations;
    private long totalSyntheticSuppliers;
    private long totalSyntheticSkus;
    private long totalMasterDataRecords;
    private LocalDateTime timestamp;

    public MasterDataStatisticsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public MasterDataStatisticsResponse(long totalLocations, long totalSuppliers, long totalSkus,
                                        long totalSyntheticLocations, long totalSyntheticSuppliers, long totalSyntheticSkus) {
        this.totalLocations = totalLocations;
        this.totalSuppliers = totalSuppliers;
        this.totalSkus = totalSkus;
        this.totalSyntheticLocations = totalSyntheticLocations;
        this.totalSyntheticSuppliers = totalSyntheticSuppliers;
        this.totalSyntheticSkus = totalSyntheticSkus;
        this.totalMasterDataRecords = totalLocations + totalSuppliers + totalSkus;
        this.timestamp = LocalDateTime.now();
    }

    public long getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(long totalLocations) {
        this.totalLocations = totalLocations;
    }

    public long getTotalSuppliers() {
        return totalSuppliers;
    }

    public void setTotalSuppliers(long totalSuppliers) {
        this.totalSuppliers = totalSuppliers;
    }

    public long getTotalSkus() {
        return totalSkus;
    }

    public void setTotalSkus(long totalSkus) {
        this.totalSkus = totalSkus;
    }

    public long getTotalSyntheticLocations() {
        return totalSyntheticLocations;
    }

    public void setTotalSyntheticLocations(long totalSyntheticLocations) {
        this.totalSyntheticLocations = totalSyntheticLocations;
    }

    public long getTotalSyntheticSuppliers() {
        return totalSyntheticSuppliers;
    }

    public void setTotalSyntheticSuppliers(long totalSyntheticSuppliers) {
        this.totalSyntheticSuppliers = totalSyntheticSuppliers;
    }

    public long getTotalSyntheticSkus() {
        return totalSyntheticSkus;
    }

    public void setTotalSyntheticSkus(long totalSyntheticSkus) {
        this.totalSyntheticSkus = totalSyntheticSkus;
    }

    public long getTotalMasterDataRecords() {
        return totalMasterDataRecords;
    }

    public void setTotalMasterDataRecords(long totalMasterDataRecords) {
        this.totalMasterDataRecords = totalMasterDataRecords;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

