package com.yowyob.loyaulty.program.application.loyalty.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.loyalty.port.in.ProcessBonificationUseCase;
import com.yowyob.loyaulty.program.domain.loyalty.service.BonificationDomainService;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.port.out.IdempotencyPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ProcessBonificationHandler implements ProcessBonificationUseCase {

    private final BonificationDomainService domainService;
    private final IdempotencyPort idempotencyPort;
    private final ObjectMapper objectMapper;

    public ProcessBonificationHandler(BonificationDomainService domainService,
                                      IdempotencyPort idempotencyPort,
                                      ObjectMapper objectMapper) {
        this.domainService = domainService;
        this.idempotencyPort = idempotencyPort;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<PointsResult> processTransaction(TenantId tenantId,
                                                  String memberId,
                                                  BigDecimal amount,
                                                  String description,
                                                  String idempotencyKey) {
        return idempotencyPort.isNew(idempotencyKey)
                .flatMap(isNew -> {
                    if (!isNew) {
                        return idempotencyPort.getStoredResponse(idempotencyKey)
                                .flatMap(json -> deserialize(json, PointsResult.class));
                    }
                    return idempotencyPort.markProcessing(idempotencyKey)
                            .then(domainService.processTransaction(tenantId, memberId, amount, description))
                            .flatMap(result -> idempotencyPort.storeResponse(idempotencyKey, serialize(result))
                                    .thenReturn(result));
                });
    }

    @Override
    public Flux<BonificationTransaction> getMemberTransactionHistory(TenantId tenantId, String memberId) {
        return domainService.getTransactionHistory(tenantId, memberId);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize response", e);
        }
    }

    private <T> Mono<T> deserialize(String json, Class<T> type) {
        try {
            return Mono.just(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException("Failed to deserialize cached response", e));
        }
    }
}
