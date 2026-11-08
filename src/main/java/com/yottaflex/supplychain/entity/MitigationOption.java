package com.yottaflex.supplychain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.MitigationStrategy;
import com.yottaflex.supplychain.entity.enums.TransportMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing tactical mitigation options for a detected shortage event.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
    name = "mitigation_options",
    indexes = {
        @Index(name = "idx_opt_code", columnList = "option_code", unique = true),
        @Index(name = "idx_opt_shortage", columnList = "shortage_id"),
        @Index(name = "idx_opt_strategy", columnList = "strategy"),
        @Index(name = "idx_opt_status", columnList = "status"),
        @Index(name = "idx_opt_po", columnList = "purchase_order_id"),
        @Index(name = "idx_opt_prod_plan", columnList = "production_plan_id")
    }
)
public class MitigationOption extends BaseAuditableEntity {

    @NotBlank(message = "Option code cannot be blank")
    @Size(min = 3, max = 50, message = "Option code must be between 3 and 50 characters")
    @Column(name = "option_code", unique = true, nullable = false, length = 50)
    private String optionCode;

    @NotNull(message = "Shortage reference is required")
    @JsonIgnoreProperties({"mitigationOptions", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shortage_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mitigation_shortage"))
    private Shortage shortage;

    @NotNull(message = "Mitigation strategy is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "strategy", nullable = false, length = 40)
    private MitigationStrategy strategy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternate_supplier_id", foreignKey = @ForeignKey(name = "fk_mitigation_supplier"))
    private Supplier alternateSupplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id", foreignKey = @ForeignKey(name = "fk_mitigation_source_location"))
    private Location sourceLocation;

    @DecimalMin(value = "0.00", inclusive = true, message = "Cost impact cannot be negative")
    @Column(name = "cost_impact", precision = 14, scale = 2)
    private BigDecimal costImpact = BigDecimal.ZERO;

    @Column(name = "time_impact_days")
    private Integer timeImpactDays = 0;

    @NotNull(message = "Mitigation status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MitigationStatus status = MitigationStatus.PROPOSED;

    @DecimalMin(value = "0.0", message = "Recommendation score cannot be negative")
    @DecimalMax(value = "100.0", message = "Recommendation score cannot exceed 100.0")
    @Column(name = "recommendation_score")
    private Double recommendationScore = 80.0;

    @Size(max = 1000, message = "Details cannot exceed 1000 characters")
    @Column(name = "details", length = 1000)
    private String details;

    @DecimalMin(value = "0.00", inclusive = true, message = "Mitigated quantity cannot be negative")
    @Column(name = "mitigated_quantity", precision = 14, scale = 2)
    private BigDecimal mitigatedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", foreignKey = @ForeignKey(name = "fk_mitigation_purchase_order"))
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_plan_id", foreignKey = @ForeignKey(name = "fk_mitigation_production_plan"))
    private ProductionPlan productionPlan;

    @Column(name = "estimated_arrival_date")
    private LocalDate estimatedArrivalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "freight_mode", length = 30)
    private TransportMode freightMode;

    @DecimalMin(value = "0.00", inclusive = true, message = "Split remaining quantity cannot be negative")
    @Column(name = "split_remaining_quantity", precision = 14, scale = 2)
    private BigDecimal splitRemainingQuantity;

    public MitigationOption() {
    }

    public MitigationOption(String optionCode, Shortage shortage, MitigationStrategy strategy, BigDecimal costImpact, Integer timeImpactDays, Double recommendationScore) {
        this.optionCode = optionCode;
        this.shortage = shortage;
        this.strategy = strategy;
        this.costImpact = costImpact;
        this.timeImpactDays = timeImpactDays;
        this.recommendationScore = recommendationScore;
        this.status = MitigationStatus.PROPOSED;
    }

    public MitigationOption(String optionCode, Shortage shortage, MitigationStrategy strategy, BigDecimal costImpact,
                            Integer timeImpactDays, Double recommendationScore, BigDecimal mitigatedQuantity,
                            LocalDate estimatedArrivalDate, TransportMode freightMode) {
        this(optionCode, shortage, strategy, costImpact, timeImpactDays, recommendationScore);
        this.mitigatedQuantity = mitigatedQuantity;
        this.estimatedArrivalDate = estimatedArrivalDate;
        this.freightMode = freightMode;
    }

    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public Shortage getShortage() {
        return shortage;
    }

    public void setShortage(Shortage shortage) {
        this.shortage = shortage;
    }

    public MitigationStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(MitigationStrategy strategy) {
        this.strategy = strategy;
    }

    public Supplier getAlternateSupplier() {
        return alternateSupplier;
    }

    public void setAlternateSupplier(Supplier alternateSupplier) {
        this.alternateSupplier = alternateSupplier;
    }

    public Location getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(Location sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public BigDecimal getCostImpact() {
        return costImpact;
    }

    public void setCostImpact(BigDecimal costImpact) {
        this.costImpact = costImpact;
    }

    public Integer getTimeImpactDays() {
        return timeImpactDays;
    }

    public void setTimeImpactDays(Integer timeImpactDays) {
        this.timeImpactDays = timeImpactDays;
    }

    public MitigationStatus getStatus() {
        return status;
    }

    public void setStatus(MitigationStatus status) {
        this.status = status;
    }

    public Double getRecommendationScore() {
        return recommendationScore;
    }

    public void setRecommendationScore(Double recommendationScore) {
        this.recommendationScore = recommendationScore;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public BigDecimal getMitigatedQuantity() {
        return mitigatedQuantity;
    }

    public void setMitigatedQuantity(BigDecimal mitigatedQuantity) {
        this.mitigatedQuantity = mitigatedQuantity;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public ProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(ProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }

    public LocalDate getEstimatedArrivalDate() {
        return estimatedArrivalDate;
    }

    public void setEstimatedArrivalDate(LocalDate estimatedArrivalDate) {
        this.estimatedArrivalDate = estimatedArrivalDate;
    }

    public TransportMode getFreightMode() {
        return freightMode;
    }

    public void setFreightMode(TransportMode freightMode) {
        this.freightMode = freightMode;
    }

    public BigDecimal getSplitRemainingQuantity() {
        return splitRemainingQuantity;
    }

    public void setSplitRemainingQuantity(BigDecimal splitRemainingQuantity) {
        this.splitRemainingQuantity = splitRemainingQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MitigationOption that)) return false;
        return Objects.equals(optionCode, that.optionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionCode);
    }

    @Override
    public String toString() {
        return "MitigationOption{" +
                "id=" + getId() +
                ", optionCode='" + optionCode + '\'' +
                ", strategy=" + strategy +
                ", costImpact=" + costImpact +
                ", status=" + status +
                ", recommendationScore=" + recommendationScore +
                ", mitigatedQuantity=" + mitigatedQuantity +
                ", estimatedArrivalDate=" + estimatedArrivalDate +
                ", freightMode=" + freightMode +
                '}';
    }
}

