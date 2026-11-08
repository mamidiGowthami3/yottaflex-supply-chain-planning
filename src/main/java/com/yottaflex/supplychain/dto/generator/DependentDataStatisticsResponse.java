package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;

/**
 * DTO providing statistics on dependent transactional data records residing in the database
 * (Inventory, ForecastDemand, SupplierCapacity, LeadTime).
 */
public class DependentDataStatisticsResponse {

    private long totalInventories;
    private long totalForecastDemands;
    private long totalSupplierCapacities;
    private long totalLeadTimes;

    private long totalSyntheticInventories;
    private long totalSyntheticForecastDemands;
    private long totalSyntheticSupplierCapacities;
    private long totalSyntheticLeadTimes;

    private long totalDependentRecords;
    private long totalSyntheticDependentRecords;
    private LocalDateTime timestamp;

    public DependentDataStatisticsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public DependentDataStatisticsResponse(long totalInventories,
                                           long totalForecastDemands,
                                           long totalSupplierCapacities,
                                           long totalLeadTimes,
                                           long totalSyntheticInventories,
                                           long totalSyntheticForecastDemands,
                                           long totalSyntheticSupplierCapacities,
                                           long totalSyntheticLeadTimes) {
        this.totalInventories = totalInventories;
        this.totalForecastDemands = totalForecastDemands;
        this.totalSupplierCapacities = totalSupplierCapacities;
        this.totalLeadTimes = totalLeadTimes;

        this.totalSyntheticInventories = totalSyntheticInventories;
        this.totalSyntheticForecastDemands = totalSyntheticForecastDemands;
        this.totalSyntheticSupplierCapacities = totalSyntheticSupplierCapacities;
        this.totalSyntheticLeadTimes = totalSyntheticLeadTimes;

        this.totalDependentRecords = totalInventories + totalForecastDemands + totalSupplierCapacities + totalLeadTimes;
        this.totalSyntheticDependentRecords = totalSyntheticInventories + totalSyntheticForecastDemands
                + totalSyntheticSupplierCapacities + totalSyntheticLeadTimes;
        this.timestamp = LocalDateTime.now();
    }

    public long getTotalInventories() {
        return totalInventories;
    }

    public void setTotalInventories(long totalInventories) {
        this.totalInventories = totalInventories;
    }

    public long getTotalForecastDemands() {
        return totalForecastDemands;
    }

    public void setTotalForecastDemands(long totalForecastDemands) {
        this.totalForecastDemands = totalForecastDemands;
    }

    public long getTotalSupplierCapacities() {
        return totalSupplierCapacities;
    }

    public void setTotalSupplierCapacities(long totalSupplierCapacities) {
        this.totalSupplierCapacities = totalSupplierCapacities;
    }

    public long getTotalLeadTimes() {
        return totalLeadTimes;
    }

    public void setTotalLeadTimes(long totalLeadTimes) {
        this.totalLeadTimes = totalLeadTimes;
    }

    public long getTotalSyntheticInventories() {
        return totalSyntheticInventories;
    }

    public void setTotalSyntheticInventories(long totalSyntheticInventories) {
        this.totalSyntheticInventories = totalSyntheticInventories;
    }

    public long getTotalSyntheticForecastDemands() {
        return totalSyntheticForecastDemands;
    }

    public void setTotalSyntheticForecastDemands(long totalSyntheticForecastDemands) {
        this.totalSyntheticForecastDemands = totalSyntheticForecastDemands;
    }

    public long getTotalSyntheticSupplierCapacities() {
        return totalSyntheticSupplierCapacities;
    }

    public void setTotalSyntheticSupplierCapacities(long totalSyntheticSupplierCapacities) {
        this.totalSyntheticSupplierCapacities = totalSyntheticSupplierCapacities;
    }

    public long getTotalSyntheticLeadTimes() {
        return totalSyntheticLeadTimes;
    }

    public void setTotalSyntheticLeadTimes(long totalSyntheticLeadTimes) {
        this.totalSyntheticLeadTimes = totalSyntheticLeadTimes;
    }

    public long getTotalDependentRecords() {
        return totalDependentRecords;
    }

    public void setTotalDependentRecords(long totalDependentRecords) {
        this.totalDependentRecords = totalDependentRecords;
    }

    public long getTotalSyntheticDependentRecords() {
        return totalSyntheticDependentRecords;
    }

    public void setTotalSyntheticDependentRecords(long totalSyntheticDependentRecords) {
        this.totalSyntheticDependentRecords = totalSyntheticDependentRecords;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
