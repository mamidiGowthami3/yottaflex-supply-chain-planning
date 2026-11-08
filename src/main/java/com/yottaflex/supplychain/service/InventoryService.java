package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.entity.Inventory;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.enums.InventoryStatus;
import com.yottaflex.supplychain.exception.BusinessValidationException;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SkuService skuService;
    private final LocationService locationService;

    public InventoryService(InventoryRepository inventoryRepository, SkuService skuService, LocationService locationService) {
        this.inventoryRepository = inventoryRepository;
        this.skuService = skuService;
        this.locationService = locationService;
    }

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
    }

    public Inventory findBySkuAndLocation(Long skuId, Long locationId) {
        return inventoryRepository.findBySkuIdAndLocationId(skuId, locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found for SKU ID " + skuId + " and Location ID " + locationId));
    }

    public List<Inventory> findByStatus(InventoryStatus status) {
        return inventoryRepository.findByStatus(status);
    }

    public List<Inventory> findItemsNeedingReorder() {
        return inventoryRepository.findItemsNeedingReorder();
    }

    @Transactional
    public Inventory recordStock(Long skuId, Long locationId, BigDecimal quantityOnHand, BigDecimal quantityReserved) {
        SKU sku = skuService.findById(skuId);
        Location location = locationService.findById(locationId);

        Inventory inventory = inventoryRepository.findBySkuIdAndLocationId(skuId, locationId)
                .orElseGet(() -> {
                    Inventory newInv = new Inventory();
                    newInv.setSku(sku);
                    newInv.setLocation(location);
                    newInv.setSafetyStockLevel(BigDecimal.valueOf(sku.getSafetyStock()));
                    newInv.setReorderPoint(BigDecimal.valueOf(sku.getReorderPoint()));
                    return newInv;
                });

        inventory.setQuantityOnHand(quantityOnHand);
        inventory.setQuantityReserved(quantityReserved);
        inventory.calculateAvailableAndStatus();
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory adjustQuantityOnHand(Long id, BigDecimal deltaQuantity) {
        Inventory inventory = findById(id);
        BigDecimal newQuantity = inventory.getQuantityOnHand().add(deltaQuantity);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Quantity on hand cannot become negative after adjustment");
        }
        inventory.setQuantityOnHand(newQuantity);
        inventory.calculateAvailableAndStatus();
        return inventoryRepository.save(inventory);
    }
}

