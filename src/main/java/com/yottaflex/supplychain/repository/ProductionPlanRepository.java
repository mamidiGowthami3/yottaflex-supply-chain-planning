package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.ProductionPlan;
import com.yottaflex.supplychain.entity.enums.ProductionPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionPlanRepository extends JpaRepository<ProductionPlan, Long> {
    Optional<ProductionPlan> findByPlanCode(String planCode);
    boolean existsByPlanCode(String planCode);
    List<ProductionPlan> findByStatus(ProductionPlanStatus status);
    List<ProductionPlan> findBySkuId(Long skuId);
    List<ProductionPlan> findByLocationId(Long locationId);
    List<ProductionPlan> findBySkuIdAndLocationId(Long skuId, Long locationId);
    List<ProductionPlan> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(pp) FROM ProductionPlan pp WHERE pp.planCode LIKE concat(:prefix, '%')")
    long countBySyntheticPlanCode(@Param("prefix") String prefix);
}

