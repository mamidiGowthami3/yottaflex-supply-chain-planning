package com.yottaflex.supplychain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing production/supply capacity constraints for a Supplier for a specific SKU.
 */
@Entity
@Table(
    name = "supplier_capacities",
    indexes = {
        @Index(name = "idx_sc_supplier_sku", columnList = "supplier_id, sku_id"),
        @Index(name = "idx_sc_dates", columnList = "effective_start_date, effective_end_date")
    }
)
public class SupplierCapacity extends BaseAuditableEntity {

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_supp_cap_supplier"))
    private Supplier supplier;

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_supp_cap_sku"))
    private SKU sku;

    @NotNull(message = "Effective start date is required")
    @Column(name = "effective_start_date", nullable = false)
    private LocalDate effectiveStartDate;

    @NotNull(message = "Effective end date is required")
    @Column(name = "effective_end_date", nullable = false)
    private LocalDate effectiveEndDate;

    @NotNull(message = "Daily capacity is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Daily capacity cannot be negative")
    @Column(name = "daily_capacity", precision = 14, scale = 2, nullable = false)
    private BigDecimal dailyCapacity;

    @NotNull(message = "Monthly capacity is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Monthly capacity cannot be negative")
    @Column(name = "monthly_capacity", precision = 14, scale = 2, nullable = false)
    private BigDecimal monthlyCapacity;

    @DecimalMin(value = "0.00", inclusive = true, message = "Allocated capacity cannot be negative")
    @Column(name = "allocated_capacity", precision = 14, scale = 2, nullable = false)
    private BigDecimal allocatedCapacity = BigDecimal.ZERO;

    @Column(name = "available_capacity", precision = 14, scale = 2, nullable = false)
    private BigDecimal availableCapacity = BigDecimal.ZERO;

    public SupplierCapacity() {
    }

    public SupplierCapacity(Supplier supplier, SKU sku, LocalDate effectiveStartDate, LocalDate effectiveEndDate, BigDecimal dailyCapacity, BigDecimal monthlyCapacity) {
        this.supplier = supplier;
        this.sku = sku;
        this.effectiveStartDate = effectiveStartDate;
        this.effectiveEndDate = effectiveEndDate;
        this.dailyCapacity = dailyCapacity;
        this.monthlyCapacity = monthlyCapacity;
        this.allocatedCapacity = BigDecimal.ZERO;
        this.availableCapacity = monthlyCapacity;
    }

    public void updateAvailableCapacity() {
        if (this.monthlyCapacity != null && this.allocatedCapacity != null) {
            this.availableCapacity = this.monthlyCapacity.subtract(this.allocatedCapacity);
        } else if (this.monthlyCapacity != null) {
            this.availableCapacity = this.monthlyCapacity;
        } else {
            this.availableCapacity = BigDecimal.ZERO;
        }
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        updateAvailableCapacity();
    }

    @PreUpdate
    @Override
    protected void onUpdate() {
        super.onUpdate();
        updateAvailableCapacity();
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

    public LocalDate getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public void setEffectiveStartDate(LocalDate effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public LocalDate getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(LocalDate effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public BigDecimal getDailyCapacity() {
        return dailyCapacity;
    }

    public void setDailyCapacity(BigDecimal dailyCapacity) {
        this.dailyCapacity = dailyCapacity;
    }

    public BigDecimal getMonthlyCapacity() {
        return monthlyCapacity;
    }

    public void setMonthlyCapacity(BigDecimal monthlyCapacity) {
        this.monthlyCapacity = monthlyCapacity;
        updateAvailableCapacity();
    }

    public BigDecimal getAllocatedCapacity() {
        return allocatedCapacity;
    }

    public void setAllocatedCapacity(BigDecimal allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
        updateAvailableCapacity();
    }

    public BigDecimal getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(BigDecimal availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierCapacity that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "SupplierCapacity{" +
                "id=" + getId() +
                ", dailyCapacity=" + dailyCapacity +
                ", monthlyCapacity=" + monthlyCapacity +
                ", allocatedCapacity=" + allocatedCapacity +
                ", availableCapacity=" + availableCapacity +
                '}';
    }
}

