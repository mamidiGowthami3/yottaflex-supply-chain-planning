package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result DTO returned after persisting detected shortages and mitigation options to MySQL.
 */
public class ShortageGenerationResult {

    private long seed;
    private LocalDate asOfDate;
    private int shortagesSaved;
    private int mitigationsSaved;
    private int totalRecordsSaved;
    private long executionDurationMs;

    private List<String> savedShortageSummaries = new ArrayList<>();
    private List<String> savedMitigationSummaries = new ArrayList<>();
    private LocalDateTime completedAt;

    public ShortageGenerationResult() {
        this.completedAt = LocalDateTime.now();
    }

    public ShortageGenerationResult(long seed,
                                    int shortagesSaved,
                                    int mitigationsSaved,
                                    long executionDurationMs,
                                    List<String> savedShortageSummaries,
                                    List<String> savedMitigationSummaries) {
        this(seed, null, shortagesSaved, mitigationsSaved, executionDurationMs, savedShortageSummaries, savedMitigationSummaries);
    }

    public ShortageGenerationResult(long seed,
                                    LocalDate asOfDate,
                                    int shortagesSaved,
                                    int mitigationsSaved,
                                    long executionDurationMs,
                                    List<String> savedShortageSummaries,
                                    List<String> savedMitigationSummaries) {
        this.seed = seed;
        this.asOfDate = asOfDate;
        this.shortagesSaved = shortagesSaved;
        this.mitigationsSaved = mitigationsSaved;
        this.totalRecordsSaved = shortagesSaved + mitigationsSaved;
        this.executionDurationMs = executionDurationMs;
        this.savedShortageSummaries = savedShortageSummaries != null ? savedShortageSummaries : new ArrayList<>();
        this.savedMitigationSummaries = savedMitigationSummaries != null ? savedMitigationSummaries : new ArrayList<>();
        this.completedAt = LocalDateTime.now();
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }

    public int getShortagesSaved() {
        return shortagesSaved;
    }

    public void setShortagesSaved(int shortagesSaved) {
        this.shortagesSaved = shortagesSaved;
    }

    public int getMitigationsSaved() {
        return mitigationsSaved;
    }

    public void setMitigationsSaved(int mitigationsSaved) {
        this.mitigationsSaved = mitigationsSaved;
    }

    public int getTotalRecordsSaved() {
        return totalRecordsSaved;
    }

    public void setTotalRecordsSaved(int totalRecordsSaved) {
        this.totalRecordsSaved = totalRecordsSaved;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public List<String> getSavedShortageSummaries() {
        return savedShortageSummaries;
    }

    public void setSavedShortageSummaries(List<String> savedShortageSummaries) {
        this.savedShortageSummaries = savedShortageSummaries;
    }

    public List<String> getSavedMitigationSummaries() {
        return savedMitigationSummaries;
    }

    public void setSavedMitigationSummaries(List<String> savedMitigationSummaries) {
        this.savedMitigationSummaries = savedMitigationSummaries;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

