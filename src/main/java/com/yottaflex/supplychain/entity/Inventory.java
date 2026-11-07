package com.yottaflex.supplychain.entity;

import com.yottaflex.supplychain.entity.enums.InventoryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing current stock levels of a SKU at a specific Location.
 */
@Entity
@Table(
    name = "inventories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_sku_location", columnNames = {"sku_id", "location_id"})
    },
    indexes = {
        @Index(name = "idx_inventory_sku", columnList = "sku_id"),
        @Index(name = "idx_inventory_location", columnList = "location_id"),
        @Index(name = "idx_inventory_status", columnList = "status")
    }
)
public class Inventory extends BaseAuditableEntity {

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_sku"))
    private SKU sku;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_location"))
    private Location location;

    @NotNull(message = "Quantity on hand is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Quantity on hand cannot be negative")
    @Column(name = "quantity_on_hand", precision = 14, scale = 2, nullable = false)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @NotNull(message = "Quantity reserved is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Quantity reserved cannot be negative")
    @Column(name = "quantity_reserved", precision = 14, scale = 2, nullable = false)
    private BigDecimal quantityReserved = BigDecimal.ZERO;

    @NotNull(message = "Quantity available is required")
    @Column(name = "quantity_available", precision = 14, scale = 2, nullable = false)
    private BigDecimal quantityAvailable = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", inclusive = true, message = "Safety stock level cannot be negative")
    @Column(name = "safety_stock_level", precision = 14, scale = 2)
    private BigDecimal safetyStockLevel;

    @DecimalMin(value = "0.00", inclusive = true, message = "Reorder point cannot be negative")
    @Column(name = "reorder_point", precision = 14, scale = 2)
    private BigDecimal reorderPoint;

    @NotNull(message = "Inventory status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InventoryStatus status = InventoryStatus.IN_STOCK;

    @Column(name = "last_audit_date")
    private LocalDate lastAuditDate;

    public Inventory() {
    }

    public Inventory(SKU sku, Location location, BigDecimal quantityOnHand, BigDecimal quantityReserved, BigDecimal safetyStockLevel, BigDecimal reorderPoint) {
        this.sku = sku;
        this.location = location;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved != null ? quantityReserved : BigDecimal.ZERO;
        this.safetyStockLevel = safetyStockLevel;
        this.reorderPoint = reorderPoint;
        calculateAvailableAndStatus();
    }

    public void calculateAvailableAndStatus() {
        if (this.quantityOnHand != null && this.quantityReserved != null) {
            this.quantityAvailable = this.quantityOnHand.subtract(this.quantityReserved);
        } else if (this.quantityOnHand != null) {
            this.quantityAvailable = this.quantityOnHand;
        } else {
            this.quantityAvailable = BigDecimal.ZERO;
        }

        if (this.quantityAvailable.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InventoryStatus.OUT_OF_STOCK;
        } else if (this.safetyStockLevel != null && this.quantityAvailable.compareTo(this.safetyStockLevel) < 0) {
            this.status = InventoryStatus.LOW_STOCK;
        } else {
            this.status = InventoryStatus.IN_STOCK;
        }
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        calculateAvailableAndStatus();
    }

    @PreUpdate
    @Override
    protected void onUpdate() {
        super.onUpdate();
        calculateAvailableAndStatus();
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

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(BigDecimal quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
        calculateAvailableAndStatus();
    }

    public BigDecimal getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(BigDecimal quantityReserved) {
        this.quantityReserved = quantityReserved;
        calculateAvailableAndStatus();
    }

    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(BigDecimal quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public BigDecimal getSafetyStockLevel() {
        return safetyStockLevel;
    }

    public void setSafetyStockLevel(BigDecimal safetyStockLevel) {
        this.safetyStockLevel = safetyStockLevel;
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(BigDecimal reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }

    public LocalDate getLastAuditDate() {
        return lastAuditDate;
    }

    public void setLastAuditDate(LocalDate lastAuditDate) {
        this.lastAuditDate = lastAuditDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + getId() +
                ", onHand=" + quantityOnHand +
                ", reserved=" + quantityReserved +
                ", available=" + quantityAvailable +
                ", status=" + status +
                '}';
    }
}

