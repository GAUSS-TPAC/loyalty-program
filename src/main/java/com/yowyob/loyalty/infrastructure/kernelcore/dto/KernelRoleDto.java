package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/** Mappé depuis GET/POST /api/roles → RoleResponse (Kernel Core). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelRoleDto {

    private UUID id;
    private String code;
    private String name;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
