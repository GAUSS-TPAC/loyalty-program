package com.yowyob.loyalty.infrastructure.bonification.config;

import com.yowyob.loyalty.domain.bonification.port.out.BonificationPort;
import com.yowyob.loyalty.infrastructure.bonification.adapter.BonificationApiAdapter;
import com.yowyob.loyalty.infrastructure.bonification.adapter.BonificationDisabledAdapter;
import io.netty.channel.ChannelOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(BonificationProperties.class)
public class BonificationConfig {

    @Bean
    public WebClient.Builder bonificationWebClientBuilder(BonificationProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs());

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean
    @ConditionalOnProperty(name = "app.bonification.enabled", havingValue = "true", matchIfMissing = true)
    public BonificationPort bonificationApiPort(
            WebClient.Builder bonificationWebClientBuilder,
            BonificationProperties properties,
            com.yowyob.loyalty.domain.bonification.port.out.BonificationTokenCachePort tokenCache
    ) {
        return new BonificationApiAdapter(bonificationWebClientBuilder, properties, tokenCache);
    }

    @Bean
    @ConditionalOnProperty(name = "app.bonification.enabled", havingValue = "false")
    public BonificationPort bonificationDisabledPort() {
        return new BonificationDisabledAdapter();
    }
}
