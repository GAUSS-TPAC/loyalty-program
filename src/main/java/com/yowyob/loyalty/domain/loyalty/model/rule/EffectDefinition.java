package com.yowyob.loyalty.domain.loyalty.model.rule;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public record EffectDefinition(
        EffectType type,
        Map<String, Object> params
) {
    public Optional<Long> getParamAsLong(String key) {
        if (params == null || !params.containsKey(key)) return Optional.empty();
        Object val = params.get(key);
        if (val instanceof Number num) {
            return Optional.of(num.longValue());
        }
        if (val instanceof String str) {
            try {
                return Optional.of(Long.parseLong(str));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public Optional<String> getParamAsString(String key) {
        if (params == null || !params.containsKey(key)) return Optional.empty();
        return Optional.ofNullable(params.get(key)).map(Object::toString);
    }

    public Optional<BigDecimal> getParamAsBigDecimal(String key) {
        if (params == null || !params.containsKey(key)) return Optional.empty();
        Object val = params.get(key);
        if (val instanceof BigDecimal bd) {
            return Optional.of(bd);
        }
        if (val instanceof Number num) {
            return Optional.of(BigDecimal.valueOf(num.doubleValue()));
        }
        if (val instanceof String str) {
            try {
                return Optional.of(new BigDecimal(str));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
