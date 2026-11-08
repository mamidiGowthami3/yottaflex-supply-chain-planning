package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    boolean existsBySupplierCode(String supplierCode);
    List<Supplier> findByActiveTrue();
    List<Supplier> findByCountry(String country);
    long countBySupplierCodeStartingWith(String prefix);
}

