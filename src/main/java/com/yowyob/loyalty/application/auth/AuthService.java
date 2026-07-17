package com.yowyob.loyalty.application.auth;

import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreAuthAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.config.KernelCoreProperties;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResultDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationSummaryDto;
import com.yowyob.loyalty.shared.exception.OrganizationNotAccessibleException;
import com.yowyob.loyalty.shared.exception.OrganizationSelectionRequiredException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
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

    public AuthService(KernelCoreAuthAdapter kernelCoreAuthAdapter, KernelCoreProperties kernelCoreProperties) {
        this.kernelCoreAuthAdapter = kernelCoreAuthAdapter;
        this.kernelCoreProperties = kernelCoreProperties;
    }

    /**
     * @param organizationId organisation KernelCore explicitement choisie par l'appelant
     *                        (optionnelle si l'acteur n'a accès qu'à une seule organisation)
     * @return soit un {@link LoginOutcome} authentifié, soit un défi MFA : KernelCore a envoyé
     *         un code par email et le client doit le confirmer via {@link #confirmMfa}.
     */
    public Mono<LoginOutcome> login(String email, String password, String organizationId) {
        return kernelCoreAuthAdapter.login(kernelCoreProperties.getTenantId(), email, password)
                .map(result -> result.mfaRequired()
                        ? LoginOutcome.mfaRequired(result.mfaToken(), result.mfaChannel())
                        : LoginOutcome.authenticated(resolveOrganization(result, organizationId)));
    }

    /** Deuxième étape du login MFA : confirme le code OTP reçu par email. */
    public Mono<AuthResult> confirmMfa(String mfaToken, String code, String organizationId) {
        return kernelCoreAuthAdapter.confirmMfaLogin(kernelCoreProperties.getTenantId(), mfaToken, code)
                .map(result -> resolveOrganization(result, organizationId));
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

    /** Issue d'un login : authentifié directement, ou défi MFA à confirmer (code envoyé par email). */
    public record LoginOutcome(AuthResult result, String mfaToken, String mfaChannel) {
        static LoginOutcome authenticated(AuthResult result) {
            return new LoginOutcome(result, null, null);
        }

        static LoginOutcome mfaRequired(String mfaToken, String mfaChannel) {
            return new LoginOutcome(null, mfaToken, mfaChannel);
        }

        public boolean isMfaRequired() {
            return result == null;
        }
    }
}
