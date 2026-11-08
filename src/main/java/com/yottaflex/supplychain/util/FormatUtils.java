package com.yottaflex.supplychain.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class FormatUtils {

    private FormatUtils() {
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        return currencyFormatter.format(amount);
    }

    public static BigDecimal roundQuantity(BigDecimal quantity, int decimals) {
        if (quantity == null) {
            return BigDecimal.ZERO;
        }
        return quantity.setScale(decimals, RoundingMode.HALF_UP);
    }
}

