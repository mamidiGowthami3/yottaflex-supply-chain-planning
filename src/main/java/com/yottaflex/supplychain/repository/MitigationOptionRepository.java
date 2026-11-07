package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.MitigationOption;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.MitigationStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MitigationOptionRepository extends JpaRepository<MitigationOption, Long> {
    Optional<MitigationOption> findByOptionCode(String optionCode);
    boolean existsByOptionCode(String optionCode);
    List<MitigationOption> findByShortageId(Long shortageId);
    List<MitigationOption> findByStatus(MitigationStatus status);
    List<MitigationOption> findByStrategy(MitigationStrategy strategy);

    @Query("SELECT COUNT(m) FROM MitigationOption m WHERE m.optionCode LIKE concat(:prefix, '%')")
    long countBySyntheticOptionCode(@Param("prefix") String prefix);

    long countByStatus(MitigationStatus status);
    long countByStrategy(MitigationStrategy strategy);

    @Query("SELECT m FROM MitigationOption m JOIN FETCH m.shortage s JOIN FETCH s.sku JOIN FETCH s.location LEFT JOIN FETCH m.alternateSupplier LEFT JOIN FETCH m.sourceLocation LEFT JOIN FETCH m.purchaseOrder LEFT JOIN FETCH m.productionPlan WHERE m.shortage.id = :shortageId ORDER BY m.recommendationScore DESC")
    List<MitigationOption> findByShortageIdWithDetails(@Param("shortageId") Long shortageId);

    @Query("SELECT m FROM MitigationOption m JOIN FETCH m.shortage s JOIN FETCH s.sku JOIN FETCH s.location LEFT JOIN FETCH m.alternateSupplier LEFT JOIN FETCH m.sourceLocation LEFT JOIN FETCH m.purchaseOrder LEFT JOIN FETCH m.productionPlan WHERE m.optionCode = :optionCode")
    Optional<MitigationOption> findByOptionCodeWithDetails(@Param("optionCode") String optionCode);

    @Query("SELECT m FROM MitigationOption m LEFT JOIN FETCH m.shortage s LEFT JOIN FETCH s.sku LEFT JOIN FETCH s.location LEFT JOIN FETCH m.alternateSupplier LEFT JOIN FETCH m.sourceLocation LEFT JOIN FETCH m.purchaseOrder LEFT JOIN FETCH m.productionPlan WHERE m.status IN :statuses")
    List<MitigationOption> findByStatusInWithDetails(@Param("statuses") Collection<MitigationStatus> statuses);
}


