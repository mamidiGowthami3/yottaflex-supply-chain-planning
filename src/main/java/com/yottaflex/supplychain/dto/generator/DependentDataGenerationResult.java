package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO returned after successful persistence of synthetic dependent transactional data
 * (Inventory, ForecastDemand, SupplierCapacity, LeadTime).
 */
public class DependentDataGenerationResult {

    private long seed;
    private int inventoriesSaved;
    private int forecastDemandsSaved;
    private int supplierCapacitiesSaved;
    private int leadTimesSaved;
    private int totalRecordsSaved;
    private long executionDurationMs;

    private List<String> savedInventorySummaries = new ArrayList<>();
    private List<String> savedForecastSummaries = new ArrayList<>();
    private List<String> savedCapacitySummaries = new ArrayList<>();
    private List<String> savedLeadTimeSummaries = new ArrayList<>();
    private LocalDateTime completedAt;

    public DependentDataGenerationResult() {
        this.completedAt = LocalDateTime.now();
    }

    public DependentDataGenerationResult(long seed,
                                         int inventoriesSaved,
                                         int forecastDemandsSaved,
                                         int supplierCapacitiesSaved,
                                         int leadTimesSaved,
                                         long executionDurationMs,
                                         List<String> savedInventorySummaries,
                                         List<String> savedForecastSummaries,
                                         List<String> savedCapacitySummaries,
                                         List<String> savedLeadTimeSummaries) {
        this.seed = seed;
        this.inventoriesSaved = inventoriesSaved;
        this.forecastDemandsSaved = forecastDemandsSaved;
        this.supplierCapacitiesSaved = supplierCapacitiesSaved;
        this.leadTimesSaved = leadTimesSaved;
        this.totalRecordsSaved = inventoriesSaved + forecastDemandsSaved + supplierCapacitiesSaved + leadTimesSaved;
        this.executionDurationMs = executionDurationMs;
        this.savedInventorySummaries = savedInventorySummaries != null ? savedInventorySummaries : new ArrayList<>();
        this.savedForecastSummaries = savedForecastSummaries != null ? savedForecastSummaries : new ArrayList<>();
        this.savedCapacitySummaries = savedCapacitySummaries != null ? savedCapacitySummaries : new ArrayList<>();
        this.savedLeadTimeSummaries = savedLeadTimeSummaries != null ? savedLeadTimeSummaries : new ArrayList<>();
        this.completedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getInventoriesSaved() {
        return inventoriesSaved;
    }

    public void setInventoriesSaved(int inventoriesSaved) {
        this.inventoriesSaved = inventoriesSaved;
    }

    public int getForecastDemandsSaved() {
        return forecastDemandsSaved;
    }

    public void setForecastDemandsSaved(int forecastDemandsSaved) {
        this.forecastDemandsSaved = forecastDemandsSaved;
    }

    public int getSupplierCapacitiesSaved() {
        return supplierCapacitiesSaved;
    }

    public void setSupplierCapacitiesSaved(int supplierCapacitiesSaved) {
        this.supplierCapacitiesSaved = supplierCapacitiesSaved;
    }

    public int getLeadTimesSaved() {
        return leadTimesSaved;
    }

    public void setLeadTimesSaved(int leadTimesSaved) {
        this.leadTimesSaved = leadTimesSaved;
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

    public List<String> getSavedInventorySummaries() {
        return savedInventorySummaries;
    }

    public void setSavedInventorySummaries(List<String> savedInventorySummaries) {
        this.savedInventorySummaries = savedInventorySummaries;
    }

    public List<String> getSavedForecastSummaries() {
        return savedForecastSummaries;
    }

    public void setSavedForecastSummaries(List<String> savedForecastSummaries) {
        this.savedForecastSummaries = savedForecastSummaries;
    }

    public List<String> getSavedCapacitySummaries() {
        return savedCapacitySummaries;
    }

    public void setSavedCapacitySummaries(List<String> savedCapacitySummaries) {
        this.savedCapacitySummaries = savedCapacitySummaries;
    }

    public List<String> getSavedLeadTimeSummaries() {
        return savedLeadTimeSummaries;
    }

    public void setSavedLeadTimeSummaries(List<String> savedLeadTimeSummaries) {
        this.savedLeadTimeSummaries = savedLeadTimeSummaries;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
