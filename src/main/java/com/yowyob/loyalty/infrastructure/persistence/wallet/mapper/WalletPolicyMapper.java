package com.yowyob.loyalty.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletPolicyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletPolicyMapper {
    WalletPolicy toDomain(WalletPolicyEntity entity);
    WalletPolicyEntity toEntity(WalletPolicy domain);
}
