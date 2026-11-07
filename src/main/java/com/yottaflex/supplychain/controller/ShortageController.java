package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.entity.MitigationOption;
import com.yottaflex.supplychain.entity.Shortage;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import com.yottaflex.supplychain.service.ShortageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing detected shortages and tactical mitigation options.
 */
@RestController
@RequestMapping("/api/v1")
public class ShortageController {

    private final ShortageService shortageService;

    public ShortageController(ShortageService shortageService) {
        this.shortageService = shortageService;
    }

    /**
     * Retrieve shortages with optional filtering by resolution status, severity, SKU, or location.
     */
    @GetMapping("/shortages")
    public ResponseEntity<ApiResponse<List<Shortage>>> getShortages(
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) ShortageSeverity severity,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) Long locationId) {
        List<Shortage> shortages = shortageService.getShortages(resolved, severity, skuId, locationId);
        return ResponseEntity.ok(ApiResponse.success("Shortages retrieved successfully", shortages));
    }

    /**
     * Retrieve a shortage by unique shortage code.
     */
    @GetMapping("/shortages/{shortageCode}")
    public ResponseEntity<ApiResponse<Shortage>> getShortageByCode(@PathVariable String shortageCode) {
        Shortage shortage = shortageService.getShortageByCode(shortageCode);
        return ResponseEntity.ok(ApiResponse.success("Shortage retrieved successfully", shortage));
    }

    /**
     * Mark a shortage as resolved.
     */
    @PatchMapping("/shortages/{shortageCode}/resolve")
    public ResponseEntity<ApiResponse<Shortage>> resolveShortage(@PathVariable String shortageCode) {
        Shortage resolved = shortageService.resolveShortage(shortageCode);
        return ResponseEntity.ok(ApiResponse.success("Shortage resolved successfully", resolved));
    }

    /**
     * Update the lifecycle status of a mitigation option (e.g. APPROVED, REJECTED, EXECUTED).
     * Supports both canonical /api/v1/mitigation-options/{optionCode}/status and /api/v1/shortages/mitigation-options/{optionCode}/status.
     */
    @PatchMapping({"/mitigation-options/{optionCode}/status", "/shortages/mitigation-options/{optionCode}/status"})
    public ResponseEntity<ApiResponse<MitigationOption>> updateMitigationStatus(
            @PathVariable String optionCode,
            @RequestParam MitigationStatus status) {
        MitigationOption updated = shortageService.updateMitigationStatus(optionCode, status);
        return ResponseEntity.ok(ApiResponse.success("Mitigation status updated successfully", updated));
    }
}

