package com.yowyob.loyaulty.program.infrastructure.redis;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

/**
 * Adaptateur Redis pour le cache des soldes de wallets.
 *
 * <p>Objectif : éviter des requêtes PostgreSQL coûteuses pour les lectures
 * fréquentes de solde (F5 — Consultation du solde). Le cache est invalidé
 * à chaque opération de crédit/débit/gel.</p>
 *
 * <p>Structure des clés Redis :</p>
 * <ul>
 *   <li>{@code wallet:balance:{tenantId}:{walletId}} → solde disponible (String NUMERIC)</li>
 *   <li>{@code wallet:status:{tenantId}:{walletId}}  → statut courant (String)</li>
 * </ul>
 *
 * <p>TTL par défaut : 5 minutes pour les soldes, 10 minutes pour le statut.
 * Reconfigurable via les propriétés de l'application.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletCacheAdapter {

    private static final String BALANCE_PREFIX = "wallet:balance";
    private static final String STATUS_PREFIX  = "wallet:status";
    private static final String MEMBER_PREFIX  = "wallet:member";

    private static final Duration BALANCE_TTL = Duration.ofMinutes(5);
    private static final Duration STATUS_TTL  = Duration.ofMinutes(10);
    private static final Duration MEMBER_TTL  = Duration.ofMinutes(15);

    private final ReactiveStringRedisTemplate redisTemplate;

    // ── Solde disponible ─────────────────────────────────────────────────────

    /**
     * Met en cache le solde disponible d'un wallet.
     *
     * @param walletId         identifiant du wallet.
     * @param tenantId         identifiant du tenant.
     * @param availableBalance solde disponible à mettre en cache.
     * @return {@code Mono<Void>} completé quand le cache est écrit.
     */
    public Mono<Void> cacheBalance(UUID walletId, UUID tenantId, BigDecimal availableBalance) {
        String key = buildBalanceKey(walletId, tenantId);
        return redisTemplate.opsForValue()
                .set(key, availableBalance.toPlainString(), BALANCE_TTL)
                .doOnSuccess(ok -> log.debug("Solde mis en cache : walletId={}, balance={}", walletId, availableBalance))
                .then();
    }

    /**
     * Récupère le solde disponible depuis le cache.
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return le solde ou {@code Mono.empty()} en cas de cache miss.
     */
    public Mono<BigDecimal> getCachedBalance(UUID walletId, UUID tenantId) {
        String key = buildBalanceKey(walletId, tenantId);
        return redisTemplate.opsForValue()
                .get(key)
                .map(BigDecimal::new)
                .doOnNext(balance -> log.debug("Cache hit solde : walletId={}, balance={}", walletId, balance))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache miss solde : walletId={}", walletId);
                    return Mono.empty();
                }));
    }

    // ── Statut ───────────────────────────────────────────────────────────────

    /**
     * Met en cache le statut d'un wallet.
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @param status   statut à mettre en cache (ex. "ACTIVE", "FROZEN").
     * @return {@code Mono<Void>} completé quand le cache est écrit.
     */
    public Mono<Void> cacheStatus(UUID walletId, UUID tenantId, String status) {
        String key = buildStatusKey(walletId, tenantId);
        return redisTemplate.opsForValue()
                .set(key, status, STATUS_TTL)
                .then();
    }

    /**
     * Récupère le statut depuis le cache.
     *
     * @param walletId identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return le statut ou {@code Mono.empty()} en cas de cache miss.
     */
    public Mono<String> getCachedStatus(UUID walletId, UUID tenantId) {
        return redisTemplate.opsForValue().get(buildStatusKey(walletId, tenantId));
    }

    // ── Wallet ID par membre ──────────────────────────────────────────────────

    /**
     * Met en cache la correspondance memberId → walletId pour éviter les lookups répétés.
     *
     * @param memberId identifiant du membre.
     * @param tenantId identifiant du tenant.
     * @param walletId identifiant du wallet correspondant.
     * @return {@code Mono<Void>} completé quand le cache est écrit.
     */
    public Mono<Void> cacheMemberWalletId(UUID memberId, UUID tenantId, UUID walletId) {
        String key = MEMBER_PREFIX + ":" + tenantId + ":" + memberId;
        return redisTemplate.opsForValue()
                .set(key, walletId.toString(), MEMBER_TTL)
                .then();
    }

    /**
     * Récupère le walletId d'un membre depuis le cache.
     *
     * @param memberId identifiant du membre.
     * @param tenantId identifiant du tenant.
     * @return le walletId ou {@code Mono.empty()} en cas de cache miss.
     */
    public Mono<UUID> getCachedWalletIdForMember(UUID memberId, UUID tenantId) {
        String key = MEMBER_PREFIX + ":" + tenantId + ":" + memberId;
        return redisTemplate.opsForValue()
                .get(key)
                .map(UUID::fromString);
    }

    // ── Invalidation ─────────────────────────────────────────────────────────

    /**
     * Invalide toutes les entrées de cache liées à un wallet.
     * À appeler après chaque opération modifiant le solde ou le statut.
     *
     * @param walletId identifiant du wallet à invalider.
     * @param tenantId identifiant du tenant.
     * @return {@code Mono<Void>} completé quand les clés sont supprimées.
     */
    public Mono<Void> evict(UUID walletId, UUID tenantId) {
        return redisTemplate.delete(
                        buildBalanceKey(walletId, tenantId),
                        buildStatusKey(walletId, tenantId)
                )
                .doOnNext(count -> log.debug("Cache invalidé : walletId={}, keysDeleted={}", walletId, count))
                .then();
    }

    /**
     * Met en cache l'ensemble des informations d'un wallet après une opération.
     * Méthode de confort qui combine cacheBalance + cacheStatus.
     *
     * @param wallet le wallet dont on met à jour le cache.
     * @return {@code Mono<Void>} completé quand le cache est écrit.
     */
    public Mono<Void> cacheWallet(Wallet wallet) {
        return Mono.when(
                cacheBalance(wallet.getId(), wallet.getTenantId(), wallet.getAvailableBalance()),
                cacheStatus(wallet.getId(), wallet.getTenantId(), wallet.getStatus().name())
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildBalanceKey(UUID walletId, UUID tenantId) {
        return BALANCE_PREFIX + ":" + tenantId + ":" + walletId;
    }

    private String buildStatusKey(UUID walletId, UUID tenantId) {
        return STATUS_PREFIX + ":" + tenantId + ":" + walletId;
    }
}
