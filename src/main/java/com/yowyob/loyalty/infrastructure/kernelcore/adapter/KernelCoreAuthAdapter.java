package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResponseDto;
import com.yowyob.loyalty.shared.exception.InvalidCredentialsException;
import com.yowyob.loyalty.shared.exception.KernelCoreUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Authentification locale tenant-scopée via KernelCore auth-core.
 * Endpoint : POST /api/auth/login (X-Tenant-Id + {principal, password} -> JWT RS256).
 * X-Client-Id/X-Api-Key (identité du backend consommateur) sont déjà portés par défaut
 * par kernelCoreWebClient (voir KernelCoreConfig).
 */
public class KernelCoreAuthAdapter {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreAuthAdapter.class);

    private final WebClient kernelCoreWebClient;

    public KernelCoreAuthAdapter(WebClient kernelCoreWebClient) {
        this.kernelCoreWebClient = kernelCoreWebClient;
    }

    public Mono<String> login(String tenantId, String principal, String password) {
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
                .bodyToMono(KernelLoginResponseDto.class)
                .map(KernelLoginResponseDto::resolveAccessToken)
                .flatMap(token -> token == null || token.isBlank()
                        ? Mono.error(new KernelCoreUnavailableException("Réponse KernelCore sans jeton d'accès"))
                        : Mono.just(token))
                .doOnError(e -> log.warn("Échec authentification KernelCore: {}", e.getMessage()));
    }
}
