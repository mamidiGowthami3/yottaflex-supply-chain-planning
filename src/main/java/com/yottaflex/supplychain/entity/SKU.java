package com.yottaflex.supplychain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing a Stock Keeping Unit (SKU) in the supply chain catalog.
 */
@Entity
@Table(
    name = "skus",
    indexes = {
        @Index(name = "idx_sku_code", columnList = "sku_code", unique = true),
        @Index(name = "idx_sku_category", columnList = "category"),
        @Index(name = "idx_sku_active", columnList = "active")
    }
)
public class SKU extends BaseAuditableEntity {

    @NotBlank(message = "SKU code cannot be blank")
    @Size(min = 3, max = 50, message = "SKU code must be between 3 and 50 characters")
    @Column(name = "sku_code", unique = true, nullable = false, length = 50)
    private String skuCode;

    @NotBlank(message = "SKU name cannot be blank")
    @Size(max = 150, message = "SKU name cannot exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @NotBlank(message = "Category cannot be blank")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @NotBlank(message = "Unit of measure cannot be blank")
    @Size(max = 20, message = "Unit of measure cannot exceed 20 characters")
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure = "UNITS";

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Unit cost must be greater than or equal to 0.00")
    @Column(name = "unit_cost", precision = 14, scale = 2, nullable = false)
    private BigDecimal unitCost;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Selling price must be greater than or equal to 0.00")
    @Column(name = "selling_price", precision = 14, scale = 2, nullable = false)
    private BigDecimal sellingPrice;

    @NotNull(message = "Reorder point is required")
    @Min(value = 0, message = "Reorder point cannot be negative")
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint;

    @NotNull(message = "Safety stock is required")
    @Min(value = 0, message = "Safety stock cannot be negative")
    @Column(name = "safety_stock", nullable = false)
    private Integer safetyStock;

    @NotNull(message = "Lead time days is required")
    @Min(value = 0, message = "Lead time days cannot be negative")
    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public SKU() {
    }

    public SKU(String skuCode, String name, String category, BigDecimal unitCost, BigDecimal sellingPrice, Integer reorderPoint, Integer safetyStock, Integer leadTimeDays) {
        this.skuCode = skuCode;
        this.name = name;
        this.category = category;
        this.unitCost = unitCost;
        this.sellingPrice = sellingPrice;
        this.reorderPoint = reorderPoint;
        this.safetyStock = safetyStock;
        this.leadTimeDays = leadTimeDays;
        this.active = true;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(Integer safetyStock) {
        this.safetyStock = safetyStock;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
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
        if (!(o instanceof SKU sku)) return false;
        return Objects.equals(skuCode, sku.skuCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skuCode);
    }

    @Override
    public String toString() {
        return "SKU{" +
                "id=" + getId() +
                ", skuCode='" + skuCode + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", unitCost=" + unitCost +
                ", active=" + active +
                '}';
    }
}

