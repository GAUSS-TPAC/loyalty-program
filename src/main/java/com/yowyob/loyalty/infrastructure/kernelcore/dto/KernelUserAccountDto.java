package com.yowyob.loyalty.infrastructure.kernelcore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/** Mappé depuis POST /api/auth/register → UserAccountResponse (Kernel Core). */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KernelUserAccountDto {

    private UUID id;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}
