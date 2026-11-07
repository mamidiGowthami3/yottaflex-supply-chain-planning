package com.yottaflex.supplychain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yottaflex.supplychain.dto.generator.ShortageDetectionRequest;
import com.yottaflex.supplychain.dto.generator.ShortageGenerationResult;
import com.yottaflex.supplychain.dto.generator.ShortagePreviewResponse;
import com.yottaflex.supplychain.dto.generator.ShortagePreviewResponse.MitigationOptionPreviewItem;
import com.yottaflex.supplychain.dto.generator.ShortagePreviewResponse.ShortagePreviewItem;
import com.yottaflex.supplychain.dto.generator.ShortageStatisticsResponse;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.MitigationStrategy;
import com.yottaflex.supplychain.entity.enums.ShortageRootCause;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import com.yottaflex.supplychain.service.ShortageService;
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

@WebMvcTest(ShortageGeneratorController.class)
class ShortageGeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShortageService shortageService;

    @Test
    @DisplayName("POST /api/v1/generator/shortages/preview should return 200 OK with preview payload")
    void testPreviewShortagesSuccess() throws Exception {
        MitigationOptionPreviewItem mitItem = new MitigationOptionPreviewItem(
                "MIT-SYN-0001", MitigationStrategy.REALLOCATE_INVENTORY, "LOC-SYN-002", null,
                BigDecimal.valueOf(150.0), 3, 90.0, MitigationStatus.PROPOSED, "Transfer 100 units from LOC-SYN-002"
        );

        ShortagePreviewItem shortageItem = new ShortagePreviewItem(
                "SH-SYN-0001", "SKU-SYN-001", "Widget A", "LOC-SYN-001", "Plant Austin", LocationType.PLANT,
                LocalDate.now().plusDays(7), BigDecimal.valueOf(100.0), ShortageSeverity.CRITICAL, ShortageRootCause.DEMAND_SURGE,
                BigDecimal.valueOf(20.0), BigDecimal.ZERO, BigDecimal.valueOf(120.0), BigDecimal.valueOf(-100.0),
                "Test notes", List.of(mitItem)
        );

        ShortagePreviewResponse previewResponse = new ShortagePreviewResponse(
                20261022L, 4, List.of(shortageItem)
        );

        when(shortageService.previewShortages(any(ShortageDetectionRequest.class))).thenReturn(previewResponse);

        ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);

        mockMvc.perform(post("/api/v1/generator/shortages/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.persisted").value(false))
                .andExpect(jsonPath("$.data.seed").value(20261022))
                .andExpect(jsonPath("$.data.horizonWeeks").value(4))
                .andExpect(jsonPath("$.data.shortageCount").value(1))
                .andExpect(jsonPath("$.data.previewShortages[0].shortageCode").value("SH-SYN-0001"))
                .andExpect(jsonPath("$.data.previewShortages[0].mitigationOptions[0].strategy").value("REALLOCATE_INVENTORY"));
    }

    @Test
    @DisplayName("POST /api/v1/generator/operational-data/shortages/preview should also work via alias")
    void testPreviewShortagesAlias() throws Exception {
        ShortagePreviewResponse previewResponse = new ShortagePreviewResponse(
                20261022L, 4, List.of()
        );

        when(shortageService.previewShortages(any(ShortageDetectionRequest.class))).thenReturn(previewResponse);

        mockMvc.perform(post("/api/v1/generator/operational-data/shortages/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortageCount").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/generator/shortages/generate should return 201 Created with result")
    void testGenerateShortagesSuccess() throws Exception {
        ShortageGenerationResult result = new ShortageGenerationResult(
                20261022L, 2, 4, 150L,
                List.of("SHORTAGE: SH-SYN-0001"), List.of("MITIGATION: MIT-SYN-0001")
        );

        when(shortageService.generateShortages(any(ShortageDetectionRequest.class))).thenReturn(result);

        ShortageDetectionRequest request = new ShortageDetectionRequest(4, 50, 20261022L);

        mockMvc.perform(post("/api/v1/generator/shortages/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortagesSaved").value(2))
                .andExpect(jsonPath("$.data.mitigationsSaved").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/generator/shortages/statistics should return 200 OK with statistics")
    void testGetShortageStatisticsSuccess() throws Exception {
        ShortageStatisticsResponse stats = new ShortageStatisticsResponse(
                5L, 5L, 4L, 1L, 2L, 2L, 1L, 0L, 10L, 10L, 7L, 2L, 1L
        );

        when(shortageService.getShortageStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/generator/shortages/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalShortages").value(5))
                .andExpect(jsonPath("$.data.activeShortages").value(4))
                .andExpect(jsonPath("$.data.criticalSeverityShortages").value(2))
                .andExpect(jsonPath("$.data.totalMitigationOptions").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/generator/shortages/preview with invalid horizonWeeks should return 400 Bad Request")
    void testValidationFailureHorizonWeeks() throws Exception {
        String invalidPayload = "{\"horizonWeeks\": 0, \"maxShortages\": 50, \"seed\": 20261022}";

        mockMvc.perform(post("/api/v1/generator/shortages/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/generator/shortages/preview with invalid maxShortages should return 400 Bad Request")
    void testValidationFailureMaxShortages() throws Exception {
        String invalidPayload = "{\"horizonWeeks\": 4, \"maxShortages\": 300, \"seed\": 20261022}";

        mockMvc.perform(post("/api/v1/generator/shortages/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
