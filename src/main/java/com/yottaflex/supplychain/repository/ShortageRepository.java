package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.Shortage;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortageRepository extends JpaRepository<Shortage, Long> {
    Optional<Shortage> findByShortageCode(String shortageCode);
    boolean existsByShortageCode(String shortageCode);
    List<Shortage> findByResolvedFalse();
    List<Shortage> findByResolvedTrue();
    List<Shortage> findBySeverity(ShortageSeverity severity);
    List<Shortage> findBySkuId(Long skuId);
    List<Shortage> findByLocationId(Long locationId);
    boolean existsBySkuIdAndLocationIdAndShortageDateAndResolvedFalse(Long skuId, Long locationId, LocalDate shortageDate);

    @Query("SELECT COUNT(s) FROM Shortage s WHERE s.shortageCode LIKE concat(:prefix, '%')")
    long countBySyntheticShortageCode(@Param("prefix") String prefix);

    long countByResolvedFalse();
    long countByResolvedTrue();
    long countBySeverity(ShortageSeverity severity);

    @Query("SELECT DISTINCT s FROM Shortage s JOIN FETCH s.sku JOIN FETCH s.location LEFT JOIN FETCH s.mitigationOptions mo LEFT JOIN FETCH mo.alternateSupplier LEFT JOIN FETCH mo.sourceLocation ORDER BY s.shortageDate ASC, s.severity DESC")
    List<Shortage> findAllWithDetails();

    @Query("SELECT DISTINCT s FROM Shortage s JOIN FETCH s.sku JOIN FETCH s.location LEFT JOIN FETCH s.mitigationOptions mo LEFT JOIN FETCH mo.alternateSupplier LEFT JOIN FETCH mo.sourceLocation WHERE s.shortageCode = :shortageCode")
    Optional<Shortage> findByShortageCodeWithDetails(@Param("shortageCode") String shortageCode);
}


