package com.yowyob.loyalty.infrastructure.kernelcore.service;

import com.yowyob.loyalty.infrastructure.kernelcore.config.KernelCoreProperties;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelCoreTokenResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Obtient et met en cache un token OAuth2 client_credentials pour les appels au Kernel Core.
 * Le token est stocké dans Redis avec un TTL légèrement inférieur à l'expiration réelle.
 */
public class KernelCoreTokenService {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreTokenService.class);
    private static final String CACHE_KEY = "kernelcore:service:token";
    private static final Duration CACHE_BUFFER = Duration.ofSeconds(30);

    private final KernelCoreProperties properties;
    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public KernelCoreTokenService(
            KernelCoreProperties properties,
            WebClient webClient,
            ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.webClient = webClient;
    }

    public Mono<String> getServiceToken() {
        return redisTemplate.opsForValue().get(CACHE_KEY)
                .cast(String.class)
                .switchIfEmpty(fetchAndCacheToken());
    }

    private Mono<String> fetchAndCacheToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getServiceClientId());
        form.add("client_secret", properties.getServiceClientSecret());

        return webClient.post()
                .uri(properties.resolvedTokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(KernelCoreTokenResponseDto.class)
                .flatMap(response -> {
                    String token = response.accessToken();
                    long expiresIn = response.expiresIn() != null ? response.expiresIn() : 300L;
                    Duration ttl = Duration.ofSeconds(expiresIn).minus(CACHE_BUFFER);
                    if (ttl.isNegative()) ttl = Duration.ofSeconds(10);
                    return redisTemplate.opsForValue()
                            .set(CACHE_KEY, token, ttl)
                            .thenReturn(token);
                })
                .doOnError(e -> log.warn("Impossible d'obtenir le token Kernel Core: {}", e.getMessage()));
    }
}
