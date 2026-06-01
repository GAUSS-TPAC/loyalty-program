package com.yowyob.loyaulty.program.infrastructure.bonification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bonification")
public record BonificationApiProperties(
        String baseUrl,
        int connectTimeoutMs,
        int readTimeoutMs,
        int maxRetries,
        String adminUsername,
        String adminPassword
) {
    public BonificationApiProperties {
        if (connectTimeoutMs <= 0) connectTimeoutMs = 3000;
        if (readTimeoutMs <= 0) readTimeoutMs = 5000;
        if (maxRetries <= 0) maxRetries = 3;
    }
}
