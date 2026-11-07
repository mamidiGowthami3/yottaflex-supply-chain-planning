package com.yottaflex.supplychain.dto.generator;

import com.yottaflex.supplychain.entity.MitigationOption;
import com.yottaflex.supplychain.entity.Shortage;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.MitigationStrategy;
import com.yottaflex.supplychain.entity.enums.ShortageRootCause;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import com.yottaflex.supplychain.entity.enums.TransportMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory preview response for time-phased shortage detection and tactical mitigations.
 * Guaranteed zero database persistence (persisted = false).
 */
public class ShortagePreviewResponse {

    private long seed;
    private LocalDate asOfDate;
    private int horizonWeeks;
    private int shortageCount;
    private int mitigationOptionCount;
    private int totalRecords;
    private boolean persisted = false;
    private LocalDateTime generatedAt;

    private List<ShortagePreviewItem> previewShortages = new ArrayList<>();

    public ShortagePreviewResponse() {
        this.generatedAt = LocalDateTime.now();
        this.persisted = false;
    }

    public ShortagePreviewResponse(long seed, int horizonWeeks, List<ShortagePreviewItem> previewShortages) {
        this(seed, null, horizonWeeks, previewShortages);
    }

    public ShortagePreviewResponse(long seed, LocalDate asOfDate, int horizonWeeks, List<ShortagePreviewItem> previewShortages) {
        this.seed = seed;
        this.asOfDate = asOfDate;
        this.horizonWeeks = horizonWeeks;
        this.previewShortages = previewShortages != null ? previewShortages : new ArrayList<>();
        this.shortageCount = this.previewShortages.size();
        this.mitigationOptionCount = this.previewShortages.stream()
                .mapToInt(s -> s.mitigationOptions() != null ? s.mitigationOptions().size() : 0)
                .sum();
        this.totalRecords = this.shortageCount + this.mitigationOptionCount;
        this.persisted = false;
        this.generatedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }

    public int getHorizonWeeks() {
        return horizonWeeks;
    }

    public void setHorizonWeeks(int horizonWeeks) {
        this.horizonWeeks = horizonWeeks;
    }

    public int getShortageCount() {
        return shortageCount;
    }

    public void setShortageCount(int shortageCount) {
        this.shortageCount = shortageCount;
    }

    public int getMitigationOptionCount() {
        return mitigationOptionCount;
    }

    public void setMitigationOptionCount(int mitigationOptionCount) {
        this.mitigationOptionCount = mitigationOptionCount;
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

    public List<ShortagePreviewItem> getPreviewShortages() {
        return previewShortages;
    }

    public void setPreviewShortages(List<ShortagePreviewItem> previewShortages) {
        this.previewShortages = previewShortages;
    }

    // =========================================================================
    // Inner Projection Records
    // =========================================================================

    public record ShortagePreviewItem(
            String shortageCode,
            String skuCode,
            String skuName,
            String locationCode,
            String locationName,
            LocationType locationType,
            LocalDate shortageDate,
            BigDecimal shortageQuantity,
            ShortageSeverity severity,
            ShortageRootCause rootCause,
            BigDecimal startingInventory,
            BigDecimal incomingSupply,
            BigDecimal forecastDemand,
            BigDecimal projectedBalance,
            String notes,
            List<MitigationOptionPreviewItem> mitigationOptions
    ) {
        public static ShortagePreviewItem from(Shortage s, BigDecimal startingInv, BigDecimal inboundSupply,
                                               BigDecimal demand, BigDecimal projBalance) {
            List<MitigationOptionPreviewItem> options = s.getMitigationOptions() != null
                    ? s.getMitigationOptions().stream().map(MitigationOptionPreviewItem::from).toList()
                    : List.of();

            return new ShortagePreviewItem(
                    s.getShortageCode(),
                    s.getSku() != null ? s.getSku().getSkuCode() : null,
                    s.getSku() != null ? s.getSku().getName() : null,
                    s.getLocation() != null ? s.getLocation().getLocationCode() : null,
                    s.getLocation() != null ? s.getLocation().getName() : null,
                    s.getLocation() != null ? s.getLocation().getType() : null,
                    s.getShortageDate(),
                    s.getShortageQuantity(),
                    s.getSeverity(),
                    s.getRootCause(),
                    startingInv,
                    inboundSupply,
                    demand,
                    projBalance,
                    s.getNotes(),
                    options
            );
        }
    }

    public record MitigationOptionPreviewItem(
            String optionCode,
            MitigationStrategy strategy,
            String sourceLocationCode,
            String alternateSupplierCode,
            BigDecimal costImpact,
            Integer timeImpactDays,
            Double recommendationScore,
            MitigationStatus status,
            String details,
            BigDecimal mitigatedQuantity,
            String purchaseOrderCode,
            String productionPlanCode,
            LocalDate estimatedArrivalDate,
            TransportMode freightMode,
            BigDecimal splitRemainingQuantity
    ) {
        public MitigationOptionPreviewItem(String optionCode, MitigationStrategy strategy, String sourceLocationCode,
                                           String alternateSupplierCode, BigDecimal costImpact, Integer timeImpactDays,
                                           Double recommendationScore, MitigationStatus status, String details) {
            this(optionCode, strategy, sourceLocationCode, alternateSupplierCode, costImpact, timeImpactDays,
                    recommendationScore, status, details, null, null, null, null, null, null);
        }

        public static MitigationOptionPreviewItem from(MitigationOption mo) {
            return new MitigationOptionPreviewItem(
                    mo.getOptionCode(),
                    mo.getStrategy(),
                    mo.getSourceLocation() != null ? mo.getSourceLocation().getLocationCode() : null,
                    mo.getAlternateSupplier() != null ? mo.getAlternateSupplier().getSupplierCode() : null,
                    mo.getCostImpact(),
                    mo.getTimeImpactDays(),
                    mo.getRecommendationScore(),
                    mo.getStatus(),
                    mo.getDetails(),
                    mo.getMitigatedQuantity(),
                    mo.getPurchaseOrder() != null ? mo.getPurchaseOrder().getPoNumber() : null,
                    mo.getProductionPlan() != null ? mo.getProductionPlan().getPlanCode() : null,
                    mo.getEstimatedArrivalDate(),
                    mo.getFreightMode(),
                    mo.getSplitRemainingQuantity()
            );
        }
    }
}

