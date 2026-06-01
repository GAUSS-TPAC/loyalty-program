package com.yowyob.loyaulty.program.domain.member.port.in;

import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface GetPointsBalanceUseCase {

    Mono<PointsAccount> getBalance(TenantId tenantId, String memberId);

    Mono<PageResult<PointsTransaction>> getHistory(TenantId tenantId, String memberId,
                                                    PageRequest pageRequest);
}
