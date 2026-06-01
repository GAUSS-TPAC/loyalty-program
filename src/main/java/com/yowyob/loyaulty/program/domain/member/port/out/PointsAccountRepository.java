package com.yowyob.loyaulty.program.domain.member.port.out;

import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public interface PointsAccountRepository {

    Mono<PointsAccount> save(PointsAccount account);

    Mono<PointsAccount> findByMemberId(String memberId, TenantId tenantId);
}
