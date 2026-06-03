package com.yowyob.loyalty.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.Wallet;
import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "tenantId", expression = "java(mapTenantId(entity.getTenantId()))")
    @Mapping(target = "memberId", expression = "java(mapUserId(entity.getMemberId()))")
    Wallet toDomain(WalletEntity entity);

    @Mapping(target = "tenantId", expression = "java(domain.getTenantId().value())")
    @Mapping(target = "memberId", expression = "java(domain.getMemberId().value())")
    WalletEntity toEntity(Wallet domain);

    default TenantId mapTenantId(UUID value) { return value != null ? TenantId.of(value) : null; }
    default UserId mapUserId(UUID value) { return value != null ? UserId.of(value) : null; }
}
