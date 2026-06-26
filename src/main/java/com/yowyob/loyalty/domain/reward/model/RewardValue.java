package com.yowyob.loyalty.domain.reward.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record RewardValue(
        BigDecimal numericValue,
        String unit,
        int maxApplicationCount
) {
    public RewardValue {
        if (numericValue == null || numericValue.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("numericValue doit être positif");
        if (unit == null || unit.isBlank())
            throw new IllegalArgumentException("unit ne peut pas être vide");
        if (maxApplicationCount < 1)
            throw new IllegalArgumentException("maxApplicationCount doit être >= 1");
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if ("PERCENT".equals(unit)) {
            return orderAmount.multiply(numericValue)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return numericValue;
    }

    public static RewardValue percent(BigDecimal pct, int applications) {
        return new RewardValue(pct, "PERCENT", applications);
    }

    public static RewardValue fixed(BigDecimal amount, String currency) {
        return new RewardValue(amount, currency, 1);
    }

    public static RewardValue product(String productId) {
        return new RewardValue(BigDecimal.ONE, productId, 1);
    }
}
