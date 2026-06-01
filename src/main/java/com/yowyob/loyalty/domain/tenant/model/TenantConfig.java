package com.yowyob.loyalty.domain.tenant.model;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import java.util.Collections;
import java.util.List;

public record TenantConfig(
    String defaultCurrencyCode,
    boolean walletAutoActivate,
    Integer pointExpiryDays,
    List<String> notificationChannels,
    String bonificationApiUsername,
    String bonificationApiPassword
) {
    public TenantConfig {
        if (defaultCurrencyCode == null || defaultCurrencyCode.isBlank()) {
            throw new DomainValidationException("defaultCurrencyCode ne doit pas être null");
        }
        notificationChannels = notificationChannels != null ? List.copyOf(notificationChannels) : Collections.emptyList();
    }

    public static TenantConfig defaults() {
        return new TenantConfig(
            "XAF",
            false,
            365,
            Collections.emptyList(),
            null,
            null
        );
    }
}
