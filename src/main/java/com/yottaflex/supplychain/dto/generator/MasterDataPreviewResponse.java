package com.yottaflex.supplychain.dto.generator;

import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Supplier;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing an in-memory preview of generated master data.
 * Does NOT reflect database persistence.
 */
public class MasterDataPreviewResponse {

    private long seed;
    private int locationCount;
    private int supplierCount;
    private int skuCount;
    private int totalRecords;
    private boolean persisted = false;
    private LocalDateTime generatedAt;

    private List<Location> previewLocations;
    private List<Supplier> previewSuppliers;
    private List<SKU> previewSkus;

    public MasterDataPreviewResponse() {
        this.generatedAt = LocalDateTime.now();
        this.persisted = false;
    }

    public MasterDataPreviewResponse(long seed, List<Location> previewLocations, List<Supplier> previewSuppliers, List<SKU> previewSkus) {
        this.seed = seed;
        this.previewLocations = previewLocations;
        this.previewSuppliers = previewSuppliers;
        this.previewSkus = previewSkus;
        this.locationCount = previewLocations != null ? previewLocations.size() : 0;
        this.supplierCount = previewSuppliers != null ? previewSuppliers.size() : 0;
        this.skuCount = previewSkus != null ? previewSkus.size() : 0;
        this.totalRecords = this.locationCount + this.supplierCount + this.skuCount;
        this.persisted = false;
        this.generatedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(int locationCount) {
        this.locationCount = locationCount;
    }

    public int getSupplierCount() {
        return supplierCount;
    }

    public void setSupplierCount(int supplierCount) {
        this.supplierCount = supplierCount;
    }

    public int getSkuCount() {
        return skuCount;
    }

    public void setSkuCount(int skuCount) {
        this.skuCount = skuCount;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<Location> getPreviewLocations() {
        return previewLocations;
    }

    public void setPreviewLocations(List<Location> previewLocations) {
        this.previewLocations = previewLocations;
    }

    public List<Supplier> getPreviewSuppliers() {
        return previewSuppliers;
    }

    public void setPreviewSuppliers(List<Supplier> previewSuppliers) {
        this.previewSuppliers = previewSuppliers;
    }

    public List<SKU> getPreviewSkus() {
        return previewSkus;
    }

    public void setPreviewSkus(List<SKU> previewSkus) {
        this.previewSkus = previewSkus;
    }
}

