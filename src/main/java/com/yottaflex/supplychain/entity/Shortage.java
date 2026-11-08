package com.yottaflex.supplychain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yottaflex.supplychain.entity.enums.ShortageRootCause;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing stock shortage risks and occurrences detected across nodes.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
    name = "shortages",
    indexes = {
        @Index(name = "idx_shortage_code", columnList = "shortage_code", unique = true),
        @Index(name = "idx_shortage_sku", columnList = "sku_id"),
        @Index(name = "idx_shortage_location", columnList = "location_id"),
        @Index(name = "idx_shortage_severity", columnList = "severity"),
        @Index(name = "idx_shortage_resolved", columnList = "resolved"),
        @Index(name = "idx_shortage_date", columnList = "shortage_date")
    }
)
public class Shortage extends BaseAuditableEntity {

    @NotBlank(message = "Shortage code cannot be blank")
    @Size(min = 3, max = 50, message = "Shortage code must be between 3 and 50 characters")
    @Column(name = "shortage_code", unique = true, nullable = false, length = 50)
    private String shortageCode;

    @NotNull(message = "SKU is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shortage_sku"))
    private SKU sku;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shortage_location"))
    private Location location;

    @NotNull(message = "Shortage date is required")
    @Column(name = "shortage_date", nullable = false)
    private LocalDate shortageDate;

    @NotNull(message = "Shortage quantity is required")
    @DecimalMin(value = "0.01", message = "Shortage quantity must be greater than zero")
    @Column(name = "shortage_quantity", precision = 14, scale = 2, nullable = false)
    private BigDecimal shortageQuantity;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private ShortageSeverity severity = ShortageSeverity.HIGH;

    @NotNull(message = "Root cause is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "root_cause", nullable = false, length = 50)
    private ShortageRootCause rootCause = ShortageRootCause.DEMAND_SURGE;

    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;

    @Column(name = "resolution_date")
    private LocalDate resolutionDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @JsonIgnore
    @OneToMany(mappedBy = "shortage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MitigationOption> mitigationOptions = new ArrayList<>();

    public Shortage() {
    }

    public Shortage(String shortageCode, SKU sku, Location location, LocalDate shortageDate, BigDecimal shortageQuantity, ShortageSeverity severity, ShortageRootCause rootCause) {
        this.shortageCode = shortageCode;
        this.sku = sku;
        this.location = location;
        this.shortageDate = shortageDate;
        this.shortageQuantity = shortageQuantity;
        this.severity = severity;
        this.rootCause = rootCause;
        this.resolved = false;
    }

    public void addMitigationOption(MitigationOption option) {
        mitigationOptions.add(option);
        option.setShortage(this);
    }

    public void removeMitigationOption(MitigationOption option) {
        mitigationOptions.remove(option);
        option.setShortage(null);
    }

    public String getShortageCode() {
        return shortageCode;
    }

    public void setShortageCode(String shortageCode) {
        this.shortageCode = shortageCode;
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

    public LocalDate getShortageDate() {
        return shortageDate;
    }

    public void setShortageDate(LocalDate shortageDate) {
        this.shortageDate = shortageDate;
    }

    public BigDecimal getShortageQuantity() {
        return shortageQuantity;
    }

    public void setShortageQuantity(BigDecimal shortageQuantity) {
        this.shortageQuantity = shortageQuantity;
    }

    public ShortageSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ShortageSeverity severity) {
        this.severity = severity;
    }

    public ShortageRootCause getRootCause() {
        return rootCause;
    }

    public void setRootCause(ShortageRootCause rootCause) {
        this.rootCause = rootCause;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public LocalDate getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(LocalDate resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<MitigationOption> getMitigationOptions() {
        return mitigationOptions;
    }

    public void setMitigationOptions(List<MitigationOption> mitigationOptions) {
        this.mitigationOptions = mitigationOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shortage shortage)) return false;
        return Objects.equals(shortageCode, shortage.shortageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortageCode);
    }

    @Override
    public String toString() {
        return "Shortage{" +
                "id=" + getId() +
                ", shortageCode='" + shortageCode + '\'' +
                ", shortageQuantity=" + shortageQuantity +
                ", severity=" + severity +
                ", rootCause=" + rootCause +
                ", resolved=" + resolved +
                '}';
    }
}

