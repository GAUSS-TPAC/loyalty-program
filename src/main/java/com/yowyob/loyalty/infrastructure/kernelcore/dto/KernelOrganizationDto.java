package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Mapped from GET /api/organizations/{organizationId} → OrganizationResponse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelOrganizationDto {

    private UUID id;
    private UUID tenantId;
    private String code;
    private String longName;
    private String shortName;
    private String displayName;
    private String status;
    private boolean isActive;
    private String organizationType;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLongName() { return longName; }
    public void setLongName(String longName) { this.longName = longName; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * @JsonProperty("isActive") requis : Jackson infère par défaut le nom de propriété
     * "active" depuis un getter isActive()/setter setActive(boolean) (convention "is"-stripping),
     * alors que Kernel Core envoie littéralement la clé JSON "isActive" — sans cette annotation
     * le champ reste silencieusement à sa valeur par défaut (false) pour CHAQUE organisation,
     * qu'elle soit approuvée ou non (vérifié en conditions réelles : mapStatus() traitait alors
     * systématiquement le tenant comme SUSPENDED).
     */
    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getOrganizationType() { return organizationType; }
    public void setOrganizationType(String organizationType) { this.organizationType = organizationType; }

    public String resolveName() {
        if (displayName != null && !displayName.isBlank()) return displayName;
        if (longName != null && !longName.isBlank()) return longName;
        if (shortName != null && !shortName.isBlank()) return shortName;
        return id != null ? id.toString() : "unknown";
    }

    public String resolveSlug() {
        if (code != null && !code.isBlank()) return code.toLowerCase();
        return id != null ? id.toString() : "unknown";
    }
}