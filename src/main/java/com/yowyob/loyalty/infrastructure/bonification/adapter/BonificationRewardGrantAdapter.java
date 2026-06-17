package com.yowyob.loyalty.infrastructure.bonification.adapter;

import com.yowyob.loyalty.application.bonification.BonificationCredentialsResolver;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionRequest;
import com.yowyob.loyalty.domain.bonification.port.out.BonificationPort;
import com.yowyob.loyalty.domain.loyalty.port.out.RewardGrantPort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.springframework.stereotype.Component;

@Component
public class BonificationRewardGrantAdapter implements RewardGrantPort {

    private final BonificationPort bonificationPort;
    private final BonificationCredentialsResolver credentialsResolver;

    public BonificationRewardGrantAdapter(
            BonificationPort bonificationPort,
            BonificationCredentialsResolver credentialsResolver) {
        this.bonificationPort = bonificationPort;
        this.credentialsResolver = credentialsResolver;
    }

    @Override
    public void grantReward(TenantId tenantId, UserId memberId, String rewardId, double amount) {
        if (amount <= 0) return;
        String clientLogin = memberId.value().toString();
        BonificationTransactionRequest request = BonificationTransactionRequest.credit(amount, clientLogin);
        credentialsResolver.resolve(tenantId)
                .flatMap(credentials -> bonificationPort.submitTransaction(tenantId, credentials, request))
                .subscribe();
    }
}
