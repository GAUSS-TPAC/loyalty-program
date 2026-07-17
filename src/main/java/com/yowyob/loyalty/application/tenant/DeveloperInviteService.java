package com.yowyob.loyalty.application.tenant;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreDeveloperInviteAdapter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeveloperInviteService {

    private final KernelCoreDeveloperInviteAdapter kernelCoreDeveloperInviteAdapter;

    public DeveloperInviteService(KernelCoreDeveloperInviteAdapter kernelCoreDeveloperInviteAdapter) {
        this.kernelCoreDeveloperInviteAdapter = kernelCoreDeveloperInviteAdapter;
    }

    public Mono<Void> invite(String bearerToken, TenantId tenantId, String firstName, String lastName, String email) {
        var organizationId = tenantId.value();
        return kernelCoreDeveloperInviteAdapter.createActor(bearerToken, organizationId, firstName, lastName, email)
                .flatMap(actorId -> kernelCoreDeveloperInviteAdapter.registerUser(bearerToken, actorId, email))
                .flatMap(userId -> kernelCoreDeveloperInviteAdapter.findOrCreateDeveloperRole(bearerToken)
                        .flatMap(roleId -> kernelCoreDeveloperInviteAdapter.assignRole(bearerToken, userId, roleId, organizationId)))
                .then(kernelCoreDeveloperInviteAdapter.sendPasswordSetupEmail(bearerToken, email));
    }
}
