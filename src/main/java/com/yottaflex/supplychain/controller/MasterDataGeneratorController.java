package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.MasterDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.MasterDataStatisticsResponse;
import com.yottaflex.supplychain.generator.SyntheticDataGenerator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing endpoints for synthetic master data generation,
 * previewing, and database statistics.
 */
@RestController
@RequestMapping("/api/v1/generator/master-data")
public class MasterDataGeneratorController {

    private final SyntheticDataGenerator syntheticDataGenerator;

    public MasterDataGeneratorController(SyntheticDataGenerator syntheticDataGenerator) {
        this.syntheticDataGenerator = syntheticDataGenerator;
    }

    /**
     * Preview synthetic master data without persisting to MySQL.
     * Guaranteed zero database mutations.
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<MasterDataPreviewResponse>> previewMasterData(
            @Valid @RequestBody(required = false) MasterDataGenerationRequest request) {
        MasterDataGenerationRequest effectiveRequest = (request != null) ? request : new MasterDataGenerationRequest();
        MasterDataPreviewResponse preview = syntheticDataGenerator.previewMasterData(effectiveRequest);
        return ResponseEntity.ok(ApiResponse.success("Master data preview generated successfully", preview));
    }

    /**
     * Generate synthetic master data and persist records into MySQL.
     * Safe duplicate prevention with sequence collision handling.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<MasterDataGenerationResult>> generateMasterData(
            @Valid @RequestBody(required = false) MasterDataGenerationRequest request) {
        MasterDataGenerationRequest effectiveRequest = (request != null) ? request : new MasterDataGenerationRequest();
        MasterDataGenerationResult result = syntheticDataGenerator.generateMasterData(effectiveRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Synthetic master data generated and persisted successfully", result),
                HttpStatus.CREATED
        );
    }

    /**
     * Query database statistics for total and synthetic master data records.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<MasterDataStatisticsResponse>> getMasterDataStatistics() {
        MasterDataStatisticsResponse stats = syntheticDataGenerator.getMasterDataStatistics();
        return ResponseEntity.ok(ApiResponse.success("Master data statistics retrieved successfully", stats));
    }
}

