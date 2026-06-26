package com.yowyob.loyalty.application.subscription.scheduler;

import com.yowyob.loyalty.domain.subscription.port.in.ProcessSubscriptionRenewalUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class SubscriptionRenewalScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionRenewalScheduler.class);

    private final ProcessSubscriptionRenewalUseCase renewalUseCase;

    public SubscriptionRenewalScheduler(ProcessSubscriptionRenewalUseCase renewalUseCase) {
        this.renewalUseCase = renewalUseCase;
    }

    @Scheduled(cron = "${loyalty.subscription.renewal.cron:0 0 1 * * *}")
    public void processRenewals() {
        renewalUseCase.processExpiredTrials()
                .doOnSuccess(n -> { if (n > 0) log.info("Expired {} trial subscriptions", n); })
                .subscribe();

        renewalUseCase.processExpiredSubscriptions()
                .doOnSuccess(n -> { if (n > 0) log.info("Marked {} subscriptions as past-due", n); })
                .subscribe();

        renewalUseCase.processOverdueInvoices()
                .doOnSuccess(n -> { if (n > 0) log.info("Failed {} overdue invoices", n); })
                .subscribe();
    }
}
