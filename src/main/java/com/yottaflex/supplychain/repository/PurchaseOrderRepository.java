package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.PurchaseOrder;
import com.yottaflex.supplychain.entity.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    boolean existsByPoNumber(String poNumber);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    List<PurchaseOrder> findBySkuId(Long skuId);
    List<PurchaseOrder> findByDestinationLocationId(Long destinationLocationId);
    List<PurchaseOrder> findBySkuIdAndDestinationLocationId(Long skuId, Long destinationLocationId);
    List<PurchaseOrder> findByExpectedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.poNumber LIKE concat(:prefix, '%')")
    long countBySyntheticPoNumber(@Param("prefix") String prefix);
}

