package com.yottaflex.supplychain.service;

import com.yottaflex.supplychain.dto.generator.ShortageDetectionRequest;
import com.yottaflex.supplychain.dto.generator.ShortageGenerationResult;
import com.yottaflex.supplychain.dto.generator.ShortagePreviewResponse;
import com.yottaflex.supplychain.dto.generator.ShortageStatisticsResponse;
import com.yottaflex.supplychain.engine.SupplyChainPlanningEngine;
import com.yottaflex.supplychain.entity.MitigationOption;
import com.yottaflex.supplychain.entity.Shortage;
import com.yottaflex.supplychain.entity.enums.MitigationStatus;
import com.yottaflex.supplychain.entity.enums.ShortageSeverity;
import com.yottaflex.supplychain.exception.ResourceNotFoundException;
import com.yottaflex.supplychain.repository.MitigationOptionRepository;
import com.yottaflex.supplychain.repository.ShortageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing shortage detection, tactical mitigations, and resolution workflows.
 */
@Service
public class ShortageService {

    private static final Logger log = LoggerFactory.getLogger(ShortageService.class);

    private final SupplyChainPlanningEngine planningEngine;
    private final ShortageRepository shortageRepository;
    private final MitigationOptionRepository mitigationOptionRepository;

    public ShortageService(SupplyChainPlanningEngine planningEngine,
                           ShortageRepository shortageRepository,
                           MitigationOptionRepository mitigationOptionRepository) {
        this.planningEngine = planningEngine;
        this.shortageRepository = shortageRepository;
        this.mitigationOptionRepository = mitigationOptionRepository;
    }

    @Transactional(readOnly = true)
    public ShortagePreviewResponse previewShortages(ShortageDetectionRequest request) {
        return planningEngine.previewShortages(request);
    }

    @Transactional
    public ShortageGenerationResult generateShortages(ShortageDetectionRequest request) {
        return planningEngine.detectAndMitigateShortages(request);
    }

    @Transactional(readOnly = true)
    public ShortageStatisticsResponse getShortageStatistics() {
        return planningEngine.getShortageStatistics();
    }

    @Transactional(readOnly = true)
    public List<Shortage> getShortages(Boolean resolved, ShortageSeverity severity, Long skuId, Long locationId) {
        List<Shortage> all = shortageRepository.findAllWithDetails();
        return all.stream()
                .filter(s -> resolved == null || s.getResolved().equals(resolved))
                .filter(s -> severity == null || s.getSeverity() == severity)
                .filter(s -> skuId == null || (s.getSku() != null && s.getSku().getId().equals(skuId)))
                .filter(s -> locationId == null || (s.getLocation() != null && s.getLocation().getId().equals(locationId)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Shortage getShortageByCode(String shortageCode) {
        return shortageRepository.findByShortageCodeWithDetails(shortageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Shortage not found with code: " + shortageCode));
    }

    @Transactional
    public Shortage resolveShortage(String shortageCode) {
        Shortage shortage = shortageRepository.findByShortageCodeWithDetails(shortageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Shortage not found with code: " + shortageCode));

        shortage.setResolved(true);
        shortage.setResolutionDate(LocalDate.now());
        log.info("Shortage {} marked as resolved on {}", shortageCode, shortage.getResolutionDate());
        return shortageRepository.save(shortage);
    }

    @Transactional
    public MitigationOption updateMitigationStatus(String optionCode, MitigationStatus newStatus) {
        MitigationOption option = mitigationOptionRepository.findByOptionCodeWithDetails(optionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mitigation option not found with code: " + optionCode));

        option.setStatus(newStatus);
        log.info("Mitigation option {} transitioned to status {}", optionCode, newStatus);
        return mitigationOptionRepository.save(option);
    }
}

