package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.exception.DuplicateResourceException;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public List<Location> findActive() {
        return locationRepository.findByActiveTrue();
    }

    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
    }

    public Location findByLocationCode(String code) {
        return locationRepository.findByLocationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "locationCode", code));
    }

    @Transactional
    public Location create(Location location) {
        if (locationRepository.existsByLocationCode(location.getLocationCode())) {
            throw new DuplicateResourceException("Location", "locationCode", location.getLocationCode());
        }
        return locationRepository.save(location);
    }

    @Transactional
    public Location update(Long id, Location updatedLocation) {
        Location existing = findById(id);
        if (!existing.getLocationCode().equals(updatedLocation.getLocationCode()) && locationRepository.existsByLocationCode(updatedLocation.getLocationCode())) {
            throw new DuplicateResourceException("Location", "locationCode", updatedLocation.getLocationCode());
        }
        existing.setLocationCode(updatedLocation.getLocationCode());
        existing.setName(updatedLocation.getName());
        existing.setType(updatedLocation.getType());
        existing.setAddress(updatedLocation.getAddress());
        existing.setCity(updatedLocation.getCity());
        existing.setState(updatedLocation.getState());
        existing.setCountry(updatedLocation.getCountry());
        existing.setPostalCode(updatedLocation.getPostalCode());
        existing.setCapacity(updatedLocation.getCapacity());
        existing.setActive(updatedLocation.getActive());
        return locationRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Location existing = findById(id);
        locationRepository.delete(existing);
    }
}

