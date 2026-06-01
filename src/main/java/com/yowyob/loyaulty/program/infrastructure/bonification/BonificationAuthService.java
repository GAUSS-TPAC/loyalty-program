package com.yowyob.loyaulty.program.infrastructure.bonification;

import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationAuthRequestDto;
import com.yowyob.loyaulty.program.infrastructure.bonification.dto.BonificationAuthResponseDto;
import com.yowyob.loyaulty.program.shared.util.RedisKeyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class BonificationAuthService {

    private static final Logger log = LoggerFactory.getLogger(BonificationAuthService.class);
    private static final String AUTH_ENDPOINT = "/api/auth/login";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(55);

    private final WebClient webClient;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final BonificationApiProperties properties;

    public BonificationAuthService(@Qualifier("bonificationWebClient") WebClient webClient,
                                   ReactiveStringRedisTemplate redisTemplate,
                                   BonificationApiProperties properties) {
        this.webClient = webClient;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public Mono<String> getValidToken() {
        String key = RedisKeyBuilder.bonificationTokenKey();
        return redisTemplate.opsForValue().get(key)
                .switchIfEmpty(authenticate().flatMap(token ->
                        redisTemplate.opsForValue().set(key, token, TOKEN_TTL)
                                .thenReturn(token)
                ));
    }

    public Mono<Void> invalidateToken() {
        return redisTemplate.delete(RedisKeyBuilder.bonificationTokenKey()).then();
    }

    private Mono<String> authenticate() {
        log.info("Authenticating against bonification API");
        return webClient.post()
                .uri(AUTH_ENDPOINT)
                .bodyValue(new BonificationAuthRequestDto(
                        properties.adminUsername(),
                        properties.adminPassword()
                ))
                .retrieve()
                .bodyToMono(BonificationAuthResponseDto.class)
                .map(BonificationAuthResponseDto::token)
                .doOnError(e -> log.error("Bonification API authentication failed: {}", e.getMessage()));
    }
}
