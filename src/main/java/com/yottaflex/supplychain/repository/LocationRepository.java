package com.yottaflex.supplychain.repository;

import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLocationCode(String locationCode);
    boolean existsByLocationCode(String locationCode);
    List<Location> findByType(LocationType type);
    List<Location> findByActiveTrue();
    long countByLocationCodeStartingWith(String prefix);
}

