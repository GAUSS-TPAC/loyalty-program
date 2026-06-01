package com.yowyob.loyaulty.program.domain.tenant.model;

import java.util.List;

public record TenantConfig(
        String defaultCurrencyCode,
        String virtualCurrencyName,
        String virtualCurrencySymbol,
        double exchangeRate,
        boolean walletAutoActivate,
        int pointExpiryDays,
        List<String> notificationChannels
) {
    public static TenantConfig defaults() {
        return new TenantConfig(
                "XAF",
                "Points",
                "PTS",
                1.0,
                true,
                365,
                List.of("EMAIL")
        );
    }
}
