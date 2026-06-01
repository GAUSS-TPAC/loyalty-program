package com.yowyob.loyaulty.program.domain.wallet.port.in;

import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import reactor.core.publisher.Mono;

public interface GetTransactionHistoryUseCase {
    Mono<PageResult<WalletTransaction>> getHistory(TenantId tenantId, String memberId, PageRequest pageRequest);
}
