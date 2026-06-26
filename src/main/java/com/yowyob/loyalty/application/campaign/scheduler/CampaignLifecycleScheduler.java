package com.yowyob.loyalty.application.campaign.scheduler;

import com.yowyob.loyalty.domain.campaign.port.in.ProcessCampaignScheduleUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CampaignLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(CampaignLifecycleScheduler.class);

    private final ProcessCampaignScheduleUseCase scheduleUseCase;

    public CampaignLifecycleScheduler(ProcessCampaignScheduleUseCase scheduleUseCase) {
        this.scheduleUseCase = scheduleUseCase;
    }

    @Scheduled(fixedDelayString = "${loyalty.campaign.scheduler.interval-ms:60000}")
    public void processCampaignLifecycle() {
        scheduleUseCase.activateDueCampaigns()
                .doOnNext(count -> { if (count > 0) log.info("Auto-activated {} campaign(s)", count); })
                .then(scheduleUseCase.deactivateExpiredCampaigns())
                .doOnNext(count -> { if (count > 0) log.info("Auto-completed {} campaign(s)", count); })
                .subscribe(
                        count -> {},
                        err -> log.error("Campaign lifecycle scheduler error: {}", err.getMessage())
                );
    }
}
