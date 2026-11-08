package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.SupplierCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SupplierCapacityRepository extends JpaRepository<SupplierCapacity, Long> {
    List<SupplierCapacity> findBySupplierId(Long supplierId);
    List<SupplierCapacity> findBySkuId(Long skuId);
    List<SupplierCapacity> findBySupplierIdAndSkuId(Long supplierId, Long skuId);
    List<SupplierCapacity> findByEffectiveStartDateLessThanEqualAndEffectiveEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);
    boolean existsBySupplierIdAndSkuIdAndEffectiveStartDate(Long supplierId, Long skuId, LocalDate effectiveStartDate);

    @Query("SELECT COUNT(sc) FROM SupplierCapacity sc WHERE sc.supplier.supplierCode LIKE concat(:prefix, '%')")
    long countBySyntheticSupplier(@Param("prefix") String prefix);
}

