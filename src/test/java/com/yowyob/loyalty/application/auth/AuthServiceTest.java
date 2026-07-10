package com.yowyob.loyalty.application.auth;

import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreAuthAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.config.KernelCoreProperties;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelLoginResultDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationSummaryDto;
import com.yowyob.loyalty.shared.exception.OrganizationNotAccessibleException;
import com.yowyob.loyalty.shared.exception.OrganizationSelectionRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Un login tenant-scopé KernelCore ne porte pas de claim d'organisation dans le JWT — l'organisation
 * cible (= "tenant" pour ce backend) doit être résolue à la connexion parmi les memberships de
 * l'acteur. Ces tests figent cette résolution : auto-sélection si une seule organisation,
 * sélection explicite sinon, erreur claire dans les deux cas d'échec.
 */
public class AuthServiceTest {

    private KernelCoreAuthAdapter kernelCoreAuthAdapter;
    private AuthService authService;

    private static KernelOrganizationSummaryDto org(String id, String code) {
        KernelOrganizationSummaryDto dto = new KernelOrganizationSummaryDto();
        dto.setOrganizationId(id);
        dto.setOrganizationCode(code);
        dto.setDisplayName(code + " Inc.");
        return dto;
    }

    @BeforeEach
    void setup() {
        kernelCoreAuthAdapter = Mockito.mock(KernelCoreAuthAdapter.class);
        KernelCoreProperties properties = new KernelCoreProperties();
        properties.setTenantId("11111111-1111-1111-1111-111111111111");
        authService = new AuthService(kernelCoreAuthAdapter, properties);
    }

    @Test
    void autoSelectsTheOnlyAccessibleOrganization() {
        KernelOrganizationSummaryDto onlyOrg = org("org-1", "LOYALTY-PROGRAM");
        when(kernelCoreAuthAdapter.login(Mockito.anyString(), Mockito.eq("admin@x.com"), Mockito.eq("pw")))
                .thenReturn(Mono.just(new KernelLoginResultDto("jwt-token", List.of(onlyOrg))));

        StepVerifier.create(authService.login("admin@x.com", "pw", null))
                .assertNext(result -> {
                    assertEquals("jwt-token", result.token());
                    assertEquals("org-1", result.organizationId());
                    assertEquals("LOYALTY-PROGRAM", result.organizationCode());
                })
                .verifyComplete();
    }

    @Test
    void honorsExplicitOrganizationSelection() {
        KernelOrganizationSummaryDto orgA = org("org-a", "ALPHA");
        KernelOrganizationSummaryDto orgB = org("org-b", "BETA");
        when(kernelCoreAuthAdapter.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(new KernelLoginResultDto("jwt-token", List.of(orgA, orgB))));

        StepVerifier.create(authService.login("admin@x.com", "pw", "org-b"))
                .assertNext(result -> assertEquals("org-b", result.organizationId()))
                .verifyComplete();
    }

    @Test
    void rejectsExplicitOrganizationNotAccessibleToActor() {
        KernelOrganizationSummaryDto orgA = org("org-a", "ALPHA");
        when(kernelCoreAuthAdapter.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(new KernelLoginResultDto("jwt-token", List.of(orgA))));

        StepVerifier.create(authService.login("admin@x.com", "pw", "org-not-mine"))
                .expectError(OrganizationNotAccessibleException.class)
                .verify();
    }

    @Test
    void requiresExplicitSelectionWhenActorHasMultipleOrganizations() {
        KernelOrganizationSummaryDto orgA = org("org-a", "ALPHA");
        KernelOrganizationSummaryDto orgB = org("org-b", "BETA");
        when(kernelCoreAuthAdapter.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(new KernelLoginResultDto("jwt-token", List.of(orgA, orgB))));

        StepVerifier.create(authService.login("admin@x.com", "pw", null))
                .expectError(OrganizationSelectionRequiredException.class)
                .verify();
    }

    @Test
    void rejectsActorWithNoAccessibleOrganization() {
        when(kernelCoreAuthAdapter.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(new KernelLoginResultDto("jwt-token", List.of())));

        StepVerifier.create(authService.login("admin@x.com", "pw", null))
                .expectError(OrganizationNotAccessibleException.class)
                .verify();
    }
}
