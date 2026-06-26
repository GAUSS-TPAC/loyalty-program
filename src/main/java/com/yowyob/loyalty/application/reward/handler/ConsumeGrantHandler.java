package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.model.ConsumeGrantRequest;
import com.yowyob.loyalty.domain.reward.model.ConsumeGrantResult;
import com.yowyob.loyalty.domain.reward.port.in.ConsumeGrantUseCase;
import com.yowyob.loyalty.domain.reward.service.ConsumeGrantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class ConsumeGrantHandler implements ConsumeGrantUseCase {

    private final ConsumeGrantService consumeService;

    public ConsumeGrantHandler(ConsumeGrantService consumeService) {
        this.consumeService = consumeService;
    }

    @Override
    @Transactional
    public Mono<ConsumeGrantResult> consumeGrant(ConsumeGrantRequest request) {
        return consumeService.consumeGrant(request);
    }
}
