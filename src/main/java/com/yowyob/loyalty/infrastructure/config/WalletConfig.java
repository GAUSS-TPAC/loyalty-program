package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.domain.wallet.port.out.*;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletConfig {

    @Bean
    public WalletDomainService walletDomainService(
        WalletRepository walletRepo,
        WalletTransactionRepository txRepo,
        WalletPolicyRepository policyRepo,
        WalletAuditLogRepository auditRepo,
        WalletEventPublisherPort eventPublisher
    ) {
        return new WalletDomainService(walletRepo, txRepo, policyRepo, auditRepo, eventPublisher);
    }
}
