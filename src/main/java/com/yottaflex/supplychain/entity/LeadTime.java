package com.yottaflex.supplychain.entity;

import com.yottaflex.supplychain.entity.enums.TransportMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing lead times from a Supplier to a destination Location for a specific SKU.
 */
@Entity
@Table(
    name = "lead_times",
    indexes = {
        @Index(name = "idx_lt_supplier_sku_dest", columnList = "supplier_id, sku_id, destination_location_id")
    }
)
public class LeadTime extends BaseAuditableEntity {

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lead_time_supplier"))
    private Supplier supplier;

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lead_time_sku"))
    private SKU sku;

    @NotNull(message = "Destination location is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lead_time_dest_location"))
    private Location destinationLocation;

    @NotNull(message = "Standard lead time in days is required")
    @Min(value = 1, message = "Standard lead time must be at least 1 day")
    @Column(name = "standard_days", nullable = false)
    private Integer standardDays;

    @Min(value = 1, message = "Expedited lead time must be at least 1 day")
    @Column(name = "expedited_days")
    private Integer expeditedDays;

    @Min(value = 1, message = "Minimum order quantity must be at least 1")
    @Column(name = "minimum_order_quantity")
    private Integer minimumOrderQuantity = 1;

    @DecimalMin(value = "0.00", inclusive = true, message = "Expedited cost per unit cannot be negative")
    @Column(name = "expedited_cost_per_unit", precision = 14, scale = 2)
    private BigDecimal expeditedCostPerUnit;

    @NotNull(message = "Transport mode is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_mode", nullable = false, length = 30)
    private TransportMode transportMode = TransportMode.ROAD;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public LeadTime() {
    }

    public LeadTime(Supplier supplier, SKU sku, Location destinationLocation, Integer standardDays, Integer expeditedDays, Integer minimumOrderQuantity, BigDecimal expeditedCostPerUnit, TransportMode transportMode) {
        this.supplier = supplier;
        this.sku = sku;
        this.destinationLocation = destinationLocation;
        this.standardDays = standardDays;
        this.expeditedDays = expeditedDays;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.expeditedCostPerUnit = expeditedCostPerUnit;
        this.transportMode = transportMode;
        this.active = true;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public SKU getSku() {
        return sku;
    }

    public void setSku(SKU sku) {
        this.sku = sku;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(Location destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public Integer getStandardDays() {
        return standardDays;
    }

    public void setStandardDays(Integer standardDays) {
        this.standardDays = standardDays;
    }

    public Integer getExpeditedDays() {
        return expeditedDays;
    }

    public void setExpeditedDays(Integer expeditedDays) {
        this.expeditedDays = expeditedDays;
    }

    public Integer getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public void setMinimumOrderQuantity(Integer minimumOrderQuantity) {
        this.minimumOrderQuantity = minimumOrderQuantity;
    }

    public BigDecimal getExpeditedCostPerUnit() {
        return expeditedCostPerUnit;
    }

    public void setExpeditedCostPerUnit(BigDecimal expeditedCostPerUnit) {
        this.expeditedCostPerUnit = expeditedCostPerUnit;
    }

    public TransportMode getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeadTime leadTime)) return false;
        return Objects.equals(getId(), leadTime.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "LeadTime{" +
                "id=" + getId() +
                ", standardDays=" + standardDays +
                ", expeditedDays=" + expeditedDays +
                ", transportMode=" + transportMode +
                ", active=" + active +
                '}';
    }
}

