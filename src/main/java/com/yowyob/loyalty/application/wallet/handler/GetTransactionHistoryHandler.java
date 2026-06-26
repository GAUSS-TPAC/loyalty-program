package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.TransactionType;
import com.yowyob.loyalty.domain.wallet.model.WalletTransaction;
import com.yowyob.loyalty.domain.wallet.port.in.GetTransactionHistoryUseCase;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Service
public class GetTransactionHistoryHandler implements GetTransactionHistoryUseCase {

    private final WalletDomainService domainService;

    public GetTransactionHistoryHandler(WalletDomainService domainService) {
        this.domainService = domainService;
    }

    @Override
    public Flux<WalletTransaction> getHistory(TenantId tenantId, UserId memberId, TransactionType typeFilter, TransactionSource sourceFilter, Instant from, Instant to, int page, int size) {
        return domainService.getHistory(tenantId, memberId, typeFilter, sourceFilter, from, to, page, size);
    }
}
