package com.yowyob.loyaulty.program.infrastructure.persistence.member.mapper;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.member.model.enums.MemberStatus;
import com.yowyob.loyaulty.program.domain.member.model.enums.PointsTransactionType;
import com.yowyob.loyaulty.program.domain.member.model.enums.TierLevel;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.member.entity.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MemberMapper {

    // ── Member ───────────────────────────────────────────────────────────

    public Member toDomain(MemberEntity e) {
        return Member.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getExternalId(),
                e.getEmail(),
                e.getPhone(),
                e.getDisplayName(),
                MemberStatus.valueOf(e.getStatus()),
                new AuditInfo(e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy())
        );
    }

    public MemberEntity toEntity(Member m) {
        MemberEntity e = new MemberEntity();
        e.setId(m.getId());
        e.setTenantId(m.getTenantId().value());
        e.setExternalId(m.getExternalId());
        e.setEmail(m.getEmail());
        e.setPhone(m.getPhone());
        e.setDisplayName(m.getDisplayName());
        e.setStatus(m.getStatus().name());
        e.setCreatedAt(m.getAuditInfo().createdAt());
        e.setUpdatedAt(Instant.now());
        e.setCreatedBy(m.getAuditInfo().createdBy());
        e.setUpdatedBy(m.getAuditInfo().updatedBy());
        return e;
    }

    // ── MemberTier ───────────────────────────────────────────────────────

    public MemberTier toDomain(MemberTierEntity e) {
        return MemberTier.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getMemberId(),
                TierLevel.valueOf(e.getLevel()),
                e.getLifetimePoints(),
                e.getReachedAt()
        );
    }

    public MemberTierEntity toEntity(MemberTier t) {
        MemberTierEntity e = new MemberTierEntity();
        e.setId(t.getId());
        e.setTenantId(t.getTenantId().value());
        e.setMemberId(t.getMemberId());
        e.setLevel(t.getLevel().name());
        e.setLifetimePoints(t.getLifetimePoints());
        e.setReachedAt(t.getReachedAt());
        return e;
    }

    // ── PointsAccount ────────────────────────────────────────────────────

    public PointsAccount toDomain(PointsAccountEntity e) {
        return PointsAccount.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getMemberId(),
                e.getAvailablePoints(),
                e.getLifetimeEarned(),
                e.getLifetimeSpent()
        );
    }

    public PointsAccountEntity toEntity(PointsAccount a) {
        PointsAccountEntity e = new PointsAccountEntity();
        e.setId(a.getId());
        e.setTenantId(a.getTenantId().value());
        e.setMemberId(a.getMemberId());
        e.setAvailablePoints(a.getAvailablePoints());
        e.setLifetimeEarned(a.getLifetimeEarned());
        e.setLifetimeSpent(a.getLifetimeSpent());
        return e;
    }

    // ── PointsTransaction ────────────────────────────────────────────────

    public PointsTransaction toDomain(PointsTransactionEntity e) {
        return PointsTransaction.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getMemberId(),
                PointsTransactionType.valueOf(e.getType()),
                e.getAmount(),
                e.getBalanceBefore(),
                e.getBalanceAfter(),
                e.getDescription(),
                e.getSourceReference(),
                e.getOccurredAt()
        );
    }

    public PointsTransactionEntity toEntity(PointsTransaction t) {
        PointsTransactionEntity e = new PointsTransactionEntity();
        e.setId(t.getId());
        e.setTenantId(t.getTenantId().value());
        e.setMemberId(t.getMemberId());
        e.setType(t.getType().name());
        e.setAmount(t.getAmount());
        e.setBalanceBefore(t.getBalanceBefore());
        e.setBalanceAfter(t.getBalanceAfter());
        e.setDescription(t.getDescription());
        e.setSourceReference(t.getSourceReference());
        e.setOccurredAt(t.getOccurredAt());
        return e;
    }
}
