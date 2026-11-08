package com.yottaflex.supplychain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.MasterDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.MasterDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.MasterDataStatisticsResponse;
import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.generator.SyntheticDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MasterDataGeneratorController.class)
class MasterDataGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SyntheticDataGenerator syntheticDataGenerator;

    @Test
    @DisplayName("POST /api/v1/generator/master-data/preview should return 200 OK with preview payload")
    void testPreviewMasterDataSuccess() throws Exception {
        Location mockLoc = new Location("LOC-SYN-001", "Chicago Hub", LocationType.DISTRIBUTION_CENTER, "Chicago", "USA", BigDecimal.valueOf(50000));
        Supplier mockSup = new Supplier("SUP-SYN-001", "Apex Precision", "John Doe", "contact@example.com", "USA", 4.8, 96.0);
        SKU mockSku = new SKU("SKU-SYN-001", "Microcontroller", "Electronics", BigDecimal.valueOf(25), BigDecimal.valueOf(45), 50, 20, 7);

        MasterDataPreviewResponse mockResponse = new MasterDataPreviewResponse(
                20261022L,
                List.of(mockLoc),
                List.of(mockSup),
                List.of(mockSku)
        );

        when(syntheticDataGenerator.previewMasterData(any(MasterDataGenerationRequest.class))).thenReturn(mockResponse);

        MasterDataGenerationRequest request = new MasterDataGenerationRequest(1, 1, 1, 20261022L);

        mockMvc.perform(post("/api/v1/generator/master-data/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.seed").value(20261022))
                .andExpect(jsonPath("$.data.locationCount").value(1))
                .andExpect(jsonPath("$.data.supplierCount").value(1))
                .andExpect(jsonPath("$.data.skuCount").value(1))
                .andExpect(jsonPath("$.data.previewLocations[0].locationCode").value("LOC-SYN-001"))
                .andExpect(jsonPath("$.data.previewSuppliers[0].supplierCode").value("SUP-SYN-001"))
                .andExpect(jsonPath("$.data.previewSkus[0].skuCode").value("SKU-SYN-001"))
                .andExpect(jsonPath("$.data.persisted").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/generator/master-data/generate should return 201 CREATED with persistence summary")
    void testGenerateMasterDataSuccess() throws Exception {
        MasterDataGenerationResult mockResult = new MasterDataGenerationResult(
                20261022L,
                3,
                5,
                10,
                45L,
                List.of("LOC-SYN-001", "LOC-SYN-002", "LOC-SYN-003"),
                List.of("SUP-SYN-001", "SUP-SYN-002", "SUP-SYN-003", "SUP-SYN-004", "SUP-SYN-005"),
                List.of("SKU-SYN-001", "SKU-SYN-002")
        );

        when(syntheticDataGenerator.generateMasterData(any(MasterDataGenerationRequest.class))).thenReturn(mockResult);

        MasterDataGenerationRequest request = new MasterDataGenerationRequest(3, 5, 10, 20261022L);

        mockMvc.perform(post("/api/v1/generator/master-data/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.locationsSaved").value(3))
                .andExpect(jsonPath("$.data.suppliersSaved").value(5))
                .andExpect(jsonPath("$.data.skusSaved").value(10))
                .andExpect(jsonPath("$.data.totalRecordsSaved").value(18))
                .andExpect(jsonPath("$.data.generatedLocationCodes").isArray())
                .andExpect(jsonPath("$.data.executionDurationMs").value(45));
    }

    @Test
    @DisplayName("GET /api/v1/generator/master-data/statistics should return 200 OK with count statistics")
    void testGetStatisticsSuccess() throws Exception {
        MasterDataStatisticsResponse mockStats = new MasterDataStatisticsResponse(
                3L, 3L, 3L, 0L, 0L, 0L
        );

        when(syntheticDataGenerator.getMasterDataStatistics()).thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/generator/master-data/statistics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalLocations").value(3))
                .andExpect(jsonPath("$.data.totalSuppliers").value(3))
                .andExpect(jsonPath("$.data.totalSkus").value(3))
                .andExpect(jsonPath("$.data.totalSyntheticLocations").value(0))
                .andExpect(jsonPath("$.data.totalMasterDataRecords").value(9));
    }

    @Test
    @DisplayName("POST /api/v1/generator/master-data/generate with invalid counts should return 400 Bad Request")
    void testGenerateValidationFailure() throws Exception {
        // Exceeds max count of 20 for locationCount
        MasterDataGenerationRequest invalidRequest = new MasterDataGenerationRequest(50, 5, 10, 20261022L);

        mockMvc.perform(post("/api/v1/generator/master-data/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors.locationCount").exists());
    }
}
