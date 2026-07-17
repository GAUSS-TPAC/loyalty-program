package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelConfirmMfaLoginRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResponseDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResultDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationSummaryDto;
import com.yowyob.loyalty.shared.exception.InvalidCredentialsException;
import com.yowyob.loyalty.shared.exception.KernelCoreUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Authentification locale tenant-scopée via KernelCore auth-core.
 * Endpoint : POST /api/auth/login (X-Tenant-Id + {principal, password} -> ApiResponse&lt;LoginResponse&gt;
 * portant le JWT RS256 et les organisations accessibles à l'acteur).
 * X-Client-Id/X-Api-Key (identité du backend consommateur) sont déjà portés par défaut
 * par kernelCoreWebClient (voir KernelCoreConfig).
 */
public class KernelCoreAuthAdapter {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreAuthAdapter.class);

    private static final ParameterizedTypeReference<KernelApiResponse<KernelLoginResponseDto>> LOGIN_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient kernelCoreWebClient;

    public KernelCoreAuthAdapter(WebClient kernelCoreWebClient) {
        this.kernelCoreWebClient = kernelCoreWebClient;
    }

    public Mono<KernelLoginResultDto> login(String tenantId, String principal, String password) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/login")
                .header("X-Tenant-Id", tenantId)
                .bodyValue(new KernelLoginRequestDto(principal, password))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new InvalidCredentialsException("Email ou mot de passe incorrect")))
                .onStatus(status -> status.value() == 429,
                        resp -> Mono.error(new InvalidCredentialsException(
                                "Un code vient déjà d'être envoyé — patientez avant de réessayer")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new InvalidCredentialsException("Authentification refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour l'authentification")))
                .bodyToMono(LOGIN_TYPE)
                .flatMap(this::unwrapLogin)
                .doOnError(e -> log.warn("Échec authentification KernelCore: {}", e.getMessage()));
    }

    /**
     * Confirme un défi MFA (code OTP reçu par email) et récupère le JWT.
     * Endpoint : POST /api/auth/login/mfa/confirm {mfaToken, code}.
     */
    public Mono<KernelLoginResultDto> confirmMfaLogin(String tenantId, String mfaToken, String code) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/login/mfa/confirm")
                .header("X-Tenant-Id", tenantId)
                .bodyValue(new KernelConfirmMfaLoginRequestDto(mfaToken, code))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new InvalidCredentialsException("Code de vérification invalide ou expiré")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new InvalidCredentialsException("Vérification refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour la vérification MFA")))
                .bodyToMono(LOGIN_TYPE)
                .flatMap(this::unwrapAuthenticated)
                .doOnError(e -> log.warn("Échec confirmation MFA KernelCore: {}", e.getMessage()));
    }

    /** Login direct (jeton présent) ou défi MFA (202 : code envoyé, mfaToken à confirmer). */
    private Mono<KernelLoginResultDto> unwrapLogin(KernelApiResponse<KernelLoginResponseDto> response) {
        if (response.getData() == null) {
            return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore invalide pour /api/auth/login"));
        }
        KernelLoginResponseDto data = response.getData();
        String token = data.resolveAccessToken();
        if (token != null && !token.isBlank()) {
            return Mono.just(KernelLoginResultDto.authenticated(token, safeOrganizations(data)));
        }
        String mfaToken = data.resolveMfaToken();
        if (mfaToken != null && !mfaToken.isBlank()) {
            return Mono.just(KernelLoginResultDto.mfaChallenge(mfaToken, data.resolveMfaChannel()));
        }
        return Mono.error(new KernelCoreUnavailableException(
                "Réponse KernelCore sans jeton d'accès ni défi MFA pour /api/auth/login"));
    }

    /** Réponse de mfa/confirm : le jeton doit être présent, pas de nouveau défi possible. */
    private Mono<KernelLoginResultDto> unwrapAuthenticated(KernelApiResponse<KernelLoginResponseDto> response) {
        if (!response.isSuccess() || response.getData() == null) {
            return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore invalide pour /api/auth/login/mfa/confirm"));
        }
        String token = response.getData().resolveAccessToken();
        if (token == null || token.isBlank()) {
            return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore sans jeton d'accès après confirmation MFA"));
        }
        return Mono.just(KernelLoginResultDto.authenticated(token, safeOrganizations(response.getData())));
    }

    private static List<KernelOrganizationSummaryDto> safeOrganizations(KernelLoginResponseDto data) {
        return data.getOrganizations() != null ? data.getOrganizations() : List.of();
    }
}
