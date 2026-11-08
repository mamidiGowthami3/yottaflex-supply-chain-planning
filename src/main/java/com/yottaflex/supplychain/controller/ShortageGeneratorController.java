package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.dto.generator.ShortageDetectionRequest;
import com.yottaflex.supplychain.dto.generator.ShortageGenerationResult;
import com.yottaflex.supplychain.dto.generator.ShortagePreviewResponse;
import com.yottaflex.supplychain.dto.generator.ShortageStatisticsResponse;
import com.yottaflex.supplychain.service.ShortageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing endpoints for shortage detection, tactical mitigations preview,
 * synthetic operational generation, and planning engine statistics.
 */
@RestController
@RequestMapping(value = {
        "/api/v1/generator/shortages",
        "/api/v1/generator/operational-data/shortages",
        "/api/v1/generator/shortage-mitigation"
})
public class ShortageGeneratorController {

    private final ShortageService shortageService;

    public ShortageGeneratorController(ShortageService shortageService) {
        this.shortageService = shortageService;
    }

    /**
     * Preview time-phased shortage detection and tactical mitigations in-memory.
     * Guaranteed zero database mutations (persisted: false).
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<ShortagePreviewResponse>> previewShortages(
            @Valid @RequestBody(required = false) ShortageDetectionRequest request) {
        ShortageDetectionRequest effectiveRequest = (request != null) ? request : new ShortageDetectionRequest();
        ShortagePreviewResponse preview = shortageService.previewShortages(effectiveRequest);
        return ResponseEntity.ok(ApiResponse.success("Shortage detection preview generated successfully", preview));
    }

    /**
     * Detect time-phased shortages, generate ranked tactical mitigations, and persist into MySQL.
     * Transactional, collision-safe, duplicate-preventing.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ShortageGenerationResult>> generateShortages(
            @Valid @RequestBody(required = false) ShortageDetectionRequest request) {
        ShortageDetectionRequest effectiveRequest = (request != null) ? request : new ShortageDetectionRequest();
        ShortageGenerationResult result = shortageService.generateShortages(effectiveRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Shortage detection and mitigations generated and persisted successfully", result),
                HttpStatus.CREATED
        );
    }

    /**
     * Query database statistics for detected shortages and tactical mitigation options.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ShortageStatisticsResponse>> getShortageStatistics() {
        ShortageStatisticsResponse stats = shortageService.getShortageStatistics();
        return ResponseEntity.ok(ApiResponse.success("Shortage statistics retrieved successfully", stats));
    }
}

