package com.yowyob.loyalty.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
public class LoyaltyKafkaTopicConfig {

    @Bean
    public NewTopic loyaltyEventsTopic(@Value("${spring.kafka.topics.loyalty-events:loyalty.events}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic loyaltyRewardGrantTopic(
            @Value("${spring.kafka.topics.loyalty-reward-grants:loyalty.rewards.grant-requests}") String topic
    ) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
