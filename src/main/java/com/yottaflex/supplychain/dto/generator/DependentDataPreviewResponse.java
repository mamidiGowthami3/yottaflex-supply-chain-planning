package com.yottaflex.supplychain.dto.generator;

import com.yottaflex.supplychain.entity.Inventory;
import com.yottaflex.supplychain.entity.ForecastDemand;
import com.yottaflex.supplychain.entity.SupplierCapacity;
import com.yottaflex.supplychain.entity.LeadTime;
import com.yottaflex.supplychain.entity.enums.DemandPattern;
import com.yottaflex.supplychain.entity.enums.DemandPeriodType;
import com.yottaflex.supplychain.entity.enums.InventoryStatus;
import com.yottaflex.supplychain.entity.enums.TransportMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing an in-memory preview of generated dependent transactional data
 * (Inventory, ForecastDemand, SupplierCapacity, LeadTime).
 * Guaranteed zero database persistence (persisted = false).
 */
public class DependentDataPreviewResponse {

    private long seed;
    private int inventoryCount;
    private int forecastDemandCount;
    private int supplierCapacityCount;
    private int leadTimeCount;
    private int totalRecords;
    private boolean persisted = false;
    private LocalDateTime generatedAt;

    private List<InventoryPreviewItem> previewInventories = new ArrayList<>();
    private List<ForecastDemandPreviewItem> previewForecastDemands = new ArrayList<>();
    private List<SupplierCapacityPreviewItem> previewSupplierCapacities = new ArrayList<>();
    private List<LeadTimePreviewItem> previewLeadTimes = new ArrayList<>();

    public DependentDataPreviewResponse() {
        this.generatedAt = LocalDateTime.now();
        this.persisted = false;
    }

