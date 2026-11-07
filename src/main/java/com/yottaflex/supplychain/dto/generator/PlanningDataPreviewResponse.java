package com.yottaflex.supplychain.dto.generator;

import com.yottaflex.supplychain.entity.ProductionPlan;
import com.yottaflex.supplychain.entity.PurchaseOrder;
import com.yottaflex.supplychain.entity.enums.ProductionPlanStatus;
import com.yottaflex.supplychain.entity.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing an in-memory preview of generated purchase orders and production plans.
 * Guaranteed zero database persistence (persisted = false).
 */
public class PlanningDataPreviewResponse {

    private long seed;
    private int purchaseOrderCount;
    private int productionPlanCount;
    private int totalRecords;
    private boolean persisted = false;
    private LocalDateTime generatedAt;

    private List<PurchaseOrderPreviewItem> previewPurchaseOrders = new ArrayList<>();
    private List<ProductionPlanPreviewItem> previewProductionPlans = new ArrayList<>();

    public PlanningDataPreviewResponse() {
        this.generatedAt = LocalDateTime.now();
        this.persisted = false;
    }

    public PlanningDataPreviewResponse(long seed,
                                       List<PurchaseOrderPreviewItem> previewPurchaseOrders,
                                       List<ProductionPlanPreviewItem> previewProductionPlans) {
        this.seed = seed;
        this.previewPurchaseOrders = previewPurchaseOrders != null ? previewPurchaseOrders : new ArrayList<>();
        this.previewProductionPlans = previewProductionPlans != null ? previewProductionPlans : new ArrayList<>();
        this.purchaseOrderCount = this.previewPurchaseOrders.size();
        this.productionPlanCount = this.previewProductionPlans.size();
        this.totalRecords = this.purchaseOrderCount + this.productionPlanCount;
        this.persisted = false;
        this.generatedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getPurchaseOrderCount() {
        return purchaseOrderCount;
    }

    public void setPurchaseOrderCount(int purchaseOrderCount) {
        this.purchaseOrderCount = purchaseOrderCount;
    }

    public int getProductionPlanCount() {
        return productionPlanCount;
    }

    public void setProductionPlanCount(int productionPlanCount) {
        this.productionPlanCount = productionPlanCount;
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

    public List<PurchaseOrderPreviewItem> getPreviewPurchaseOrders() {
        return previewPurchaseOrders;
    }

    public void setPreviewPurchaseOrders(List<PurchaseOrderPreviewItem> previewPurchaseOrders) {
        this.previewPurchaseOrders = previewPurchaseOrders;
    }

    public List<ProductionPlanPreviewItem> getPreviewProductionPlans() {
        return previewProductionPlans;
    }

    public void setPreviewProductionPlans(List<ProductionPlanPreviewItem> previewProductionPlans) {
        this.previewProductionPlans = previewProductionPlans;
    }

    // =========================================================================
    // Inner Preview Item Records
    // =========================================================================

    public record PurchaseOrderPreviewItem(
            String poNumber,
            String supplierCode,
            String skuCode,
            String destinationLocationCode,
            LocalDate orderDate,
            LocalDate expectedDeliveryDate,
            LocalDate actualDeliveryDate,
            BigDecimal quantityOrdered,
            BigDecimal quantityReceived,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            PurchaseOrderStatus status,
            String notes
    ) {
        public static PurchaseOrderPreviewItem from(PurchaseOrder po) {
            return new PurchaseOrderPreviewItem(
                    po.getPoNumber(),
                    po.getSupplier() != null ? po.getSupplier().getSupplierCode() : null,
                    po.getSku() != null ? po.getSku().getSkuCode() : null,
                    po.getDestinationLocation() != null ? po.getDestinationLocation().getLocationCode() : null,
                    po.getOrderDate(),
                    po.getExpectedDeliveryDate(),
                    po.getActualDeliveryDate(),
                    po.getQuantityOrdered(),
                    po.getQuantityReceived(),
                    po.getUnitPrice(),
                    po.getTotalAmount(),
                    po.getStatus(),
                    po.getNotes()
            );
        }
    }

    public record ProductionPlanPreviewItem(
            String planCode,
            String skuCode,
            String locationCode,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal plannedQuantity,
            BigDecimal producedQuantity,
            Double capacityUtilization,
            ProductionPlanStatus status,
            String notes
    ) {
        public static ProductionPlanPreviewItem from(ProductionPlan pp) {
            return new ProductionPlanPreviewItem(
                    pp.getPlanCode(),
                    pp.getSku() != null ? pp.getSku().getSkuCode() : null,
                    pp.getLocation() != null ? pp.getLocation().getLocationCode() : null,
                    pp.getStartDate(),
                    pp.getEndDate(),
                    pp.getPlannedQuantity(),
                    pp.getProducedQuantity(),
                    pp.getCapacityUtilization(),
                    pp.getStatus(),
                    pp.getNotes()
            );
        }
    }
}
