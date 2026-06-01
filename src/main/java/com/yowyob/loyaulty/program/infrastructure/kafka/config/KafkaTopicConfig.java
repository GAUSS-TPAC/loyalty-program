package com.yowyob.loyaulty.program.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic loyaltyDlqTopic() {
        return TopicBuilder.name("loyalty.dlq")
                .partitions(3)
                .replicas(1)
                .config("retention.ms", String.valueOf(7 * 24 * 60 * 60 * 1000L))
                .build();
    }

    @Bean
    public NewTopic tenantEventsTopic() {
        return TopicBuilder.name("tenant.events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic walletEventsTopic() {
        return TopicBuilder.name("wallet.events")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic businessEventsTopic() {
        return TopicBuilder.name("business.events")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic loyaltyEventsTopic() {
        return TopicBuilder.name("loyalty.events")
                .partitions(6)
                .replicas(1)
                .build();
    }
}
