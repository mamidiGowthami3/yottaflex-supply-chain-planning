package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SKURepository extends JpaRepository<SKU, Long> {
    Optional<SKU> findBySkuCode(String skuCode);
    boolean existsBySkuCode(String skuCode);
    List<SKU> findByCategory(String category);
    List<SKU> findByActiveTrue();
    long countBySkuCodeStartingWith(String prefix);
}

