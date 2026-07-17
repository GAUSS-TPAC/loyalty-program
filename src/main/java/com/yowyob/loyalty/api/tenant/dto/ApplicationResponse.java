package com.yowyob.loyalty.api.tenant.dto;

import com.yowyob.loyalty.application.tenant.IntegrationApplicationService.ApplicationView;
import com.yowyob.loyalty.application.tenant.IntegrationApplicationService.CreatedApplication;
import com.yowyob.loyalty.domain.tenant.model.IntegrationApplication;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        String name,
        String description,
        String websiteUrl,
        String logoUrl,
        String publicKey,
        String privateKeyPrefix,
        ApiKeyMode mode,
        boolean active,
        UUID webhookEndpointId,
        String callbackUrl,
        Instant createdAt,
        Instant updatedAt,
        String privateKey,
        String webhookSecret
) {
    public static ApplicationResponse from(ApplicationView view) {
        IntegrationApplication app = view.app();
        return new ApplicationResponse(app.id(), app.name(), app.description(), app.websiteUrl(), app.logoUrl(),
                app.publicKey(), view.keyPrefix() != null ? view.keyPrefix() + "..." : null,
                app.mode(), app.active(), app.webhookEndpointId(), view.callbackUrl(),
                app.createdAt(), app.updatedAt(), null, null);
    }

    /** Les secrets (clé privée, secret webhook) ne sont renvoyés qu'ici, une seule fois. */
    public static ApplicationResponse fromCreated(CreatedApplication created, String callbackUrl) {
        IntegrationApplication app = created.app();
        return new ApplicationResponse(app.id(), app.name(), app.description(), app.websiteUrl(), app.logoUrl(),
                app.publicKey(), null, app.mode(), app.active(), app.webhookEndpointId(), callbackUrl,
                app.createdAt(), app.updatedAt(), created.rawPrivateKey(), created.webhookSecret());
    }
}
