package com.yowyob.loyaulty.program.domain.wallet.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité d'audit immuable pour toutes les actions sensibles sur un wallet.
 *
 * <p>Chaque action administrative (gel, dégel, clôture, ajustement manuel,
 * surcharge de policy) génère une entrée dans l'AuditLog. Ces entrées ne sont
 * jamais mises à jour ni supprimées.</p>
 *
 * <p>L'AuditLog sert à :</p>
 * <ul>
 *   <li>Tracer le motif et l'acteur de chaque action sensible.</li>
 *   <li>Répondre aux exigences réglementaires (KYC, compliance).</li>
 *   <li>Fournir une piste d'investigation en cas de litige ou de fraude.</li>
 * </ul>
 */
@Getter
@Builder
public class WalletAuditLog {

    // ── Identité ─────────────────────────────────────────────────────────────

    /** Identifiant unique de l'entrée d'audit. */
    private final UUID id;

    /** Identifiant du wallet concerné par l'action. */
    private final UUID walletId;

    /** Identifiant du tenant (pour le partitionnement multi-tenant). */
    private final UUID tenantId;

    // ── Acteur ────────────────────────────────────────────────────────────────

    /**
     * Identifiant de l'utilisateur ayant réalisé l'action.
     * Peut être un UUID admin, un ID système (pour les actions automatiques)
     * ou l'ID d'un service IA.
     */
    private final String actorId;

    /**
     * Type d'acteur : "ADMIN", "SYSTEM", "AI_SERVICE", "MEMBER".
     * Permet de distinguer les actions humaines des actions automatiques.
     */
    private final String actorType;

    // ── Action ────────────────────────────────────────────────────────────────

    /**
     * Code de l'action réalisée.
     * Exemples : "FREEZE", "UNFREEZE", "CLOSE", "POLICY_OVERRIDE",
     * "MANUAL_CREDIT", "FRAUD_DETECTED", "KYC_VALIDATED".
     */
    private final String action;

    /**
     * Motif détaillé de l'action (obligatoire pour les actions sensibles).
     * Ex. : "Comportement suspect : 5 débits en 2 minutes", "Demande du client".
     */
    private final String reason;

    // ── Contexte ──────────────────────────────────────────────────────────────

    /**
     * État du wallet AVANT l'action (pour traçabilité complète).
     */
    private final String previousStatus;

    /**
     * État du wallet APRÈS l'action.
     */
    private final String newStatus;

    /**
     * Identifiant de la transaction liée, si l'action concerne une transaction.
     * Null pour les actions purement administratives (gel, dégel…).
     */
    private final UUID relatedTransactionId;

    /**
     * Métadonnées additionnelles en JSON (contexte IA, paramètres de fraude…).
     */
    private final String metadata;

    // ── Contexte réseau ───────────────────────────────────────────────────────

    /**
     * Adresse IP de l'acteur au moment de l'action (pour les actions humaines).
     * Null pour les actions système.
     */
    private final String ipAddress;

    /**
     * User-Agent HTTP de l'acteur (pour les actions via l'API).
     */
    private final String userAgent;

    // ── Horodatage ────────────────────────────────────────────────────────────

    /**
     * Date/heure UTC exacte de l'action.
     * Non modifiable après création.
     */
    private final Instant occurredAt;
}
