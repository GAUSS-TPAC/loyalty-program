package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.request.IncomingEventRequest;
import com.yowyob.loyalty.api.loyalty.dto.response.EventProcessingResponse;
import com.yowyob.loyalty.application.loyalty.handler.ProcessEventHandler;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final ProcessEventHandler processEventHandler;

    public EventController(ProcessEventHandler processEventHandler) {
        this.processEventHandler = processEventHandler;
    }

    @PostMapping
    public Mono<EventProcessingResponse> processEvent(
            @Valid @RequestBody IncomingEventRequest request,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return TenantContextHolder.getTenantId()
                .map(tenantId -> LoyaltyApiMapper.toIncomingEvent(request, tenantId, idempotencyKey))
                .flatMap(processEventHandler::handle)
                .map(EventProcessingResponse::from);
    }
}
