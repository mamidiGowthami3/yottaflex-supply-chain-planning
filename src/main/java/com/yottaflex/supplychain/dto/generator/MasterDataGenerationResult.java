package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result DTO returned after successful persistence of synthetic master data.
 */
public class MasterDataGenerationResult {

    private long seed;
    private int locationsSaved;
    private int suppliersSaved;
    private int skusSaved;
    private int totalRecordsSaved;
    private long executionDurationMs;
    private List<String> generatedLocationCodes;
    private List<String> generatedSupplierCodes;
    private List<String> generatedSkuCodes;
    private LocalDateTime completedAt;

    public MasterDataGenerationResult() {
        this.completedAt = LocalDateTime.now();
    }

    public MasterDataGenerationResult(long seed, int locationsSaved, int suppliersSaved, int skusSaved, long executionDurationMs,
                                      List<String> generatedLocationCodes, List<String> generatedSupplierCodes, List<String> generatedSkuCodes) {
        this.seed = seed;
        this.locationsSaved = locationsSaved;
        this.suppliersSaved = suppliersSaved;
        this.skusSaved = skusSaved;
        this.totalRecordsSaved = locationsSaved + suppliersSaved + skusSaved;
        this.executionDurationMs = executionDurationMs;
        this.generatedLocationCodes = generatedLocationCodes;
        this.generatedSupplierCodes = generatedSupplierCodes;
        this.generatedSkuCodes = generatedSkuCodes;
        this.completedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getLocationsSaved() {
        return locationsSaved;
    }

    public void setLocationsSaved(int locationsSaved) {
        this.locationsSaved = locationsSaved;
    }

    public int getSuppliersSaved() {
        return suppliersSaved;
    }

    public void setSuppliersSaved(int suppliersSaved) {
        this.suppliersSaved = suppliersSaved;
    }

    public int getSkusSaved() {
        return skusSaved;
    }

    public void setSkusSaved(int skusSaved) {
        this.skusSaved = skusSaved;
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

    public List<String> getGeneratedLocationCodes() {
        return generatedLocationCodes;
    }

    public void setGeneratedLocationCodes(List<String> generatedLocationCodes) {
        this.generatedLocationCodes = generatedLocationCodes;
    }

    public List<String> getGeneratedSupplierCodes() {
        return generatedSupplierCodes;
    }

    public void setGeneratedSupplierCodes(List<String> generatedSupplierCodes) {
        this.generatedSupplierCodes = generatedSupplierCodes;
    }

    public List<String> getGeneratedSkuCodes() {
        return generatedSkuCodes;
    }

    public void setGeneratedSkuCodes(List<String> generatedSkuCodes) {
        this.generatedSkuCodes = generatedSkuCodes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

