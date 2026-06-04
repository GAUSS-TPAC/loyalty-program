package com.yowyob.loyalty.application.loyalty.handler;

import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.port.in.ProcessEventUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ProcessEventHandler {

    private final ProcessEventUseCase processEventUseCase;

    public ProcessEventHandler(ProcessEventUseCase processEventUseCase) {
        this.processEventUseCase = processEventUseCase;
    }

    public Mono<EventProcessingResult> handle(IncomingEvent event) {
        return Mono.fromCallable(() -> processEventUseCase.processEvent(event))
                // Can wrap with metrics, logging, or transaction boundaries here if needed
                .onErrorMap(e -> new RuntimeException("Failed to process loyalty event: " + e.getMessage(), e));
    }
}
