package com.yottaflex.supplychain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yottaflex.supplychain.dto.generator.DependentDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.DependentDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.DependentDataPreviewResponse.InventoryPreviewItem;
import com.yottaflex.supplychain.dto.generator.DependentDataStatisticsResponse;
import com.yottaflex.supplychain.entity.enums.InventoryStatus;
import com.yottaflex.supplychain.generator.SyntheticDataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DependentDataGeneratorController.class)
class DependentDataGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SyntheticDataGenerator syntheticDataGenerator;

    @Test
    @DisplayName("POST /api/v1/generator/dependent-data/preview should return 200 OK with preview payload")
    void testPreviewDependentDataSuccess() throws Exception {
        InventoryPreviewItem item = new InventoryPreviewItem(
                "SKU-SYN-001", "LOC-SYN-001",
                BigDecimal.valueOf(100.0), BigDecimal.valueOf(20.0), BigDecimal.valueOf(80.0),
                BigDecimal.valueOf(30.0), BigDecimal.valueOf(60.0),
                InventoryStatus.IN_STOCK, LocalDate.now()
        );

        DependentDataPreviewResponse mockResponse = new DependentDataPreviewResponse(
                20261022L,
                List.of(item),
                List.of(),
                List.of(),
                List.of()
        );

        when(syntheticDataGenerator.previewDependentData(any(DependentDataGenerationRequest.class))).thenReturn(mockResponse);

        DependentDataGenerationRequest request = new DependentDataGenerationRequest(1, 0, 0, 0, 20261022L);

        mockMvc.perform(post("/api/v1/generator/dependent-data/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.seed").value(20261022))
                .andExpect(jsonPath("$.data.inventoryCount").value(1))
                .andExpect(jsonPath("$.data.previewInventories[0].skuCode").value("SKU-SYN-001"))
                .andExpect(jsonPath("$.data.previewInventories[0].locationCode").value("LOC-SYN-001"))
                .andExpect(jsonPath("$.data.persisted").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/generator/dependent-data/generate should return 201 CREATED with persistence summary")
    void testGenerateDependentDataSuccess() throws Exception {
        DependentDataGenerationResult mockResult = new DependentDataGenerationResult(
                20261022L,
                20,
                50,
                15,
                15,
                120L,
                List.of("SKU: SKU-SYN-001 @ LOC: LOC-SYN-001 | OnHand: 500.00"),
                List.of("SKU: SKU-SYN-001 @ LOC: LOC-SYN-001 | Date: 2026-11-16"),
                List.of("SUP: SUP-SYN-001 -> SKU: SKU-SYN-001 | Monthly: 5000.00"),
                List.of("SUP: SUP-SYN-001 -> SKU: SKU-SYN-001 -> LOC: LOC-SYN-001 | Std: 10d")
        );

        when(syntheticDataGenerator.generateDependentData(any(DependentDataGenerationRequest.class))).thenReturn(mockResult);

        DependentDataGenerationRequest request = new DependentDataGenerationRequest(20, 50, 15, 15, 20261022L);

        mockMvc.perform(post("/api/v1/generator/dependent-data/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.inventoriesSaved").value(20))
                .andExpect(jsonPath("$.data.forecastDemandsSaved").value(50))
                .andExpect(jsonPath("$.data.supplierCapacitiesSaved").value(15))
                .andExpect(jsonPath("$.data.leadTimesSaved").value(15))
                .andExpect(jsonPath("$.data.totalRecordsSaved").value(100))
                .andExpect(jsonPath("$.data.executionDurationMs").value(120));
    }

    @Test
    @DisplayName("GET /api/v1/generator/dependent-data/statistics should return 200 OK with count statistics")
    void testGetStatisticsSuccess() throws Exception {
        DependentDataStatisticsResponse mockStats = new DependentDataStatisticsResponse(
                20L, 50L, 15L, 15L,
                20L, 50L, 15L, 15L
        );

        when(syntheticDataGenerator.getDependentDataStatistics()).thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/generator/dependent-data/statistics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalInventories").value(20))
                .andExpect(jsonPath("$.data.totalForecastDemands").value(50))
                .andExpect(jsonPath("$.data.totalSupplierCapacities").value(15))
                .andExpect(jsonPath("$.data.totalLeadTimes").value(15))
                .andExpect(jsonPath("$.data.totalDependentRecords").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/generator/dependent-data/generate with invalid counts should return 400 Bad Request")
    void testGenerateValidationFailure() throws Exception {
        // Exceeds max count of 500 for inventoryCount
        DependentDataGenerationRequest invalidRequest = new DependentDataGenerationRequest(600, 50, 15, 15, 20261022L);

        mockMvc.perform(post("/api/v1/generator/dependent-data/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors.inventoryCount").exists());
    }
}
