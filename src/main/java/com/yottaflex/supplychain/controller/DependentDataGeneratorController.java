package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.dto.generator.DependentDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.DependentDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.DependentDataStatisticsResponse;
import com.yottaflex.supplychain.generator.SyntheticDataGenerator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing endpoints for synthetic dependent transactional data generation,
 * previewing, and database statistics (Inventory, ForecastDemand, SupplierCapacity, LeadTime).
 */
@RestController
@RequestMapping("/api/v1/generator/dependent-data")
public class DependentDataGeneratorController {

    private final SyntheticDataGenerator syntheticDataGenerator;

    public DependentDataGeneratorController(SyntheticDataGenerator syntheticDataGenerator) {
        this.syntheticDataGenerator = syntheticDataGenerator;
    }

    /**
     * Preview synthetic dependent data in-memory without persisting to MySQL.
     * Guaranteed zero database mutations (persisted: false).
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<DependentDataPreviewResponse>> previewDependentData(
            @Valid @RequestBody(required = false) DependentDataGenerationRequest request) {
        DependentDataGenerationRequest effectiveRequest = (request != null) ? request : new DependentDataGenerationRequest();
        DependentDataPreviewResponse preview = syntheticDataGenerator.previewDependentData(effectiveRequest);
        return ResponseEntity.ok(ApiResponse.success("Dependent data preview generated successfully", preview));
    }

    /**
     * Generate synthetic dependent transactional data and persist records into MySQL.
     * Collision-safe, duplicate-preventing, transactional.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<DependentDataGenerationResult>> generateDependentData(
            @Valid @RequestBody(required = false) DependentDataGenerationRequest request) {
        DependentDataGenerationRequest effectiveRequest = (request != null) ? request : new DependentDataGenerationRequest();
        DependentDataGenerationResult result = syntheticDataGenerator.generateDependentData(effectiveRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Synthetic dependent data generated and persisted successfully", result),
                HttpStatus.CREATED
        );
    }

    /**
     * Query database statistics for total and synthetic dependent data records.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DependentDataStatisticsResponse>> getDependentDataStatistics() {
        DependentDataStatisticsResponse stats = syntheticDataGenerator.getDependentDataStatistics();
        return ResponseEntity.ok(ApiResponse.success("Dependent data statistics retrieved successfully", stats));
    }
}
