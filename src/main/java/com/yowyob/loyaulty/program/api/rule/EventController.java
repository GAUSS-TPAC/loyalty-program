package com.yowyob.loyaulty.program.api.rule;

import com.yowyob.loyaulty.program.api.rule.dto.request.SendEventRequest;
import com.yowyob.loyaulty.program.api.rule.dto.response.EvaluationResultResponse;
import com.yowyob.loyaulty.program.domain.rule.model.LoyaltyEvent;
import com.yowyob.loyaulty.program.domain.rule.port.in.ProcessEventUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final ProcessEventUseCase processEventUseCase;

    public EventController(ProcessEventUseCase processEventUseCase) {
        this.processEventUseCase = processEventUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<EvaluationResultResponse> sendEvent(
            @Valid @RequestBody SendEventRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> {
                    LoyaltyEvent event = new LoyaltyEvent(
                            UUID.randomUUID(),
                            tenantId,
                            request.memberId(),
                            request.eventType(),
                            request.occurredAt() != null ? request.occurredAt() : Instant.now(),
                            request.payload() != null ? request.payload() : Map.of(),
                            idempotencyKey
                    );
                    return processEventUseCase.process(event);
                })
                .map(EvaluationResultResponse::from);
    }
}
