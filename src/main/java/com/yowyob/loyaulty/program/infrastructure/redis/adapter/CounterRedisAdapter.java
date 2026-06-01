package com.yowyob.loyaulty.program.infrastructure.redis.adapter;

import com.yowyob.loyaulty.program.domain.rule.port.out.CounterRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.shared.util.RedisKeyBuilder;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class CounterRedisAdapter implements CounterRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    public CounterRedisAdapter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Long> increment(TenantId tenantId, String memberId, UUID ruleId) {
        String key = RedisKeyBuilder.counterKey(tenantId, memberId, ruleId.toString());
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Mono<Void> reset(TenantId tenantId, String memberId, UUID ruleId) {
        String key = RedisKeyBuilder.counterKey(tenantId, memberId, ruleId.toString());
        return redisTemplate.delete(key).then();
    }

    @Override
    public Mono<Map<UUID, Long>> getAll(TenantId tenantId, String memberId, List<UUID> ruleIds) {
        if (ruleIds.isEmpty()) return Mono.just(Map.of());

        List<String> keys = ruleIds.stream()
                .map(id -> RedisKeyBuilder.counterKey(tenantId, memberId, id.toString()))
                .toList();

        // Construire une map ruleId → key pour retrouver la correspondance après
        Map<String, UUID> keyToRuleId = new HashMap<>();
        for (int i = 0; i < ruleIds.size(); i++) {
            keyToRuleId.put(keys.get(i), ruleIds.get(i));
        }

        return Flux.fromIterable(keys)
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .defaultIfEmpty("0")
                        .map(Long::parseLong)
                        .map(val -> Map.entry(key, val))
                )
                .collectList()
                .map(entries -> {
                    Map<UUID, Long> result = new HashMap<>();
                    for (var entry : entries) {
                        UUID ruleId = keyToRuleId.get(entry.getKey());
                        if (ruleId != null) result.put(ruleId, entry.getValue());
                    }
                    return result;
                });
    }
}
