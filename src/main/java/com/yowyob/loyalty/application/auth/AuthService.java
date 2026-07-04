package com.yowyob.loyalty.application.auth;

import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreAuthAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Authentification des administrateurs de tenant, déléguée à KernelCore auth-core
 * (POST /api/auth/login, tenant-scopé). Aucune sélection de tenant n'est exposée
 * côté portail admin pour l'instant : le tenant par défaut du déploiement est utilisé,
 * comme pour DevTenantResolutionFilter.
 */
@Service
public class AuthService {

    private final KernelCoreAuthAdapter kernelCoreAuthAdapter;
    private final String defaultTenantId;

    public AuthService(
            KernelCoreAuthAdapter kernelCoreAuthAdapter,
            @Value("${app.dev.default-tenant-id:00000000-0000-0000-0000-000000000001}") String defaultTenantId) {
        this.kernelCoreAuthAdapter = kernelCoreAuthAdapter;
        this.defaultTenantId = defaultTenantId;
    }

    public Mono<String> login(String email, String password) {
        return kernelCoreAuthAdapter.login(defaultTenantId, email, password);
    }
}
