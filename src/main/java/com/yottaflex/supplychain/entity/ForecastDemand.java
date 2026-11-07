package com.yottaflex.supplychain.entity;

import com.yottaflex.supplychain.entity.enums.DemandPattern;
import com.yottaflex.supplychain.entity.enums.DemandPeriodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing demand forecasts per SKU and Location over time.
 */
@Entity
@Table(
    name = "forecast_demands",
    indexes = {
        @Index(name = "idx_fd_sku_location_date", columnList = "sku_id, location_id, forecast_date"),
        @Index(name = "idx_fd_forecast_date", columnList = "forecast_date")
    }
)
public class ForecastDemand extends BaseAuditableEntity {

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_forecast_sku"))
    private SKU sku;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_forecast_location"))
    private Location location;

    @NotNull(message = "Forecast date is required")
    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @NotNull(message = "Period type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private DemandPeriodType periodType = DemandPeriodType.DAILY;

    @NotNull(message = "Forecast quantity is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Forecast quantity cannot be negative")
    @Column(name = "forecast_quantity", precision = 14, scale = 2, nullable = false)
    private BigDecimal forecastQuantity;

    @DecimalMin(value = "0.00", inclusive = true, message = "Actual quantity cannot be negative")
    @Column(name = "actual_quantity", precision = 14, scale = 2)
    private BigDecimal actualQuantity;

    @DecimalMin(value = "0.0", message = "Confidence score cannot be less than 0.0")
    @DecimalMax(value = "1.0", message = "Confidence score cannot exceed 1.0")
    @Column(name = "confidence_score")
    private Double confidenceScore = 0.95;

    @Enumerated(EnumType.STRING)
    @Column(name = "demand_pattern", length = 30)
    private DemandPattern demandPattern = DemandPattern.STABLE;

    public ForecastDemand() {
    }

    public ForecastDemand(SKU sku, Location location, LocalDate forecastDate, DemandPeriodType periodType, BigDecimal forecastQuantity, Double confidenceScore, DemandPattern demandPattern) {
        this.sku = sku;
        this.location = location;
        this.forecastDate = forecastDate;
        this.periodType = periodType;
        this.forecastQuantity = forecastQuantity;
        this.confidenceScore = confidenceScore;
        this.demandPattern = demandPattern;
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

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }

    public DemandPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(DemandPeriodType periodType) {
        this.periodType = periodType;
    }

    public BigDecimal getForecastQuantity() {
        return forecastQuantity;
    }

    public void setForecastQuantity(BigDecimal forecastQuantity) {
        this.forecastQuantity = forecastQuantity;
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public DemandPattern getDemandPattern() {
        return demandPattern;
    }

    public void setDemandPattern(DemandPattern demandPattern) {
        this.demandPattern = demandPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForecastDemand that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "ForecastDemand{" +
                "id=" + getId() +
                ", forecastDate=" + forecastDate +
                ", periodType=" + periodType +
                ", forecastQuantity=" + forecastQuantity +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}

