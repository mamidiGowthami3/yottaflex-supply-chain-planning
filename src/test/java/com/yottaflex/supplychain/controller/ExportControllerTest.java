package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.service.DatasetExportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetExportService datasetExportService;

    private DatasetExportService.ExportManifest createMockManifest() {
        return new DatasetExportService.ExportManifest(
                "YottaFlex Supply Chain System",
                "1.0.0-SNAPSHOT",
                "2026-11-07",
                20261022L,
                50,
                500,
                3000,
                52,
                5200,
                10000,
                3000,
                1500,
                1500,
                500,
                100,
                200,
                Map.of("csv/locations.csv", "a1b2c3d4e5f6"),
                Map.of("csv/locations.csv", 4096L),
                Map.of("locations", "50 facilities")
        );
    }

    @Test
    @DisplayName("POST /api/v1/export/generate should generate challenge dataset and return 200 OK")
    void testGenerateChallengeDataset() throws Exception {
        DatasetExportService.ExportManifest manifest = createMockManifest();
        when(datasetExportService.generateAndExportChallengeDataset(anyLong(), any())).thenReturn(manifest);

        mockMvc.perform(post("/api/v1/export/generate")
                        .param("seed", "20261022")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.skuCount").value(3000))
                .andExpect(jsonPath("$.data.locationCount").value(50))
                .andExpect(jsonPath("$.data.supplierCount").value(500))
                .andExpect(jsonPath("$.data.purchaseOrderCount").value(10000))
                .andExpect(jsonPath("$.data.forecastDemandWeeks").value(52));
    }

    @Test
    @DisplayName("GET /api/v1/export/summary should return 200 OK when manifest exists")
    void testGetExportSummaryExists() throws Exception {
        DatasetExportService.ExportManifest manifest = createMockManifest();
        when(datasetExportService.getManifest(any())).thenReturn(Optional.of(manifest));

        mockMvc.perform(get("/api/v1/export/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purchaseOrderCount").value(10000));
    }

    @Test
    @DisplayName("GET /api/v1/export/summary should return 404 when manifest is absent")
    void testGetExportSummaryNotFound() throws Exception {
        when(datasetExportService.getManifest(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/export/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/export/download/{format}/{filename} should stream resource")
    void testDownloadExportFile() throws Exception {
        Path tempFile = Files.createTempFile("test_export", ".csv");
        Files.writeString(tempFile, "col1,col2\nval1,val2\n");

        when(datasetExportService.resolveExportFile(any(), any(), any())).thenReturn(tempFile);

        mockMvc.perform(get("/api/v1/export/download/csv/locations.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"locations.csv\""))
                .andExpect(content().string("col1,col2\nval1,val2\n"));

        Files.deleteIfExists(tempFile);
    }
}

