package com.yottaflex.supplychain.dto.generator;

import java.time.LocalDateTime;

/**
 * Statistics DTO reporting current shortage events and mitigation options counts.
 */
public class ShortageStatisticsResponse {

    private long totalShortages;
    private long syntheticShortages;
    private long activeShortages;
    private long resolvedShortages;
    private long criticalSeverityShortages;
    private long highSeverityShortages;
    private long mediumSeverityShortages;
    private long lowSeverityShortages;

    private long totalMitigationOptions;
    private long syntheticMitigationOptions;
    private long proposedMitigations;
    private long approvedMitigations;
    private long executedMitigations;

    private LocalDateTime timestamp;

    public ShortageStatisticsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ShortageStatisticsResponse(long totalShortages,
                                      long syntheticShortages,
                                      long activeShortages,
                                      long resolvedShortages,
                                      long criticalSeverityShortages,
                                      long highSeverityShortages,
                                      long mediumSeverityShortages,
                                      long lowSeverityShortages,
                                      long totalMitigationOptions,
                                      long syntheticMitigationOptions,
                                      long proposedMitigations,
                                      long approvedMitigations,
                                      long executedMitigations) {
        this.totalShortages = totalShortages;
        this.syntheticShortages = syntheticShortages;
        this.activeShortages = activeShortages;
        this.resolvedShortages = resolvedShortages;
        this.criticalSeverityShortages = criticalSeverityShortages;
        this.highSeverityShortages = highSeverityShortages;
        this.mediumSeverityShortages = mediumSeverityShortages;
        this.lowSeverityShortages = lowSeverityShortages;
        this.totalMitigationOptions = totalMitigationOptions;
        this.syntheticMitigationOptions = syntheticMitigationOptions;
        this.proposedMitigations = proposedMitigations;
        this.approvedMitigations = approvedMitigations;
        this.executedMitigations = executedMitigations;
        this.timestamp = LocalDateTime.now();
    }

    public long getTotalShortages() {
        return totalShortages;
    }

    public void setTotalShortages(long totalShortages) {
        this.totalShortages = totalShortages;
    }

    public long getSyntheticShortages() {
        return syntheticShortages;
    }

    public void setSyntheticShortages(long syntheticShortages) {
        this.syntheticShortages = syntheticShortages;
    }

    public long getActiveShortages() {
        return activeShortages;
    }

    public void setActiveShortages(long activeShortages) {
        this.activeShortages = activeShortages;
    }

    public long getResolvedShortages() {
        return resolvedShortages;
    }

    public void setResolvedShortages(long resolvedShortages) {
        this.resolvedShortages = resolvedShortages;
    }

    public long getCriticalSeverityShortages() {
        return criticalSeverityShortages;
    }

    public void setCriticalSeverityShortages(long criticalSeverityShortages) {
        this.criticalSeverityShortages = criticalSeverityShortages;
    }

    public long getHighSeverityShortages() {
        return highSeverityShortages;
    }

    public void setHighSeverityShortages(long highSeverityShortages) {
        this.highSeverityShortages = highSeverityShortages;
    }

    public long getMediumSeverityShortages() {
        return mediumSeverityShortages;
    }

    public void setMediumSeverityShortages(long mediumSeverityShortages) {
        this.mediumSeverityShortages = mediumSeverityShortages;
    }

    public long getLowSeverityShortages() {
        return lowSeverityShortages;
    }

    public void setLowSeverityShortages(long lowSeverityShortages) {
        this.lowSeverityShortages = lowSeverityShortages;
    }

    public long getTotalMitigationOptions() {
        return totalMitigationOptions;
    }

    public void setTotalMitigationOptions(long totalMitigationOptions) {
        this.totalMitigationOptions = totalMitigationOptions;
    }

    public long getSyntheticMitigationOptions() {
        return syntheticMitigationOptions;
    }

    public void setSyntheticMitigationOptions(long syntheticMitigationOptions) {
        this.syntheticMitigationOptions = syntheticMitigationOptions;
    }

    public long getProposedMitigations() {
        return proposedMitigations;
    }

    public void setProposedMitigations(long proposedMitigations) {
        this.proposedMitigations = proposedMitigations;
    }

    public long getApprovedMitigations() {
        return approvedMitigations;
    }

    public void setApprovedMitigations(long approvedMitigations) {
        this.approvedMitigations = approvedMitigations;
    }

    public long getExecutedMitigations() {
        return executedMitigations;
    }

    public void setExecutedMitigations(long executedMitigations) {
        this.executedMitigations = executedMitigations;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

