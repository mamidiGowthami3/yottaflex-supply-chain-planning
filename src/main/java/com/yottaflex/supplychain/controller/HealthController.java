package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.dto.HealthCheckResponse;
import com.yottaflex.supplychain.service.HealthCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller providing health diagnostics and monitoring for the YottaFlex system.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthCheckResponse>> checkHealth() {
        HealthCheckResponse health = healthCheckService.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.success("YottaFlex system health status check successful", health));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        return ResponseEntity.ok(ApiResponse.success("Ping successful", Map.of(
                "status", "UP",
                "message", "YottaFlex Supply Chain Planning System is alive and reachable"
        )));
    }
}

