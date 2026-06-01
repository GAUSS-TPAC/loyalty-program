package com.yowyob.loyaulty.program.domain.rule.model;

import java.util.Map;

public record Trigger(
        String eventType,
        Map<String, String> filters
) {
    public boolean matches(String incomingEventType, Map<String, Object> payload) {
        if (!this.eventType.equals(incomingEventType)) return false;
        if (filters == null || filters.isEmpty()) return true;
        for (var entry : filters.entrySet()) {
            Object payloadValue = payload.get(entry.getKey());
            if (payloadValue == null || !payloadValue.toString().equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
