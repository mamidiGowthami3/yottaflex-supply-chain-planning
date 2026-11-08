package com.yottaflex.supplychain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationRequest;
import com.yottaflex.supplychain.dto.generator.PlanningDataGenerationResult;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse.ProductionPlanPreviewItem;
import com.yottaflex.supplychain.dto.generator.PlanningDataPreviewResponse.PurchaseOrderPreviewItem;
import com.yottaflex.supplychain.dto.generator.PlanningDataStatisticsResponse;
import com.yottaflex.supplychain.entity.enums.ProductionPlanStatus;
import com.yottaflex.supplychain.entity.enums.PurchaseOrderStatus;
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

@WebMvcTest(PlanningDataGeneratorController.class)
class PlanningDataGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SyntheticDataGenerator syntheticDataGenerator;

    @Test
    @DisplayName("POST /api/v1/generator/operational-data/preview should return 200 OK with preview payload")
    void testPreviewPlanningDataSuccess() throws Exception {
        PurchaseOrderPreviewItem poItem = new PurchaseOrderPreviewItem(
                "PO-SYN-0001", "SUP-SYN-001", "SKU-SYN-001", "LOC-SYN-001",
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), null,
                BigDecimal.valueOf(150.0), BigDecimal.ZERO, BigDecimal.valueOf(25.0), BigDecimal.valueOf(3750.0),
                PurchaseOrderStatus.IN_TRANSIT, "Test PO"
        );

        ProductionPlanPreviewItem planItem = new ProductionPlanPreviewItem(
                "PP-SYN-0001", "SKU-SYN-001", "LOC-SYN-001",
                LocalDate.now(), LocalDate.now().plusDays(14),
                BigDecimal.valueOf(500.0), BigDecimal.ZERO, 85.5,
                ProductionPlanStatus.SCHEDULED, "Test Plan"
        );

        PlanningDataPreviewResponse mockResponse = new PlanningDataPreviewResponse(
                20261022L,
                List.of(poItem),
                List.of(planItem)
        );

        when(syntheticDataGenerator.previewPlanningData(any(PlanningDataGenerationRequest.class))).thenReturn(mockResponse);

        PlanningDataGenerationRequest request = new PlanningDataGenerationRequest(1, 1, 20261022L);

        // Test operational-data endpoint
        mockMvc.perform(post("/api/v1/generator/operational-data/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.seed").value(20261022))
                .andExpect(jsonPath("$.data.purchaseOrderCount").value(1))
                .andExpect(jsonPath("$.data.productionPlanCount").value(1))
                .andExpect(jsonPath("$.data.previewPurchaseOrders[0].poNumber").value("PO-SYN-0001"))
                .andExpect(jsonPath("$.data.previewProductionPlans[0].planCode").value("PP-SYN-0001"))
                .andExpect(jsonPath("$.data.persisted").value(false));

        // Test planning-data alias endpoint
        mockMvc.perform(post("/api/v1/generator/planning-data/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.previewProductionPlans[0].planCode").value("PP-SYN-0001"));
    }

    @Test
    @DisplayName("POST /api/v1/generator/operational-data/generate should return 201 CREATED with persistence summary")
    void testGeneratePlanningDataSuccess() throws Exception {
        PlanningDataGenerationResult mockResult = new PlanningDataGenerationResult(
                20261022L,
                20,
                10,
                85L,
                List.of("PO: PO-SYN-0001 | SUP: SUP-SYN-001 -> SKU: SKU-SYN-001 -> LOC: LOC-SYN-001 | Qty: 150.00"),
                List.of("PLAN: PP-SYN-0001 | SKU: SKU-SYN-001 @ LOC: LOC-SYN-001 | Planned: 500.00")
        );

        when(syntheticDataGenerator.generatePlanningData(any(PlanningDataGenerationRequest.class))).thenReturn(mockResult);

        PlanningDataGenerationRequest request = new PlanningDataGenerationRequest(20, 10, 20261022L);

        mockMvc.perform(post("/api/v1/generator/operational-data/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.purchaseOrdersSaved").value(20))
                .andExpect(jsonPath("$.data.productionPlansSaved").value(10))
                .andExpect(jsonPath("$.data.totalRecordsSaved").value(30))
                .andExpect(jsonPath("$.data.executionDurationMs").value(85));
    }

    @Test
    @DisplayName("GET /api/v1/generator/operational-data/statistics should return 200 OK with count statistics")
    void testGetStatisticsSuccess() throws Exception {
        PlanningDataStatisticsResponse mockStats = new PlanningDataStatisticsResponse(
                35L, 25L, 20L, 10L
        );

        when(syntheticDataGenerator.getPlanningDataStatistics()).thenReturn(mockStats);

        mockMvc.perform(get("/api/v1/generator/operational-data/statistics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPurchaseOrders").value(35))
                .andExpect(jsonPath("$.data.totalProductionPlans").value(25))
                .andExpect(jsonPath("$.data.totalSyntheticPurchaseOrders").value(20))
                .andExpect(jsonPath("$.data.totalSyntheticProductionPlans").value(10))
                .andExpect(jsonPath("$.data.totalPlanningRecords").value(60))
                .andExpect(jsonPath("$.data.totalSyntheticPlanningRecords").value(30));
    }

    @Test
    @DisplayName("POST /api/v1/generator/operational-data/generate with invalid counts should return 400 Bad Request")
    void testGenerateValidationFailure() throws Exception {
        // Exceeds max count of 500 for PO and 300 for Plan
        PlanningDataGenerationRequest invalidRequest = new PlanningDataGenerationRequest(501, 301, 20261022L);

        mockMvc.perform(post("/api/v1/generator/operational-data/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors.purchaseOrderCount").exists())
                .andExpect(jsonPath("$.validationErrors.productionPlanCount").exists());
    }
}
