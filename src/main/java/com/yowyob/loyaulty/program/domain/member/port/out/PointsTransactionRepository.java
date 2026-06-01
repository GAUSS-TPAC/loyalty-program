package com.yowyob.loyaulty.program.domain.member.port.out;

import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.shared.model.PageResult;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface PointsTransactionRepository {

    Mono<PointsTransaction> save(PointsTransaction tx);

    Mono<PageResult<PointsTransaction>> findByMemberId(String memberId, TenantId tenantId,
                                                        PageRequest pageRequest);
}
