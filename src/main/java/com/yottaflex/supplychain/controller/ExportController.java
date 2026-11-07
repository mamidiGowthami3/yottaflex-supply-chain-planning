package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.service.DatasetExportService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * REST controller for multi-format export of synthetic supply chain datasets,
 * benchmark files, and cryptographic verification manifests.
 */
@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final DatasetExportService datasetExportService;
    private final Path defaultExportDir = Paths.get("exports");

    public ExportController(DatasetExportService datasetExportService) {
        this.datasetExportService = datasetExportService;
    }

    /**
     * Trigger generation and export of the complete YottaFlex challenge benchmark dataset.
     * Scale: 50 Locations, 500 Suppliers, 3,000 SKUs, 52 weeks Forecast Demand, 10,000 Inbound POs.
     * Outputs CSV, JSON, SQL inserts, and manifest.json with SHA-256 checksums.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<DatasetExportService.ExportManifest>> generateChallengeDataset(
            @RequestParam(defaultValue = "20261022") Long seed) throws IOException {
        DatasetExportService.ExportManifest manifest = datasetExportService.generateAndExportChallengeDataset(seed, defaultExportDir);
        return ResponseEntity.ok(ApiResponse.success("Challenge benchmark dataset generated and exported successfully", manifest));
    }

    /**
     * Retrieve the current export manifest containing record counts, file sizes, and SHA-256 checksums.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DatasetExportService.ExportManifest>> getExportSummary() {
        Optional<DatasetExportService.ExportManifest> manifestOpt = datasetExportService.getManifest(defaultExportDir);
        return manifestOpt
                .map(manifest -> ResponseEntity.ok(ApiResponse.success("Export manifest retrieved successfully", manifest)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(ApiResponse.error("No export manifest found. Run POST /api/v1/export/generate first.")));
    }

    /**
     * Download an exported file by format folder (csv, json, sql, root) and filename.
     */
    @GetMapping("/download/{format}/{filename}")
    public ResponseEntity<Resource> downloadExportFile(
            @PathVariable String format,
            @PathVariable String filename) {
        Path filePath = datasetExportService.resolveExportFile(defaultExportDir, format, filename);
        Resource resource = new FileSystemResource(filePath.toFile());

        String mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (filename.endsWith(".csv")) {
            mediaType = "text/csv";
        } else if (filename.endsWith(".json")) {
            mediaType = MediaType.APPLICATION_JSON_VALUE;
        } else if (filename.endsWith(".sql")) {
            mediaType = "application/sql";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, mediaType)
                .body(resource);
    }
}
