package com.yowyob.loyalty.infrastructure.bonification.adapter;

import com.yowyob.loyalty.domain.bonification.exception.BonificationException;
import com.yowyob.loyalty.domain.bonification.model.BonificationCredentials;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionRequest;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionResult;
import com.yowyob.loyalty.domain.bonification.port.out.BonificationPort;
import com.yowyob.loyalty.domain.bonification.port.out.BonificationTokenCachePort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.bonification.config.BonificationProperties;
import com.yowyob.loyalty.infrastructure.bonification.dto.BonificationJwtResponseDto;
import com.yowyob.loyalty.infrastructure.bonification.dto.BonificationLoginRequestDto;
import com.yowyob.loyalty.infrastructure.bonification.dto.BonificationTransactionRequestDto;
import com.yowyob.loyalty.infrastructure.bonification.dto.BonificationTransactionResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BonificationApiAdapter implements BonificationPort {

    private static final Logger log = LoggerFactory.getLogger(BonificationApiAdapter.class);
    private static final String API_PREFIX = "/api";

    private final WebClient webClient;
    private final BonificationProperties properties;
    private final BonificationTokenCachePort tokenCache;

    public BonificationApiAdapter(
            WebClient.Builder webClientBuilder,
            BonificationProperties properties,
            BonificationTokenCachePort tokenCache
    ) {
        this.properties = properties;
        this.tokenCache = tokenCache;
        this.webClient = webClientBuilder
                .baseUrl(normalizeBaseUrl(properties.getBaseUrl()))
                .build();
    }

    @Override
    public Mono<Boolean> checkConnectivity() {
        if (!properties.isEnabled()) {
            return Mono.just(false);
        }
        BonificationCredentials probe = new BonificationCredentials(
                properties.getDefaultLogin(),
                properties.getDefaultPassword()
        );
        if (!probe.isConfigured()) {
            return Mono.just(false);
        }
        return authenticate(probe)
                .map(token -> token != null && !token.isBlank())
                .onErrorResume(ex -> {
                    log.warn("Bonification connectivity check failed: {}", ex.getMessage());
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Boolean> verifyCredentials(BonificationCredentials credentials) {
        if (!properties.isEnabled() || !credentials.isConfigured()) {
            return Mono.just(false);
        }
        return authenticate(credentials)
                .map(token -> token != null && !token.isBlank())
                .onErrorReturn(false);
    }

    @Override
    public Mono<BonificationTransactionResult> submitTransaction(
            TenantId tenantId,
            BonificationCredentials credentials,
            BonificationTransactionRequest request
    ) {
        if (!properties.isEnabled()) {
            return Mono.error(new BonificationException("Intégration Bonification désactivée"));
        }
        if (!credentials.isConfigured()) {
            return Mono.error(new BonificationException(
                    "Identifiants Bonification manquants pour le tenant (bonificationApiUsername / bonificationApiPassword)"));
        }

        return resolveAccessToken(tenantId, credentials)
                .flatMap(token -> postTransaction(token, request))
                .map(this::toDomainResult);
    }

    private Mono<String> resolveAccessToken(TenantId tenantId, BonificationCredentials credentials) {
        return tokenCache.getToken(tenantId)
                .switchIfEmpty(authenticate(credentials)
                        .flatMap(token -> tokenCache
                                .saveToken(tenantId, token, properties.getTokenTtl())
                                .thenReturn(token)));
    }

    private Mono<String> authenticate(BonificationCredentials credentials) {
        return webClient.post()
                .uri(API_PREFIX + "/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new BonificationLoginRequestDto(credentials.login(), credentials.password()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> Mono.error(new BonificationException(
                                "Authentification Bonification échouée: " + body))))
                .bodyToMono(BonificationJwtResponseDto.class)
                .map(BonificationJwtResponseDto::token)
                .switchIfEmpty(Mono.error(new BonificationException("Réponse auth Bonification vide")));
    }

    private Mono<BonificationTransactionResponseDto> postTransaction(String token, BonificationTransactionRequest request) {
        BonificationTransactionRequestDto body = new BonificationTransactionRequestDto(
                request.amount(),
                request.status(),
                request.clientLogin(),
                request.debit()
        );

        return webClient.post()
                .uri(API_PREFIX + "/transactions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(err -> Mono.error(new BonificationException(
                                "Création transaction Bonification échouée: " + err))))
                .bodyToMono(BonificationTransactionResponseDto.class);
    }

    private BonificationTransactionResult toDomainResult(BonificationTransactionResponseDto dto) {
        return new BonificationTransactionResult(
                dto.id(),
                dto.amount(),
                dto.clientLogin(),
                Boolean.TRUE.equals(dto.isDebit()),
                dto.statuts(),
                dto.message()
        );
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://bonusapi.onrender.com";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
