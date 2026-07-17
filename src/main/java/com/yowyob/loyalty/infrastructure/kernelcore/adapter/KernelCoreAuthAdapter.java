package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelDiscoverContextsResponseDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelDiscoverSignUpContextsRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelDiscoverSignUpContextsResponseDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelDiscoveredContextDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResponseDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResultDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationSummaryDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelPublicSignUpRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelSignUpResultDto;
import com.yowyob.loyalty.shared.exception.InvalidCredentialsException;
import com.yowyob.loyalty.shared.exception.KernelCoreUnavailableException;
import com.yowyob.loyalty.shared.exception.RegistrationFailedException;
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

    private static final ParameterizedTypeReference<KernelApiResponse<KernelDiscoverContextsResponseDto>> DISCOVER_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<KernelApiResponse<KernelDiscoverSignUpContextsResponseDto>> DISCOVER_SIGNUP_TYPE =
            new ParameterizedTypeReference<>() {};

    private static final ParameterizedTypeReference<KernelApiResponse<KernelSignUpResultDto>> SIGNUP_TYPE =
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
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new InvalidCredentialsException("Authentification refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour l'authentification")))
                .bodyToMono(LOGIN_TYPE)
                .flatMap(this::unwrap)
                .doOnError(e -> log.warn("Échec authentification KernelCore: {}", e.getMessage()));
    }

    /**
     * Découverte des contextes de connexion (tenants plateforme) accessibles au compte,
     * via POST /api/auth/discover-contexts — pas de X-Tenant-Id requis. Utilisée quand le
     * tenant plateforme n'est pas fixé par configuration (app.kernel-core.tenant-id).
     */
    public Mono<List<KernelDiscoveredContextDto>> discoverContexts(String principal, String password) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/discover-contexts")
                .bodyValue(new KernelLoginRequestDto(principal, password))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        resp -> Mono.error(new InvalidCredentialsException("Email ou mot de passe incorrect")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new InvalidCredentialsException("Découverte des contextes refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour la découverte des contextes")))
                .bodyToMono(DISCOVER_TYPE)
                .flatMap(this::unwrapContexts)
                .doOnError(e -> log.warn("Échec découverte des contextes KernelCore: {}", e.getMessage()));
    }

    private Mono<List<KernelDiscoveredContextDto>> unwrapContexts(KernelApiResponse<KernelDiscoverContextsResponseDto> response) {
        if (!response.isSuccess() || response.getData() == null) {
            return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore invalide pour /api/auth/discover-contexts"));
        }
        return Mono.just(response.getData().getContexts());
    }

    /**
     * Découverte du jeton court d'inscription pour une organisation donnée (code fixe de ce
     * déploiement, app.kernel-core.organization-code), via POST /api/auth/discover-sign-up-contexts.
     * Le selectionToken renvoyé est réutilisé par signUp().
     */
    public Mono<String> discoverSignUpSelectionToken(String organizationCode) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/discover-sign-up-contexts")
                .bodyValue(new KernelDiscoverSignUpContextsRequestDto(organizationCode))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new RegistrationFailedException(
                                        "Découverte du contexte d'inscription refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException(
                                "KernelCore indisponible pour la découverte du contexte d'inscription")))
                .bodyToMono(DISCOVER_SIGNUP_TYPE)
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null
                            || response.getData().getSelectionToken() == null) {
                        return Mono.<String>error(new KernelCoreUnavailableException(
                                "Réponse KernelCore invalide pour /api/auth/discover-sign-up-contexts"));
                    }
                    return Mono.just(response.getData().getSelectionToken());
                })
                .doOnError(e -> log.warn("Échec découverte du contexte d'inscription KernelCore: {}", e.getMessage()));
    }

    /**
     * Inscription publique via POST /api/auth/sign-up. Le compte créé reste
     * EMAIL_VERIFICATION_REQUIRED (login refusé) tant que l'adresse n'est pas confirmée.
     */
    public Mono<KernelSignUpResultDto> signUp(String selectionToken, String firstName, String lastName,
                                               String email, String password) {
        return kernelCoreWebClient.post()
                .uri("/api/auth/sign-up")
                .bodyValue(new KernelPublicSignUpRequestDto(selectionToken, firstName, lastName, email, password))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new RegistrationFailedException(
                                        "Inscription refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour l'inscription")))
                .bodyToMono(SIGNUP_TYPE)
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        String message = response.getMessage() != null ? response.getMessage() : "réponse KernelCore invalide";
                        return Mono.<KernelSignUpResultDto>error(new RegistrationFailedException(message));
                    }
                    return Mono.just(response.getData());
                })
                .doOnError(e -> log.warn("Échec inscription KernelCore: {}", e.getMessage()));
    }

    private Mono<KernelLoginResultDto> unwrap(KernelApiResponse<KernelLoginResponseDto> response) {
        if (!response.isSuccess() || response.getData() == null) {
            return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore invalide pour /api/auth/login"));
        }
        String token = response.getData().resolveAccessToken();
        if (token == null || token.isBlank()) {
            return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore sans jeton d'accès"));
        }
        List<KernelOrganizationSummaryDto> organizations =
                response.getData().getOrganizations() != null ? response.getData().getOrganizations() : List.of();
        return Mono.just(new KernelLoginResultDto(token, organizations));
    }
}
