package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import com.yowyob.loyalty.domain.wallet.port.in.UpdateWalletPolicyUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.WalletPolicyRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class UpdateWalletPolicyHandler implements UpdateWalletPolicyUseCase {

    private final WalletPolicyRepository policyRepo;

    public UpdateWalletPolicyHandler(WalletPolicyRepository policyRepo) {
        this.policyRepo = policyRepo;
    }

    @Override
    public Mono<WalletPolicy> updatePointsConversion(
            TenantId tenantId, String currencyName, String currencySymbol, BigDecimal exchangeRate) {
        // findByTenant retombe sur WalletPolicy.defaults() si le tenant n'a pas encore
        // de politique : la mise à jour vaut alors création avec les limites par défaut.
        return policyRepo.findByTenant(tenantId)
                .map(current -> current.withPointsConversion(currencyName, currencySymbol, exchangeRate))
                .flatMap(updated -> policyRepo.save(tenantId, updated));
    }
}
