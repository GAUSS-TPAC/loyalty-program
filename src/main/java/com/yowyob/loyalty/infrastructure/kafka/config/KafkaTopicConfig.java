package com.yowyob.loyalty.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic tenantEventsTopic() {
        return TopicBuilder.name("loyalty.tenant.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic walletEventsTopic() {
        return TopicBuilder.name("loyalty.wallet.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
