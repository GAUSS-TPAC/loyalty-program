package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/** Mappé depuis POST /api/actors → ActorResponse (Kernel Core). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelActorDto {

    private UUID id;
    private UUID organizationId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
}
