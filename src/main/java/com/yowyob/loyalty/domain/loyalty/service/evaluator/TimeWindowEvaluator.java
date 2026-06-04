package com.yowyob.loyalty.domain.loyalty.service.evaluator;

import com.yowyob.loyalty.domain.loyalty.model.engine.ConditionEvaluationResult;
import com.yowyob.loyalty.domain.loyalty.model.engine.EvaluationContext;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionDefinition;
import com.yowyob.loyalty.domain.loyalty.model.rule.ConditionType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class TimeWindowEvaluator implements ConditionEvaluator {

    @Override
    public boolean supports(ConditionType type) {
        return type == ConditionType.TIME_WINDOW;
    }

    @Override
    public ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) {
        if (!(condition.thresholdValue() instanceof Map)) {
            return ConditionEvaluationResult.failed(condition.type().name(), "Invalid configuration for TIME_WINDOW");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) condition.thresholdValue();
        ZonedDateTime eventTime = context.event().occurredAt().atZone(ZoneOffset.UTC); // Ideally tenant timezone

        if (config.containsKey("days_of_week")) {
            @SuppressWarnings("unchecked")
            List<String> days = (List<String>) config.get("days_of_week");
            DayOfWeek eventDay = eventTime.getDayOfWeek();
            boolean matchedDay = days.stream()
                    .map(String::toUpperCase)
                    .anyMatch(day -> eventDay.name().equals(day));
            if (!matchedDay) {
                return ConditionEvaluationResult.failed(condition.type().name(), "Day of week does not match");
            }
        }

        if (config.containsKey("hours")) {
            @SuppressWarnings("unchecked")
            List<String> hoursList = (List<String>) config.get("hours");
            LocalTime eventTimeOfDay = eventTime.toLocalTime();
            boolean matchedHour = false;
            
            for (String range : hoursList) {
                String[] parts = range.split("-");
                if (parts.length == 2) {
                    LocalTime start = LocalTime.parse(parts[0].trim());
                    LocalTime end = LocalTime.parse(parts[1].trim());
                    if (!eventTimeOfDay.isBefore(start) && !eventTimeOfDay.isAfter(end)) {
                        matchedHour = true;
                        break;
                    }
                }
            }
            if (!matchedHour) {
                return ConditionEvaluationResult.failed(condition.type().name(), "Hour does not match window");
            }
        }

        return ConditionEvaluationResult.passed(condition.type().name());
    }
}
