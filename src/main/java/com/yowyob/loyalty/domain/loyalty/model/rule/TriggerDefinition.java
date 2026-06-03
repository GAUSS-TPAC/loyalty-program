package com.yowyob.loyalty.domain.loyalty.model.rule;

import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;

import java.util.Map;

public record TriggerDefinition(
        String eventType,
        Map<String, Object> filters
) {
    public boolean matches(IncomingEvent event) {
        if (event == null || !this.eventType.equals(event.eventType())) {
            return false;
        }

        if (filters == null || filters.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            Object eventValue = event.getPayloadValue(filter.getKey()).orElse(null);
            if (eventValue == null || !filter.getValue().equals(eventValue)) {
                return false;
            }
        }

        return true;
    }
}
