package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.Inventory;
import com.yottaflex.supplychain.entity.enums.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySkuIdAndLocationId(Long skuId, Long locationId);
    boolean existsBySkuIdAndLocationId(Long skuId, Long locationId);
    List<Inventory> findByLocationId(Long locationId);
    List<Inventory> findBySkuId(Long skuId);
    List<Inventory> findByStatus(InventoryStatus status);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.sku.skuCode LIKE concat(:prefix, '%')")
    long countBySyntheticSku(@org.springframework.data.repository.query.Param("prefix") String prefix);

    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.reorderPoint")
    List<Inventory> findItemsNeedingReorder();
}

