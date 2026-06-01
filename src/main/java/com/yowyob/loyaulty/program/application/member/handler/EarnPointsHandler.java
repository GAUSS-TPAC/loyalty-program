package com.yowyob.loyaulty.program.application.member.handler;

import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.member.model.enums.TierLevel;
import com.yowyob.loyaulty.program.domain.member.port.in.EarnPointsUseCase;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberTierRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsAccountRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsTransactionRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EarnPointsHandler implements EarnPointsUseCase {

    private final PointsAccountRepository accountRepository;
    private final PointsTransactionRepository txRepository;
    private final MemberTierRepository tierRepository;

    public EarnPointsHandler(PointsAccountRepository accountRepository,
                              PointsTransactionRepository txRepository,
                              MemberTierRepository tierRepository) {
        this.accountRepository = accountRepository;
        this.txRepository = txRepository;
        this.tierRepository = tierRepository;
    }

    @Override
    public Mono<PointsAccount> earn(TenantId tenantId, String memberId,
                                     long basePoints, String description, String sourceRef) {
        Mono<PointsAccount> accountMono = accountRepository.findByMemberId(memberId, tenantId)
                .switchIfEmpty(Mono.defer(() -> {
                    PointsAccount newAccount = PointsAccount.create(tenantId, memberId);
                    return accountRepository.save(newAccount);
                }));

        Mono<MemberTier> tierMono = tierRepository.findByMemberId(memberId, tenantId)
                .switchIfEmpty(Mono.defer(() -> {
                    MemberTier newTier = MemberTier.create(tenantId, memberId);
                    return tierRepository.save(newTier);
                }));

        return Mono.zip(accountMono, tierMono)
                .flatMap(tuple -> {
                    PointsAccount account = tuple.getT1();
                    MemberTier tier = tuple.getT2();

                    long effectivePoints = Math.round(basePoints * tier.getMultiplier());

                    PointsTransaction tx = account.earn(effectivePoints, description, sourceRef);
                    boolean tierChanged = tier.addPoints(effectivePoints);

                    Mono<PointsTransaction> saveTx = txRepository.save(tx);
                    Mono<PointsAccount> saveAccount = accountRepository.save(account);
                    Mono<MemberTier> saveTier = tierChanged
                            ? tierRepository.save(tier)
                            : Mono.just(tier);

                    return Mono.zip(saveTx, saveAccount, saveTier)
                            .thenReturn(account);
                });
    }
}
