package com.yowyob.loyalty.application.tenant;

import com.yowyob.loyalty.application.tenant.ApiKeyService.CreatedKey;
import com.yowyob.loyalty.application.tenant.IntegrationApplicationService.CreatedApplication;
import com.yowyob.loyalty.application.webhook.WebhookEndpointService;
import com.yowyob.loyalty.application.webhook.WebhookEndpointService.CreatedEndpoint;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationDomainException;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationNotFoundException;
import com.yowyob.loyalty.domain.tenant.exception.InvalidWebhookEventTypeException;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import com.yowyob.loyalty.domain.tenant.model.IntegrationApplication;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import com.yowyob.loyalty.domain.tenant.port.out.IntegrationApplicationRepository;
import com.yowyob.loyalty.domain.webhook.model.WebhookEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Une application d'intégration (modèle My-CoolPay) regroupe clé publique + clé privée + callback.
 * Ces tests figent : les préfixes de clés selon le mode, la création conditionnelle de l'endpoint
 * webhook, l'ordre create-puis-revoke à la rotation, et le nettoyage complet à la suppression.
 */
public class IntegrationApplicationServiceTest {

    private static final TenantId TENANT = TenantId.of(UUID.randomUUID());

    private IntegrationApplicationRepository repository;
    private ApiKeyService apiKeyService;
    private WebhookEndpointService webhookEndpointService;
    private IntegrationApplicationService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(IntegrationApplicationRepository.class);
        apiKeyService = Mockito.mock(ApiKeyService.class);
        webhookEndpointService = Mockito.mock(WebhookEndpointService.class);
        service = new IntegrationApplicationService(repository, apiKeyService, webhookEndpointService);
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    }

    private CreatedKey createdKey(ApiKeyMode mode, String raw) {
        ApiKey key = ApiKey.create(TENANT, "app:Shop", "hash", raw.substring(0, 12), mode);
        return new CreatedKey(key, raw);
    }

    private CreatedEndpoint createdEndpoint(String url) {
        WebhookEndpoint endpoint = WebhookEndpoint.create(TENANT, url, "whsec_secret", "Application: Shop",
                List.of("points.earned"));
        return new CreatedEndpoint(endpoint, "whsec_secret");
    }

    @Test
    void createsTestModeApplicationWithTestPrefixedKeysAndNoWebhook() {
        when(apiKeyService.create(eq(TENANT), eq("app:Shop"), eq(ApiKeyMode.TEST), eq("sk")))
                .thenReturn(Mono.just(createdKey(ApiKeyMode.TEST, "sk_test_abcdefghijklmnop")));

        StepVerifier.create(service.create(TENANT, "Shop", null, null, null, ApiKeyMode.TEST, null, null))
                .assertNext(created -> {
                    assertTrue(created.app().publicKey().startsWith("pk_test_"));
                    assertTrue(created.rawPrivateKey().startsWith("sk_test_"));
                    assertNull(created.webhookSecret());
                    assertNull(created.app().webhookEndpointId());
                })
                .verifyComplete();

        verify(webhookEndpointService, never()).create(any(), any(), any(), anyList());
    }

    @Test
    void createsLiveApplicationWithWebhookSubscribedToAllBusinessEventsByDefault() {
        when(apiKeyService.create(eq(TENANT), eq("app:Shop"), eq(ApiKeyMode.LIVE), eq("sk")))
                .thenReturn(Mono.just(createdKey(ApiKeyMode.LIVE, "sk_live_abcdefghijklmnop")));
        when(webhookEndpointService.create(eq(TENANT), eq("https://shop.example.com/cb"), any(), anyList()))
                .thenReturn(Mono.just(createdEndpoint("https://shop.example.com/cb")));

        StepVerifier.create(service.create(TENANT, "Shop", null, null, null, ApiKeyMode.LIVE,
                        "https://shop.example.com/cb", null))
                .assertNext(created -> {
                    assertTrue(created.app().publicKey().startsWith("pk_live_"));
                    assertEquals("whsec_secret", created.webhookSecret());
                    assertNotNull(created.app().webhookEndpointId());
                })
                .verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> typesCaptor = ArgumentCaptor.forClass(List.class);
        verify(webhookEndpointService).create(eq(TENANT), eq("https://shop.example.com/cb"), any(), typesCaptor.capture());
        List<String> types = typesCaptor.getValue();
        assertTrue(types.containsAll(List.of("points.earned", "points.redeemed", "reward.granted",
                "reward.redeemed", "tier.changed")));
        assertFalse(types.contains("webhook.test"));
    }

    @Test
    void rejectsUnknownWebhookEventType() {
        StepVerifier.create(service.create(TENANT, "Shop", null, null, null, ApiKeyMode.LIVE,
                        "https://shop.example.com/cb", List.of("points.earned", "bogus.event")))
                .expectError(InvalidWebhookEventTypeException.class)
                .verify();
    }

    @Test
    void rotatePrivateKeyCreatesNewKeyBeforeRevokingOld() {
        UUID oldKeyId = UUID.randomUUID();
        IntegrationApplication app = IntegrationApplication.create(TENANT, "Shop", null, null, null,
                "pk_live_x", oldKeyId, null, ApiKeyMode.LIVE);
        when(repository.findByIdAndTenantId(app.id(), TENANT)).thenReturn(Mono.just(app));
        CreatedKey newKey = createdKey(ApiKeyMode.LIVE, "sk_live_newkeyabcdefgh");
        when(apiKeyService.create(eq(TENANT), eq("app:Shop"), eq(ApiKeyMode.LIVE), eq("sk")))
                .thenReturn(Mono.just(newKey));
        when(apiKeyService.revoke(TENANT, oldKeyId)).thenReturn(Mono.empty());

        StepVerifier.create(service.rotatePrivateKey(TENANT, app.id()))
                .assertNext(created -> {
                    assertEquals("sk_live_newkeyabcdefgh", created.rawPrivateKey());
                    assertEquals(newKey.record().id(), created.app().apiKeyId());
                })
                .verifyComplete();

        verify(apiKeyService).revoke(TENANT, oldKeyId);
    }

    @Test
    void rotateWebhookSecretFailsWhenNoCallbackConfigured() {
        IntegrationApplication app = IntegrationApplication.create(TENANT, "Shop", null, null, null,
                "pk_live_x", UUID.randomUUID(), null, ApiKeyMode.LIVE);
        when(repository.findByIdAndTenantId(app.id(), TENANT)).thenReturn(Mono.just(app));

        StepVerifier.create(service.rotateWebhookSecret(TENANT, app.id()))
                .expectError(ApplicationDomainException.class)
                .verify();
    }

    @Test
    void deleteRevokesKeyDeletesEndpointAndRow() {
        UUID keyId = UUID.randomUUID();
        UUID endpointId = UUID.randomUUID();
        IntegrationApplication app = IntegrationApplication.create(TENANT, "Shop", null, null, null,
                "pk_live_x", keyId, endpointId, ApiKeyMode.LIVE);
        when(repository.findByIdAndTenantId(app.id(), TENANT)).thenReturn(Mono.just(app));
        when(apiKeyService.revoke(TENANT, keyId)).thenReturn(Mono.empty());
        when(webhookEndpointService.delete(TENANT, endpointId)).thenReturn(Mono.empty());
        when(repository.deleteByIdAndTenantId(app.id(), TENANT)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(TENANT, app.id())).verifyComplete();

        verify(apiKeyService).revoke(TENANT, keyId);
        verify(webhookEndpointService).delete(TENANT, endpointId);
        verify(repository).deleteByIdAndTenantId(app.id(), TENANT);
    }

    @Test
    void getUnknownApplicationFailsWithNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndTenantId(id, TENANT)).thenReturn(Mono.empty());

        StepVerifier.create(service.get(TENANT, id))
                .expectError(ApplicationNotFoundException.class)
                .verify();
    }
}
