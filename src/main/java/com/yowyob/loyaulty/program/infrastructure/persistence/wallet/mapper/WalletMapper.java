package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletPolicy;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletOperation;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletStatus;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WalletMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public Wallet toDomain(WalletEntity e) {
        return Wallet.reconstitute(
                e.getId(),
                TenantId.of(e.getTenantId()),
                e.getMemberId(),
                e.getCurrency(),
                e.getBalance(),
                WalletStatus.valueOf(e.getStatus()),
                parsePolicy(e.getPolicy()),
                new AuditInfo(e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy())
        );
    }

    public WalletEntity toEntity(Wallet w) {
        WalletEntity e = new WalletEntity();
        e.setId(w.getId());
        e.setTenantId(w.getTenantId().value());
        e.setMemberId(w.getMemberId());
        e.setCurrency(w.getCurrency());
        e.setBalance(w.getBalance());
        e.setStatus(w.getStatus().name());
        e.setPolicy(serializePolicy(w.getPolicy()));
        e.setCreatedAt(w.getAuditInfo().createdAt());
        e.setUpdatedAt(Instant.now());
        e.setCreatedBy(w.getAuditInfo().createdBy());
        e.setUpdatedBy(w.getAuditInfo().updatedBy());
        return e;
    }

    @SuppressWarnings("unchecked")
    private WalletPolicy parsePolicy(String json) {
        if (json == null || json.isBlank()) return WalletPolicy.defaults();
        try {
            Map<String, Object> map = mapper.readValue(json, new TypeReference<>() {});
            Set<WalletOperation> ops = ((List<String>) map.getOrDefault("allowedOperations",
                    List.of("TOPUP", "PURCHASE"))).stream()
                    .map(WalletOperation::valueOf).collect(Collectors.toSet());
            return new WalletPolicy(
                    bd(map, "maxBalance", "10000000"),
                    bd(map, "maxTopupPerTransaction", "500000"),
                    bd(map, "dailySpendCap", "1000000"),
                    bd(map, "minBalance", "0"),
                    ((Number) map.getOrDefault("withdrawDelayHours", 24)).intValue(),
                    (boolean) map.getOrDefault("kycRequired", false),
                    ops
            );
        } catch (Exception ex) {
            return WalletPolicy.defaults();
        }
    }

    private String serializePolicy(WalletPolicy p) {
        try {
            return mapper.writeValueAsString(Map.of(
                    "maxBalance", p.maxBalance(),
                    "maxTopupPerTransaction", p.maxTopupPerTransaction(),
                    "dailySpendCap", p.dailySpendCap(),
                    "minBalance", p.minBalance(),
                    "withdrawDelayHours", p.withdrawDelayHours(),
                    "kycRequired", p.kycRequired(),
                    "allowedOperations", p.allowedOperations().stream()
                            .map(Enum::name).collect(Collectors.toList())
            ));
        } catch (Exception ex) {
            return "{}";
        }
    }

    private BigDecimal bd(Map<String, Object> map, String key, String defaultVal) {
        Object v = map.get(key);
        if (v == null) return new BigDecimal(defaultVal);
        return new BigDecimal(v.toString());
    }
}
