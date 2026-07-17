package com.yowyob.loyalty.infrastructure.kernelcore.dto;

/** Corps de requête pour POST /api/auth/forgot-password (Kernel Core ForgotPasswordRequest). */
public record KernelForgotPasswordRequest(String principal) {}
