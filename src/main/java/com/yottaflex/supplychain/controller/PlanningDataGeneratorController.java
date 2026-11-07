package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.PlanningDataStatisticsResponse;
import com.yottaflex.supplychain.generator.SyntheticDataGenerator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing endpoints for synthetic purchase orders and production plans
 * (Preview, Generate, and Statistics).
 */
@RestController
@RequestMapping(value = {
        "/api/v1/generator/operational-data",
        "/api/v1/generator/planning-data",
        "/api/v1/generator/orders-plans",
        "/api/v1/generator/order-plan-data"
})
public class PlanningDataGeneratorController {

    private final SyntheticDataGenerator syntheticDataGenerator;

    public PlanningDataGeneratorController(SyntheticDataGenerator syntheticDataGenerator) {
        this.syntheticDataGenerator = syntheticDataGenerator;
    }

    /**
     * Preview synthetic purchase orders and production plans in-memory.
     * Guaranteed zero database persistence (persisted: false).
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<PlanningDataPreviewResponse>> previewPlanningData(
            @Valid @RequestBody(required = false) PlanningDataGenerationRequest request) {
        PlanningDataGenerationRequest effectiveRequest = (request != null) ? request : new PlanningDataGenerationRequest();
        PlanningDataPreviewResponse preview = syntheticDataGenerator.previewPlanningData(effectiveRequest);
        return ResponseEntity.ok(ApiResponse.success("Planning data preview generated successfully", preview));
    }

    /**
     * Generate and persist synthetic purchase orders and production plans into MySQL.
     * Transactional, collision-safe, duplicate-preventing.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<PlanningDataGenerationResult>> generatePlanningData(
            @Valid @RequestBody(required = false) PlanningDataGenerationRequest request) {
        PlanningDataGenerationRequest effectiveRequest = (request != null) ? request : new PlanningDataGenerationRequest();
        PlanningDataGenerationResult result = syntheticDataGenerator.generatePlanningData(effectiveRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Synthetic planning data generated and persisted successfully", result),
                HttpStatus.CREATED
        );
    }

    /**
     * Query database statistics for total and synthetic purchase orders and production plans.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<PlanningDataStatisticsResponse>> getPlanningDataStatistics() {
        PlanningDataStatisticsResponse stats = syntheticDataGenerator.getPlanningDataStatistics();
        return ResponseEntity.ok(ApiResponse.success("Planning data statistics retrieved successfully", stats));
    }
}
