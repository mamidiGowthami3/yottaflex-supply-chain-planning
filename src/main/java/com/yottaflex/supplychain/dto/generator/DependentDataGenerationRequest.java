package com.yottaflex.supplychain.dto.generator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for configuring synthetic dependent transactional data generation
 * (Inventory, ForecastDemand, SupplierCapacity, LeadTime).
 */
public class DependentDataGenerationRequest {

    public static final int DEFAULT_INVENTORY_COUNT = 20;
    public static final int MAX_INVENTORY_COUNT = 500;

    public static final int DEFAULT_FORECAST_DEMAND_COUNT = 50;
    public static final int MAX_FORECAST_DEMAND_COUNT = 2000;

    public static final int DEFAULT_SUPPLIER_CAPACITY_COUNT = 15;
    public static final int MAX_SUPPLIER_CAPACITY_COUNT = 500;

    public static final int DEFAULT_LEAD_TIME_COUNT = 15;
    public static final int MAX_LEAD_TIME_COUNT = 500;

    public static final long DEFAULT_SEED = 20261022L;

    @NotNull(message = "Inventory count cannot be null")
    @Min(value = 0, message = "Inventory count cannot be negative")
    @Max(value = MAX_INVENTORY_COUNT, message = "Inventory count cannot exceed " + MAX_INVENTORY_COUNT)
    private Integer inventoryCount = DEFAULT_INVENTORY_COUNT;

    @NotNull(message = "Forecast demand count cannot be null")
    @Min(value = 0, message = "Forecast demand count cannot be negative")
    @Max(value = MAX_FORECAST_DEMAND_COUNT, message = "Forecast demand count cannot exceed " + MAX_FORECAST_DEMAND_COUNT)
    private Integer forecastDemandCount = DEFAULT_FORECAST_DEMAND_COUNT;

    @NotNull(message = "Supplier capacity count cannot be null")
    @Min(value = 0, message = "Supplier capacity count cannot be negative")
    @Max(value = MAX_SUPPLIER_CAPACITY_COUNT, message = "Supplier capacity count cannot exceed " + MAX_SUPPLIER_CAPACITY_COUNT)
    private Integer supplierCapacityCount = DEFAULT_SUPPLIER_CAPACITY_COUNT;

    @NotNull(message = "Lead time count cannot be null")
    @Min(value = 0, message = "Lead time count cannot be negative")
    @Max(value = MAX_LEAD_TIME_COUNT, message = "Lead time count cannot exceed " + MAX_LEAD_TIME_COUNT)
    private Integer leadTimeCount = DEFAULT_LEAD_TIME_COUNT;

    @NotNull(message = "Random seed cannot be null")
    private Long seed = DEFAULT_SEED;

    public DependentDataGenerationRequest() {
    }

    public DependentDataGenerationRequest(Integer inventoryCount, Integer forecastDemandCount,
                                          Integer supplierCapacityCount, Integer leadTimeCount, Long seed) {
        this.inventoryCount = inventoryCount != null ? inventoryCount : DEFAULT_INVENTORY_COUNT;
        this.forecastDemandCount = forecastDemandCount != null ? forecastDemandCount : DEFAULT_FORECAST_DEMAND_COUNT;
        this.supplierCapacityCount = supplierCapacityCount != null ? supplierCapacityCount : DEFAULT_SUPPLIER_CAPACITY_COUNT;
        this.leadTimeCount = leadTimeCount != null ? leadTimeCount : DEFAULT_LEAD_TIME_COUNT;
        this.seed = seed != null ? seed : DEFAULT_SEED;
    }

    public Integer getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(Integer inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public Integer getForecastDemandCount() {
        return forecastDemandCount;
    }

    public void setForecastDemandCount(Integer forecastDemandCount) {
        this.forecastDemandCount = forecastDemandCount;
    }

    public Integer getSupplierCapacityCount() {
        return supplierCapacityCount;
    }

    public void setSupplierCapacityCount(Integer supplierCapacityCount) {
        this.supplierCapacityCount = supplierCapacityCount;
    }

    public Integer getLeadTimeCount() {
        return leadTimeCount;
    }

    public void setLeadTimeCount(Integer leadTimeCount) {
        this.leadTimeCount = leadTimeCount;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public int getTotalRequestedCount() {
        int inv = inventoryCount != null ? inventoryCount : 0;
        int fd = forecastDemandCount != null ? forecastDemandCount : 0;
        int sc = supplierCapacityCount != null ? supplierCapacityCount : 0;
        int lt = leadTimeCount != null ? leadTimeCount : 0;
        return inv + fd + sc + lt;
    }
}
