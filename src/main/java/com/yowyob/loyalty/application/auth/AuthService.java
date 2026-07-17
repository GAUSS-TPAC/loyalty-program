package com.yowyob.loyalty.application.auth;

import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreActorAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreAuthAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreTenantAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.config.KernelCoreProperties;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelDiscoveredContextDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResultDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationSummaryDto;
import com.yowyob.loyalty.shared.exception.OrganizationNotAccessibleException;
import com.yowyob.loyalty.shared.exception.OrganizationSelectionRequiredException;
import com.yowyob.loyalty.shared.exception.RegistrationFailedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentification des administrateurs de tenant, déléguée à KernelCore auth-core
 * (POST /api/auth/login, tenant-scopé au tenant plateforme KernelCore de ce déploiement).
 *
 * Le JWT renvoyé par un login tenant-scopé ne porte pas de claim d'organisation : le
 * modèle multi-tenant de ce backend associe un "tenant" loyalty à une organisation
 * KernelCore, donc l'organisation cible doit être résolue explicitement à la connexion
 * et propagée par le client (header X-Organization-Id) sur les appels suivants — voir
 * TenantResolutionFilter et ApiKeyResolutionFilter.
 */
@Service
public class AuthService {

    private final KernelCoreAuthAdapter kernelCoreAuthAdapter;
    private final KernelCoreProperties kernelCoreProperties;
    private final KernelCoreActorAdapter kernelCoreActorAdapter;
    private final KernelCoreTenantAdapter kernelCoreTenantAdapter;

    public AuthService(KernelCoreAuthAdapter kernelCoreAuthAdapter, KernelCoreProperties kernelCoreProperties,
                        KernelCoreActorAdapter kernelCoreActorAdapter, KernelCoreTenantAdapter kernelCoreTenantAdapter) {
        this.kernelCoreAuthAdapter = kernelCoreAuthAdapter;
        this.kernelCoreProperties = kernelCoreProperties;
        this.kernelCoreActorAdapter = kernelCoreActorAdapter;
        this.kernelCoreTenantAdapter = kernelCoreTenantAdapter;
    }

    /**
     * @param organizationId organisation KernelCore explicitement choisie par l'appelant
     *                        (optionnelle si l'acteur n'a accès qu'à une seule organisation)
     */
    public Mono<AuthResult> login(String email, String password, String organizationId) {
        return resolveTenantId(email, password, organizationId)
                .flatMap(tenantId -> kernelCoreAuthAdapter.login(tenantId, email, password))
                .flatMap(result -> {
                    // L'inscription publique (sign-up) ne crée qu'un compte, jamais d'organisation :
                    // premier login sans organisation ni choix explicite -> on en provisionne une.
                    boolean hasNoChoice = (organizationId == null || organizationId.isBlank());
                    if (result.organizations().isEmpty() && hasNoChoice) {
                        return provisionDefaultOrganization(result.accessToken(), email);
                    }
                    return Mono.just(resolveOrganization(result, organizationId));
                });
    }

    /**
     * Auto-provisionnement d'un espace de travail au premier login d'un compte inscrit en
     * self-service : POST /api/actors/me (businessActorId) puis POST /api/organizations.
     */
    private Mono<AuthResult> provisionDefaultOrganization(String accessToken, String email) {
        return kernelCoreActorAdapter.getMyProfile(accessToken)
                .flatMap(actor -> {
                    String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    String code = "ORG-" + suffix;
                    String name = actor.getName() != null && !actor.getName().isBlank() ? actor.getName() : email;
                    return kernelCoreTenantAdapter.createOrganization(accessToken, actor.getId(), code, name, name);
                })
                .map(org -> new AuthResult(accessToken, org.getId().toString(), org.getCode(), org.resolveName()));
    }

    /**
     * Le tenant plateforme KernelCore est normalement fixé par configuration
     * (app.kernel-core.tenant-id / KERNEL_TENANT_ID). À défaut, il est découvert via
     * POST /api/auth/discover-contexts : KernelCore refuse un login sans X-Tenant-Id
     * (TENANT_REQUIRED) et exige ce flux en deux temps.
     */
    private Mono<String> resolveTenantId(String email, String password, String requestedOrganizationId) {
        String configured = kernelCoreProperties.getTenantId();
        if (configured != null && !configured.isBlank()) {
            return Mono.just(configured);
        }
        return kernelCoreAuthAdapter.discoverContexts(email, password)
                .map(contexts -> selectContext(contexts, requestedOrganizationId).getTenantId());
    }

