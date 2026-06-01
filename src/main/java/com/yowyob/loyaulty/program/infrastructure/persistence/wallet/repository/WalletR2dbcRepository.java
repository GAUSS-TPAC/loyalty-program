package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository;

import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository Spring Data R2DBC pour la table {@code wallets}.
 *
 * <p>Étend {@link ReactiveCrudRepository} pour les opérations CRUD de base.
 * Les requêtes personnalisées utilisent {@code @Query} avec SQL natif PostgreSQL.</p>
 */
@Repository
public interface WalletR2dbcRepository extends ReactiveCrudRepository<WalletEntity, UUID> {

    /**
     * Cherche un wallet par son identifiant en respectant le cloisonnement tenant.
     * Protège contre les accès cross-tenant (vérification double : id + tenant_id).
     *
     * @param id       identifiant du wallet.
     * @param tenantId identifiant du tenant.
     * @return le wallet ou {@code Mono.empty()}.
     */
    Mono<WalletEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Cherche le wallet d'un membre dans un tenant donné.
     *
     * @param memberId identifiant du membre.
     * @param tenantId identifiant du tenant.
     * @return le wallet ou {@code Mono.empty()}.
     */
    Mono<WalletEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId);

    /**
     * Vérifie l'existence d'un wallet pour un membre dans un tenant.
     *
     * @param memberId identifiant du membre.
     * @param tenantId identifiant du tenant.
     * @return {@code true} si un wallet existe.
     */
    Mono<Boolean> existsByMemberIdAndTenantId(UUID memberId, UUID tenantId);

    /**
     * Met à jour le statut et les informations de gel d'un wallet.
     * Requête ciblée pour éviter de recharger et ré-écrire tout le wallet.
     *
     * @param id           identifiant du wallet à mettre à jour.
     * @param tenantId     identifiant du tenant (sécurité).
     * @param status       nouveau statut (nom de l'enum sous forme de VARCHAR).
     * @param freezeReason motif du gel ({@code NULL} si dégel ou autre).
     * @return nombre de lignes affectées (0 si wallet introuvable).
     */
    @Modifying
    @Query("""
            UPDATE wallets
            SET    status        = :status,
                   freeze_reason = :freezeReason,
                   frozen_at     = CASE WHEN :status = 'FROZEN' THEN NOW() ELSE NULL END,
                   closed_at     = CASE WHEN :status = 'CLOSED' THEN NOW() ELSE NULL END,
                   updated_at    = NOW()
            WHERE  id        = :id
              AND  tenant_id = :tenantId
            """)
    Mono<Integer> updateStatus(UUID id, UUID tenantId, String status, String freezeReason);

    /**
     * Met à jour le solde disponible et le solde réservé atomiquement.
     * Utilisé après chaque crédit / débit pour maintenir la cohérence.
     *
     * @param id               identifiant du wallet.
     * @param tenantId         identifiant du tenant.
     * @param availableBalance nouveau solde disponible.
     * @param reservedBalance  nouveau solde réservé.
     * @return nombre de lignes affectées.
     */
    @Modifying
    @Query("""
            UPDATE wallets
            SET    available_balance = :availableBalance,
                   reserved_balance  = :reservedBalance,
                   updated_at        = NOW()
            WHERE  id        = :id
              AND  tenant_id = :tenantId
            """)
    Mono<Integer> updateBalances(UUID id, UUID tenantId,
                                  java.math.BigDecimal availableBalance,
                                  java.math.BigDecimal reservedBalance);
}
