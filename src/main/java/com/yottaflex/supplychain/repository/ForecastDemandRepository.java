package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.ForecastDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ForecastDemandRepository extends JpaRepository<ForecastDemand, Long> {
    List<ForecastDemand> findBySkuId(Long skuId);
    List<ForecastDemand> findByLocationId(Long locationId);
    List<ForecastDemand> findByForecastDateBetween(LocalDate startDate, LocalDate endDate);
    List<ForecastDemand> findBySkuIdAndLocationId(Long skuId, Long locationId);
    boolean existsBySkuIdAndLocationIdAndForecastDate(Long skuId, Long locationId, LocalDate forecastDate);

    @Query("SELECT COUNT(f) FROM ForecastDemand f WHERE f.sku.skuCode LIKE concat(:prefix, '%')")
    long countBySyntheticSku(@Param("prefix") String prefix);
}

