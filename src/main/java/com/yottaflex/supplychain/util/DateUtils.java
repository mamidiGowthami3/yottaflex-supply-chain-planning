package com.yottaflex.supplychain.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private DateUtils() {
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    public static LocalDate addDays(LocalDate baseDate, int days) {
        if (baseDate == null) {
            return null;
        }
        return baseDate.plusDays(days);
    }
}

