package com.yowyob.loyaulty.program.api.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetails(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        Instant timestamp,
        Map<String, Object> errors
) {
    public static ProblemDetails of(String type, String title, int status,
                                     String detail, String instance) {
        return new ProblemDetails(type, title, status, detail, instance, Instant.now(), null);
    }

    public static ProblemDetails withErrors(String type, String title, int status,
                                             String detail, String instance,
                                             Map<String, Object> errors) {
        return new ProblemDetails(type, title, status, detail, instance, Instant.now(), errors);
    }
}
