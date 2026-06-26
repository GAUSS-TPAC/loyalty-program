package com.yowyob.loyalty.infrastructure.kernelcore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.kernel-core")
public class KernelCoreProperties {

    private String baseUrl = "http://localhost:8090";
    private String serviceClientId;
    private String serviceClientSecret;
    private String tokenEndpoint;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;

    public String resolvedTokenEndpoint() {
        if (tokenEndpoint != null && !tokenEndpoint.isBlank()) {
            return tokenEndpoint;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/oauth2/token";
    }

    public boolean hasTokenEndpoint() {
        return tokenEndpoint != null && !tokenEndpoint.isBlank();
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getServiceClientId() { return serviceClientId; }
    public void setServiceClientId(String serviceClientId) { this.serviceClientId = serviceClientId; }

    public String getServiceClientSecret() { return serviceClientSecret; }
    public void setServiceClientSecret(String serviceClientSecret) { this.serviceClientSecret = serviceClientSecret; }

    public String getTokenEndpoint() { return tokenEndpoint; }
    public void setTokenEndpoint(String tokenEndpoint) { this.tokenEndpoint = tokenEndpoint; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}
