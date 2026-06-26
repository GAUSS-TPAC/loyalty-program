package com.yowyob.loyalty.infrastructure.redis.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.OtpChallenge;
import com.yowyob.loyalty.domain.wallet.port.out.OtpChallengePort;
import com.yowyob.loyalty.shared.util.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OtpChallengeRedisAdapter implements OtpChallengePort {

    private final ReactiveRedisTemplate<String, String> redis;
    private final ObjectMapper objectMapper;

    public OtpChallengeRedisAdapter(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redis,
            ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> store(OtpChallenge challenge, Duration ttl) {
        String key = RedisKeyBuilder.otpChallengeKey(challenge.challengeId());
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("walletId", challenge.walletId().toString());
            fields.put("tenantId", challenge.tenantId().value().toString());
            fields.put("memberId", challenge.memberId().value().toString());
            fields.put("amount", challenge.amount().toPlainString());
            fields.put("description", challenge.description() != null ? challenge.description() : "");
            fields.put("orderReference", challenge.orderReference() != null ? challenge.orderReference() : "");
            fields.put("idempotencyKey", challenge.idempotencyKey() != null ? challenge.idempotencyKey() : "");
            fields.put("otpCode", challenge.otpCode());
            fields.put("expiresAt", challenge.expiresAt().toString());
            String json = objectMapper.writeValueAsString(fields);
            return redis.opsForValue().set(key, json, ttl).then();
        } catch (Exception e) {
            return Mono.error(new IllegalStateException("Failed to serialize OTP challenge", e));
        }
    }

    @Override
    public Mono<OtpChallenge> findById(String challengeId) {
        String key = RedisKeyBuilder.otpChallengeKey(challengeId);
        return redis.opsForValue().get(key)
            .flatMap(json -> {
                try {
                    Map<String, String> fields = objectMapper.readValue(json, new TypeReference<>() {});
                    OtpChallenge challenge = new OtpChallenge(
                        challengeId,
                        UUID.fromString(fields.get("walletId")),
                        TenantId.of(fields.get("tenantId")),
                        UserId.of(fields.get("memberId")),
                        new BigDecimal(fields.get("amount")),
                        fields.get("description"),
                        fields.get("orderReference"),
                        fields.get("idempotencyKey"),
                        fields.get("otpCode"),
                        Instant.parse(fields.get("expiresAt"))
                    );
                    return Mono.just(challenge);
                } catch (Exception e) {
                    return Mono.error(new IllegalStateException("Failed to deserialize OTP challenge", e));
                }
            });
    }

    @Override
    public Mono<Void> delete(String challengeId) {
        return redis.opsForValue().delete(RedisKeyBuilder.otpChallengeKey(challengeId)).then();
    }
}
