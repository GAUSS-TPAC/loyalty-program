package com.yowyob.loyalty.api.platform;

import com.yowyob.loyalty.api.platform.dto.response.PlatformTenantResponse;
import com.yowyob.loyalty.domain.subscription.port.in.ListPlatformTenantsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Console plateforme : vue cross-tenant réservée à l'équipe Yowyob, protégée
 * par {@link com.yowyob.loyalty.shared.security.PlatformAdminAuthFilter}
 * (secret statique X-Platform-Admin-Key), pas par le JWT/clé API d'un tenant.
 */
@RestController
@RequestMapping("/api/v1/admin/platform")
@Tag(name = "Platform Admin", description = "Console plateforme (cross-tenant)")
public class PlatformAdminController {

    private final ListPlatformTenantsUseCase listPlatformTenantsUseCase;

    public PlatformAdminController(ListPlatformTenantsUseCase listPlatformTenantsUseCase) {
        this.listPlatformTenantsUseCase = listPlatformTenantsUseCase;
    }

    @GetMapping("/tenants")
    public Flux<PlatformTenantResponse> listTenants() {
        return listPlatformTenantsUseCase.listSubscribedTenants().map(PlatformTenantResponse::from);
    }
}
