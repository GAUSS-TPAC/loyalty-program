package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Mappé depuis GET/PUT /api/actors/me → BusinessActorResponse (Kernel Core).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelBusinessActorDto {

    private UUID id;
    private UUID tenantId;
    private UUID actorId;
    private String code;
    private boolean isIndividual;
    private boolean isActive;
    private boolean isVerified;
    private String type;
    private String role;
    private String name;
    private String businessId;
    private String contactPhone;
    private String website;
    private String biography;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public boolean isIndividual() { return isIndividual; }
    public void setIndividual(boolean individual) { isIndividual = individual; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
}
