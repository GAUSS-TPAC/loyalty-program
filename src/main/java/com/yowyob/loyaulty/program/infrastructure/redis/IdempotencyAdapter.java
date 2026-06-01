package com.yowyob.loyaulty.program.infrastructure.redis;

import com.yowyob.loyaulty.program.domain.wallet.port.out.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Adaptateur Redis pour la gestion de l'idempotence des opérations wallet.
 *
 * <p>Stratégie : chaque clé est stockée sous la forme {@code idempotency:{tenantId}:{key}}
 * avec un TTL configurable. La valeur stockée est le payload JSON du résultat initial,
 * permettant de rejouer une réponse sans re-traitement.</p>
 *
 * <p>L'opération {@link #registerIfAbsent} utilise la commande Redis {@code SETNX}
 * (SET if Not eXists) via {@code setIfAbsent}, qui est atomique et garantit
 * qu'un seul thread "gagne" en cas de requêtes concurrentes identiques.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyAdapter implements IdempotencyPort {

    private static final String KEY_PREFIX = "idempotency";

    private final ReactiveStringRedisTemplate redisTemplate;

    // ── Implémentation du port ────────────────────────────────────────────────

    @Override
    public Mono<Boolean> exists(String idempotencyKey, String tenantId) {
        String redisKey = buildKey(idempotencyKey, tenantId);
        return redisTemplate.hasKey(redisKey)
                .doOnNext(exists -> {
                    if (exists) {
                        log.debug("Clé idempotence trouvée : key={}, tenant={}", idempotencyKey, tenantId);
                    }
                });
    }

    @Override
    public Mono<Boolean> registerIfAbsent(String idempotencyKey, String tenantId,
                                           Duration ttl, String resultPayload) {
        String redisKey = buildKey(idempotencyKey, tenantId);

        // SETNX atomique : retourne true si la clé vient d'être créée (premier appel)
        return redisTemplate.opsForValue()
                .setIfAbsent(redisKey, resultPayload != null ? resultPayload : "", ttl)
                .defaultIfEmpty(false)
                .doOnNext(created -> {
                    if (created) {
                        log.debug("Clé idempotence enregistrée : key={}, tenant={}, ttl={}",
                                idempotencyKey, tenantId, ttl);
                    } else {
                        log.warn("Doublon détecté — clé idempotence déjà existante : key={}, tenant={}",
                                idempotencyKey, tenantId);
                    }
                });
    }

    @Override
    public Mono<String> getResult(String idempotencyKey, String tenantId) {
        String redisKey = buildKey(idempotencyKey, tenantId);
        return redisTemplate.opsForValue()
                .get(redisKey)
                .filter(payload -> !payload.isBlank())
                .doOnNext(payload -> log.debug("Payload idempotence récupéré : key={}", idempotencyKey));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Construit la clé Redis composite : {@code idempotency:{tenantId}:{idempotencyKey}}.
     * Le tenantId est inclus pour cloisonner les namespaces par tenant.
     */
    private String buildKey(String idempotencyKey, String tenantId) {
        return KEY_PREFIX + ":" + tenantId + ":" + idempotencyKey;
    }
}