    private KernelDiscoveredContextDto selectContext(List<KernelDiscoveredContextDto> contexts,
                                                     String requestedOrganizationId) {
        if (contexts.isEmpty()) {
            throw new OrganizationNotAccessibleException("Aucun contexte de connexion accessible pour ce compte");
        }
        if (contexts.size() == 1) {
            return contexts.get(0);
        }
        if (requestedOrganizationId != null && !requestedOrganizationId.isBlank()) {
            return contexts.stream()
                    .filter(c -> c.getOrganizations().stream()
                            .anyMatch(o -> requestedOrganizationId.equals(o.getOrganizationId())))
                    .findFirst()
                    .orElseThrow(() -> new OrganizationNotAccessibleException(
                            "L'organisation demandée n'est accessible dans aucun contexte de ce compte"));
        }
        Map<String, Object> available = Map.of(
                "organizations", contexts.stream()
                        .flatMap(c -> c.getOrganizations().stream())
                        .map(o -> Map.of(
                                "organizationId", String.valueOf(o.getOrganizationId()),
                                "organizationCode", String.valueOf(o.getOrganizationCode()),
                                "displayName", String.valueOf(o.getDisplayName())))
                        .collect(Collectors.toList()));
        throw new OrganizationSelectionRequiredException(
                "Ce compte a accès à plusieurs contextes ; précisez organizationId", available);
    }

    private AuthResult resolveOrganization(KernelLoginResultDto result, String requestedOrganizationId) {
        List<KernelOrganizationSummaryDto> organizations = result.organizations();

        if (requestedOrganizationId != null && !requestedOrganizationId.isBlank()) {
            KernelOrganizationSummaryDto match = organizations.stream()
                    .filter(o -> requestedOrganizationId.equals(o.getOrganizationId()))
                    .findFirst()
                    .orElseThrow(() -> new OrganizationNotAccessibleException(
                            "L'organisation demandée n'est pas accessible à cet acteur"));
            return AuthResult.from(result.accessToken(), match);
        }

        if (organizations.isEmpty()) {
            throw new OrganizationNotAccessibleException("Aucune organisation accessible pour cet acteur");
        }
        if (organizations.size() == 1) {
            return AuthResult.from(result.accessToken(), organizations.get(0));
        }

        Map<String, Object> available = Map.of(
                "organizations", organizations.stream()
                        .map(o -> Map.of(
                                "organizationId", String.valueOf(o.getOrganizationId()),
                                "organizationCode", String.valueOf(o.getOrganizationCode()),
                                "displayName", String.valueOf(o.getDisplayName())))
                        .collect(Collectors.toList()));
        throw new OrganizationSelectionRequiredException(
                "Cet acteur a accès à plusieurs organisations ; précisez organizationId", available);
    }

    public record AuthResult(String token, String organizationId, String organizationCode, String organizationName) {
        static AuthResult from(String token, KernelOrganizationSummaryDto org) {
            return new AuthResult(token, org.getOrganizationId(), org.getOrganizationCode(), org.getDisplayName());
        }
    }

    /**
     * Inscription publique (page "Ouvrir un compte") : crée un compte KernelCore sous
     * l'organisation fixe de ce déploiement (app.kernel-core.organization-code). Le compte
     * reste EMAIL_VERIFICATION_REQUIRED — le login échouera tant que l'email n'est pas
     * confirmé (email envoyé par KernelCore).
     */
    public Mono<RegisterResult> register(String firstName, String lastName, String email, String password) {
        String organizationCode = kernelCoreProperties.getOrganizationCode();
        if (organizationCode == null || organizationCode.isBlank()) {
            return Mono.error(new RegistrationFailedException(
                    "Inscription indisponible : organisation cible non configurée (app.kernel-core.organization-code)"));
        }
        return kernelCoreAuthAdapter.discoverSignUpSelectionToken(organizationCode)
                .flatMap(selectionToken -> kernelCoreAuthAdapter.signUp(selectionToken, firstName, lastName, email, password))
                .map(result -> new RegisterResult(result.getEmail(), result.getStatus(), result.isEmailVerified()));
    }

    public record RegisterResult(String email, String status, boolean emailVerified) {}
}
