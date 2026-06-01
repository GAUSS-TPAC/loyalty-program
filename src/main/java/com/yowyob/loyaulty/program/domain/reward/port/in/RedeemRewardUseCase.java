package com.yowyob.loyaulty.program.domain.reward.port.in;

import com.yowyob.loyaulty.program.domain.reward.model.Reward;
import com.yowyob.loyaulty.program.domain.reward.model.RewardGrant;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RedeemRewardUseCase {

    /** Échange les points d'un membre contre une récompense du catalogue. */
    Mono<RewardGrant> redeem(TenantId tenantId, String memberId, UUID rewardId);

    /** Marque un RewardGrant comme utilisé (validation par la plateforme cliente). */
    Mono<RewardGrant> consume(TenantId tenantId, UUID grantId, String useContext);

    /** Liste les récompenses actives d'un membre. */
    Flux<RewardGrant> listActiveGrants(TenantId tenantId, String memberId);

    /** Liste toutes les récompenses disponibles dans le catalogue du tenant. */
    Flux<Reward> listCatalog(TenantId tenantId);

    Mono<Reward> createReward(TenantId tenantId, String name, String description,
                               String type, long costPoints, Integer stock,
                               String validFrom, String validUntil);
}
