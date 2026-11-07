package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.HealthCheckResponse;
import com.yottaflex.supplychain.service.HealthCheckService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckService healthCheckService;

    @Test
    @DisplayName("GET /api/v1/health should return UP status and health details")
    void testCheckHealth() throws Exception {
        HealthCheckResponse mockHealth = new HealthCheckResponse();
        mockHealth.setStatus("UP");
        mockHealth.setApplicationName("yottaflex-supply-chain");
        mockHealth.setDatabaseStatus("CONNECTED");
        mockHealth.setDatabaseProduct("MySQL 8.0.46");
        mockHealth.setEntityTableCounts(Map.of("skus", 0L, "locations", 0L));

        when(healthCheckService.getSystemHealth()).thenReturn(mockHealth);

        mockMvc.perform(get("/api/v1/health")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.databaseStatus").value("CONNECTED"))
                .andExpect(jsonPath("$.data.databaseProduct").value("MySQL 8.0.46"));
    }

    @Test
    @DisplayName("GET /api/v1/health/ping should return lightweight ping response")
    void testPing() throws Exception {
        mockMvc.perform(get("/api/v1/health/ping")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}

