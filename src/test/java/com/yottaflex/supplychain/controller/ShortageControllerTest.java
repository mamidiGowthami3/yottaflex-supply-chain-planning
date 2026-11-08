package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.entity.Location;
import com.yottaflex.supplychain.entity.MitigationOption;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.entity.Shortage;
import com.yottaflex.supplychain.entity.enums.LocationType;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.MitigationStrategy;
import com.yottaflex.supplychain.entity.enums.ShortageRootCause;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.service.ShortageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortageController.class)
class ShortageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortageService shortageService;

    @Test
    @DisplayName("GET /api/v1/shortages should return 200 OK with shortage list")
    void testGetShortages() throws Exception {
        SKU sku = new SKU("SKU-SYN-001", "Widget A", "CAT1", BigDecimal.valueOf(10.0), BigDecimal.valueOf(15.0), 10, 5, 3);
        Location loc = new Location("LOC-SYN-001", "Austin Plant", LocationType.PLANT, "Austin", "USA", BigDecimal.valueOf(10000.0));
        Shortage shortage = new Shortage("SH-SYN-0001", sku, loc, LocalDate.now().plusDays(5),
                BigDecimal.valueOf(50.0), ShortageSeverity.HIGH, ShortageRootCause.DEMAND_SURGE);

        when(shortageService.getShortages(any(), any(), any(), any())).thenReturn(List.of(shortage));

        mockMvc.perform(get("/api/v1/shortages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].shortageCode").value("SH-SYN-0001"))
                .andExpect(jsonPath("$.data[0].shortageQuantity").value(50.0));
    }

    @Test
    @DisplayName("GET /api/v1/shortages/{shortageCode} should return 200 OK")
    void testGetShortageByCode() throws Exception {
        SKU sku = new SKU("SKU-SYN-001", "Widget A", "CAT1", BigDecimal.valueOf(10.0), BigDecimal.valueOf(15.0), 10, 5, 3);
        Location loc = new Location("LOC-SYN-001", "Austin Plant", LocationType.PLANT, "Austin", "USA", BigDecimal.valueOf(10000.0));
        Shortage shortage = new Shortage("SH-SYN-0001", sku, loc, LocalDate.now().plusDays(5),
                BigDecimal.valueOf(50.0), ShortageSeverity.HIGH, ShortageRootCause.DEMAND_SURGE);

        when(shortageService.getShortageByCode("SH-SYN-0001")).thenReturn(shortage);

        mockMvc.perform(get("/api/v1/shortages/SH-SYN-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shortageCode").value("SH-SYN-0001"));
    }

    @Test
    @DisplayName("GET /api/v1/shortages/{shortageCode} when not found should return 404")
    void testGetShortageNotFound() throws Exception {
        when(shortageService.getShortageByCode("SH-NONEXISTENT"))
                .thenThrow(new ResourceNotFoundException("Shortage not found"));

        mockMvc.perform(get("/api/v1/shortages/SH-NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PATCH /api/v1/shortages/{shortageCode}/resolve should mark resolved")
    void testResolveShortage() throws Exception {
        SKU sku = new SKU("SKU-SYN-001", "Widget A", "CAT1", BigDecimal.valueOf(10.0), BigDecimal.valueOf(15.0), 10, 5, 3);
        Location loc = new Location("LOC-SYN-001", "Austin Plant", LocationType.PLANT, "Austin", "USA", BigDecimal.valueOf(10000.0));
        Shortage shortage = new Shortage("SH-SYN-0001", sku, loc, LocalDate.now().plusDays(5),
                BigDecimal.valueOf(50.0), ShortageSeverity.HIGH, ShortageRootCause.DEMAND_SURGE);
        shortage.setResolved(true);
        shortage.setResolutionDate(LocalDate.now());

        when(shortageService.resolveShortage("SH-SYN-0001")).thenReturn(shortage);

        mockMvc.perform(patch("/api/v1/shortages/SH-SYN-0001/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.resolved").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/mitigation-options/{optionCode}/status should update status")
    void testUpdateMitigationStatus() throws Exception {
        MitigationOption option = new MitigationOption("MIT-SYN-0001", null, MitigationStrategy.REALLOCATE_INVENTORY,
                BigDecimal.valueOf(100.0), 2, 90.0);
        option.setStatus(MitigationStatus.APPROVED);

        when(shortageService.updateMitigationStatus(eq("MIT-SYN-0001"), eq(MitigationStatus.APPROVED)))
                .thenReturn(option);

        mockMvc.perform(patch("/api/v1/mitigation-options/MIT-SYN-0001/status?status=APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}
