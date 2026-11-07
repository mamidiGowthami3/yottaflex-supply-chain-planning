package com.yottaflex.supplychain.entity;

import com.yottaflex.supplychain.entity.enums.ProductionPlanStatus;
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
 * Entity representing planned and executed production runs across manufacturing locations.
 */
@Entity
@Table(
    name = "production_plans",
    indexes = {
        @Index(name = "idx_plan_code", columnList = "plan_code", unique = true),
        @Index(name = "idx_pp_sku", columnList = "sku_id"),
        @Index(name = "idx_pp_location", columnList = "location_id"),
        @Index(name = "idx_pp_status", columnList = "status"),
        @Index(name = "idx_pp_dates", columnList = "start_date, end_date")
    }
)
public class ProductionPlan extends BaseAuditableEntity {

    @NotBlank(message = "Plan code cannot be blank")
    @Size(min = 3, max = 50, message = "Plan code must be between 3 and 50 characters")
    @Column(name = "plan_code", unique = true, nullable = false, length = 50)
    private String planCode;

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prod_plan_sku"))
    private SKU sku;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prod_plan_location"))
    private Location location;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Planned quantity is required")
    @DecimalMin(value = "0.01", message = "Planned quantity must be greater than zero")
    @Column(name = "planned_quantity", precision = 14, scale = 2, nullable = false)
    private BigDecimal plannedQuantity;

    @DecimalMin(value = "0.00", inclusive = true, message = "Produced quantity cannot be negative")
    @Column(name = "produced_quantity", precision = 14, scale = 2, nullable = false)
    private BigDecimal producedQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Capacity utilization cannot be negative")
    @DecimalMax(value = "150.0", message = "Capacity utilization cannot exceed 150%")
    @Column(name = "capacity_utilization")
    private Double capacityUtilization;

    @NotNull(message = "Production status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProductionPlanStatus status = ProductionPlanStatus.SCHEDULED;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    public ProductionPlan() {
    }

    public ProductionPlan(String planCode, SKU sku, Location location, LocalDate startDate, LocalDate endDate, BigDecimal plannedQuantity, Double capacityUtilization) {
        this.planCode = planCode;
        this.sku = sku;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.plannedQuantity = plannedQuantity;
        this.producedQuantity = BigDecimal.ZERO;
        this.capacityUtilization = capacityUtilization;
        this.status = ProductionPlanStatus.SCHEDULED;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public SKU getSku() {
        return sku;
    }

    public void setSku(SKU sku) {
        this.sku = sku;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public Double getCapacityUtilization() {
        return capacityUtilization;
    }

    public void setCapacityUtilization(Double capacityUtilization) {
        this.capacityUtilization = capacityUtilization;
    }

    public ProductionPlanStatus getStatus() {
        return status;
    }

    public void setStatus(ProductionPlanStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductionPlan that)) return false;
        return Objects.equals(planCode, that.planCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planCode);
    }

    @Override
    public String toString() {
        return "ProductionPlan{" +
                "id=" + getId() +
                ", planCode='" + planCode + '\'' +
                ", plannedQuantity=" + plannedQuantity +
                ", producedQuantity=" + producedQuantity +
                ", status=" + status +
                '}';
    }
}

