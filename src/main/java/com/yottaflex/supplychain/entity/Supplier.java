package com.yottaflex.supplychain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Entity representing an external or internal supplier providing materials or SKUs.
 */
@Entity
@Table(
    name = "suppliers",
    indexes = {
        @Index(name = "idx_supplier_code", columnList = "supplier_code", unique = true),
        @Index(name = "idx_supplier_name", columnList = "name"),
        @Index(name = "idx_supplier_active", columnList = "active")
    }
)
public class Supplier extends BaseAuditableEntity {

    @NotBlank(message = "Supplier code cannot be blank")
    @Size(min = 2, max = 50, message = "Supplier code must be between 2 and 50 characters")
    @Column(name = "supplier_code", unique = true, nullable = false, length = 50)
    private String supplierCode;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(max = 150, message = "Supplier name cannot exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    @Column(name = "phone", length = 50)
    private String phone;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Column(name = "country", length = 100)
    private String country;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    @Column(name = "rating")
    private Double rating = 5.0;

    @DecimalMin(value = "0.0", message = "Reliability score must be at least 0.0")
    @DecimalMax(value = "100.0", message = "Reliability score cannot exceed 100.0")
    @Column(name = "reliability_score")
    private Double reliabilityScore = 95.0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public Supplier() {
    }

    public Supplier(String supplierCode, String name, String contactPerson, String email, String country, Double rating, Double reliabilityScore) {
        this.supplierCode = supplierCode;
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.country = country;
        this.rating = rating;
        this.reliabilityScore = reliabilityScore;
        this.active = true;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getReliabilityScore() {
        return reliabilityScore;
    }

    public void setReliabilityScore(Double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
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
        if (!(o instanceof Supplier supplier)) return false;
        return Objects.equals(supplierCode, supplier.supplierCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierCode);
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + getId() +
                ", supplierCode='" + supplierCode + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", rating=" + rating +
                ", reliabilityScore=" + reliabilityScore +
                ", active=" + active +
                '}';
    }
}

