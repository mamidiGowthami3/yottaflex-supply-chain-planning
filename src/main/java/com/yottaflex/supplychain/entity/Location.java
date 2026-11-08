package com.yottaflex.supplychain.entity;

import com.yottaflex.supplychain.entity.enums.LocationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entity representing physical nodes in the supply network (warehouses, plants, DCs, etc.).
 */
@Entity
@Table(
    name = "locations",
    indexes = {
        @Index(name = "idx_location_code", columnList = "location_code", unique = true),
        @Index(name = "idx_location_type", columnList = "type"),
        @Index(name = "idx_location_active", columnList = "active")
    }
)
public class Location extends BaseAuditableEntity {

    @NotBlank(message = "Location code cannot be blank")
    @Size(min = 2, max = 50, message = "Location code must be between 2 and 50 characters")
    @Column(name = "location_code", unique = true, nullable = false, length = 50)
    private String locationCode;

    @NotBlank(message = "Location name cannot be blank")
    @Size(max = 100, message = "Location name cannot exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Location type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private LocationType type;

    @Column(name = "address", length = 255)
    private String address;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    @Column(name = "state", length = 100)
    private String state;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @DecimalMin(value = "0.00", inclusive = true, message = "Capacity cannot be negative")
    @Column(name = "capacity", precision = 14, scale = 2)
    private BigDecimal capacity;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public Location() {
    }

    public Location(String locationCode, String name, LocationType type, String city, String country, BigDecimal capacity) {
        this.locationCode = locationCode;
        this.name = name;
        this.type = type;
        this.city = city;
        this.country = country;
        this.capacity = capacity;
        this.active = true;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public BigDecimal getCapacity() {
        return capacity;
    }

    public void setCapacity(BigDecimal capacity) {
        this.capacity = capacity;
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
        if (!(o instanceof Location location)) return false;
        return Objects.equals(locationCode, location.locationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationCode);
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + getId() +
                ", locationCode='" + locationCode + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", active=" + active +
                '}';
    }
}

