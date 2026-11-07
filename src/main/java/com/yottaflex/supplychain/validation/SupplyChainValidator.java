package com.yottaflex.supplychain.validation;

import com.yottaflex.supplychain.exception.BusinessValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class SupplyChainValidator {

    public void validateDateRange(LocalDate startDate, LocalDate endDate, String context) {
        if (startDate == null || endDate == null) {
            throw new BusinessValidationException(context + ": Start date and end date must not be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessValidationException(context + ": End date cannot be before start date");
        }
    }

    public void validatePositiveQuantity(BigDecimal quantity, String fieldName) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(fieldName + " must be strictly positive");
        }
    }

    public void validateNonNegativeQuantity(BigDecimal quantity, String fieldName) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException(fieldName + " cannot be negative");
        }
    }
}

