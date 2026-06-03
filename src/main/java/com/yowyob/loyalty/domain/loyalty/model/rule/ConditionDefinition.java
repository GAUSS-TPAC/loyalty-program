package com.yowyob.loyalty.domain.loyalty.model.rule;

import java.math.BigDecimal;

public record ConditionDefinition(
        ConditionType type,
        ConditionOperator operator,
        Object thresholdValue,
        String windowType,
        String counterKey
) {
    public Long getThresholdAsLong() {
        if (thresholdValue instanceof Number num) {
            return num.longValue();
        }
        if (thresholdValue instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public BigDecimal getThresholdAsBigDecimal() {
        if (thresholdValue instanceof BigDecimal bd) {
            return bd;
        }
        if (thresholdValue instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        if (thresholdValue instanceof String str) {
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
