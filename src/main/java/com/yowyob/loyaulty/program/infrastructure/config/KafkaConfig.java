package com.yowyob.loyaulty.program.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka pour les producers et consumers wallet.
 *
 * <p>Producer : idempotent, acks=all, sérialisation JSON.</p>
 * <p>Consumer : acknowledgment manuel (at-least-once), désérialisation JSON typée.</p>
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:loyalty-wallet-group}")
    private String consumerGroupId;

    // ── Producer ──────────────────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, Object> walletProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,          bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,       StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,     JsonSerializer.class);
        // Producteur idempotent : garantit exactly-once delivery vers le broker
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,         true);
        config.put(ProducerConfig.ACKS_CONFIG,                       "all");
        config.put(ProducerConfig.RETRIES_CONFIG,                    3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(walletProducerFactory());
    }

    // ── Consumer ──────────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> walletConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,    bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,             consumerGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,    "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  JsonDeserializer.class);
        // Désactiver l'auto-commit : on utilise l'acknowledgment manuel
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,   false);
        // Packages de confiance pour la désérialisation JSON
        config.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.yowyob.loyaulty.program.domain.wallet.event");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Factory de listener avec accusé de réception manuel.
     * Requis par le {@code @KafkaListener} du {@code PaymentWebhookConsumer}.
     */
    @Bean("walletKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> walletKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(walletConsumerFactory());
        // Acknowledgment manuel : le consumer appelle ack.acknowledge() après succès
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(3); // 3 threads de consommation parallèle
        return factory;
    }
}