    public DependentDataPreviewResponse(long seed,
                                        List<InventoryPreviewItem> previewInventories,
                                        List<ForecastDemandPreviewItem> previewForecastDemands,
                                        List<SupplierCapacityPreviewItem> previewSupplierCapacities,
                                        List<LeadTimePreviewItem> previewLeadTimes) {
        this.seed = seed;
        this.previewInventories = previewInventories != null ? previewInventories : new ArrayList<>();
        this.previewForecastDemands = previewForecastDemands != null ? previewForecastDemands : new ArrayList<>();
        this.previewSupplierCapacities = previewSupplierCapacities != null ? previewSupplierCapacities : new ArrayList<>();
        this.previewLeadTimes = previewLeadTimes != null ? previewLeadTimes : new ArrayList<>();
        this.inventoryCount = this.previewInventories.size();
        this.forecastDemandCount = this.previewForecastDemands.size();
        this.supplierCapacityCount = this.previewSupplierCapacities.size();
        this.leadTimeCount = this.previewLeadTimes.size();
        this.totalRecords = this.inventoryCount + this.forecastDemandCount + this.supplierCapacityCount + this.leadTimeCount;
        this.persisted = false;
        this.generatedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getInventoryCount() {
        return inventoryCount;
    }

    public void setInventoryCount(int inventoryCount) {
        this.inventoryCount = inventoryCount;
    }

    public int getForecastDemandCount() {
        return forecastDemandCount;
    }

    public void setForecastDemandCount(int forecastDemandCount) {
        this.forecastDemandCount = forecastDemandCount;
    }

    public int getSupplierCapacityCount() {
        return supplierCapacityCount;
    }

    public void setSupplierCapacityCount(int supplierCapacityCount) {
        this.supplierCapacityCount = supplierCapacityCount;
    }

    public int getLeadTimeCount() {
        return leadTimeCount;
    }

    public void setLeadTimeCount(int leadTimeCount) {
        this.leadTimeCount = leadTimeCount;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<InventoryPreviewItem> getPreviewInventories() {
        return previewInventories;
    }

    public void setPreviewInventories(List<InventoryPreviewItem> previewInventories) {
        this.previewInventories = previewInventories;
    }

    public List<ForecastDemandPreviewItem> getPreviewForecastDemands() {
        return previewForecastDemands;
    }

    public void setPreviewForecastDemands(List<ForecastDemandPreviewItem> previewForecastDemands) {
        this.previewForecastDemands = previewForecastDemands;
    }

    public List<SupplierCapacityPreviewItem> getPreviewSupplierCapacities() {
        return previewSupplierCapacities;
    }

    public void setPreviewSupplierCapacities(List<SupplierCapacityPreviewItem> previewSupplierCapacities) {
        this.previewSupplierCapacities = previewSupplierCapacities;
    }

    public List<LeadTimePreviewItem> getPreviewLeadTimes() {
        return previewLeadTimes;
    }

    public void setPreviewLeadTimes(List<LeadTimePreviewItem> previewLeadTimes) {
        this.previewLeadTimes = previewLeadTimes;
    }

    // =========================================================================
    // Preview DTO Item Records
    // =========================================================================

    public record InventoryPreviewItem(
            String skuCode,
            String locationCode,
            BigDecimal quantityOnHand,
            BigDecimal quantityReserved,
            BigDecimal quantityAvailable,
            BigDecimal safetyStockLevel,
            BigDecimal reorderPoint,
            InventoryStatus status,
            LocalDate lastAuditDate
    ) {
        public static InventoryPreviewItem from(Inventory inv) {
            return new InventoryPreviewItem(
                    inv.getSku() != null ? inv.getSku().getSkuCode() : null,
                    inv.getLocation() != null ? inv.getLocation().getLocationCode() : null,
                    inv.getQuantityOnHand(),
                    inv.getQuantityReserved(),
                    inv.getQuantityAvailable(),
                    inv.getSafetyStockLevel(),
                    inv.getReorderPoint(),
                    inv.getStatus(),
                    inv.getLastAuditDate()
            );
        }
    }

    public record ForecastDemandPreviewItem(
            String skuCode,
            String locationCode,
            LocalDate forecastDate,
            DemandPeriodType periodType,
            BigDecimal forecastQuantity,
            Double confidenceScore,
            DemandPattern demandPattern
    ) {
        public static ForecastDemandPreviewItem from(ForecastDemand fd) {
            return new ForecastDemandPreviewItem(
                    fd.getSku() != null ? fd.getSku().getSkuCode() : null,
                    fd.getLocation() != null ? fd.getLocation().getLocationCode() : null,
                    fd.getForecastDate(),
                    fd.getPeriodType(),
                    fd.getForecastQuantity(),
                    fd.getConfidenceScore(),
                    fd.getDemandPattern()
            );
        }
    }

    public record SupplierCapacityPreviewItem(
            String supplierCode,
            String skuCode,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            BigDecimal dailyCapacity,
            BigDecimal monthlyCapacity,
            BigDecimal allocatedCapacity,
            BigDecimal availableCapacity
    ) {
        public static SupplierCapacityPreviewItem from(SupplierCapacity sc) {
            return new SupplierCapacityPreviewItem(
                    sc.getSupplier() != null ? sc.getSupplier().getSupplierCode() : null,
                    sc.getSku() != null ? sc.getSku().getSkuCode() : null,
                    sc.getEffectiveStartDate(),
                    sc.getEffectiveEndDate(),
                    sc.getDailyCapacity(),
                    sc.getMonthlyCapacity(),
                    sc.getAllocatedCapacity(),
                    sc.getAvailableCapacity()
            );
        }
    }

    public record LeadTimePreviewItem(
            String supplierCode,
            String skuCode,
            String destinationLocationCode,
            Integer standardDays,
            Integer expeditedDays,
            Integer minimumOrderQuantity,
            BigDecimal expeditedCostPerUnit,
            TransportMode transportMode
    ) {
        public static LeadTimePreviewItem from(LeadTime lt) {
            return new LeadTimePreviewItem(
                    lt.getSupplier() != null ? lt.getSupplier().getSupplierCode() : null,
                    lt.getSku() != null ? lt.getSku().getSkuCode() : null,
                    lt.getDestinationLocation() != null ? lt.getDestinationLocation().getLocationCode() : null,
                    lt.getStandardDays(),
                    lt.getExpeditedDays(),
                    lt.getMinimumOrderQuantity(),
                    lt.getExpeditedCostPerUnit(),
                    lt.getTransportMode()
            );
        }
    }
}
