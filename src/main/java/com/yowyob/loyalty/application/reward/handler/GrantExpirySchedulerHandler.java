package com.yowyob.loyalty.application.reward.handler;

import com.yowyob.loyalty.domain.reward.service.GrantExpiryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class GrantExpirySchedulerHandler {

    private static final Logger log = LoggerFactory.getLogger(GrantExpirySchedulerHandler.class);

    private final GrantExpiryService expiryService;

    public GrantExpirySchedulerHandler(GrantExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void runExpiry() {
        expiryService.expireOutdatedGrants(Instant.now())
                .doOnNext(count -> log.info("[EXPIRY] {} grants expirés", count))
                .doOnError(e -> log.error("[EXPIRY] Erreur lors de l'expiration", e))
                .subscribe();
    }
}
