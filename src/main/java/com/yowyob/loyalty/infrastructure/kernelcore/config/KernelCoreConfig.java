package com.yowyob.loyalty.infrastructure.kernelcore.config;

import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreTenantAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreTokenService;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(KernelCoreProperties.class)
public class KernelCoreConfig {

    @Bean
    public WebClient kernelCoreWebClient(KernelCoreProperties properties) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs());

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public KernelCoreTokenService kernelCoreTokenService(KernelCoreProperties properties) {
        return new KernelCoreTokenService(properties);
    }

    @Bean
    public KernelCoreTenantAdapter kernelCoreTenantAdapter(
            @Qualifier("kernelCoreWebClient") WebClient kernelCoreWebClient,
            KernelCoreTokenService kernelCoreTokenService,
            TenantCacheAdapter tenantCacheAdapter) {
        return new KernelCoreTenantAdapter(kernelCoreWebClient, kernelCoreTokenService, tenantCacheAdapter);
    }
}
