package com.yowyob.loyalty.application.loyalty.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.port.in.ProcessEventUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.IdempotencyPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ProcessEventHandler {

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final ProcessEventUseCase processEventUseCase;
    private final IdempotencyPort idempotencyPort;
    private final ObjectMapper objectMapper;

    public ProcessEventHandler(
            ProcessEventUseCase processEventUseCase,
            IdempotencyPort idempotencyPort,
            ObjectMapper objectMapper
    ) {
        this.processEventUseCase = processEventUseCase;
        this.idempotencyPort = idempotencyPort;
        this.objectMapper = objectMapper;
    }

    public Mono<EventProcessingResult> handle(IncomingEvent event) {
        if (event.idempotencyKey() == null || event.idempotencyKey().isBlank()) {
            return Mono.fromCallable(() -> processEventUseCase.processEvent(event));
        }

        String tenantId = event.tenantId().value().toString();
        String key = "event:" + event.idempotencyKey();

        return idempotencyPort.exists(key, tenantId)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return idempotencyPort.getResult(key, tenantId)
                                .flatMap(json -> Mono.fromCallable(() -> deserialize(json)));
                    }
                    return Mono.fromCallable(() -> processEventUseCase.processEvent(event))
                            .flatMap(result -> idempotencyPort
                                    .registerIfAbsent(key, tenantId, IDEMPOTENCY_TTL, serialize(result))
                                    .thenReturn(result));
                });
    }

    private String serialize(EventProcessingResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event result", e);
        }
    }

    private EventProcessingResult deserialize(String json) {
        try {
            return objectMapper.readValue(json, EventProcessingResult.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize cached event result", e);
        }
    }
}
