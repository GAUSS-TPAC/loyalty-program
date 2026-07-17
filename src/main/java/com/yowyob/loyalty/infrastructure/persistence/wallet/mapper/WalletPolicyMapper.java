package com.yowyob.loyalty.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletPolicyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletPolicyMapper {
    @Mapping(target = "maxTopupPerTransaction", source = "maxTopupPerTxn")
    @Mapping(target = "kycRequiredForWithdrawal", source = "kycRequired")
    WalletPolicy toDomain(WalletPolicyEntity entity);

    @Mapping(target = "maxTopupPerTxn", source = "maxTopupPerTransaction")
    @Mapping(target = "kycRequired", source = "kycRequiredForWithdrawal")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    WalletPolicyEntity toEntity(WalletPolicy domain);
}
