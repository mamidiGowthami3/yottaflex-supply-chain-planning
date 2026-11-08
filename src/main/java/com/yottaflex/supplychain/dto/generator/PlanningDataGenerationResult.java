package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO returned after successful persistence of synthetic purchase orders and production plans.
 */
public class PlanningDataGenerationResult {

    private long seed;
    private int purchaseOrdersSaved;
    private int productionPlansSaved;
    private int totalRecordsSaved;
    private long executionDurationMs;

    private List<String> savedPurchaseOrderSummaries = new ArrayList<>();
    private List<String> savedProductionPlanSummaries = new ArrayList<>();
    private LocalDateTime completedAt;

    public PlanningDataGenerationResult() {
        this.completedAt = LocalDateTime.now();
    }

    public PlanningDataGenerationResult(long seed,
                                        int purchaseOrdersSaved,
                                        int productionPlansSaved,
                                        long executionDurationMs,
                                        List<String> savedPurchaseOrderSummaries,
                                        List<String> savedProductionPlanSummaries) {
        this.seed = seed;
        this.purchaseOrdersSaved = purchaseOrdersSaved;
        this.productionPlansSaved = productionPlansSaved;
        this.totalRecordsSaved = purchaseOrdersSaved + productionPlansSaved;
        this.executionDurationMs = executionDurationMs;
        this.savedPurchaseOrderSummaries = savedPurchaseOrderSummaries != null ? savedPurchaseOrderSummaries : new ArrayList<>();
        this.savedProductionPlanSummaries = savedProductionPlanSummaries != null ? savedProductionPlanSummaries : new ArrayList<>();
        this.completedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getPurchaseOrdersSaved() {
        return purchaseOrdersSaved;
    }

    public void setPurchaseOrdersSaved(int purchaseOrdersSaved) {
        this.purchaseOrdersSaved = purchaseOrdersSaved;
    }

    public int getProductionPlansSaved() {
        return productionPlansSaved;
    }

    public void setProductionPlansSaved(int productionPlansSaved) {
        this.productionPlansSaved = productionPlansSaved;
    }

    public int getTotalRecordsSaved() {
        return totalRecordsSaved;
    }

    public void setTotalRecordsSaved(int totalRecordsSaved) {
        this.totalRecordsSaved = totalRecordsSaved;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public List<String> getSavedPurchaseOrderSummaries() {
        return savedPurchaseOrderSummaries;
    }

    public void setSavedPurchaseOrderSummaries(List<String> savedPurchaseOrderSummaries) {
        this.savedPurchaseOrderSummaries = savedPurchaseOrderSummaries;
    }

    public List<String> getSavedProductionPlanSummaries() {
        return savedProductionPlanSummaries;
    }

    public void setSavedProductionPlanSummaries(List<String> savedProductionPlanSummaries) {
        this.savedProductionPlanSummaries = savedProductionPlanSummaries;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
