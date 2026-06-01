package com.yowyob.loyaulty.program.infrastructure.persistence.reward.mapper;

import com.yowyob.loyaulty.program.domain.reward.model.Reward;
import com.yowyob.loyaulty.program.domain.reward.model.RewardGrant;
import com.yowyob.loyaulty.program.domain.reward.model.enums.GrantStatus;
import com.yowyob.loyaulty.program.domain.reward.model.enums.RewardStatus;
import com.yowyob.loyaulty.program.domain.reward.model.enums.RewardType;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.infrastructure.persistence.reward.entity.RewardEntity;
import com.yowyob.loyaulty.program.infrastructure.persistence.reward.entity.RewardGrantEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RewardMapper {

    public Reward toDomain(RewardEntity e) {
        return Reward.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getName(),
                e.getDescription(),
                RewardType.valueOf(e.getType()),
                e.getCostPoints(),
                e.getStock(),
                RewardStatus.valueOf(e.getStatus()),
                e.getValidFrom(),
                e.getValidUntil(),
                e.getCreatedAt()
        );
    }

    public RewardEntity toEntity(Reward r) {
        RewardEntity e = new RewardEntity();
        e.setId(r.getId());
        e.setTenantId(r.getTenantId().value());
        e.setName(r.getName());
        e.setDescription(r.getDescription());
        e.setType(r.getType().name());
        e.setCostPoints(r.getCostPoints());
        e.setStock(r.getStock());
        e.setStatus(r.getStatus().name());
        e.setValidFrom(r.getValidFrom());
        e.setValidUntil(r.getValidUntil());
        e.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    public RewardGrant toDomain(RewardGrantEntity e) {
        return RewardGrant.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getMemberId(),
                e.getRewardId(),
                GrantStatus.valueOf(e.getStatus()),
                e.getGrantedAt(),
                e.getExpiresAt(),
                e.getUsedAt(),
                e.getUseContext()
        );
    }

    public RewardGrantEntity toEntity(RewardGrant g) {
        RewardGrantEntity e = new RewardGrantEntity();
        e.setId(g.getId());
        e.setTenantId(g.getTenantId().value());
        e.setMemberId(g.getMemberId());
        e.setRewardId(g.getRewardId());
        e.setStatus(g.getStatus().name());
        e.setGrantedAt(g.getGrantedAt());
        e.setExpiresAt(g.getExpiresAt());
        e.setUsedAt(g.getUsedAt());
        e.setUseContext(g.getUseContext());
        return e;
    }
}
