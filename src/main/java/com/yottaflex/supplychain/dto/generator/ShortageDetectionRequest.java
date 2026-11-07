package com.yottaflex.supplychain.dto.generator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload for time-phased shortage detection and tactical mitigation generation.
 */
public class ShortageDetectionRequest {

    public static final int DEFAULT_HORIZON_WEEKS = 4;
    public static final int MAX_HORIZON_WEEKS = 12;
    public static final int DEFAULT_MAX_SHORTAGES = 50;
    public static final int MAX_SHORTAGES_LIMIT = 200;
    public static final long DEFAULT_SEED = 20261022L;

    @NotNull(message = "Horizon weeks is required")
    @Min(value = 1, message = "Horizon weeks must be at least 1")
    @Max(value = MAX_HORIZON_WEEKS, message = "Horizon weeks cannot exceed 12")
    private Integer horizonWeeks = DEFAULT_HORIZON_WEEKS;

    @NotNull(message = "Maximum shortages count is required")
    @Min(value = 1, message = "Maximum shortages count must be at least 1")
    @Max(value = MAX_SHORTAGES_LIMIT, message = "Maximum shortages count cannot exceed 200")
    private Integer maxShortages = DEFAULT_MAX_SHORTAGES;

    @NotNull(message = "Random seed is required")
    private Long seed = DEFAULT_SEED;

    /**
     * Optional planning as-of date. If omitted, engine uses current date (or earliest forecast date).
     */
    private LocalDate asOfDate;

    public ShortageDetectionRequest() {
    }

    public ShortageDetectionRequest(Integer horizonWeeks, Integer maxShortages, Long seed) {
        this(horizonWeeks, maxShortages, seed, null);
    }

    public ShortageDetectionRequest(Integer horizonWeeks, Integer maxShortages, Long seed, LocalDate asOfDate) {
        this.horizonWeeks = horizonWeeks != null ? horizonWeeks : DEFAULT_HORIZON_WEEKS;
        this.maxShortages = maxShortages != null ? maxShortages : DEFAULT_MAX_SHORTAGES;
        this.seed = seed != null ? seed : DEFAULT_SEED;
        this.asOfDate = asOfDate;
    }

    public Integer getHorizonWeeks() {
        return horizonWeeks;
    }

    public void setHorizonWeeks(Integer horizonWeeks) {
        this.horizonWeeks = horizonWeeks;
    }

    public Integer getMaxShortages() {
        return maxShortages;
    }

    public void setMaxShortages(Integer maxShortages) {
        this.maxShortages = maxShortages;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(LocalDate asOfDate) {
        this.asOfDate = asOfDate;
    }
}

