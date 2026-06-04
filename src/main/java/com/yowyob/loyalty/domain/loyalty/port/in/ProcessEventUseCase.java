package com.yowyob.loyalty.domain.loyalty.port.in;

import com.yowyob.loyalty.domain.loyalty.model.event.EventProcessingResult;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;

public interface ProcessEventUseCase {
    EventProcessingResult processEvent(IncomingEvent event);
}
