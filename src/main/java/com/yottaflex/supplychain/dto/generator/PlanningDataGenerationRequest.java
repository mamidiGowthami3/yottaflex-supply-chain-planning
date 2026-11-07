package com.yottaflex.supplychain.dto.generator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for configuring synthetic purchase order and production plan data generation.
 */
public class PlanningDataGenerationRequest {

    public static final int DEFAULT_PO_COUNT = 20;
    public static final int MAX_PO_COUNT = 500;

    public static final int DEFAULT_PLAN_COUNT = 10;
    public static final int MAX_PLAN_COUNT = 300;

    public static final long DEFAULT_SEED = 20261022L;

    @NotNull(message = "Purchase order count cannot be null")
    @Min(value = 0, message = "Purchase order count cannot be negative")
    @Max(value = MAX_PO_COUNT, message = "Purchase order count cannot exceed " + MAX_PO_COUNT)
    private Integer purchaseOrderCount = DEFAULT_PO_COUNT;

    @NotNull(message = "Production plan count cannot be null")
    @Min(value = 0, message = "Production plan count cannot be negative")
    @Max(value = MAX_PLAN_COUNT, message = "Production plan count cannot exceed " + MAX_PLAN_COUNT)
    private Integer productionPlanCount = DEFAULT_PLAN_COUNT;

    @NotNull(message = "Random seed cannot be null")
    private Long seed = DEFAULT_SEED;

    public PlanningDataGenerationRequest() {
    }

    public PlanningDataGenerationRequest(Integer purchaseOrderCount, Integer productionPlanCount, Long seed) {
        this.purchaseOrderCount = purchaseOrderCount != null ? purchaseOrderCount : DEFAULT_PO_COUNT;
        this.productionPlanCount = productionPlanCount != null ? productionPlanCount : DEFAULT_PLAN_COUNT;
        this.seed = seed != null ? seed : DEFAULT_SEED;
    }

    public Integer getPurchaseOrderCount() {
        return purchaseOrderCount;
    }

    public void setPurchaseOrderCount(Integer purchaseOrderCount) {
        this.purchaseOrderCount = purchaseOrderCount;
    }

    public Integer getProductionPlanCount() {
        return productionPlanCount;
    }

    public void setProductionPlanCount(Integer productionPlanCount) {
        this.productionPlanCount = productionPlanCount;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public int getTotalRequestedCount() {
        int po = purchaseOrderCount != null ? purchaseOrderCount : 0;
        int plan = productionPlanCount != null ? productionPlanCount : 0;
        return po + plan;
    }
}
