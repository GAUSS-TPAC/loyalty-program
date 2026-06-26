package com.yowyob.loyalty.domain.campaign.port.in;

import reactor.core.publisher.Mono;

public interface ProcessCampaignScheduleUseCase {
    Mono<Integer> activateDueCampaigns();
    Mono<Integer> deactivateExpiredCampaigns();
}
