package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
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
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new InvalidCredentialsException("Authentification refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour l'authentification")))
                .bodyToMono(LOGIN_TYPE)
                .flatMap(this::unwrap)
                .doOnError(e -> log.warn("Échec authentification KernelCore: {}", e.getMessage()));
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
