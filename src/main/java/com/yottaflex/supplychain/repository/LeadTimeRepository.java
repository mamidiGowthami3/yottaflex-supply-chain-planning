package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.LeadTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadTimeRepository extends JpaRepository<LeadTime, Long> {
    List<LeadTime> findBySupplierId(Long supplierId);
    List<LeadTime> findBySkuId(Long skuId);
    List<LeadTime> findByDestinationLocationId(Long destinationLocationId);
    Optional<LeadTime> findBySupplierIdAndSkuIdAndDestinationLocationId(Long supplierId, Long skuId, Long destinationLocationId);
    boolean existsBySupplierIdAndSkuIdAndDestinationLocationId(Long supplierId, Long skuId, Long destinationLocationId);

    @Query("SELECT COUNT(lt) FROM LeadTime lt WHERE lt.supplier.supplierCode LIKE concat(:prefix, '%')")
    long countBySyntheticSupplier(@Param("prefix") String prefix);
}

