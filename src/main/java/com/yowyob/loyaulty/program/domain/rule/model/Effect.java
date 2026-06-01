package com.yowyob.loyaulty.program.domain.rule.model;

import com.yowyob.loyaulty.program.domain.rule.model.enums.EffectType;

import java.util.Map;

public record Effect(
        EffectType type,
        Map<String, Object> params
) {
    public Object param(String key) {
        return params != null ? params.get(key) : null;
    }

    public String paramAsString(String key) {
        Object v = param(key);
        return v != null ? v.toString() : null;
    }

    public long paramAsLong(String key, long defaultValue) {
        Object v = param(key);
        if (v == null) return defaultValue;
        return ((Number) v).longValue();
    }
}
