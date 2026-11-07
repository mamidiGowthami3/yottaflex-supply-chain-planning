package com.yottaflex.supplychain.dto.generator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for configuring synthetic master data generation.
 */
public class MasterDataGenerationRequest {

    public static final int DEFAULT_LOCATION_COUNT = 3;
    public static final int MAX_LOCATION_COUNT = 20;

    public static final int DEFAULT_SUPPLIER_COUNT = 5;
    public static final int MAX_SUPPLIER_COUNT = 50;

    public static final int DEFAULT_SKU_COUNT = 10;
    public static final int MAX_SKU_COUNT = 100;

    public static final long DEFAULT_SEED = 20261022L;

    @NotNull(message = "Location count cannot be null")
    @Min(value = 0, message = "Location count cannot be negative")
    @Max(value = MAX_LOCATION_COUNT, message = "Location count cannot exceed " + MAX_LOCATION_COUNT)
    private Integer locationCount = DEFAULT_LOCATION_COUNT;

    @NotNull(message = "Supplier count cannot be null")
    @Min(value = 0, message = "Supplier count cannot be negative")
    @Max(value = MAX_SUPPLIER_COUNT, message = "Supplier count cannot exceed " + MAX_SUPPLIER_COUNT)
    private Integer supplierCount = DEFAULT_SUPPLIER_COUNT;

    @NotNull(message = "SKU count cannot be null")
    @Min(value = 0, message = "SKU count cannot be negative")
    @Max(value = MAX_SKU_COUNT, message = "SKU count cannot exceed " + MAX_SKU_COUNT)
    private Integer skuCount = DEFAULT_SKU_COUNT;

    @NotNull(message = "Random seed cannot be null")
    private Long seed = DEFAULT_SEED;

    public MasterDataGenerationRequest() {
    }

    public MasterDataGenerationRequest(Integer locationCount, Integer supplierCount, Integer skuCount, Long seed) {
        this.locationCount = locationCount != null ? locationCount : DEFAULT_LOCATION_COUNT;
        this.supplierCount = supplierCount != null ? supplierCount : DEFAULT_SUPPLIER_COUNT;
        this.skuCount = skuCount != null ? skuCount : DEFAULT_SKU_COUNT;
        this.seed = seed != null ? seed : DEFAULT_SEED;
    }

    public Integer getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(Integer locationCount) {
        this.locationCount = locationCount;
    }

    public Integer getSupplierCount() {
        return supplierCount;
    }

    public void setSupplierCount(Integer supplierCount) {
        this.supplierCount = supplierCount;
    }

    public Integer getSkuCount() {
        return skuCount;
    }

    public void setSkuCount(Integer skuCount) {
        this.skuCount = skuCount;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public int getTotalRequestedCount() {
        int loc = locationCount != null ? locationCount : 0;
        int sup = supplierCount != null ? supplierCount : 0;
        int sku = skuCount != null ? skuCount : 0;
        return loc + sup + sku;
    }
}

