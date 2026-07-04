package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelBusinessActorDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelBusinessActorUpdateRequest;
import com.yowyob.loyalty.shared.exception.KernelCoreUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Profil de l'acteur métier ("me") côté Kernel Core.
 * Endpoint : GET/PUT /api/actors/me — nécessite le Bearer JWT de l'utilisateur courant
 * (en plus des headers X-Client-Id/X-Api-Key déjà portés par défaut par kernelCoreWebClient).
 */
public class KernelCoreActorAdapter {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreActorAdapter.class);

    private static final ParameterizedTypeReference<KernelApiResponse<KernelBusinessActorDto>> ACTOR_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient kernelCoreWebClient;

    public KernelCoreActorAdapter(WebClient kernelCoreWebClient) {
        this.kernelCoreWebClient = kernelCoreWebClient;
    }

    public Mono<KernelBusinessActorDto> getMyProfile(String bearerToken) {
        return kernelCoreWebClient.get()
                .uri("/api/actors/me")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .retrieve()
                .bodyToMono(ACTOR_TYPE)
                .flatMap(this::unwrap)
                .doOnError(e -> log.warn("Échec récupération du profil acteur Kernel Core: {}", e.getMessage()));
    }

    public Mono<KernelBusinessActorDto> updateMyProfile(String bearerToken, KernelBusinessActorUpdateRequest request) {
        return kernelCoreWebClient.put()
                .uri("/api/actors/me")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ACTOR_TYPE)
                .flatMap(this::unwrap)
                .doOnError(e -> log.warn("Échec mise à jour du profil acteur Kernel Core: {}", e.getMessage()));
    }

    private Mono<KernelBusinessActorDto> unwrap(KernelApiResponse<KernelBusinessActorDto> response) {
        if (!response.isSuccess() || response.getData() == null) {
            return Mono.error(new KernelCoreUnavailableException("Réponse Kernel Core invalide pour /api/actors/me"));
        }
        return Mono.just(response.getData());
    }
}
