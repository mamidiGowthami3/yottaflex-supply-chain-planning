package com.yottaflex.supplychain.entity;

import com.yottaflex.supplychain.entity.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing purchase orders placed with suppliers.
 */
@Entity
@Table(
    name = "purchase_orders",
    indexes = {
        @Index(name = "idx_po_number", columnList = "po_number", unique = true),
        @Index(name = "idx_po_supplier", columnList = "supplier_id"),
        @Index(name = "idx_po_sku", columnList = "sku_id"),
        @Index(name = "idx_po_destination", columnList = "destination_location_id"),
        @Index(name = "idx_po_status", columnList = "status"),
        @Index(name = "idx_po_order_date", columnList = "order_date")
    }
)
public class PurchaseOrder extends BaseAuditableEntity {

    @NotBlank(message = "PO Number cannot be blank")
    @Size(min = 3, max = 50, message = "PO Number must be between 3 and 50 characters")
    @Column(name = "po_number", unique = true, nullable = false, length = 50)
    private String poNumber;

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_po_supplier"))
    private Supplier supplier;

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_po_sku"))
    private SKU sku;

    @NotNull(message = "Destination location is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_po_destination_location"))
    private Location destinationLocation;

    @NotNull(message = "Order date is required")
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @NotNull(message = "Expected delivery date is required")
    @Column(name = "expected_delivery_date", nullable = false)
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @NotNull(message = "Quantity ordered is required")
    @DecimalMin(value = "0.01", message = "Quantity ordered must be greater than 0")
    @Column(name = "quantity_ordered", precision = 14, scale = 2, nullable = false)
    private BigDecimal quantityOrdered;

    @DecimalMin(value = "0.00", inclusive = true, message = "Quantity received cannot be negative")
    @Column(name = "quantity_received", precision = 14, scale = 2, nullable = false)
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Unit price cannot be negative")
    @Column(name = "unit_price", precision = 14, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Total amount cannot be negative")
    @Column(name = "total_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @NotNull(message = "PO Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PurchaseOrderStatus status = PurchaseOrderStatus.PLACED;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    public PurchaseOrder() {
    }

    public PurchaseOrder(String poNumber, Supplier supplier, SKU sku, Location destinationLocation, LocalDate orderDate, LocalDate expectedDeliveryDate, BigDecimal quantityOrdered, BigDecimal unitPrice) {
        this.poNumber = poNumber;
        this.supplier = supplier;
        this.sku = sku;
        this.destinationLocation = destinationLocation;
        this.orderDate = orderDate;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = BigDecimal.ZERO;
        this.unitPrice = unitPrice;
        if (quantityOrdered != null && unitPrice != null) {
            this.totalAmount = quantityOrdered.multiply(unitPrice);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
        this.status = PurchaseOrderStatus.PLACED;
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
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

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public BigDecimal getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(BigDecimal quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
        if (this.quantityOrdered != null && this.unitPrice != null) {
            this.totalAmount = this.quantityOrdered.multiply(this.unitPrice);
        }
    }

    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }

    public void setQuantityReceived(BigDecimal quantityReceived) {
        this.quantityReceived = quantityReceived;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        if (this.quantityOrdered != null && this.unitPrice != null) {
            this.totalAmount = this.quantityOrdered.multiply(this.unitPrice);
        }
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
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
        if (!(o instanceof PurchaseOrder that)) return false;
        return Objects.equals(poNumber, that.poNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(poNumber);
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "id=" + getId() +
                ", poNumber='" + poNumber + '\'' +
                ", quantityOrdered=" + quantityOrdered +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                '}';
    }
}

