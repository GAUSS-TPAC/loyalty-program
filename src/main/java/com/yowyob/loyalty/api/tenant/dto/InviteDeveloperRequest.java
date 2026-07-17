package com.yowyob.loyalty.api.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteDeveloperRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email
) {}
