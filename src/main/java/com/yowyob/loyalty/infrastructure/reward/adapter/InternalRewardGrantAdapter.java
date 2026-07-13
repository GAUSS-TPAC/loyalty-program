package com.yowyob.loyalty.infrastructure.reward.adapter;

import com.yowyob.loyalty.domain.loyalty.port.out.RewardGrantPort;
import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fulfils the rule engine's GRANT_REWARD effect against loyalty-program's own
 * reward catalog (domain/reward), instead of the external BonusAPI partner.
 */
@Component
public class InternalRewardGrantAdapter implements RewardGrantPort {

    private static final Logger log = LoggerFactory.getLogger(InternalRewardGrantAdapter.class);

    private final GrantRewardUseCase grantRewardUseCase;

    public InternalRewardGrantAdapter(GrantRewardUseCase grantRewardUseCase) {
        this.grantRewardUseCase = grantRewardUseCase;
    }

    @Override
    public void grantReward(TenantId tenantId, UserId memberId, String rewardId, double amount,
                             UUID sourceRuleId, String sourceEventKey) {
        UUID rewardUuid;
        try {
            rewardUuid = UUID.fromString(rewardId);
        } catch (IllegalArgumentException e) {
            log.warn("GRANT_REWARD effect skipped: reward_id '{}' is not a valid reward catalog UUID", rewardId);
            return;
        }

        grantRewardUseCase.grantReward(tenantId, memberId, rewardUuid, GrantSource.RULE_ENGINE,
                        sourceRuleId, sourceEventKey, sourceEventKey)
                .subscribe(
                        grant -> log.debug("Reward {} granted to member {} via rule {}", rewardUuid, memberId, sourceRuleId),
                        error -> log.warn("GRANT_REWARD effect failed for member {} reward {}: {}",
                                memberId, rewardId, error.getMessage())
                );
    }
}
