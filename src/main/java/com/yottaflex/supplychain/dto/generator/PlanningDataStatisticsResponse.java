package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;

/**
 * DTO providing database record count statistics for purchase orders and production plans.
 */
public class PlanningDataStatisticsResponse {

    private long totalPurchaseOrders;
    private long totalProductionPlans;
    private long totalSyntheticPurchaseOrders;
    private long totalSyntheticProductionPlans;
    private long totalPlanningRecords;
    private long totalSyntheticPlanningRecords;
    private LocalDateTime timestamp;

    public PlanningDataStatisticsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public PlanningDataStatisticsResponse(long totalPurchaseOrders,
                                          long totalProductionPlans,
                                          long totalSyntheticPurchaseOrders,
                                          long totalSyntheticProductionPlans) {
        this.totalPurchaseOrders = totalPurchaseOrders;
        this.totalProductionPlans = totalProductionPlans;
        this.totalSyntheticPurchaseOrders = totalSyntheticPurchaseOrders;
        this.totalSyntheticProductionPlans = totalSyntheticProductionPlans;
        this.totalPlanningRecords = totalPurchaseOrders + totalProductionPlans;
        this.totalSyntheticPlanningRecords = totalSyntheticPurchaseOrders + totalSyntheticProductionPlans;
        this.timestamp = LocalDateTime.now();
    }

    public long getTotalPurchaseOrders() {
        return totalPurchaseOrders;
    }

    public void setTotalPurchaseOrders(long totalPurchaseOrders) {
        this.totalPurchaseOrders = totalPurchaseOrders;
    }

    public long getTotalProductionPlans() {
        return totalProductionPlans;
    }

    public void setTotalProductionPlans(long totalProductionPlans) {
        this.totalProductionPlans = totalProductionPlans;
    }

    public long getTotalSyntheticPurchaseOrders() {
        return totalSyntheticPurchaseOrders;
    }

    public void setTotalSyntheticPurchaseOrders(long totalSyntheticPurchaseOrders) {
        this.totalSyntheticPurchaseOrders = totalSyntheticPurchaseOrders;
    }

    public long getTotalSyntheticProductionPlans() {
        return totalSyntheticProductionPlans;
    }

    public void setTotalSyntheticProductionPlans(long totalSyntheticProductionPlans) {
        this.totalSyntheticProductionPlans = totalSyntheticProductionPlans;
    }

    public long getTotalPlanningRecords() {
        return totalPlanningRecords;
    }

    public void setTotalPlanningRecords(long totalPlanningRecords) {
        this.totalPlanningRecords = totalPlanningRecords;
    }

    public long getTotalSyntheticPlanningRecords() {
        return totalSyntheticPlanningRecords;
    }

    public void setTotalSyntheticPlanningRecords(long totalSyntheticPlanningRecords) {
        this.totalSyntheticPlanningRecords = totalSyntheticPlanningRecords;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
