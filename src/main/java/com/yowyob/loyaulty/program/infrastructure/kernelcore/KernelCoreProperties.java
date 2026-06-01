package com.yowyob.loyaulty.program.infrastructure.kernelcore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de configuration pour la connexion au Kernel Core.
 *
 * <p>Ces propriétés seront utilisées par les vrais adapters Kernel Core
 * quand ils remplaceront les stubs.</p>
 */
@Component
@ConfigurationProperties(prefix = "app.kernel-core")
public class KernelCoreProperties {

    /** URL de base du Kernel Core (ex. http://kernel-core:8090). */
    private String baseUrl = "http://localhost:8090";

    /** Client ID de ce service dans le Kernel Core (OAuth2 client credentials). */
    private String serviceClientId = "loyalty-service";

    /** Secret du client OAuth2 (à externaliser en production via vault). */
    private String serviceClientSecret = "changeme";

    /** Endpoint de génération de token (laisser vide si non utilisé). */
    private String tokenEndpoint = "";

    /** Timeout de connexion en millisecondes vers le Kernel Core. */
    private int connectTimeoutMs = 3000;

    /** Timeout de lecture en millisecondes vers le Kernel Core. */
    private int readTimeoutMs = 5000;

    // ── Getters et Setters ────────────────────────────────────────────────────

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
