package com.yowyob.loyalty.api.shared.dto;

import com.yowyob.loyalty.shared.exception.AppException;
import java.time.Instant;
import java.util.Map;

public record ProblemDetails(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Instant timestamp,
    Map<String, Object> errors
) {
    public static ProblemDetails from(AppException ex, String requestId) {
        String typeUri = "https://loyalty.yowyob.com/errors/" + ex.getErrorCode().name().toLowerCase();
        return new ProblemDetails(
            typeUri,
            ex.getErrorCode().name(),
            ex.getHttpStatus(),
            ex.getMessage(),
            requestId,
            Instant.now(),
            ex.getProperties()
        );
    }
}
