package com.yowyob.loyaulty.program.infrastructure.bonification;

import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationReward;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationPort;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationBeneficiaryRequestDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationHistoryItemDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationRewardDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationTransactionResponseDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.mapper.BonificationMapper;
import com.yowyob.loyaulty.program.shared.exception.BonificationApiException;
import com.yowyob.loyaulty.program.shared.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class BonificationApiAdapter implements BonificationPort {

    private static final Logger log = LoggerFactory.getLogger(BonificationApiAdapter.class);

    private final WebClient webClient;
    private final BonificationAuthService authService;
    private final BonificationMapper mapper;

    public BonificationApiAdapter(@Qualifier("bonificationWebClient") WebClient webClient,
                                   BonificationAuthService authService,
                                   BonificationMapper mapper) {
        this.webClient = webClient;
        this.authService = authService;
        this.mapper = mapper;
    }

    @Override
    @CircuitBreaker(name = "bonification-api", fallbackMethod = "fallbackRecordTransaction")
    @Retry(name = "bonification-api")
    public Mono<PointsResult> recordTransaction(TenantId tenantId,
                                                 String externalUserId,
                                                 BigDecimal amount,
                                                 String description) {
        return authService.getValidToken()
                .flatMap(token -> webClient.post()
                        .uri("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(mapper.toRequestDto(externalUserId, amount, description))
                        .retrieve()
                        .bodyToMono(BonificationTransactionResponseDto.class)
                        .map(mapper::toPointsResult)
                )
                .onErrorResume(WebClientResponseException.Unauthorized.class, ex ->
                        authService.invalidateToken()
                                .then(authService.getValidToken())
                                .flatMap(newToken -> webClient.post()
                                        .uri("/api/transactions")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken)
                                        .bodyValue(mapper.toRequestDto(externalUserId, amount, description))
                                        .retrieve()
                                        .bodyToMono(BonificationTransactionResponseDto.class)
                                        .map(mapper::toPointsResult)
                                )
                )
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Bonification API error {}: {}", ex.getStatusCode(), ex.getMessage());
                    return Mono.error(new BonificationApiException(
                            ErrorCode.BONIFICATION_API_TRANSACTION_FAILED,
                            "Bonification API returned " + ex.getStatusCode()
                    ));
                });
    }

    @SuppressWarnings("unused")
    public Mono<PointsResult> fallbackRecordTransaction(TenantId tenantId,
                                                         String externalUserId,
                                                         BigDecimal amount,
                                                         String description,
                                                         Throwable ex) {
        log.warn("Bonification API unavailable for tenant={}, member={}. Returning degraded result. Cause: {}",
                tenantId.value(), externalUserId, ex.getMessage());
        return Mono.just(PointsResult.degraded());
    }

    @Override
    @CircuitBreaker(name = "bonification-api", fallbackMethod = "fallbackGetHistory")
    public Flux<BonificationTransaction> getTransactionHistory(TenantId tenantId,
                                                                String externalUserId) {
        return authService.getValidToken()
                .flatMapMany(token -> webClient.get()
                        .uri("/api/transactions/{userId}", externalUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(BonificationHistoryItemDto.class)
                        .map(mapper::toTransactionFromHistory)
                );
    }

    @SuppressWarnings("unused")
    public Flux<BonificationTransaction> fallbackGetHistory(TenantId tenantId,
                                                             String externalUserId,
                                                             Throwable ex) {
        log.warn("Bonification API unavailable for history. tenant={}, member={}", tenantId.value(), externalUserId);
        return Flux.empty();
    }

    @Override
    public Mono<Void> createBeneficiary(TenantId tenantId,
                                         String externalUserId,
                                         String email,
                                         String name) {
        return authService.getValidToken()
                .flatMap(token -> webClient.post()
                        .uri("/api/beneficiaries")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(new BonificationBeneficiaryRequestDto(externalUserId, email, name))
                        .retrieve()
                        .toBodilessEntity()
                        .then()
                )
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Failed to create beneficiary for member={}: {}", externalUserId, ex.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Flux<BonificationReward> getAvailableRewards(TenantId tenantId, String externalUserId) {
        return authService.getValidToken()
                .flatMapMany(token -> webClient.get()
                        .uri("/api/rewards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(BonificationRewardDto.class)
                        .map(mapper::toReward)
                );
    }
}
