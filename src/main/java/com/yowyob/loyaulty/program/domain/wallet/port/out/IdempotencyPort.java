package com.yowyob.loyaulty.program.domain.wallet.port.out;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Port de sortie (driven) : gestion de l'idempotence via un store distribué.
 *
 * <p>Avant tout traitement d'une opération de crédit ou de débit, le domaine
 * vérifie si la clé d'idempotence a déjà été traitée. Si oui, le résultat
 * précédent est retourné sans re-traitement.</p>
 *
 * <p>Typiquement implémenté avec Redis (TTL configurable par type d'opération).</p>
 */
public interface IdempotencyPort {

    /**
     * Vérifie si une clé d'idempotence a déjà été enregistrée.
     *
     * @param idempotencyKey clé unique fournie par l'appelant.
     * @param tenantId       identifiant du tenant (cloisonnement).
     * @return {@code true} si la clé existe déjà (doublon), {@code false} sinon.
     */
    Mono<Boolean> exists(String idempotencyKey, String tenantId);

    /**
     * Enregistre une clé d'idempotence avec un TTL.
     * Doit être appelé <strong>avant</strong> de persister la transaction,
     * dans une opération atomique (compare-and-set si possible).
     *
     * @param idempotencyKey clé à enregistrer.
     * @param tenantId       identifiant du tenant.
     * @param ttl            durée de vie de la clé (après laquelle une nouvelle
     *                       tentative sera traitée comme une nouvelle opération).
     * @param resultPayload  payload JSON du résultat associé (pour le retourner en cas de doublon).
     * @return {@code true} si la clé a été créée (premier appel), {@code false} si elle existait déjà.
     */
    Mono<Boolean> registerIfAbsent(String idempotencyKey, String tenantId, Duration ttl, String resultPayload);

    /**
     * Récupère le payload JSON du résultat associé à une clé d'idempotence.
     * Permet de retourner la réponse originale sans re-traitement.
     *
     * @param idempotencyKey clé d'idempotence.
     * @param tenantId       identifiant du tenant.
     * @return le payload JSON ou {@code Mono.empty()} si la clé n'existe pas.
     */
    Mono<String> getResult(String idempotencyKey, String tenantId);
}
