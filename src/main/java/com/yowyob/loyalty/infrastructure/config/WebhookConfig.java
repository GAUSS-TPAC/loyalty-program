package com.yowyob.loyalty.infrastructure.config;

import com.yowyob.loyalty.application.webhook.scheduler.WebhookRetryScheduler;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookDeliveryRepository;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookEndpointRepository;
import com.yowyob.loyalty.domain.webhook.port.out.WebhookSenderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookConfig {

    @Bean
    public WebhookRetryScheduler webhookRetryScheduler(WebhookDeliveryRepository deliveryRepository,
                                                         WebhookEndpointRepository endpointRepository,
                                                         WebhookSenderPort senderPort) {
        return new WebhookRetryScheduler(deliveryRepository, endpointRepository, senderPort);
    }
}
