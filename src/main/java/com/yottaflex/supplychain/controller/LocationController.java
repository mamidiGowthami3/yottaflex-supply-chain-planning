package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Location>>> getAllLocations() {
        return ResponseEntity.ok(ApiResponse.success(locationService.findAll()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Location>>> getActiveLocations() {
        return ResponseEntity.ok(ApiResponse.success(locationService.findActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Location>> getLocationById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(locationService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Location>> createLocation(@Valid @RequestBody Location location) {
        Location created = locationService.create(location);
        return new ResponseEntity<>(ApiResponse.success("Location created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Location>> updateLocation(@PathVariable Long id, @Valid @RequestBody Location location) {
        Location updated = locationService.update(id, location);
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Location deleted successfully", null));
    }
}

